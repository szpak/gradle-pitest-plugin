package pitest.test;

import org.junit.jupiter.api.Test;
import pitest.test.Library;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryJUnit5Test {
    @Test public void testSomeLibraryMethod() {
        Library classUnderTest = new Library();
        assertTrue(classUnderTest.someLibraryMethod(), "someLibraryMethod should return 'true'");
    }
}
