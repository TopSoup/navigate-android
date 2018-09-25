package com.topsoup.navigate.activity;

import org.xutils.x;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.topsoup.navigate.R;

@ContentView(R.layout.activity_authorize)
public class AuthorizeActivity extends Activity {
	@ViewInject(R.id.message)
	private TextView message;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		x.view().inject(this);
		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		message.setTextColor(Color.RED);
	}

}
