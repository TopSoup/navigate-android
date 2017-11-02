package com.topsoup.navigate;

import java.util.List;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.topsoup.navigate.activity.MyInfoActivity;
import com.topsoup.navigate.activity.NavigateListMainActivity;
import com.topsoup.navigate.activity.SOSActivity;
import com.topsoup.navigate.activity.SettingsActivity;
import com.topsoup.navigate.base.BaseActivity;
import com.topsoup.navigate.model.Contact;
import com.topsoup.navigate.service.MyService;
import com.topsoup.navigate.service.MyService.MyBinder;
import com.topsoup.navigate.utils.PhoneReader;
import com.topsoup.navigate.worker.DBWorker;
import com.topsoup.navigate.worker.PhoneWorker;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity implements ServiceConnection {
	private MyService service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DBWorker.instance().init(this);
		openGPSSettings();
		// startService(new Intent(this, MyService.class));
		bindService(new Intent(this, MyService.class), this,
				Context.BIND_AUTO_CREATE);
//		ComPassWorker comPassWorker = new ComPassWorker();
//		comPassWorker.start(this, null);
		PhoneWorker.instance().start(this);
		PhoneReader.printInfo(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkSettings();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.service = ((MyBinder) service).getService();
		this.service.setGPS(app.getGpsWorker());
		this.service.setSOS(app.getSmsWorker());
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {

	}

	@Event({ R.id.myinfo, R.id.navigate, R.id.sos })
	private void onBtnClick(View v) {
		switch (v.getId()) {
		case R.id.myinfo:
			startActivity(new Intent(this, MyInfoActivity.class));
			break;
		case R.id.navigate:
			showNavigateDialog();
			break;
		case R.id.sos:
			new AlertDialog.Builder(this).setMessage("是否要发起SOS")
					.setNegativeButton("取消", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).setPositiveButton("发起", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(MainActivity.this,
									SOSActivity.class));
						}
					}).create().show();
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
			// TODO
			break;
		default:
			break;
		}
	}

	@Override
	public String[] buildOptionsMenu() {
		return new String[] { "使用说明", "设置", "版本" };
	}

	@Override
	public void onOptionMenuSelect(DialogInterface dialog, int which) {
		switch (which) {
		case 0:
			new AlertDialog.Builder(this)
					.setMessage("软件共分为【我在哪儿】【领航】和【SOS】三个模块，每个模块内都有相应介绍")
					.create().show();
			break;
		case 1:
			SettingsActivity.start(this);
			break;
		case 2:
			showToast("V1.0.0_Beta_20171020");
			break;
		default:
			break;
		}
	}

	private void openGPSSettings() {
		LocationManager alm = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			Toast.makeText(this, "GPS模块正常", Toast.LENGTH_SHORT).show();
			// tvInfo.setText("");
			return;
		}
		Toast.makeText(this, "请开启GPS！", Toast.LENGTH_SHORT).show();
		// tvInfo.setText("请打开GPS！");
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面
	}

	private void showNavigateDialog() {
		startActivity(new Intent(this, NavigateListMainActivity.class));
		// new AlertDialog.Builder(this)
		// .setItems(new String[] { "目的地列表", "短信目的列表", "使用说明" },
		// new OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog,
		// int which) {
		// switch (which) {
		// case 0://
		// NavigateListActivity
		// .showHistoryList(MainActivity.this);
		// break;
		// case 1:
		// NavigateListActivity
		// .showSOSList(MainActivity.this);
		// break;
		// case 2:
		// new AlertDialog.Builder(MainActivity.this)
		// .setMessage("领航说明").create().show();
		// break;
		// default:
		// break;
		// }
		// }
		// }).create().show();
	}

	public void checkSettings() {
		List<Contact> contacts = app.getDbWorker().getContactList();
		if (contacts == null || contacts.size() == 0) {
			SettingsActivity.start(this);
			showToast("首次使用请设置紧急联系人");
		}
	}
}
