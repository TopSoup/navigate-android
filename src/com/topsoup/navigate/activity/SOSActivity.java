package com.topsoup.navigate.activity;

import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.topsoup.navigate.AppConfig;
import com.topsoup.navigate.MainActivity;
import com.topsoup.navigate.R;
import com.topsoup.navigate.base.BaseActivity;
import com.topsoup.navigate.event.SOSEvent;
import com.topsoup.navigate.model.Contact;

@ContentView(R.layout.activity_sos)
public class SOSActivity extends BaseActivity implements Runnable {

	private Handler mhHandler = new Handler();
	@ViewInject(R.id.tip)
	private TextView sosTip;
	private int count = 0;

	@ViewInject(R.id.right)
	private TextView back;

	private PowerManager pwManager;
	private PowerManager.WakeLock wakeLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pwManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pwManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "sosac");
		wakeLock.acquire();
		back.setText("取消");
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				MainActivity.start(SOSActivity.this);
				finish();
			}
		});
	}

	private void openGPSSettings() {
		LocationManager alm = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			Toast.makeText(this, "GPS定位模块正常", Toast.LENGTH_SHORT).show();
			return;
		} else if (alm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Toast.makeText(this, "网络定位模块正常", Toast.LENGTH_SHORT).show();
			return;
		}
		Toast.makeText(this, "请开启GPS！", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面
	}

	private void start() {
		SOSEvent event = EventBus.getDefault().getStickyEvent(SOSEvent.class);

		if (event == null || event != SOSEvent.START) {
			app.getGpsWorker().start(this, AppConfig.GPS_minTime, "sendSos");// 开启定位
			EventBus.getDefault().postSticky(SOSEvent.START);
			mhHandler.postDelayed(this, 500);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		openGPSSettings();
		if (checkSettings())
			start();
	}

	public boolean checkSettings() {
		List<Contact> contacts = app.getDbWorker().getContactList();
		if (contacts == null || contacts.size() == 0) {
			SettingsActivity.start(this);
			showToast("首次使用请设置紧急联系人");
			return false;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		EventBus.getDefault().postSticky(SOSEvent.STOP);
		app.getGpsWorker().stop("sendSos");
		if (wakeLock != null && wakeLock.isHeld())
			wakeLock.release();
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
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			showOptions();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
