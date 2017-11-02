package com.topsoup.navigate.service;

import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.topsoup.navigate.AppConfig;
import com.topsoup.navigate.R;
import com.topsoup.navigate.event.SOSEvent;
import com.topsoup.navigate.model.Contact;
import com.topsoup.navigate.model.SOS;
import com.topsoup.navigate.worker.PhoneWorker;

public class MyService extends Service implements IGPSListener, ISOSListener,
		IComPassListener, ILOG {
	private static final String TAG = "SL-MyService";
	private IGPS gps;
	private ISOS sos;
	private IComPass compass;
	private Handler uiHandler;
	private AppConfig app;
	private Location last;
	private PhoneWorker phone;

	public class MyBinder extends Binder {
		public MyService getService() {
			return MyService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		app = (AppConfig) getApplication();
		phone = PhoneWorker.instance().start(app);
		startForeground(R.id.info, new Notification());
		if (!EventBus.getDefault().isRegistered(this))
			EventBus.getDefault().register(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopForeground(true);
		if (phone != null)
			phone.stop();
		if (sos != null)
			sos.stop();
		if (gps != null)
			gps.stop();
		if (EventBus.getDefault().isRegistered(this))
			EventBus.getDefault().unregister(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new MyBinder();
	}

	public MyService setGPS(IGPS newGps) {
		this.gps = newGps;
		if (gps != null) {
			gps.setListener(this);
			// gps.start(this, 60000);
			if (uiHandler != null) {
				uiHandler.sendMessage(uiHandler.obtainMessage(0, "配置GPS了"));
			}
		}
		return this;
	}

	public MyService setSOS(ISOS newSOS) {
		sos = newSOS;
		if (sos != null) {
			sos.setListener(this);
			sos.start(this);
			if (uiHandler != null) {
				uiHandler.sendMessage(uiHandler.obtainMessage(0, "配置SOS了"));
			}
		}
		return this;
	}

	public MyService setComPass(IComPass newCompass) {
		compass = newCompass;
		if (compass != null) {
			compass.start(this, this);
			if (uiHandler != null) {
				uiHandler.sendMessage(uiHandler.obtainMessage(0, "配置ComPass了"));
			}
		}
		return this;
	}

	public MyService setHandler(Handler handler) {
		uiHandler = handler;
		uiHandler.sendMessage(uiHandler.obtainMessage(0, "设置成功"));
		return this;
	}

	@Override
	public void onSOS(Location location, String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatus(int status) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocate(Location last) {
		this.last = last;
		if (sosing) {
			onSOS(SOSEvent.START);
		}
		if (uiHandler != null) {
			uiHandler.sendMessage(uiHandler.obtainMessage(0,
					"lat:" + last.getLatitude() + " lon:" + last.getLongitude()
							+ "/" + last.getSpeed() + "/" + last.getBearing()));
		}
	}

	@Override
	public void onSatelliteCount(int count) {
		if (uiHandler != null) {
			uiHandler.sendMessage(uiHandler.obtainMessage(0, "卫星个数：" + count));
		}
	}

	@Override
	public void onMsg(String msg) {
		if (uiHandler != null)
			uiHandler.sendMessage(uiHandler.obtainMessage(0, msg));
	}

	@Override
	public void onDegree(float degree) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logi(String msg) {
		Log.e(TAG, msg);
	}

	@Override
	public void logd(String msg) {
		Log.e(TAG, msg);
	}

	@Override
	public void loge(String msg) {
		Log.e(TAG, msg);
	}

	private boolean sosing = false;

	@Subscribe()
	public void onSOS(SOSEvent event) {
		if (event == SOSEvent.START) {
			List<Contact> contacts = app.getDbWorker().getContactList();
			if (contacts != null && contacts.size() > 0) {
				if (last != null) {
					int count = 0;
					for (Contact contact : contacts) {
						SOS.sendSOS(contact.getPhoneNumber(),
								last.getLatitude(), last.getLongitude());
						count++;
					}
					sosing = false;
					Log.i("SL", "发送了" + count + "条短信SOS，包含位置信息，不用发送了");
				} else {
					int count = 0;
					for (Contact contact : contacts) {
						SOS.sendSOS(contact.getPhoneNumber(), 0, 0);
						count++;
					}
					sosing = true;
					Log.i("SL", "发送了" + count + "条短信SOS，不包含位置信息");
				}
				List<String> numbers = new ArrayList<String>();
				for (Contact contact : contacts) {
					if (!TextUtils.isEmpty(contact.getPhoneNumber()))
						numbers.add(contact.getPhoneNumber());
				}
				phone.startSOS(numbers);
			}
		} else {
			Log.i("SL", "结束SOS");
			sosing = false;
			phone.stopSOS();
		}
	}
}
