package pitest.test;

import spock.lang.Specification;

class LibrarySpock2Spec extends Specification {

    void "should generate some mutation coverage"() {
        given:
            Library classUnderTest = new Library();
        expect:
            classUnderTest.someLibraryMethod()
    }
}
