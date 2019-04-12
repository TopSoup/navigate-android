package com.topsoup.navigate.activity;

import java.util.List;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.topsoup.navigate.AppConfig;
import com.topsoup.navigate.R;
import com.topsoup.navigate.base.BaseActivity;
import com.topsoup.navigate.model.Contact;

@ContentView(R.layout.activity_settings)
public class SettingsActivity extends BaseActivity {

	public static final void start(BaseActivity activity) {
		activity.startActivity(new Intent(activity, SettingsActivity.class));
	}

	@ViewInject(R.id.contact1)
	private Button btn1;
	@ViewInject(R.id.contact2)
	private Button btn2;
	@ViewInject(R.id.contact3)
	private Button btn3;
	@ViewInject(R.id.swipe)
	private SwipeRefreshLayout swipeRefreshLayout;

	//@ViewInject(R.id.cb_sos)
	//private CheckBox checkBox;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		// 设置监听
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				loadData();
			}
		});
		loadData();
		showTitle("紧急联系人管理");
	}

	@Override
	public String[] buildOptionsMenu() {
		return new String[] { "说明", "取消" };
	}

	@Override
	public void onOptionMenuSelect(DialogInterface dialog, int which) {
		switch (which) {
		case 0:
			new AlertDialog.Builder(this)
					.setMessage(
							"【紧急联系人】\n1.可设置三个紧急联系人，SOS过程中会向设置的三个联系发送求救短信。\n2.点击进入设置界面进行设置。\n3.长按可删除选中联系人。")
					.create().show();
			break;
		case 1:
			onBackPressed();
			break;
		default:
			break;
		}
	}

	@Event({ R.id.left, R.id.center, R.id.right })
	private void myinfo(View v) {
		switch (v.getId()) {
		case R.id.left:
			showOptions();
			break;
		case R.id.center:
			// TODO
			break;
		case R.id.right:
			onBackPressed();
			break;
		default:
			break;
		}
	}

	private void loadData() {
		swipeRefreshLayout.setRefreshing(true);
		List<Contact> list = app.getDbWorker().getContactList();
		btn1.setText("联系人1\n(空)");
		btn1.setTag(null);
		btn2.setText("联系人2\n(空)");
		btn2.setTag(null);
		btn3.setText("联系人3\n(空)");
		btn3.setTag(null);
//		checkBox.setChecked(app.config().getBoolean("soskey",
//				AppConfig.SOS_KEY_LISTEN));
//		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//			@Override
//			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
//				app.edit().putBoolean("soskey", arg1).commit();
//			}
//		});
		if (list != null && list.size() > 0) {
			for (Contact contact : list)
				if (contact.index == 1) {
					showContact(btn1, contact);
				} else if (contact.index == 2) {
					showContact(btn2, contact);
				} else if (contact.index == 3) {
					showContact(btn3, contact);
				}
		} else {
			btn1.setText("联系人1\n(空)");
			btn2.setText("联系人2\n(空)");
			btn3.setText("联系人3\n(空)");
		}
		swipeRefreshLayout.setRefreshing(false);
	}

	private void showContact(Button btn, Contact contact) {
		if (contact != null)
			btn.setText(contact.getName() + "\n" + contact.getPhoneNumber());
		btn.setTag(contact);
	}

	@Event(type = View.OnLongClickListener.class, value = { R.id.contact1,
			R.id.contact2, R.id.contact3 })
	private boolean onLongClick(View v) {
		Object obj = v.getTag();
		if (obj != null && obj instanceof Contact)
			showAskDialog((Contact) obj);
		return true;
	}

	private void showAskDialog(final Contact contact) {
		if (contact != null)
			new AlertDialog.Builder(this)
					.setMessage(
							String.format("要删除联系人“%s”吗？", contact.getName()))
					.setNegativeButton("取消", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).setPositiveButton("删除", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (app.getDbWorker().remove(contact)) {
								showToast(String.format("联系人“%s”删除成功。",
										contact.getName()));
								loadData();
							}
						}
					}).create().show();
	}

	@Event({ R.id.contact1, R.id.contact2, R.id.contact3 })
	private void onBtnClick(View v) {
		Object obj = v.getTag();
		if (obj != null && obj instanceof Contact)
			startActivityForResult(
					new Intent(this, ContactActivity.class).putExtra("contact",
							(Contact) obj), v.getId());
		else
			startActivityForResult(new Intent(this, ContactActivity.class),
					v.getId());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Contact contact = (Contact) data.getSerializableExtra("contact");
			switch (requestCode) {
			case R.id.contact1:
				contact.setIndex(1);
				if (app.getDbWorker().addContact(contact)) {
					showToast("保存成功");
					showContact(btn1, contact);
				}
				break;
			case R.id.contact2:
				contact.setIndex(2);
				if (app.getDbWorker().addContact(contact)) {
					showToast("保存成功");
					showContact(btn2, contact);
				}
				break;
			case R.id.contact3:
				contact.setIndex(3);
				if (app.getDbWorker().addContact(contact)) {
					showToast("保存成功");
					showContact(btn3, contact);
				}
				break;
			default:
				break;
			}
		}
	}
}
