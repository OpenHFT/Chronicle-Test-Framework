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
/**
 * Utility class for working with mapped files, specifically dealing with parsing the "/proc/self/maps" file on Linux systems.
 * This provides information about virtual memory mappings.
 * <p>
 * NOTE: This utility is designed to work on Linux or Linux-like environments.
 */
public enum MappedFileUtil {
    ;

    private static final Logger LOGGER = LoggerFactory.getLogger(MappedFileUtil.class);
    private static final Path PROC_SELF_MAPS = Paths.get("/proc/self/maps");

    // Regular expression pattern to parse lines from /proc/self/maps.
    private static final Pattern LINE_PATTERN = Pattern.compile("([\\p{XDigit}\\-]+)\\s+([rwxsp\\-]+)\\s+(\\p{XDigit}+)\\s+(\\p{XDigit}+:\\p{XDigit}+)\\s+(\\d+)(?:\\s+(.*))?");
    // Index constants for the parsed groups.
    private static final int ADDRESS_INDEX = 1;
    private static final int PERMS_INDEX = 2;
    private static final int OFFSET_INDEX = 3;
    private static final int DEV_INDEX = 4;
    private static final int INODE_INDEX = 5;
    private static final int PATH_INDEX = 6;

    /**
     * Gets the distinct files that are currently mapped to the current process.
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

    // Internal method to process each line from /proc/self/maps
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

    // Processes a single line and adds the file to the list if applicable
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

    /**
     * Parses the provided line using the pattern defined for parsing lines from "/proc/self/maps".
     *
     * @param line The line to be parsed
     * @return A Matcher object after matching the pattern
     */
    public static Matcher parseMapsLine(String line) {
        return LINE_PATTERN.matcher(line);
    }

    /**
     * Gets the path (if any) from the provided Matcher object.
     *
     * @param matcher Matcher object containing the parsed line
     * @return The path if available, null otherwise
     */
    @Nullable
    public static String getPath(Matcher matcher) {
        return matcher.group(PATH_INDEX);
    }

    /**
     * Gets the memory address range from the provided Matcher object.
     *
     * @param matcher Matcher object containing the parsed line
     * @return The address range as a string
     */
    public static String getAddress(Matcher matcher) {
        return matcher.group(ADDRESS_INDEX);
    }
}
