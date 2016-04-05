package pitest.sample.multimodule.shared;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SharedTest {
    @Test
    public void testName() {
        Shared shared = new Shared("testname1");
        assertEquals("testname1", shared.getName());
        shared.setName("testname2");
        assertEquals("testname2", shared.getName());
    }

    @Test
    public void testCallLogger() {
        new Shared("P").readProperty();
    }
}
