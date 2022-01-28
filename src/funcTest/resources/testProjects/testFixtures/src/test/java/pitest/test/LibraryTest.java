package pitest.test;

import org.junit.Test;

import pitest.test.Library;

import static org.junit.Assert.*;

public class LibraryTest {
    @Test
    public void testSomeLibraryMethod() {
        Library classUnderTest = new Library();
        assertTrue("someLibraryMethod should return 'true'", classUnderTest.someLibraryMethod());
    }
}
