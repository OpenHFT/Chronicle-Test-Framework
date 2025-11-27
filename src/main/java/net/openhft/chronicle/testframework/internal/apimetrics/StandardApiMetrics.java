/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal.apimetrics;

import net.openhft.chronicle.testframework.apimetrics.Accumulator;
import net.openhft.chronicle.testframework.apimetrics.ApiMetrics;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Combines the results from a set of accumulators.
 * <p>
 * Instances of this class are usually created by
 * {@link StandardApiMetricsBuilder}. During the build each accumulator is
 * applied to every selected metric and the results are stored here. Two
 * collections are kept: one for public packages and another for internal
 * packages.
 * <p>
 * The {@link #accumulators()} method exposes the accumulators used for public
 * analysis. The {@link #internalAccumulators()} method returns those for
 * internal analysis. Both methods supply the accumulators in the order they
 * were added to the builder.
 */
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
