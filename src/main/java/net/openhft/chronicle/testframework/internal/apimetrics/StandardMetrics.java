package net.openhft.chronicle.testframework.internal.apimetrics;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;
import net.openhft.chronicle.testframework.apimetrics.Metric;

import java.lang.reflect.Modifier;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

final class StandardMetrics {

    // Suppresses default constructor, ensuring non-instantiability.
    private StandardMetrics() {
    }

    static final Metric<ClassInfo> CLASS_PUBLIC = Metric.of(ClassInfo.class, predicateOfClass(Modifier::isPublic), "Class Public", 10);
    static final Metric<ClassInfo> CLASS_PROTECTED = Metric.of(ClassInfo.class, predicateOfClass(Modifier::isProtected), "Class Protected", 5);
    static final Metric<ClassInfo> CLASS_EXTENDABLE = Metric.of(ClassInfo.class, ci -> !ci.isFinal(), "Class Extendable", 10);
    static final Metric<MethodInfo> METHOD_PUBLIC = Metric.of(MethodInfo.class, predicateOfMethod(Modifier::isPublic), "Method Public", 2);
    static final Metric<MethodInfo> METHOD_PROTECTED = Metric.of(MethodInfo.class, predicateOfMethod(Modifier::isProtected), "Method Protected", 1);
    static final Metric<MethodInfo> METHOD_OVERRIDABLE = Metric.of(MethodInfo.class, mi -> !mi.isFinal(), "Method Overridable", 1);
    static final Metric<FieldInfo> FIELD_PUBLIC = Metric.of(FieldInfo.class, predicateOfField(Modifier::isPublic), "Field Public", 2);
    static final Metric<FieldInfo> FIELD_PROTECTED = Metric.of(FieldInfo.class, predicateOfField(Modifier::isProtected), "Field Protected", 1);

    static Stream<Metric<?>> stream() {
        return Stream.<Metric<?>>of(
                CLASS_PUBLIC,
                CLASS_PROTECTED,
                CLASS_EXTENDABLE,
                METHOD_PUBLIC,
                METHOD_PROTECTED,
                METHOD_OVERRIDABLE,
                FIELD_PUBLIC,
                FIELD_PROTECTED);
    }

    private static Predicate<ClassInfo> predicateOfClass(final IntPredicate modifierFilter) {
        return ci -> modifierFilter.test(ci.loadClass().getModifiers());
    }

    private static Predicate<MethodInfo> predicateOfMethod(final IntPredicate modifierFilter) {
        return mi -> modifierFilter.test(mi.getModifiers());
    }

    private static Predicate<FieldInfo> predicateOfField(final IntPredicate modifierFilter) {
        return fi -> modifierFilter.test(fi.getModifiers());
    }

}