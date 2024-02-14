package net.openhft.chronicle.testframework.internal;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BootstrapUtilsTest {
    @Test
    void testPotentialBootstrapIssues() {
        HashSet<String> excluded = new HashSet<>();
        excluded.add("software.chronicle.enterprise.queue.replication.tools.ReplicatorMain");
        Set<String> candidates = new BootstrapUtils(excluded).scanClasses();
        assertTrue(candidates.isEmpty());
    }
}