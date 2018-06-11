package pl.droidsonroids.robolectric.fails;

import android.content.Context;
import android.support.annotation.NonNull;

public class StringHolderWithContext {
	private final String _test;

	public StringHolderWithContext(@NonNull Context context) {
		_test = "Test string";
	}

	public String getTest() {
		return _test;
	}
}
