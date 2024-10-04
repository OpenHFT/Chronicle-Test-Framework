package net.openhft.chronicle.testframework.internal.codestructure.rules;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.function.Supplier;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Defines an ArchUnit rule that enforces that non-internal classes do not extend internal classes.
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
