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
    static final Metric<ClassInfo> CLASS_EXTENDABLE_ACROSS_PACKAGE_BOUNDARIES = Metric.of(ClassInfo.class, ci -> (Modifier.isProtected(ci.getModifiers()) || Modifier.isPublic(ci.getModifiers())) && !ci.isFinal(), "Class Extendable Across Package Boundaries", 20);
    static final Metric<MethodInfo> METHOD_PUBLIC = Metric.of(MethodInfo.class, predicateOfMethod(Modifier::isPublic), "Method Public", 2);
    static final Metric<MethodInfo> METHOD_PROTECTED = Metric.of(MethodInfo.class, predicateOfMethod(Modifier::isProtected), "Method Protected", 1);
    static final Metric<MethodInfo> METHOD_PUBLIC_AND_OVERRIDABLE = Metric.of(MethodInfo.class, mi -> mi.isPublic() && !mi.isFinal() && !mi.getClassInfo().isFinal(), "Method Public And Overridable", 3);
    static final Metric<MethodInfo> METHOD_PROTECTED_AND_OVERRIDABLE = Metric.of(MethodInfo.class, mi -> !Modifier.isProtected(mi.getModifiers()) && !mi.isFinal() && !mi.getClassInfo().isFinal(), "Method Protected and Overridable", 2);
    static final Metric<FieldInfo> FIELD_PUBLIC = Metric.of(FieldInfo.class, predicateOfField(Modifier::isPublic), "Field Public", 5);
    static final Metric<FieldInfo> FIELD_PROTECTED = Metric.of(FieldInfo.class, predicateOfField(Modifier::isProtected), "Field Protected", 2);

    static Stream<Metric<?>> stream() {
        return Stream.<Metric<?>>of(
                CLASS_PUBLIC,
                CLASS_PROTECTED,
                CLASS_EXTENDABLE_ACROSS_PACKAGE_BOUNDARIES,
                METHOD_PUBLIC,
                METHOD_PROTECTED,
                METHOD_PUBLIC_AND_OVERRIDABLE,
                METHOD_PROTECTED_AND_OVERRIDABLE,
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