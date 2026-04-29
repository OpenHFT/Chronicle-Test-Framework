/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal.codestructure.rules;

import com.tngtech.archunit.core.domain.AccessTarget;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaStaticInitializer;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Supplies an ArchUnit rule requiring each {@code DtoAlias} class to call the
 * {@code Bootstrap.bootstrap()} method from Chronicle Core and the bootstrap
 * method in the same package.  Both calls must appear exactly once inside a
 * static initialiser block.
 */
public class DtoAliasMustInvokeBootstrapRuleSupplier implements Supplier<ArchRule> {

    @Override
    public ArchRule get() {
        return classes().that().haveSimpleName("DtoAlias").should(new ContainsStaticBlockCallingBootstrap()).allowEmptyShould(true);
    }

    private static class ContainsStaticBlockCallingBootstrap extends ArchCondition<JavaClass> {

        /**
         * Fully qualified name of the bootstrap method defined in Chronicle Core.
         * Used to detect the mandatory invocation from a static block.
         */
        public static final String CORE_BOOTSTRAP_METHOD_TARGET = "net.openhft.chronicle.core.Bootstrap.bootstrap()";

        public ContainsStaticBlockCallingBootstrap() {
            super("contain a static block that calls bootstrap appropriately");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents conditionEvents) {

            // Fetch the static blocks
            Set<JavaCodeUnit> codeUnits = javaClass.getCodeUnits();
            List<JavaCodeUnit> staticBlocks = codeUnits.stream().filter(codeUnit -> codeUnit.getClass().isAssignableFrom(JavaStaticInitializer.class)).collect(Collectors.toList());

            // Count the calls to various bootstrap methods
            long coreBootstrapCount = getMethodInvocationsFromCodeUnits(staticBlocks, CORE_BOOTSTRAP_METHOD_TARGET);
            String samePackageBootstrapClass = javaClass.getPackageName() + ".Bootstrap.bootstrap()";
            long samePackageBootstrapCount = getMethodInvocationsFromCodeUnits(staticBlocks, samePackageBootstrapClass);

            // Fail if there is not exactly one call to each bootstrap method
            conditionEvents.add(
                    new SimpleConditionEvent(
                            javaClass,
                            coreBootstrapCount == 1,
                            String.format("The class %s does not contain one call to %s", javaClass.getName(), CORE_BOOTSTRAP_METHOD_TARGET))
            );
            conditionEvents.add(
                    new SimpleConditionEvent(
                            javaClass,
                            samePackageBootstrapCount == 1,
                            String.format("The class %s does not contain one call to %s", javaClass.getName(), samePackageBootstrapClass))
            );
        }

        private static long getMethodInvocationsFromCodeUnits(List<JavaCodeUnit> codeUnits, String methodTarget) {
            long totalCount = 0;
            for (JavaCodeUnit staticBlock : codeUnits) {
                long callCount = staticBlock.getMethodCallsFromSelf().stream().filter(javaMethodCall -> {
                    AccessTarget.MethodCallTarget target = javaMethodCall.getTarget();
                    return target.getFullName().equals(methodTarget);
                }).count();
                totalCount += callCount;
            }
            return totalCount;
        }
    }
}
