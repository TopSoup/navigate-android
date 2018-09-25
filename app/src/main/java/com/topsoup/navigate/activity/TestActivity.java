package com.topsoup.navigate.activity;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;

import android.content.DialogInterface;
import android.view.View;

import com.topsoup.navigate.R;
import com.topsoup.navigate.base.BaseActivity;
import com.topsoup.navigate.worker.PhoneWorker;

@ContentView(R.layout.activity_test)
public class TestActivity extends BaseActivity {

	@Override
	public String[] buildOptionsMenu() {
		return null;
	}

	@Override
	public void onOptionMenuSelect(DialogInterface dialog, int which) {
	}

	@Event({ R.id.call_end, R.id.call_start, R.id.sos_start, R.id.sos_end })
	private void onBtnClick(View v) {
		switch (v.getId()) {
		case R.id.call_end:
			PhoneWorker.instance().endCall();
			break;
		case R.id.call_start:
			PhoneWorker.instance().call("18131120320");
			break;
		case R.id.sos_end:
			PhoneWorker.instance().stopSOS();
			break;
		case R.id.sos_start:
			PhoneWorker.instance().startSOS("18131120320", "10000");
			break;
		default:
			break;
		}
	}
}
