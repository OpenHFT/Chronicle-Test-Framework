package net.openhft.chronicle.testframework.internal.codestructure;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import net.openhft.chronicle.testframework.internal.codestructure.rules.DtoAliasMustInvokeBootstrapRuleSupplier;
import net.openhft.chronicle.testframework.internal.codestructure.rules.MainMethodRuleSupplier;
import net.openhft.chronicle.testframework.internal.codestructure.rules.NonInternalClassesMustNotExtendInternalClassesRuleSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Runs a set of tests to check for potential code structure issues. Intended to be used from within unit tests to
 * assert that the structure of the codebase meets a number of predefined rules. Create a builder, instantiate the
 * verifier and run the {@link #verify()} method. This method will throw an {@link AssertionError} if any of the rules
 * are violated. A simple usage example is shown below:
 *
 * <pre>
 * {@code
 *     CodeStructureVerifier.builder()
 *      .importClass(ExampleClass.class)
 *      .build()
 *      .verify()
 * }
 * </pre>
 */
public class CodeStructureVerifier {

    private static final Logger log = LoggerFactory.getLogger(CodeStructureVerifier.class);

    private final JavaClasses javaClasses;

    private final Set<ArchRule> rules;

    private CodeStructureVerifier(JavaClasses javaClasses, Set<ArchRule> rules) {
        if (javaClasses == null)
            throw new IllegalArgumentException("Cannot set up test runner with no classes");
        this.javaClasses = javaClasses;
        if (rules == null || rules.isEmpty())
            throw new IllegalArgumentException("Cannot set up test runner with no rules");
        this.rules = rules;
    }

    /**
     * Runs the code structure verification test and ensures that all rules are met - if they are not then an
     * assertion error is thrown.
     *
     * @throws AssertionError if any of the rules are violated
     */
    public void verify() {
        log.info("Running code structure test with the following rules: {}", rules);
        CompositeArchRule compositeArchRule = CompositeArchRule.of(rules);
        compositeArchRule.check(javaClasses);
    }

    /**
     * Creates a new builder.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder that constructs {@link CodeStructureVerifier} instances.
     */
    public static class Builder {

        private final Set<ArchRule> rulesToSkip = new HashSet<>();
        private final Set<ArchRule> rules = new HashSet<>();
        private Class<?> clazz;
        private String[] packages;

        /**
         * Add additional arch rules to the test runner
         */
        public Builder withRule(ArchRule rule) {
            if (rule == null) throw new NullPointerException("rule cannot be null");
            rules.add(rule);
            return this;
        }

        /**
         * Skip a specific rule.
         */
        public Builder skipRule(ArchRule rule) {
            if (rule == null) throw new NullPointerException("rule cannot be null");
            rulesToSkip.add(rule);
            return this;
        }

        /**
         * Configure the test runner to analyse a single class.
         */
        public Builder importClass(Class<?> clazz) {
            if (clazz == null) throw new NullPointerException("clazz cannot be null");
            this.clazz = clazz;
            return this;
        }

        /**
         * Set up an expression to scan a set of packages.
         */
        public Builder importPackages(String... packages) {
            if (packages == null || packages.length == 0)
                throw new IllegalArgumentException("Cannot import empty packages");
            this.packages = packages;
            return this;
        }

        /**
         * Set up default imports to scan that will cover most use cases and be used in most tests.
         */
        public Builder defaultImports() {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Install default set of project wide rules.
         */
        private void installDefaultRules() {
            rules.add(new MainMethodRuleSupplier().get());
            rules.add(new NonInternalClassesMustNotExtendInternalClassesRuleSupplier().get());
            rules.add(new DtoAliasMustInvokeBootstrapRuleSupplier().get());
        }

        private JavaClasses getJavaClasses() {
            JavaClasses javaClasses;
            if (clazz != null) {
                javaClasses = new ClassFileImporter().importClasses(clazz);
            } else if (packages != null && packages.length > 0) {
                javaClasses = new ClassFileImporter().importPackages(packages);
            } else {
                throw new IllegalArgumentException("Cannot build test runner with no packages");
            }
            return javaClasses;
        }

        /**
         * Skips rules if skipping has been specified. Unfortunately comparing descriptions of the skippable rules and
         * the pre-configured rules is the only way to check for equivalence.
         */
        private void skipRules() {
            for (ArchRule ruleToSkip : rulesToSkip) {
                rules.removeIf(rule -> rule.getDescription().equals(ruleToSkip.getDescription()));
            }
        }

        /**
         * Build the {@link CodeStructureVerifier} instance.
         */
        public CodeStructureVerifier build() {
            installDefaultRules();
            skipRules();
            JavaClasses javaClasses;
            javaClasses = getJavaClasses();
            return new CodeStructureVerifier(javaClasses, rules);
        }

    }

}
