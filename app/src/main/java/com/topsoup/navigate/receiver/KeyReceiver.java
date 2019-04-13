package com.topsoup.navigate.receiver;

import com.topsoup.navigate.AppConfig;
import com.topsoup.navigate.activity.SOSActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class KeyReceiver extends BroadcastReceiver {
	private Toast mToast;

//	protected void showToast(Context context, String msg) {
//		if (mToast == null)
//			mToast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
//		else
//			mToast.setText(msg);
//		mToast.show();
//	}

//	android.intent.action.PTT.up
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
//		showToast(context,"event:"+action);

		Log.i("NKEY", "event:"+action);
		AppConfig app = (AppConfig) context.getApplicationContext();
		if (app.config().getBoolean("soskey", AppConfig.SOS_KEY_LISTEN)) {
			if (action.equals("android.action.sos")) {// 段按
				int sos_action = intent.getIntExtra("", 1);// 0:down ,1:up。默认抬起
				if (sos_action == 0) {
					// 按下
				} else {
					// 抬起
				}
			} else if (action.equals("android.com.sos")) {// 长按
				context.startActivity(new Intent(context, SOSActivity.class)
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			}
		}
	}
}
