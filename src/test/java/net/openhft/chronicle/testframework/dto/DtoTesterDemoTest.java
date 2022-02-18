package net.openhft.chronicle.testframework.dto;

import org.junit.jupiter.api.Test;

import static net.openhft.chronicle.testframework.dto.DtoTester.MutatorType.MANDATORY;
import static net.openhft.chronicle.testframework.dto.DtoTester.MutatorType.OPTIONAL;

class DtoTesterDemoTest {

    @Test
    void demo() {

        DtoTester.builder(MyDto.class, MyDtoImpl::new)
                // Define all the accessors. This makes sure that
                // accessors have the same type and that they actually
                // exist as specified (compile time).
                .withAccessors(MyDto::name, MyDto::name)
                .withAccessors(MyDto::shoeSize, MyDto::shoeSize)

                // These mutators will be applied in various combinations
                .addMutator(MANDATORY, "name", MyDto::name, "Per")
                .addMutator(OPTIONAL, "shoeSize", MyDto::shoeSize, 12)

                // When applied, makes sure the object equals a new object of type T even
                // though the original object were mutated.
                .withResetter(MyDto::reset)

                // Setup validation rules
                /*
                .addValidationRule("name", MyDto::name, null)
                */

                // Assert the validation rules when all mandatory mutators have been invoked and with a
                // combination of optional ones
                .withValidator(MyDto::validate)

                .build()

                // Do all the tests, for example:
                // for each setter invocation, makes sure the hashCode is changed (might fail), equals
                // is false to a newly constructed object and with all combinations of mutators
                // This could be expanded in the future
                .test();
    }

    private interface MyDto extends Resettable, Validatable {

        String name();

        void name(String name);

        int shoeSize();

        void shoeSize(int shoeSize);

    }

    private static final class MyDtoImpl implements MyDto {

        private String name;
        private int shoeSize;

        public String name() {
            return name;
        }

        public void name(String name) {
            this.name = name;
        }

        public int shoeSize() {
            return shoeSize;
        }

        public void shoeSize(int shoeSize) {
            this.shoeSize = shoeSize;
        }

        @Override
        public void reset() {
            name = null;
            shoeSize = 0;
        }

        @Override
        public void validate() {
            if (name == null)
                throw new IllegalStateException();
            if (shoeSize < 0)
                throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MyDtoImpl myDto = (MyDtoImpl) o;

            if (shoeSize != myDto.shoeSize) return false;
            return name != null ? name.equals(myDto.name) : myDto.name == null;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + shoeSize;
            return result;
        }

        @Override
        public String toString() {
            return "MyDtoImpl{" +
                    "name='" + name + '\'' +
                    ", shoeSize=" + shoeSize +
                    '}';
        }
    }

    interface Resettable {
        void reset();
    }

    interface Validatable {
        void validate();
    }

}
