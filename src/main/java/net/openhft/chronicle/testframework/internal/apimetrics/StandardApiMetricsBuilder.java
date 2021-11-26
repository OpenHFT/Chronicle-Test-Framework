package net.openhft.chronicle.testframework.internal.apimetrics;

import io.github.classgraph.*;
import net.openhft.chronicle.testframework.apimetrics.Accumulator;
import net.openhft.chronicle.testframework.apimetrics.ApiMetrics;
import net.openhft.chronicle.testframework.apimetrics.Metric;

import java.util.*;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class StandardApiMetricsBuilder implements ApiMetrics.ApiMetricsBuilder {

    // Configurations
    private final List<String> packageNames = new ArrayList<>();
    private final Map<Class<?>, Set<Metric<?>>> metrics = new HashMap<>();
    private final Set<Accumulator> accumulators = new LinkedHashSet<>();
/*    // Accumulators
    private final Map<Metric<?>, Double> metricSums = new HashMap<>();
    private final Map<Class<?>, Double> classSums = new HashMap<>();*/

    @Override
    public StandardApiMetricsBuilder addPackage(final String packageName) {
        requireNonNull(packageName);
        packageNames.add(packageName);
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

    @Override
    public ApiMetrics build() {
        if (metrics.isEmpty())
            throw new IllegalStateException("There are no Metrics provided");
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().enableAllInfo().scan()) {
            final ClassInfoList exposedClassInfoList = scanResult.getAllClasses()
                    .filter(ci -> !ci.getPackageName().contains(".internal."))
                    .filter(ci -> packageNames.stream().anyMatch(pn -> ci.getPackageInfo().getName().startsWith(pn)));

            exposedClassInfoList.forEach(ci -> {
                this.accumulateApplicableMetrics(ci);
                ci.getMethodInfo().forEach(this::accumulateApplicableMetrics);
                ci.getFieldInfo().forEach(this::accumulateApplicableMetrics);
            });
        }
        return new StandardApiMetrics(accumulators);
    }

    void accumulateApplicableMetrics(final FieldInfo fieldInfo) {
        metricsFor(FieldInfo.class).stream()
                .filter(m -> m.isApplicable(fieldInfo))
                .forEach(m -> accumulate(m, fieldInfo.getClassInfo(), fieldInfo));
    }

    void accumulateApplicableMetrics(final MethodInfo methodInfo) {
        metricsFor(MethodInfo.class).stream()
                .filter(m -> m.isApplicable(methodInfo))
                .forEach(m -> accumulate(m, methodInfo.getClassInfo(), methodInfo));
    }

    void accumulateApplicableMetrics(final ClassInfo classInfo) {
        metricsFor(ClassInfo.class).stream()
                .filter(m -> m.isApplicable(classInfo))
                .forEach(m -> accumulate(m, classInfo, classInfo));
    }

    void accumulate(Metric<?> metric, ClassInfo classInfo, HasName leaf) {
        accumulators.forEach(a -> a.accept(metric, classInfo, leaf));
    }

    @SuppressWarnings("unchecked")
    private <T> Set<Metric<T>> metricsFor(Class<T> nodeType) {
        return (Set<Metric<T>>) (Set<?>) metrics.getOrDefault(nodeType, Collections.emptySet());
    }
}