package net.openhft.chronicle.testframework.mappedfiles;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public enum MappedFileUtil {
    ;
    private static final Logger LOGGER = LoggerFactory.getLogger(MappedFileUtil.class);
    private static final Path PROC_SELF_MAPS = Paths.get("/proc/self/maps");

    // See https://man7.org/linux/man-pages/man5/proc.5.html
    private static final Pattern LINE_PATTERN = Pattern.compile("([\\p{XDigit}\\-]+)\\s+([rwxsp\\-]+)\\s+(\\p{XDigit}+)\\s+(\\p{XDigit}+:\\p{XDigit}+)\\s+(\\d+)(?:\\s+(.*))?");
    private static final int ADDRESS_INDEX = 1;
    private static final int PERMS_INDEX = 2;
    private static final int OFFSET_INDEX = 3;
    private static final int DEV_INDEX = 4;
    private static final int INODE_INDEX = 5;
    private static final int PATH_INDEX = 6;

    /**
     * Get the distinct files that are currently mapped to the current process
     * <p>
     * NOTE: this is only likely to work on linux or linux-like environments
     *
     * @return A set of the distinct files listed in /proc/self/maps
     * @throws UnsupportedOperationException if /proc/self/maps does not exist or can't be read
     */
    public static Set<String> getAllMappedFiles() {
        final Set<String> fileList = new HashSet<>();

        if (Files.exists(PROC_SELF_MAPS) && Files.isReadable(PROC_SELF_MAPS)) {
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(PROC_SELF_MAPS)))) {
                processProcSelfMaps(fileList, reader);
            } catch (IOException e) {
                throw new IllegalStateException("Getting mapped files failed", e);
            }
            return fileList;
        } else {
            throw new UnsupportedOperationException(
                    format("This only works on systems that have a /proc/self/maps (exists=%s, isReadable=%s)",
                            Files.exists(PROC_SELF_MAPS), Files.isReadable(PROC_SELF_MAPS)));
        }
    }

    private static void processProcSelfMaps(Set<String> fileList, BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            final Matcher matcher = parseMapsLine(line);
            if (matcher.matches()) {
                processOneLine(fileList, matcher);
            } else {
                LOGGER.warn("Found non-matching line in /proc/self/maps: {}", line);
            }
        }
    }

    private static void processOneLine(Set<String> fileList, Matcher matcher) {
        String filename = getPath(matcher);
        if (filename == null) {
            return;
        }
        if (filename.startsWith("/")) {
            fileList.add(filename);
        } else if (!filename.trim().isEmpty()) {
            LOGGER.debug("Ignoring non-file {}", filename);
        }
    }

    public static Matcher parseMapsLine(String line) {
        return LINE_PATTERN.matcher(line);
    }

    @Nullable
    public static String getPath(Matcher matcher) {
        return matcher.group(PATH_INDEX);
    }

    public static String getAddress(Matcher matcher) {
        return matcher.group(ADDRESS_INDEX);
    }
}
