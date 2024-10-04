package net.openhft.chronicle.testframework.codestructure;

import net.openhft.chronicle.testframework.internal.codestructure.CodeStructureVerifier;
import net.openhft.chronicle.testframework.internal.codestructure.rules.DtoAliasMustInvokeBootstrapRuleSupplier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CodeStructureVerifierTest {

    @Nested
    class BuilderTests {

        @Test
        public void builderWithImproperArgumentsShouldFail() {
            assertThrows(IllegalArgumentException.class, () -> CodeStructureVerifier.builder().build().verify(), "Cannot build test runner with no packages");
        }

        @Test
        void explicitlySkippingRuleShouldRemoveItFromTheRuleSet() {
            CodeStructureVerifier.builder()
                    .importClass(net.openhft.chronicle.testframework.codestructure.broken.DtoAlias.class) // This would break if the rule wasn't skipped
                    .skipRule(new DtoAliasMustInvokeBootstrapRuleSupplier().get()) // Skip this rule
                    .build()
                    .verify();
        }

        @Test
        void explicitlyExcludeAClassShouldStopItFromFailingATest() {
            CodeStructureVerifier.builder()
                    .importClass(net.openhft.chronicle.testframework.codestructure.broken.DtoAlias.class) // This would break if the class wasn't excluded from scanning
                    .skipClass(net.openhft.chronicle.testframework.codestructure.broken.DtoAlias.class) // Skip this class
                    .build()
                    .verify();
        }

        @Test
        void scanEverythingInPackageAndFindArchitectureErrors() {
            String packageToScan = this.getClass().getPackage().getName();
            assertThrows(AssertionError.class, () -> CodeStructureVerifier.builder().importPackages(packageToScan).build().verify(), "Architecture Violation [Priority: MEDIUM]");
        }

        /**
         * If this test fails you likely need to add another call to {@link CodeStructureVerifier.Builder#skipClass(Class)}.
         */
        @Test
        void scanEverythingInPackageButSkipAllBrokenClasses() {
            String packageToScan = this.getClass().getPackage().getName();
            CodeStructureVerifier.builder()
                    .importPackages(packageToScan)
                    .skipClass(NonCompliantMainNoStaticBlock.class)
                    .skipClass(net.openhft.chronicle.testframework.codestructure.DtoAlias.class)
                    .skipClass(net.openhft.chronicle.testframework.codestructure.broken.DtoAlias.class)
                    .skipClass(ExtendsInternal.class)
                    .build()
                    .verify();
        }

    }

    @Nested
    class MainMethodRuleTests {

        @Test
        public void compliantMain() {
            CodeStructureVerifier.builder().importClass(CompliantMain.class).build().verify();
        }

        @Test
        public void nonCompliantMainNoStaticBlock() {
            assertThrows(AssertionError.class,
                    () -> CodeStructureVerifier.builder().importClass(NonCompliantMainNoStaticBlock.class).build().verify(),
                    "NonCompliantMainNoStaticBlock does not contain exactly one static block that calls DtoAlias.init()"
            );
        }

    }

    @Nested
    class NonInternalClassesMustNotExtendInternalClassesRuleTests {

        @Test
        void shouldNotBeAbleToExtendInternalClass() {
            assertThrows(AssertionError.class,
                    () -> CodeStructureVerifier.builder().importClass(ExtendsInternal.class).build().verify(),
                    "ExtendsInternal extends an internal class"
            );
        }

        @Test
        void shouldBeAbleToDelegateToInternalClass() {
            CodeStructureVerifier.builder().importClass(DelegatesToInternal.class).build().verify();
        }

    }

    @Nested
    class DtoAliasMustInvokeBootstrapRuleTests {

        @Test
        void compliantDtoAlias() {
            CodeStructureVerifier.builder().importClass(net.openhft.chronicle.testframework.codestructure.DtoAlias.class).build().verify();
        }

        @Test
        void nonCompliantDtoAlias() {
            assertThrows(
                    AssertionError.class,
                    () -> CodeStructureVerifier.builder().importClass(net.openhft.chronicle.testframework.codestructure.broken.DtoAlias.class).build().verify(),
                    "The class net.openhft.chronicle.testframework.codestructure.broken.DtoAlias does not contain one call to net.openhft.chronicle.core.Bootstrap.bootstrap()\n" +
                            "The class net.openhft.chronicle.testframework.codestructure.broken.DtoAlias does not contain one call to net.openhft.chronicle.testframework.codestructure.broken.Bootstrap.bootstrap()"
            );
        }

    }

}