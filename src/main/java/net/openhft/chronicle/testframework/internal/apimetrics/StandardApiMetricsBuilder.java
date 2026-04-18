/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal.apimetrics;

import io.github.classgraph.*;
import net.openhft.chronicle.testframework.apimetrics.Accumulator;
import net.openhft.chronicle.testframework.apimetrics.ApiMetrics;
import net.openhft.chronicle.testframework.apimetrics.Metric;

import java.util.*;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Standard implementation of {@link ApiMetrics.ApiMetricsBuilder}.
 * <p>
 * The builder starts empty. Users specify the packages to scan, any packages
 * to exclude, the metrics to apply and the accumulators that gather results.
 * Package exclusions override inclusions.
 * <p>
 * Metrics and accumulators can be registered one by one or via
 * {@link #addStandardMetrics()} and {@link #addStandardAccumulators()}, which
 * include Chronicle's predefined sets. If no metrics are added {@link #build()}
 * will throw an {@link IllegalStateException}. Accumulators are optional, but
 * an empty set yields no output.
 * <p>
 * The build step scans the configured packages, respecting exclusions, and
 * separates results from public and internal packages.
 */
public final class StandardApiMetricsBuilder implements ApiMetrics.ApiMetricsBuilder {

    // Configurations
    private final List<String> packageNames = new ArrayList<>();
    private final List<String> packageNameExclusions = new ArrayList<>();
    private final Map<Class<?>, Set<Metric<?>>> metrics = new HashMap<>();
    private final Set<Accumulator> accumulators = new LinkedHashSet<>();
    private final Set<Accumulator> internalAccumulators = new LinkedHashSet<>();

    @Override
    public StandardApiMetricsBuilder addPackage(final String packageName) {
        requireNonNull(packageName);
        packageNames.add(packageName);
        return this;
    }

    @Override
    public ApiMetrics.ApiMetricsBuilder addPackageExclusion(String paketName) {
        requireNonNull(paketName);
        packageNameExclusions.add(paketName);
        return this;
    }

    @Override
    public ApiMetrics.ApiMetricsBuilder addMetric(Metric<?> metric) {
        requireNonNull(metric);
        metrics.computeIfAbsent(metric.nodeType(), nt -> Collections.newSetFromMap(new IdentityHashMap<>()))
                .add(metric);
        return this;
    }

    @Override
    public ApiMetrics.ApiMetricsBuilder addStandardMetrics() {
        StandardMetrics.stream().forEach(this::addMetric);
        return this;
    }

    @Override
    public ApiMetrics.ApiMetricsBuilder addAccumulator(Supplier<Accumulator> accumulator) {
        requireNonNull(accumulator);
        accumulators.add(requireNonNull(accumulator.get()));
        return this;
    }

    @Override
    public ApiMetrics.ApiMetricsBuilder addStandardAccumulators() {
        StandardAccumulators.stream().forEach(this::addAccumulator);
        return this;
    }

    /**
     * Scans the configured packages and builds the resulting {@link ApiMetrics}.
     * Packages with names containing {@code .internal.} or ending with
     * {@code .internal} are treated as internal and are aggregated separately.
     *
     * @return the constructed metrics instance
     * @throws IllegalStateException if no metrics have been supplied
     */
    @Override
    public ApiMetrics build() {
        if (metrics.isEmpty())
            throw new IllegalStateException("No Metrics provided");

        ClassInfoList.ClassInfoFilter filter = ci -> !ci.getPackageName().contains(".internal.") && !ci.getPackageName().contains(".internal");
        computeFor(accumulators, filter);

        ClassInfoList.ClassInfoFilter internalFilter = ci -> ci.getPackageName().contains(".internal.") || ci.getPackageName().contains(".internal");
        computeFor(internalAccumulators, internalFilter);

        return new StandardApiMetrics(accumulators, internalAccumulators);
    }

    private void computeFor(final Set<Accumulator> accumulators,
                            final ClassInfoList.ClassInfoFilter filter) {
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().enableAllInfo().scan()) {
            final ClassInfoList exposedClassInfoList = scanResult.getAllClasses()
                    .filter(filter)
                    .filter(ci -> packageNames.stream().anyMatch(pn -> ci.getPackageInfo().getName().startsWith(pn)))
                    .filter(ci -> packageNameExclusions.stream().noneMatch(pn -> ci.getPackageInfo().getName().startsWith(pn)));

            exposedClassInfoList.forEach(ci -> {
                this.accumulateApplicableMetrics(accumulators, ci);
                ci.getMethodInfo().forEach(mi -> accumulateApplicableMetrics(accumulators, mi));
                ci.getFieldInfo().forEach(fi -> accumulateApplicableMetrics(accumulators, fi));
            });
        }
    }

    void accumulateApplicableMetrics(final Set<Accumulator> accumulators,
                                     final FieldInfo fieldInfo) {
        metricsFor(FieldInfo.class).stream()
                .filter(m -> m.isApplicable(fieldInfo))
                .forEach(m -> accumulate(accumulators, m, fieldInfo.getClassInfo(), fieldInfo));
    }

    void accumulateApplicableMetrics(final Set<Accumulator> accumulators,
                                     final MethodInfo methodInfo) {
        metricsFor(MethodInfo.class).stream()
                .filter(m -> m.isApplicable(methodInfo))
                .forEach(m -> accumulate(accumulators, m, methodInfo.getClassInfo(), methodInfo));
    }

    void accumulateApplicableMetrics(final Set<Accumulator> accumulators,
                                     final ClassInfo classInfo) {
        metricsFor(ClassInfo.class).stream()
                .filter(m -> m.isApplicable(classInfo))
                .forEach(m -> accumulate(accumulators, m, classInfo, classInfo));
    }

    void accumulate(Set<Accumulator> accumulators, Metric<?> metric, ClassInfo classInfo, HasName leaf) {
        accumulators.forEach(a -> a.accept(metric, classInfo, leaf));
    }

    @SuppressWarnings("unchecked")
    private <T> Set<Metric<T>> metricsFor(Class<T> nodeType) {
        return (Set<Metric<T>>) (Set<?>) metrics.getOrDefault(nodeType, Collections.emptySet());
    }
}
