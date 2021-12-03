package net.openhft.chronicle.testframework.internal.apimetrics;

import net.openhft.chronicle.testframework.apimetrics.Accumulator;
import net.openhft.chronicle.testframework.apimetrics.ApiMetrics;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class StandardApiMetrics implements ApiMetrics {

    private final Set<Accumulator> accumulators;
    private final Set<Accumulator> internalAccumulators;

    public StandardApiMetrics(final Set<Accumulator> accumulators,
                              final Set<Accumulator> internalAccumulators) {
        requireNonNull(accumulators);
        requireNonNull(internalAccumulators);
        this.accumulators = new LinkedHashSet<>(accumulators);
        this.internalAccumulators = new LinkedHashSet<>(internalAccumulators);
    }

    @Override
    public Stream<Accumulator> accumulators() {
        return accumulators.stream();
    }

    @Override
    public Stream<Accumulator> internalAccumulators() {
        return internalAccumulators.stream();
    }

    @Override
    public String toString() {
        return accumulators.stream()
                .map(Objects::toString)
                .collect(Collectors.joining(String.format("%n")));
    }
}