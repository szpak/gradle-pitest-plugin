package pl.droidsonroids.robolectric.works;

import android.content.Context;

import org.junit.Ignore;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import pl.droidsonroids.robolectric.RobolectricTest;

import static org.junit.Assert.assertEquals;

@Ignore
public class StringHolderTest extends RobolectricTest {
	private Context _context = RuntimeEnvironment.application;

	@Test
	public void Test() {
		StringHolder stringHolder = new StringHolder(_context);
		assertEquals("Wrong test string", "Test string", stringHolder.getTest());
	}
}