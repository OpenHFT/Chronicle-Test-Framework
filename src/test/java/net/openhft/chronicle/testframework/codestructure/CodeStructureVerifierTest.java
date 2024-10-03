package net.openhft.chronicle.testframework.codestructure;

import net.openhft.chronicle.testframework.internal.codestructure.CodeStructureVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CodeStructureVerifierTest {

    @Test
    public void builderWithImproperArgumentsShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> CodeStructureVerifier.builder().build().verify(), "Cannot build test runner with no packages");
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

}