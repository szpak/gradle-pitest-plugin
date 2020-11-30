package pitest.sample.multimodule.forreport;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ForReportTest {
    @Test
    public void testName() {
        ForReport shared = new ForReport("testname1");
        assertEquals("testname1", shared.getName());
        shared.setName("testname2");
        assertEquals("testname2", shared.getName());
    }

    @Test
    public void testCallLogger() {
        new ForReport("P").readProperty();
    }
}
