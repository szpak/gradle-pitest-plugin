package pitest.sample.multimodule.itest;

import pitest.sample.multimodule.shared.Shared;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IntegrationTest {

    @Test
    public void shouldGenerateMutationInLocalClass() {
        int result = new IntegrationUtil
                ().multiplyBy2(5);
        assertEquals(10, result);
    }

    @Test
    public void shouldGenerateMutationInSharedClass() {
        Shared shared = new Shared("testname1");
        assertEquals("testname1", shared.getName());
        shared.setName("testname2");
        assertEquals("testname2", shared.getName());
    }
}
