package com.topsoup.navigate;

import java.util.ArrayList;
import java.util.List;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.topsoup.navigate.activity.MyInfoActivity;
import com.topsoup.navigate.activity.NavigateListMainActivity;
import com.topsoup.navigate.activity.SOSActivity;
import com.topsoup.navigate.activity.SettingsActivity;
import com.topsoup.navigate.base.BaseActivity;
import com.topsoup.navigate.model.Contact;
import com.topsoup.navigate.service.MyService;
import com.topsoup.navigate.utils.PhoneReader;
import com.topsoup.navigate.worker.DBWorker;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {
	private final int SDK_PERMISSION_REQUEST = 127;
	private String permissionInfo;

	public static final void start(BaseActivity activity) {
		if (activity != null)
			activity.startActivity(new Intent(activity, MainActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DBWorker.instance().init(this);
		openGPSSettings();
		getPersimmions();
		Log.i("SL", permissionInfo + "");
		startService(new Intent(this, MyService.class));
		// ComPassWorker comPassWorker = new ComPassWorker();
		// comPassWorker.start(this, null);
		PhoneReader.printInfo(this);
		// abc();
	}

	//
	// private void abc() {
	// SMSTask task = new SMSTask("abc", "http://58.56.109.38:18851/", "SPID",
	// "APPID", "REQID", "FIXTYPE");
	// Location location = new Location("test");
	// location.setLatitude(123.4);
	// location.setLongitude(12.4);
	// String xml = XMLBuilder.build(task, location);
	// Log.i("SL", "xml>" + xml);
	// Repoart.run(task.getSPURL_2(), xml);
	// }

	@Override
	protected void onResume() {
		super.onResume();
		checkSettings();
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
			onBackPressed();
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
			showToast("V1.0.4_Beta_20180517");
			break;
		default:
			break;
		}
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

	@TargetApi(23)
	private void getPersimmions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			ArrayList<String> permissions = new ArrayList<String>();
			/***
			 * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
			 */
			// 定位精确位置
			if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
			}
			if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
			}
			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
			// 读写权限
			if (addPermission(permissions,
					Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
			}
			// 读取电话状态权限
			if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
				permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
			}

			if (permissions.size() > 0) {
				requestPermissions(
						permissions.toArray(new String[permissions.size()]),
						SDK_PERMISSION_REQUEST);
			}
		}
	}

	@TargetApi(23)
	private boolean addPermission(ArrayList<String> permissionsList,
			String permission) {
		if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
			if (shouldShowRequestPermissionRationale(permission)) {
				return true;
			} else {
				permissionsList.add(permission);
				return false;
			}

		} else {
			return true;
		}
	}

	@TargetApi(23)
	@Override
	public void onRequestPermissionsResult(int requestCode,
			String[] permissions, int[] grantResults) {
		// TODO Auto-generated method stub
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

	}
}
