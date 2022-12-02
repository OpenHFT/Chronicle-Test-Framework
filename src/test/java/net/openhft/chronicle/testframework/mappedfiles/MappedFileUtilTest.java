package net.openhft.chronicle.testframework.mappedfiles;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MappedFileUtilTest {

    private static Stream<Object[]> exampleLines() {
        return Stream.of(
                new Object[]{"7f3b9ee38000-7f3b9ee3b000 r--p 00000000 103:03 3147723                   /usr/lib/x86_64-linux-gnu/libgcc_s.so.1", "/usr/lib/x86_64-linux-gnu/libgcc_s.so.1", "7f3b9ee38000-7f3b9ee3b000"},
                new Object[]{"d0000000-e4b00000 rw-p 00000000 00:00 0", null, "d0000000-e4b00000"},
                new Object[]{"100240000-140000000 ---p 00000000 00:00 0", null, "100240000-140000000"},
                new Object[]{"564609d75000-564609d76000 r--p 00000000 fe:02 15994495                   /usr/lib/jvm/java-8-openjdk/jre/bin/java", "/usr/lib/jvm/java-8-openjdk/jre/bin/java", "564609d75000-564609d76000"},
                new Object[]{"56460ac26000-56460ac47000 rw-p 00000000 00:00 0                          [heap]", "[heap]", "56460ac26000-56460ac47000"},
                new Object[]{"7f22b44b3000-7f22b44b4000 r--s 00001000 fe:02 11535264                   /opt/intellij-idea-ultimate-edition/plugins/maven/lib/maven-event-listener.jar", "/opt/intellij-idea-ultimate-edition/plugins/maven/lib/maven-event-listener.jar", "7f22b44b3000-7f22b44b4000"},
                new Object[]{"ffffffffff600000-ffffffffff601000 --xp 00000000 00:00 0                  [vsyscall]", "[vsyscall]", "ffffffffff600000-ffffffffff601000"}
        );
    }

    @ParameterizedTest(name = "line={0} should resolve path={1} and address={2}")
    @MethodSource("exampleLines")
    void shouldParseExampleLine(String procMapsLine, String expectedPath, String expectedAddress) {
        final Matcher matcher = MappedFileUtil.parseMapsLine(procMapsLine);
        assertTrue(matcher.matches());
        assertEquals(expectedPath, MappedFileUtil.getPath(matcher));
        assertEquals(expectedAddress, MappedFileUtil.getAddress(matcher));
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void shouldDetectMappedFilesOnLinux() throws IOException {
        final Path mappedFileUtilTest = Files.createTempFile("MappedFileUtilTest", "");
        try (FileChannel channel = FileChannel.open(mappedFileUtilTest, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE)) {
            final MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0, 100);
            assertTrue(MappedFileUtil.getAllMappedFiles().contains(mappedFileUtilTest.toAbsolutePath().toString()));
        }
    }
}
