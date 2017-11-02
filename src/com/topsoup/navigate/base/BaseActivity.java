package com.topsoup.navigate.base;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.x;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.topsoup.navigate.AppConfig;
import com.topsoup.navigate.R;
import com.topsoup.navigate.activity.NavigateActivity;
import com.topsoup.navigate.activity.SOSActivity;
import com.topsoup.navigate.model.SOS;
import com.topsoup.navigate.utils.SLUtils;

@ContentView(R.layout.activity_navigatelist)
public abstract class BaseActivity extends Activity {
	protected AppConfig app;
	private Toast mToast;
	private boolean hasOptions = false;
	@ViewInject(R.id.title)
	private TextView title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (AppConfig) x.app();
		x.view().inject(this);
		hasOptions = buildOptionsMenu() != null;
		if (findViewById(R.id.center) != null)
			findViewById(R.id.center).setVisibility(View.GONE);
		if (!hasOptions) {
			findViewById(R.id.left).setVisibility(View.GONE);
			findViewById(R.id.right).setOnClickListener(
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							onBackPressed();
						}
					});
		}
		registerEvent(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unRegisterEvent(this);
	}

	public void showTitle(String title) {
		showTitle(title, Gravity.CENTER);
	}

	public void showTitle(String title, int gravity) {
		this.title.setGravity(gravity);
		this.title.setText(title);
		this.title.setVisibility(View.VISIBLE);
	}

	public void hideTitle() {
		this.title.setVisibility(View.GONE);
	}

	protected void registerEvent(BaseActivity fragment) {
		if (!EventBus.getDefault().isRegistered(fragment))
			EventBus.getDefault().register(fragment);
	}

	protected void unRegisterEvent(BaseActivity fragment) {
		if (EventBus.getDefault().isRegistered(fragment))
			EventBus.getDefault().unregister(fragment);
	}

	protected void showToast(String msg) {
		if (mToast == null)
			mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		else
			mToast.setText(msg);
		mToast.show();
	}

	public void showOptions() {
		if (hasOptions)
			new AlertDialog.Builder(this)
					.setItems(buildOptionsMenu(), new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							onOptionMenuSelect(dialog, which);
						}
					}).create().show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			showOptions();
			break;
		case 137:
			if (event.getRepeatCount() == 50)
				startActivity(new Intent(this, SOSActivity.class));
			break;
		case KeyEvent.KEYCODE_BACK:
			if (delWord())
				return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	public abstract String[] buildOptionsMenu();

	public abstract void onOptionMenuSelect(DialogInterface dialog, int which);

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSos(final SOS sos) {
		new AlertDialog.Builder(this)
				.setMessage(
						sos.getUser() + "发起SOS,时候领航?\n"
								+ SLUtils.format(sos.getCreateTime()))
				.setNegativeButton("取消", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setPositiveButton("发起", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						NavigateActivity.start(BaseActivity.this, sos);
					}
				}).create().show();
	}

	protected boolean delWord() {
		View view = getCurrentFocus();
		if (view instanceof EditText) {
			EditText editText = (EditText) view;
			if (editText.isEnabled()) {
				String old = editText.getText().toString();
				if (old.length() > 0) {
					editText.setText(old.substring(0, old.length() - 1));
					editText.setSelection(old.length() - 1);
					return true;
				}
			}
		}
		return false;
	}
}
