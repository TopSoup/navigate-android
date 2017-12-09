package com.topsoup.navigate.activity;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.topsoup.navigate.R;
import com.topsoup.navigate.base.BaseActivity;
import com.topsoup.navigate.utils.PhoneReader;
import com.topsoup.navigate.worker.ComPassWorker;
import com.topsoup.navigate.worker.DBWorker;
import com.topsoup.navigate.worker.PhoneWorker;

@ContentView(R.layout.activity_navigatemain)
public class NavigateListMainActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DBWorker.instance().init(this);
		// startService(new Intent(this, MyService.class));
		ComPassWorker comPassWorker = new ComPassWorker();
		comPassWorker.start(this, null);
		PhoneWorker.instance().start(this);
		PhoneReader.printInfo(this);
	}

	@Event({ R.id.history_help, R.id.history_my, R.id.history_sos })
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
					.setMessage(
							"【领航说明】\n[目的地列表]为手动保存列表，未保存数据时该列表显示为空。\n[短信目的列表]会自动筛选收件箱内求助信息，如果无求助短信该列表为空。")
					.create().show();
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
