//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.chronicle.testframework.internal.apimetrics;

import io.github.classgraph.MethodInfo;
import net.openhft.chronicle.testframework.apimetrics.Accumulator;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Collection of predefined accumulator suppliers used by the API metrics
 * subsystem.  Each supplier creates an {@link Accumulator} configured with a
 * common aggregation scheme.  These constants are intended for quick setup of
 * standard analysis pipelines and can also serve as examples when creating
 * bespoke accumulators.
 */
public final class StandardAccumulators {

    /**
     * Aggregates results by method signature.  Use this when overloaded
     * methods should be treated as distinct.
     */
    public static final Supplier<Accumulator> PER_METHOD = () -> Accumulator.of(
            "method",
            (m, ci, l) -> ((MethodInfo) l).getClassName() + "." + l.getName() +
                    ((MethodInfo) l).getTypeSignatureOrTypeDescriptorStr(),
            (m, ci, l) -> l instanceof MethodInfo);

    /**
     * Aggregates first by class and then by metric.  Useful for a
     * class-centric view of which metrics apply.
     */
    public static final Supplier<Accumulator> PER_CLASS_AND_METRIC = () ->
            Accumulator.of(
                    "class",
                    (m, ci, l) -> ci.loadClass().getName(),
                    "metric",
                    (m, ci, l) -> m.toString());

    /**
     * Aggregates only by metric across the entire scan.  Use this for a
     * quick overview of metric totals.
     */
    static final Supplier<Accumulator> PER_METRIC = () ->
            Accumulator.of("metric", (m, ci, l) -> m.toString());

    /**
     * Aggregates results by class.  Helpful when metrics should be summed for
     * each class irrespective of individual methods.
     */
    static final Supplier<Accumulator> PER_CLASS = () ->
            Accumulator.of("class", (m, ci, l) -> ci.loadClass().getName());

    /**
     * Aggregates results by package.  Use when a high level overview per
     * package is required.
     */
    static final Supplier<Accumulator> PER_PACKAGE = () ->
            Accumulator.of("package", (m, ci, l) -> ci.getPackageName());

    /**
     * Aggregates by class and method name only, ignoring the signature.
     * Suitable when overloading details are not important.
     */
    static final Supplier<Accumulator> PER_METHOD_REFERENCE = () ->
            Accumulator.of(
                    "method reference",
                    (m, ci, l) -> ((MethodInfo) l).getClassName() + "::" + l.getName(),
                    (m, ci, l) -> l instanceof MethodInfo);

    // Suppresses default constructor, ensuring non-instantiability.
    private StandardAccumulators() {
    }

    static Stream<Supplier<Accumulator>> stream() {
        return Stream.of(PER_METRIC, PER_PACKAGE, PER_CLASS, PER_METHOD_REFERENCE);
    }
}
