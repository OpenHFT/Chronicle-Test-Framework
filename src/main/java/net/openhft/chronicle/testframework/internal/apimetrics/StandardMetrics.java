/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal.apimetrics;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;
import net.openhft.chronicle.testframework.apimetrics.Metric;

import java.lang.reflect.Modifier;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Provides the standard metrics used for API analysis.
 *
 * <p>The metrics examine class, method and field characteristics:
 * <ul>
 *   <li>{@link #CLASS_PUBLIC} - public classes (weight 10).</li>
 *   <li>{@link #CLASS_PROTECTED} - protected classes (weight 5).</li>
 *   <li>{@link #CLASS_EXTENDABLE_ACROSS_PACKAGE_BOUNDARIES}
 *       - public or protected classes that are neither final nor enums
 *       (weight 20).</li>
 *   <li>{@link #METHOD_PUBLIC} - public methods (weight 2).</li>
 *   <li>{@link #METHOD_PROTECTED} - protected methods (weight 1).</li>
 *   <li>{@link #METHOD_PUBLIC_AND_OVERRIDABLE}
 *       - public methods that may be overridden because neither the method
 *       nor its declaring class is final or an enum (weight 3).</li>
 *   <li>{@link #METHOD_PROTECTED_AND_OVERRIDABLE}
 *       - protected methods that may be overridden when the class is not final
 *       or an enum (weight 2).</li>
 *   <li>{@link #FIELD_PUBLIC}
 *       - public, non-static, non-final fields excluding enum constants
 *       (weight 5).</li>
 *   <li>{@link #FIELD_PUBLIC_STATIC_FINAL}
 *       - public static final fields excluding enum constants (weight 2).</li>
 *   <li>{@link #FIELD_PROTECTED} - protected fields (weight 2).</li>
 * </ul>
 * This class cannot be instantiated.
 */

final class StandardMetrics {

    static final Metric<ClassInfo> CLASS_PUBLIC = Metric.of(ClassInfo.class, predicateOfClass(Modifier::isPublic), "Class Public", 10);
    static final Metric<ClassInfo> CLASS_PROTECTED = Metric.of(ClassInfo.class, predicateOfClass(Modifier::isProtected), "Class Protected", 5);
    static final Metric<ClassInfo> CLASS_EXTENDABLE_ACROSS_PACKAGE_BOUNDARIES = Metric.of(ClassInfo.class, ci -> (Modifier.isProtected(ci.getModifiers()) || Modifier.isPublic(ci.getModifiers())) && !ci.isFinal() && !ci.isEnum(), "Class Extendable Across Package Boundaries", 20);
    static final Metric<MethodInfo> METHOD_PUBLIC = Metric.of(MethodInfo.class, predicateOfMethod(Modifier::isPublic), "Method Public", 2);
    static final Metric<MethodInfo> METHOD_PROTECTED = Metric.of(MethodInfo.class, predicateOfMethod(Modifier::isProtected), "Method Protected", 1);
    static final Metric<MethodInfo> METHOD_PUBLIC_AND_OVERRIDABLE = Metric.of(MethodInfo.class, mi -> mi.isPublic() && !mi.isFinal() && !mi.getClassInfo().isFinal() && !mi.getClassInfo().isEnum(), "Method Public And Overridable", 3);
    static final Metric<MethodInfo> METHOD_PROTECTED_AND_OVERRIDABLE = Metric.of(MethodInfo.class, mi -> !Modifier.isProtected(mi.getModifiers()) && !mi.isFinal() && !mi.getClassInfo().isFinal() && !mi.getClassInfo().isEnum(), "Method Protected And Overridable", 2);
    static final Metric<FieldInfo> FIELD_PUBLIC = Metric.of(FieldInfo.class, predicateOfPublicField(false), "Field Public", 5);
    static final Metric<FieldInfo> FIELD_PUBLIC_STATIC_FINAL = Metric.of(FieldInfo.class, predicateOfPublicField(true), "Field Public Static Final", 2);
    static final Metric<FieldInfo> FIELD_PROTECTED = Metric.of(FieldInfo.class, predicateOfField(Modifier::isProtected), "Field Protected", 2);
    // Suppresses default constructor, ensuring non-instantiability.
    private StandardMetrics() {
    }

    static Stream<Metric<?>> stream() {
        return Stream.of(
                CLASS_PUBLIC,
                CLASS_PROTECTED,
                CLASS_EXTENDABLE_ACROSS_PACKAGE_BOUNDARIES,
                METHOD_PUBLIC,
                METHOD_PROTECTED,
                METHOD_PUBLIC_AND_OVERRIDABLE,
                METHOD_PROTECTED_AND_OVERRIDABLE,
                FIELD_PUBLIC,
                FIELD_PUBLIC_STATIC_FINAL,
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

    private static Predicate<FieldInfo> predicateOfPublicField(boolean staticFinal) {
        return fieldInfo -> {
            final String typeName = fieldInfo.getTypeSignatureOrTypeDescriptor().toString();
            final boolean isEnumField = fieldInfo.getClassInfo().isEnum() && typeName.equals(fieldInfo.getClassName()) && fieldInfo.isFinal() && fieldInfo.isStatic();
            // public static final fields in enum classes that are of the same type as the Enum itself should not count
            if (staticFinal) {
                return fieldInfo.isPublic() && fieldInfo.isStatic() && fieldInfo.isFinal() && !isEnumField;
            } else {
                return fieldInfo.isPublic() && !fieldInfo.isStatic() && !fieldInfo.isFinal() && !isEnumField;
            }
        };
    }
}
