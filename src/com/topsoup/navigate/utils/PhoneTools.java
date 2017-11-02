package com.topsoup.navigate.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class PhoneTools {
	public static void d(Context context, String number) {
		Intent intent = new Intent(Intent.ACTION_CALL);
		Uri data = Uri.parse("tel:" + number);
		intent.setData(data);
		context.startActivity(intent);
	}
}
