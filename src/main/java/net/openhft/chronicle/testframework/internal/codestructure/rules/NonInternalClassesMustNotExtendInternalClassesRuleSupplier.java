/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal.codestructure.rules;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.function.Supplier;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Supplies a rule that fails when a non-internal class extends an internal one.
 * The rule relies on the regular expression
 * {@link RuleUtil#INTERNAL_PACKAGE_REGEX} which matches any class whose
 * package path contains {@code .impl.} or {@code .internal.}.  Any class
 * outside those packages must not inherit from a class within them.
 */
public class NonInternalClassesMustNotExtendInternalClassesRuleSupplier implements Supplier<ArchRule> {

    @Override
    public ArchRule get() {
        return classes()
                .that()
                .resideOutsideOfPackage("..internal..")
                .and()
                .resideOutsideOfPackage("..impl..")
                .should(new ArchCondition<JavaClass>("not extend internal classes") {
                    @Override
                    public void check(JavaClass javaClass, ConditionEvents events) {
                        if (javaClass.getSuperclass().isPresent()) {
                            String fullName = javaClass.getSuperclass().get().getName();
                            boolean packageIsInternal = fullName.matches(RuleUtil.INTERNAL_PACKAGE_REGEX);
                            events.add(new SimpleConditionEvent(javaClass, !packageIsInternal,
                                    String.format("The non-internal class %s extends internal class %s", javaClass.getName(), fullName)));
                        }
                    }
                }).allowEmptyShould(true);
    }
}
