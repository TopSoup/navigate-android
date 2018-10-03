package com.topsoup.navigate.worker;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.topsoup.navigate.service.IComPass;
import com.topsoup.navigate.service.IComPassListener;

public class ComPassWorker implements IComPass, SensorEventListener {
	private static final String TAG = "ComPassWorker";
	private SensorManager mSensorManager;
	private IComPassListener listener;
	private Sensor accelerometer; // 加速度传感器
	private Sensor magnetic; // 地磁场传感器

	@Override
	public void start(Context context, IComPassListener listener) {
		if (mSensorManager == null) {
			// 实例化传感器管理者
			mSensorManager = (SensorManager) context
					.getSystemService(Context.SENSOR_SERVICE);
			// 初始化加速度传感器
			accelerometer = mSensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			// 初始化地磁场传感器
			magnetic = mSensorManager
					.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			if (hasHW()) {
				// 开始监听
				mSensorManager.registerListener(this, accelerometer,
						Sensor.TYPE_ACCELEROMETER);
				mSensorManager.registerListener(this, magnetic,
						Sensor.TYPE_MAGNETIC_FIELD);
			}
		}
		this.listener = listener;
	}

	@Override
	public void stop() {
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this);
		}
	}

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

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if (listener != null) {
			listener.onMsg("accuracy:" + accuracy);
		}
	}

	public boolean hasHW() {
		boolean has1 = false, has2 = false;
		List<Sensor> deviceSensors = mSensorManager
				.getSensorList(Sensor.TYPE_ALL);
		for (Sensor sensor : deviceSensors) {
			if (sensor.getType() == Sensor.TYPE_ACCELEROMETER)
				has1 = true;
			else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
				has2 = true;
			Log.i("SL", sensor.getName());
		}
		return has1 && has2;
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
		if (listener != null)
			listener.onDegree(values[0]);
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
	}
}
