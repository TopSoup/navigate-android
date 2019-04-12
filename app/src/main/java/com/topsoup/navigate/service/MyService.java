package com.topsoup.navigate.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.topsoup.navigate.AppConfig;
import com.topsoup.navigate.R;
import com.topsoup.navigate.event.SOSEvent;
import com.topsoup.navigate.model.Contact;
import com.topsoup.navigate.model.SOS;
import com.topsoup.navigate.task.SMSTask;
import com.topsoup.navigate.utils.PowerUtils;
import com.topsoup.navigate.worker.PhoneWorker;

public class MyService extends Service implements IGPSListener, ISOSListener,
		IComPassListener, ILOG {
	private static final String TAG = "SL-MyService";
	private IGPS gps;
	private ISOS sos;
	private AppConfig app;
	private Location last;
	private PhoneWorker phone;
	private HashMap<String, SMSTask> runningTask = new HashMap<String, SMSTask>();
	private PowerUtils cpuLock;

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
		sos = app.getSmsWorker();
		sos.setListener(this);
		sos.start(this);
		gps = app.getGpsWorker();
		gps.setListener(this);
		cpuLock = PowerUtils.makeCpuLock(this, "service_cpu");
		// gps.start(this, 0, "service");
		//startForeground(R.id.info, new Notification());
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
			gps.stop("service");
		if (EventBus.getDefault().isRegistered(this))
			EventBus.getDefault().unregister(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new MyBinder();
	}

	// public MyService setGPS(IGPS newGps) {
	// this.gps = newGps;
	// if (gps != null) {
	// gps.setListener(this);
	// // gps.start(this, 60000);
	// if (uiHandler != null) {
	// uiHandler.sendMessage(uiHandler.obtainMessage(0, "配置GPS了"));
	// }
	// }
	// return this;
	// }
	//
	// public MyService setSOS(ISOS newSOS) {
	// sos = newSOS;
	// if (sos != null) {
	// sos.setListener(this);
	// sos.start(this);
	// if (uiHandler != null) {
	// uiHandler.sendMessage(uiHandler.obtainMessage(0, "配置SOS了"));
	// }
	// }
	// return this;
	// }
	//
	// public MyService setComPass(IComPass newCompass) {
	// compass = newCompass;
	// if (compass != null) {
	// compass.start(this, this);
	// if (uiHandler != null) {
	// uiHandler.sendMessage(uiHandler.obtainMessage(0, "配置ComPass了"));
	// }
	// }
	// return this;
	// }

	// public MyService setHandler(Handler handler) {
	// uiHandler = handler;
	// uiHandler.sendMessage(uiHandler.obtainMessage(0, "设置成功"));
	// return this;
	// }

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
		if (runningTask.size() > 0) {
			synchronized (MyService.class) {
				for (SMSTask task : runningTask.values()) {
					task.report(last);
				}
				runningTask.clear();
				gps.stop("service_bytask_1");
			}
		}
		if (sosing) {
			onSOS(SOSEvent.START);
		}
	}

	@Override
	public void onSatelliteCount(int count) {
	}

	@Override
	public void onMsg(String msg) {
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

	@Subscribe
	public void onTask(SMSTask task) {
		switch (Integer.valueOf(task.getFIXTYPE_6())) {
		case 0:// 0-取消单次定位或跟踪定位;
			break;
		case 1:// 单次定位
			synchronized (MyService.class) {
				if (!runningTask.containsKey(task.getAPPID_4())) {
					runningTask.put(task.getAPPID_4(), task);
					gps.start(this, 0, "service_bytask_1");
					cpuLock.acquire();
				}
			}
			break;
		case 2:// 跟踪定位

			break;
		case 3:// 启动定位;
			break;
		case 4:// 取消启动跟踪定位
			break;
		case 5:// 取消启动单次定位
			break;
		}
	}

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
