package com.topsoup.navigate.activity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.ex.DbException;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.topsoup.navigate.AppConfig;
import com.topsoup.navigate.R;
import com.topsoup.navigate.base.BaseActivity;
import com.topsoup.navigate.model.MyLocation;
import com.topsoup.navigate.model.Satellite;
import com.topsoup.navigate.service.IComPassListener;
import com.topsoup.navigate.utils.SLUtils;
import com.topsoup.navigate.worker.ComPassWorker;
import com.topsoup.navigate.worker.DBWorker;

@ContentView(R.layout.activity_myinfo)
public class MyInfoActivity extends BaseActivity implements IComPassListener {
	@ViewInject(R.id.tv_status)
	private TextView tvStatus;

	@ViewInject(R.id.tv_time)
	private TextView tvTime;

	@ViewInject(R.id.tv_lon)
	private TextView tvLon;

	@ViewInject(R.id.tv_lat)
	private TextView tvLat;

	@ViewInject(R.id.tv_speed)
	private TextView tvSpeed;

	@ViewInject(R.id.tv_direction)
	private TextView tvDirection;

	@ViewInject(R.id.tv_altitude)
	private TextView tvAltitude;

	private Location last;
	private Satellite satellite;

	private ComPassWorker comPassWorker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showTitle("我在哪儿");
		app.getGpsWorker().start(this, AppConfig.GPS_minTime);
	}

	@Override
	protected void onDestroy() {
		app.getGpsWorker().stop();
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (comPassWorker == null)
			comPassWorker = new ComPassWorker();
		comPassWorker.start(this, this);
		registerEvent(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (comPassWorker != null)
			comPassWorker.stop();
		unRegisterEvent(this);
	}

	@Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
	public void onLocate(Location last) {
		this.last = last;
		refreshUI();
	}

	@Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
	public void onSatellite(Satellite last) {
		satellite = last;
		refreshUI();
	}

	private void refreshUI() {
		int count = 0;
		double lon = 0, lat = 0;
		float speed = 0, speed_j = 0;
		long time = 0;
		double altitude = 0;
		if (last != null) {
			lon = last.getLongitude();
			lat = last.getLatitude();
			time = last.getTime();
			altitude = last.getAltitude();
			speed = (float) (last.getSpeed() * 3.6);
			speed_j = (float) (speed * 0.5144444);
		}
		if (satellite != null)
			count = satellite.count;
		tvStatus.setText(getString(R.string.myinfo_1_status, "卫星", count));
		tvTime.setText(getString(R.string.myinfo_2_time, SLUtils.format(time)));
		String[] lonResult = SLUtils.dd2dm(lon);
		tvLon.setText(getString(R.string.myinfo_3_lon, lonResult[0],
				lonResult[1]));
		String[] latResult = SLUtils.dd2dm(lat);
		tvLat.setText(getString(R.string.myinfo_4_lat, latResult[0],
				latResult[1]));
		tvSpeed.setText(getString(R.string.myinfo_5_speed, speed_j + "", speed
				+ ""));
		tvDirection.setText(getString(R.string.myinfo_6_direction, 0));
		tvDirection.setVisibility(View.GONE);
		tvAltitude.setText(getString(R.string.myinfo_7_altitude, altitude));
	}

	private void onSaveClick(View v) {
		if (last != null) {
			MyLocation myLocation = new MyLocation();
			myLocation.createTime = last.getTime();
			myLocation.lat = last.getLatitude();
			myLocation.lon = last.getLongitude();
			myLocation.name = SLUtils.format(last.getTime());
			try {
				DBWorker.instance().addHistory(myLocation);
				showToast("保存成功");
			} catch (DbException e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(this, "没有定位，请稍后重试", Toast.LENGTH_SHORT).show();
		}
		// try {
		// List<MyLocation> list = DBWorker.instance().getHistoryList();
		// if (list == null)
		// list = new ArrayList<MyLocation>();
		// // tvCount.setText("历史记录条数：" + list.size());
		// } catch (DbException e) {
		// e.printStackTrace();
		// }
	}

	private void onReportClick(View v) {
		SendLocateActivity.start(this, last);
	}

	private void onHelpClick(View v) {
		new AlertDialog.Builder(this)
				.setMessage(
						"1、经纬度数据为卫星定位结果；\n2、现在前进的速度用公里和节表示；\n3、现在前进的方向用数字表示(0正北，90正东，180正南，270正西)；\n4、海拔表示高度；")
				.setCancelable(true).create().show();
	}

	@Override
	public void onDegree(float degree) {
		if (tvDirection != null)
			tvDirection.setText(getString(R.string.myinfo_6_direction, degree
					+ ""));
	}

	@Override
	public void onMsg(String msg) {
		showToast(msg);
	}

	@Event({ R.id.left, R.id.center, R.id.right })
	private void onToolBarClick(View v) {
		switch (v.getId()) {
		case R.id.left:
			showOptions();
			break;
		case R.id.center:
			break;
		case R.id.right:
			onBackPressed();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			showOptions();
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public String[] buildOptionsMenu() {
		return new String[] { "保存位置", "报告位置", "文本说明" };
	}

	@Override
	public void onOptionMenuSelect(DialogInterface dialog, int which) {
		switch (which) {
		case 0:
			onSaveClick(null);
			break;
		case 1:
			onReportClick(null);
			break;
		case 2:
			onHelpClick(null);
			break;
		default:
			break;
		}
	}
}
