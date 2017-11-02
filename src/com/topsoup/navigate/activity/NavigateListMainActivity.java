package com.topsoup.navigate.activity;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.topsoup.navigate.R;
import com.topsoup.navigate.base.BaseActivity;
import com.topsoup.navigate.service.MyService;
import com.topsoup.navigate.service.MyService.MyBinder;
import com.topsoup.navigate.utils.PhoneReader;
import com.topsoup.navigate.worker.ComPassWorker;
import com.topsoup.navigate.worker.DBWorker;
import com.topsoup.navigate.worker.PhoneWorker;

@ContentView(R.layout.activity_navigatemain)
public class NavigateListMainActivity extends BaseActivity implements
		ServiceConnection {
	private MyService service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DBWorker.instance().init(this);
		// startService(new Intent(this, MyService.class));
		bindService(new Intent(this, MyService.class), this,
				Context.BIND_AUTO_CREATE);
		ComPassWorker comPassWorker = new ComPassWorker();
		comPassWorker.start(this, null);
		PhoneWorker.instance().start(this);
		PhoneReader.printInfo(this);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.service = ((MyBinder) service).getService();
		this.service.setGPS(app.getGpsWorker());
		this.service.setSOS(app.getSmsWorker());
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {

	}

	@Event({ R.id.history_help, R.id.history_my, R.id.history_sos})
	private void onBtnClick(View v) {
		switch (v.getId()) {
		case R.id.history_my:
			NavigateListActivity.showHistoryList(NavigateListMainActivity.this);
			break;
		case R.id.history_sos:
			NavigateListActivity.showSOSList(NavigateListMainActivity.this);
			break;
		case R.id.history_help:
			new AlertDialog.Builder(NavigateListMainActivity.this)
					.setMessage("领航说明").create().show();
			break;
		default:
			break;
		}
	}

	@Override
	public String[] buildOptionsMenu() {
		return null;
	}

	@Override
	public void onOptionMenuSelect(DialogInterface dialog, int which) {
	}
}
