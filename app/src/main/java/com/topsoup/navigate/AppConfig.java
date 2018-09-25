package com.topsoup.navigate;

import org.greenrobot.eventbus.EventBus;
import org.xutils.x;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.topsoup.navigate.service.MyService;
import com.topsoup.navigate.worker.DBWorker;
import com.topsoup.navigate.worker.GPSWorker;
import com.topsoup.navigate.worker.SMSWorker;

public class AppConfig extends Application {
	public static final int GPS_minTime = 1000;
	public static final int GPS_minDistance = 5;
	public static final boolean SOS_KEY_LISTEN = true;

	private GPSWorker gpsWorker;
	private SMSWorker smsWorker;
	private DBWorker dbWorker;
	private SharedPreferences mPreferences;
	private Editor mEditor;

	@Override
	public void onCreate() {
		super.onCreate();
		mPreferences = getSharedPreferences(getString(R.string.app_name),
				Context.MODE_PRIVATE);
		mEditor = mPreferences.edit();
		x.Ext.init(this);
		x.Ext.setDebug(false);
		EventBus.clearCaches();
		gpsWorker = new GPSWorker();
		smsWorker = new SMSWorker();
		dbWorker = DBWorker.instance();
		DBWorker.instance().init(this);
		startService(new Intent(this, MyService.class));
	}

	public GPSWorker getGpsWorker() {
		return gpsWorker;
	}

	public SMSWorker getSmsWorker() {
		return smsWorker;
	}

	public DBWorker getDbWorker() {
		return dbWorker;
	}

	public SharedPreferences config() {
		return mPreferences;
	}

	public Editor edit() {
		return mEditor;
	}

}
