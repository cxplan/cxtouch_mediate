package com.cxplan.mediate.inputer;

import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.InputConnection;

import com.cxplan.mediate.CXApplication;
import com.cxplan.mediate.R;

public class CXTouchIME extends InputMethodService {
	@Override
	public View onCreateInputView() {
		View mInputView = getLayoutInflater().inflate(R.layout.inputer_view, null);

		return mInputView;
	}

	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onBindInput() {
		CXApplication.isIMERunning = true;
	}

	@Override
	public void onUnbindInput() {
		CXApplication.isIMERunning = false;
	}

	@Override
	public int onStartCommand(Intent paramIntent, int flags, int startId)
	{
		String text = paramIntent.getStringExtra("s");
		InputConnection inputConnection = getCurrentInputConnection();
		if (inputConnection != null) {
			inputConnection.commitText(text, 1);
		}
		return super.onStartCommand(paramIntent, flags, startId);
	}
}