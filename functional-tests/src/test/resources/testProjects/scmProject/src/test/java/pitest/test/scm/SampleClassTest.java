package pitest.test.scm;

import org.junit.Assert;
import org.junit.Test;

public class SampleClassTest {

    @Test
    public void test() {
        pitest.test.scm.SampleClass sampleClass = new pitest.test.scm.SampleClass();
        Assert.assertTrue(sampleClass.alwaysTrue());
    }
}
