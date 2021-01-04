package pitest.test;

import spock.lang.Specification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LibrarySpock2Spec extends Specification {

    @Test
    public void shouldGenerateSomeMutationCoverage() {
        Library classUnderTest = new Library();
        assertTrue(classUnderTest.someLibraryMethod());
    }
}
