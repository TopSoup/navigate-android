package com.topsoup.navigate.activity;

import org.greenrobot.eventbus.Subscribe;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.widget.TextView;

import com.topsoup.navigate.AppConfig;
import com.topsoup.navigate.R;
import com.topsoup.navigate.base.BaseActivity;
import com.topsoup.navigate.model.MyLocation;
import com.topsoup.navigate.model.SOS;
import com.topsoup.navigate.view.CompassView;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.util.Log;

import java.util.List;

@ContentView(R.layout.activity_navigate)
public class NavigateActivity extends BaseActivity implements SensorEventListener {
	private static final String TAG = NavigateActivity.class.getSimpleName();

	public static final void start(BaseActivity baseActivity, MyLocation locate) {
		baseActivity.startActivity(new Intent(baseActivity,
				NavigateActivity.class).putExtra("locate", locate));
	}

	public static final void start(BaseActivity baseActivity, SOS sos) {
		baseActivity.startActivity(new Intent(baseActivity,
				NavigateActivity.class).putExtra("sos", sos));
	}

	private MyLocation locate;
	private SOS sos;
	private String name;
	private double lat, lon;

	@ViewInject(R.id.name)
	private TextView tvName;
	@ViewInject(R.id.angle)
	private TextView tvAngle;
	@ViewInject(R.id.distance)
	private TextView tvDistance;
	@ViewInject(R.id.swipe)
	private SwipeRefreshLayout swipeRefreshLayout;
	@ViewInject(R.id.compassView)
	private CompassView compassView;

	private Sensor accelerometer; // 加速度传感器
	private Sensor sensor;
	private SensorManager sensorManager;
	private float currentDegree = 0f;

	private String gpsTag;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		findViewById(R.id.toolbar).setVisibility(View.GONE);
		Intent intent = getIntent();
		if (intent.hasExtra("locate")) {
			locate = (MyLocation) intent.getSerializableExtra("locate");
			name = locate.name;
			lat = locate.lat;
			lon = locate.lon;
			app.getGpsWorker().start(this, AppConfig.GPS_minTime,
					gpsTag = "navigate");
		} else if (intent.hasExtra("sos")) {
			sos = (SOS) intent.getSerializableExtra("sos");
			name = sos.user;
			lat = sos.getLat();
			lon = sos.getLon();
			if (sos.hasLocation)
				app.getGpsWorker().start(this, AppConfig.GPS_minTime,
						gpsTag = "sos");
		} else {
			showToast("参数错误");
			finish();
			return;
		}
		swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		// 设置监听
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				loadData();
			}
		});
		loadData();
		initSensor();
	}

	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
	}


	@Override
	protected void onPause() {
		sensorManager.unregisterListener(this);
		super.onPause();
	}

	@Override
	protected void onStop() {
		sensorManager.unregisterListener(this);
		super.onStop();
	}

	private void initSensor() {
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		// 初始化加速度传感器
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		if (hasHW()) {
			// 开始监听
			sensorManager.registerListener(this, accelerometer,
					Sensor.TYPE_ACCELEROMETER);
			sensorManager.registerListener(this, sensor,
					Sensor.TYPE_MAGNETIC_FIELD);
		}
	}

	public boolean hasHW() {
		boolean has1 = true, has2 = false;
		List<Sensor> deviceSensors = sensorManager
				.getSensorList(Sensor.TYPE_ALL);
		for (Sensor sensor : deviceSensors) {
//			if (sensor.getType() == Sensor.TYPE_ACCELEROMETER)
//				has1 = true;
//			else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
//				has2 = true;
			if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
				has2 = true;
			Log.i(TAG, sensor.getName());
		}
		return has1 && has2;
	}
	//----SensorEventListener---
	@Override
	public void onSensorChanged(SensorEvent event) {

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			accelerometerValues = event.values;
		}
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			magneticFieldValues = event.values;

			Log.i("SL", "magnetic value: " + magneticFieldValues);
		}
		calculateOrientation();
	}

	private float[] accelerometerValues = new float[3];
	private float[] magneticFieldValues = new float[3];

	// 计算方向
	private void calculateOrientation() {
		float[] values = new float[3];
		float[] R = new float[9];
		SensorManager.getRotationMatrix(R, null, accelerometerValues,
				magneticFieldValues);
		SensorManager.getOrientation(R, values);
		values[0] = (float) Math.toDegrees(values[0]);

		Log.i(TAG, values[0] + "");
		if (values[0] >= -5 && values[0] < 5) {
			Log.i(TAG, "正北");
		} else if (values[0] >= 5 && values[0] < 85) {
			Log.i(TAG, "东北");
		} else if (values[0] >= 85 && values[0] <= 95) {
			Log.i(TAG, "正东");
		} else if (values[0] >= 95 && values[0] < 175) {
			Log.i(TAG, "东南");
		} else if ((values[0] >= 175 && values[0] <= 180)
				|| (values[0]) >= -180 && values[0] < -175) {
			Log.i(TAG, "正南");
		} else if (values[0] >= -175 && values[0] < -95) {
			Log.i(TAG, "西南");
		} else if (values[0] >= -95 && values[0] < -85) {
			Log.i(TAG, "正西");
		} else if (values[0] >= -85 && values[0] < -5) {
			Log.i(TAG, "西北");
		}

		float degree = values[0];
		if (Math.abs(currentDegree - degree) > 1) {
			compassView.setRotate(degree);
			currentDegree = degree;

			Log.i(TAG, "========setRotate :" + degree);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		app.getGpsWorker().stop(gpsTag);
	}

	private void loadData() {
		swipeRefreshLayout.setRefreshing(true);
		Location target = new Location("test");
		target.setLatitude(lat);
		target.setLongitude(lon);
		if (sos != null && !sos.hasLocation) {
			showToast("无目标位置信息!");
		}
		float angle = app.getGpsWorker().angle(target);
		float distanceBysys = app.getGpsWorker().distance(target);
		float angleRun = app.getGpsWorker().angleRun();
		tvDistance.setText(String.format("距离：%.2f米", distanceBysys));
		tvDistance.setVisibility(View.GONE);
		tvName.setText(String.format("目标：%s", name));
		tvName.setVisibility(View.GONE);
		showTitle(String.format("目标：%s", name));
		tvAngle.setText(String.format("方位角：%.2f°", angle));
		tvAngle.setVisibility(View.GONE);
		compassView.setDegrees(angle, distanceBysys, angleRun);
		swipeRefreshLayout.setRefreshing(false);
	}

	@Override
	public String[] buildOptionsMenu() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onOptionMenuSelect(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub

	}

	@Subscribe(sticky = true)
	public void onLocate(Location last) {
		loadData();
	}

	@Subscribe()
	public void onSOS(SOS sos) {
		if (sos.hasLocation && sos.user.equals(tvName.getText().toString())) {
			lat = sos.getLat();
			lon = sos.getLon();
			loadData();
		}
	}
}
