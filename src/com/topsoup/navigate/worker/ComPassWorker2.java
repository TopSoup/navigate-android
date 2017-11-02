package com.topsoup.navigate.worker;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.topsoup.navigate.model.Test;
import com.topsoup.navigate.service.IComPass;
import com.topsoup.navigate.service.IComPassListener;

public class ComPassWorker2 implements IComPass, SensorEventListener {
	private SensorManager mSensorManager;
	private IComPassListener listener;

	@Override
	public void start(Context context, IComPassListener listener) {
		if (mSensorManager == null) {
			mSensorManager = (SensorManager) context
					.getSystemService(Context.SENSOR_SERVICE);
			Sensor magneticSensor = mSensorManager
					.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			Sensor accelerometerSensor = mSensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mSensorManager.registerListener(this, magneticSensor,
					SensorManager.SENSOR_DELAY_GAME);
			mSensorManager.registerListener(this, accelerometerSensor,
					SensorManager.SENSOR_DELAY_GAME);
		}
		this.listener = listener;
	}

	@Override
	public void stop() {
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this);
		}
	}

	float[] accelerometerValues = new float[3];
	float[] magneticValues = new float[3];

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			accelerometerValues = event.values.clone();
		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			magneticValues = event.values.clone();
		}
		float[] R = new float[9];
		float[] values = new float[3];
		SensorManager.getRotationMatrix(R, null, accelerometerValues,
				magneticValues);
		SensorManager.getOrientation(R, values);
		float degree = -(float) Math.toDegrees(values[0]);// 旋转角度
		if (listener != null)
			listener.onDegree(degree);
		Test test = new Test();
		test.degree = degree;
		EventBus.getDefault().postSticky(test);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

}
