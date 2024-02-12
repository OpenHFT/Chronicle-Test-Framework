package net.openhft.chronicle.testframework.internal;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BootstrapUtilsTest {
    @Test
    void testPotentialBootstrapIssues() {
        Set<String> candidates = new BootstrapUtils(
                Set.of("software.chronicle.enterprise.queue.replication.tools.ReplicatorMain"))
                .scanClasses();
        assertTrue(candidates.isEmpty());
    }
}