package com.topsoup.navigate.web;

import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import android.util.Log;

public class Repoart {

	public static void run(String url, String xml) {
		RequestParams params = new RequestParams(url);// "http://58.56.109.38:18851/"
		params.setBodyContent(xml);
		params.addHeader("content-length", String.valueOf(xml.length()));
		x.http().post(params, new CommonCallback<String>() {

			@Override
			public void onCancelled(CancelledException e) {
				Log.e("SL", "onCancelled", e);
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
				Log.e("SL", "onError>" + arg1 + "/" + arg0.getMessage(), arg0);
			}

			@Override
			public void onFinished() {
				Log.i("SL", "onFinished");
			}

			@Override
			public void onSuccess(String arg0) {
				Log.i("SL", "onSuccess>" + arg0);
			}

		});
	}
}
