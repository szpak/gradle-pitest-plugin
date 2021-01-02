package pl.droidsonroids.robolectric.fails;

import android.content.Context;

import org.junit.Ignore;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import pl.droidsonroids.robolectric.R;
import pl.droidsonroids.robolectric.RobolectricTest;

import static org.junit.Assert.assertEquals;

public class StringHolderWithContextTest extends RobolectricTest {
	private Context _context = RuntimeEnvironment.application;

	@Test
	public void Test() {
		StringHolderWithContext worksInPITest = new StringHolderWithContext(_context);
		assertEquals("Wrong test string", _context.getString(R.string.test), worksInPITest.getTest());
	}
}