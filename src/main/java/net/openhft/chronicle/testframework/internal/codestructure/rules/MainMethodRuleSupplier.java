package net.openhft.chronicle.testframework.internal.codestructure.rules;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.*;
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
 * ArchUnit rule ensuring every class with a main method has a static
 * initialiser that calls {@code DtoAlias.init()}.
 * <p>
 * A main method is a static method named {@code main} that accepts one
 * {@code String[]} parameter. The static block registers alias metadata
 * before {@code main} runs.
 */
public class MainMethodRuleSupplier implements Supplier<ArchRule> {

    @Override
    public ArchRule get() {
        return classes().that().containAnyMethodsThat(new MainMethodPredicate()).should(new ContainsStaticBlockCondition()).allowEmptyShould(true);
    }

    /**
     * Matches methods regarded as a main method.
     * A method qualifies if it is static, named {@code main}
     * and takes one {@code String[]} parameter.
     */
    private static class MainMethodPredicate extends DescribedPredicate<JavaMethod> {
        public MainMethodPredicate() {
            super("are static main methods");
        }

        @Override
        public boolean test(JavaMethod javaMethod) {
            boolean isCalledMain = javaMethod.getName().equals("main");
            boolean isStatic = javaMethod.getModifiers().contains(JavaModifier.STATIC);
            boolean hasSingleParameter = javaMethod.getParameters().size() == 1;
            boolean hasCorrectParameterType = hasSingleParameter && javaMethod.getParameters().get(0).getRawType().isAssignableFrom(String[].class);
            return isCalledMain && isStatic && hasSingleParameter && hasCorrectParameterType;
        }
    }

    /**
     * Checks the presence of a static block calling {@code DtoAlias.init()}.
     * This ensures alias information is loaded before any main method runs.
     */
    private static class ContainsStaticBlockCondition extends ArchCondition<JavaClass> {

        public ContainsStaticBlockCondition() {
            super("contain a static block that calls DtoAlias.init()");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents conditionEvents) {

            // Enforce static block
            Set<JavaCodeUnit> codeUnits = javaClass.getCodeUnits();
            List<JavaCodeUnit> staticBlocks = codeUnits.stream().filter(codeUnit -> codeUnit.getClass().isAssignableFrom(JavaStaticInitializer.class)).collect(Collectors.toList());

            long totalCount = 0;
            for (JavaCodeUnit staticBlock : staticBlocks) {
                long appropriateStaticCallCount = staticBlock.getMethodCallsFromSelf().stream().filter(javaMethodCall -> {
                    AccessTarget.MethodCallTarget target = javaMethodCall.getTarget();
                    return target.getFullName().endsWith("DtoAlias.init()");
                }).count();
                totalCount += appropriateStaticCallCount;
            }

            conditionEvents.add(new SimpleConditionEvent(javaClass, totalCount == 1, String.format("The class %s does not contain exactly one static block that calls DtoAlias.init()", javaClass.getName())));
        }
    }
}
