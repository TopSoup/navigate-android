package com.topsoup.navigate.receiver;

import com.topsoup.navigate.service.MyService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Boot extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {// 开机启动服务
			context.startService(new Intent(context, MyService.class));
		}
	}

}
