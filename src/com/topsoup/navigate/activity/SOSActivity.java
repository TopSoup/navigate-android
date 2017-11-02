package com.topsoup.navigate.activity;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.topsoup.navigate.AppConfig;
import com.topsoup.navigate.R;
import com.topsoup.navigate.base.BaseActivity;
import com.topsoup.navigate.event.SOSEvent;

@ContentView(R.layout.activity_sos)
public class SOSActivity extends BaseActivity implements Runnable {

	private Handler mhHandler = new Handler();
	@ViewInject(R.id.tip)
	private TextView sosTip;
	private int count = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		findViewById(R.id.right).setVisibility(View.GONE);
		EventBus.getDefault().postSticky(SOSEvent.START);
		mhHandler.postDelayed(this, 500);
		app.getGpsWorker().start(this, AppConfig.GPS_minTime);
	}

	@Override
	protected void onDestroy() {
		app.getGpsWorker().stop();
		super.onDestroy();
	}

	@Override
	public void run() {
		if (sosTip != null)
			sosTip.setTextColor((count % 2 == 0) ? Color.BLACK : Color.RED);
		count++;
		mhHandler.postDelayed(this, 500);
	}

	@Override
	public String[] buildOptionsMenu() {
		return new String[] { "结束SOS", "取消" };
	}

	@Override
	public void onOptionMenuSelect(DialogInterface dialog, int which) {
		switch (which) {
		case 0:
			onBackPressed();
			EventBus.getDefault().postSticky(SOSEvent.STOP);
			break;
		case 1:

			break;
		default:
			break;
		}
	}

	@Event({ R.id.left, R.id.center, R.id.right })
	private void myinfo(View v) {
		switch (v.getId()) {
		case R.id.left:
			showOptions();
			break;
		case R.id.center:
			// TODO
			break;
		case R.id.right:
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU)
			showOptions();
		return true;
	}
}
