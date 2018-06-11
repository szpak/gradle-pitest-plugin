package pl.droidsonroids.robolectric.works;

import android.content.Context;
import android.support.annotation.NonNull;

public class StringHolder {
	private final String _test;

	public StringHolder(@NonNull Context context) {
		_test = "Test string";
	}

	public String getTest() {
		return _test;
	}
}
