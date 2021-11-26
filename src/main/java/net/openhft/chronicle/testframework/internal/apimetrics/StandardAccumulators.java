package net.openhft.chronicle.testframework.internal.apimetrics;

import io.github.classgraph.MethodInfo;
import net.openhft.chronicle.testframework.apimetrics.Accumulator;

import java.util.function.Supplier;
import java.util.stream.Stream;

public final class StandardAccumulators {

    // Suppresses default constructor, ensuring non-instantiability.
    private StandardAccumulators() {
    }

    static final Supplier<Accumulator> PER_METRIC = () -> Accumulator.of("metric", (m, ci, l) -> m.toString());
    static final Supplier<Accumulator> PER_CLASS = () -> Accumulator.of("class", (m, ci, l) -> ci.loadClass().getName());
    static final Supplier<Accumulator> PER_PACKAGE = () -> Accumulator.of("package", (m, ci, l) -> ci.getPackageName());
    static final Supplier<Accumulator> PER_METHOD_REFERENCE = () -> Accumulator.of("method reference", (m, ci, l) -> ((MethodInfo) l).getClassName() + "::" + l.getName(), (m, ci, l) -> l instanceof MethodInfo);

    static Stream<Supplier<Accumulator>> stream() {
        return Stream.of(PER_METRIC, PER_PACKAGE, PER_CLASS, PER_METHOD_REFERENCE);
    }

    public static final Supplier<Accumulator> PER_METHOD = () -> Accumulator.of("method reference", (m, ci, l) -> ((MethodInfo) l).getClassName() + "." + l.getName() + ((MethodInfo) l).getTypeSignatureOrTypeDescriptorStr(), (m, ci, l) -> l instanceof MethodInfo);
    public static final Supplier<Accumulator> PER_CLASS_AND_METRIC = () -> Accumulator.of(
            "class",
            (m, ci, l) -> ci.loadClass().getName(),
            "metric",
            (m, ci, l) -> m.toString());

}
