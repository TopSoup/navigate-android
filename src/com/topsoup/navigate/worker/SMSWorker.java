package com.topsoup.navigate.worker;

import java.text.ParseException;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.telephony.SmsMessage;
import android.util.Log;

import com.topsoup.navigate.model.SOS;
import com.topsoup.navigate.service.ISOS;
import com.topsoup.navigate.service.ISOSListener;
import com.topsoup.navigate.task.SMSTask;
import com.topsoup.navigate.utils.SMSReader;

public class SMSWorker extends BroadcastReceiver implements ISOS {
	public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
	public static final String ACTION_TEST = "com.topsoup.navigate.sms";
	private ISOSListener listener;
	private Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (ACTION.equals(intent.getAction())) {
			Object[] pdus = (Object[]) intent.getExtras().get("pdus");
			for (Object pdu : pdus) {
				@SuppressWarnings("deprecation")
				SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
				String sender = smsMessage.getDisplayOriginatingAddress();
				// 短信内容
				String content = smsMessage.getDisplayMessageBody();
				// long date = smsMessage.getTimestampMillis();
				// Date tiemDate = new Date(date);
				try {
					SOS sos = SOS.parseSMS(sender, content);
					if (sos != null)
						EventBus.getDefault().post(sos);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

			if (listener != null)
				listener.onSOS(null, null);
		} else if (ACTION_TEST.equals(intent.getAction())) {
			String sender = intent.getStringExtra("sender");
			// 短信内容
			String content = intent.getStringExtra("sms");
			System.out.println("模拟短信sender:" + sender);
			System.out.println("模拟短信sms:" + content);
			try {
				if (SMSTask.isTrue(content)) {
					SMSTask task = SMSTask.paste(content);
					if (task != null) {// 正确的任务
						EventBus.getDefault().postSticky(task);
					}
				} else {
					SOS sos = SOS.parseSMS(sender, content);
					if (sos != null)
						EventBus.getDefault().post(sos);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setListener(ISOSListener listener) {
		this.listener = listener;
	}

	@Override
	public SMSWorker start(Context context) {
		if (mContext == null) {
			mContext = context.getApplicationContext();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(ACTION);
			intentFilter.addAction(ACTION_TEST);
			intentFilter.setPriority(Integer.MAX_VALUE);
			mContext.registerReceiver(this, intentFilter);
		}
		if (!EventBus.getDefault().isRegistered(this))
			EventBus.getDefault().register(this);
		return this;
	}

	@Override
	public void stop() {
		if (mContext != null) {
			mContext.unregisterReceiver(this);
			mContext = null;
		}
		if (EventBus.getDefault().isRegistered(this))
			EventBus.getDefault().unregister(this);
	}

	@Subscribe
	public void onLocate(Location last) {
		if (sending) {
			sendSOS(targetPhoneNum);
		}
	}

	private boolean sending = false;
	private String targetPhoneNum;

	public void sendSOS(String target) {
		Location location = EventBus.getDefault()
				.getStickyEvent(Location.class);
		if (location != null) {
			Log.i("SL", location.getProvider());
			// SOS.sendSOS(target, location.getLatitude(),
			// location.getLongitude());
			sending = false;
		} else {
			// SOS.sendSOS(target, 0, 0);
			sending = true;
		}
	}

	@Override
	public Location getLastLocate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SOS> getList() {
		return SMSReader.getSmsInPhone(mContext);
	}
}
