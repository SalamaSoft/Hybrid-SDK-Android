package com.salama.android.webviewutil;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;

public class TabContentSpec {
	private String _tag;
	private Intent _intent;

	public String getTag() {
		return _tag;
	}

	public void setTag(String tag) {
		_tag = tag;
	}

	public Intent getIntent() {
		return _intent;
	}

	public void setIntent(Intent intent) {
		_intent = intent;
	}

	public TabContentSpec() {
	}

	public TabContentSpec(String tag, Intent intent) {
		_tag = tag;
		_intent = intent;
	}

}
