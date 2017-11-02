package com.topsoup.navigate.worker;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

public class PhoneWorker implements Runnable {

	private static final String TAG = "SL-PhoneWorker";
	public static final String ACTION_ENDCALL = "com.topsoup.call.shutdown";
	public static final String ACTION_NOTIFY_CALL_SUCCESS = "com.topsoup.call.success";
	public static final String ACTION_NOTIFY_CALL_FAIL = "com.topsoup.call.fail";

	private static final PhoneWorker instance = new PhoneWorker();
	private boolean sosing = false, calling = false;

	private PhoneWorker() {
	}

	public static final PhoneWorker instance() {
		return instance;
	}

	private Context mContext;
	private PhoneReceiver mPhoneReceiver;
	private Handler mHandler = new Handler();
	private List<String> numbers;
	private String phoneNumber;

	public PhoneWorker start(Context context) {
		if (mContext == null) {
			this.mContext = context.getApplicationContext();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction("android.intent.action.PHONE_STATE");
			intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
			intentFilter.addAction(ACTION_NOTIFY_CALL_FAIL);
			intentFilter.addAction(ACTION_NOTIFY_CALL_SUCCESS);
			mContext.registerReceiver(mPhoneReceiver = new PhoneReceiver(),
					intentFilter);
		}
		return this;
	}

	public PhoneWorker stop() {
		if (mContext != null) {
			if (mPhoneReceiver != null)
				mContext.unregisterReceiver(mPhoneReceiver);
			mPhoneReceiver = null;
			mContext = null;
		}
		return this;
	}

	private long startTime;

	public PhoneWorker call(String number) {
		Intent intent = new Intent(Intent.ACTION_CALL);
		Uri data = Uri.parse("tel:" + number);
		intent.setData(data);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
		// mHandler.postDelayed(this, 5000);// 5秒检查一次
		Log.i("SL", "开始拨打电话：" + number);
		return this;
	}

	public void endCall() {
		mContext.sendBroadcast(new Intent(ACTION_ENDCALL));// 停止拨号
		try {
			Method method = Class.forName("android.os.ServiceManager")
					.getMethod("getService", String.class);
			IBinder binder = (IBinder) method.invoke(null,
					new Object[] { Context.TELEPHONY_SERVICE });
			ITelephony telephony = ITelephony.Stub.asInterface(binder);
			telephony.endCall();
		} catch (NoSuchMethodException e) {
			Log.d(TAG, "", e);
		} catch (ClassNotFoundException e) {
			Log.d(TAG, "", e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PhoneWorker startSOS(List<String> phoneNumbers) {
		this.numbers = phoneNumbers;
		mHandler.removeCallbacks(this);
		mHandler.postDelayed(this, 3000);
		sosing = call();
		return this;
	}

	public PhoneWorker startSOS(String... phoneNumbers) {
		this.numbers = Arrays.asList(phoneNumbers);
		mHandler.removeCallbacks(this);
		mHandler.postDelayed(this, 3000);
		sosing = call();
		return this;
	}

	private int index = 0;

	private boolean call() {
		if (numbers == null || numbers.size() <= 0)
			return false;
		if (index >= numbers.size())
			index = 0;
		phoneNumber = numbers.get(index++);
		call(phoneNumber);
		return true;
	}

	public PhoneWorker stopSOS() {
		sosing = false;
		mHandler.removeCallbacks(PhoneWorker.this);
		return this;
	}

	public class PhoneReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 如果是去电
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
				String phoneNumber = intent
						.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
				Log.d("SL1", "call OUT:" + phoneNumber);
			} else if (action
					.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				switch (tm.getCallState()) {
				case TelephonyManager.CALL_STATE_IDLE:
					Log.i("SL1", "CALL_STATE_IDLE");
					calling = false;
					// if (isCalling) {
					// mHandler.post(PhoneWorker.this);
					// }
					if (sosing) {// 正在呼叫被挂断了
						long useTime = System.currentTimeMillis() - startTime;
						Log.i("SL11", "呼叫用时:" + useTime);
						mHandler.removeCallbacks(PhoneWorker.this);
						mHandler.postAtTime(PhoneWorker.this, 2000);
					}
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					Log.i("SL1", "CALL_STATE_OFFHOOK");
					startTime = System.currentTimeMillis();
					calling = true;
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					Log.i("SL1", "CALL_STATE_RINGING");
					calling = true;
					break;
				}
			} else if (action.equals(ACTION_NOTIFY_CALL_FAIL)) {// 系统通知没接通，应该停止拨号后重播
				mContext.sendBroadcast(new Intent(ACTION_ENDCALL));// 停止拨号
				mHandler.removeCallbacks(PhoneWorker.this);
				mHandler.postDelayed(PhoneWorker.this, 2000);// 3秒以后继续拨号
			} else if (action.equals(ACTION_NOTIFY_CALL_SUCCESS)) {
				mHandler.removeCallbacks(PhoneWorker.this);
				sosing = false;// 拨通后结束此次电话呼救
			}
		}
	}

	@SuppressLint("NewApi")
	private boolean isInCall() {
		TelecomManager tm = (TelecomManager) mContext
				.getSystemService(Context.TELECOM_SERVICE);
		return tm.isInCall();
	}

	@Override
	public void run() {
		if (sosing) {
			if (!calling) {
				call();// 继续拨号
			}
		}
	}

}
