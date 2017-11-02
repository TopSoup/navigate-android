package com.topsoup.navigate.activity;

import java.util.List;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.widget.Button;

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

	private Contact contact1, contact2, contact3;

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
							"【紧急联系人】\n可设置三个紧急联系人，SOS过程中会向设置的三个联系发送求救短信。\n点击进入设置界面进行设置。")
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

		if (list != null && list.size() > 0) {
			for (Contact contact : list)
				if (contact.index == 1) {
					contact1 = contact;
					showContact(btn1, contact1);
				} else if (contact.index == 2) {
					contact2 = contact;
					showContact(btn2, contact2);
				} else if (contact.index == 3) {
					contact3 = contact;
					showContact(btn3, contact3);
				}
		} else {
			btn1.setText("联系人1\n(空)");
			btn2.setText("联系人2\n(空)");
			btn3.setText("联系人3\n(空)");
		}
		swipeRefreshLayout.setRefreshing(false);
	}

	private void showContact(Button btn, Contact contact) {
		btn.setText(contact.getName() + "\n" + contact.getPhoneNumber());
	}

	@Event({ R.id.contact1, R.id.contact2, R.id.contact3 })
	private void onBtnClick(View v) {
		switch (v.getId()) {
		case R.id.contact1:
			if (contact1 != null)
				startActivityForResult(
						new Intent(this, ContactActivity.class).putExtra(
								"contact", contact1), 1);
			else
				startActivityForResult(new Intent(this, ContactActivity.class),
						1);
			break;
		case R.id.contact2:
			if (contact2 != null)
				startActivityForResult(
						new Intent(this, ContactActivity.class).putExtra(
								"contact", contact2), 2);
			else
				startActivityForResult(new Intent(this, ContactActivity.class),
						2);
			break;
		case R.id.contact3:
			if (contact3 != null)
				startActivityForResult(
						new Intent(this, ContactActivity.class).putExtra(
								"contact", contact3), 3);
			else
				startActivityForResult(new Intent(this, ContactActivity.class),
						3);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK)
			switch (requestCode) {
			case 1:
				Contact contact1 = (Contact) data
						.getSerializableExtra("contact");
				contact1.setIndex(1);
				if (app.getDbWorker().addContact(contact1)) {
					showToast("保存成功");
					showContact(btn1, contact1);
				}
				break;
			case 2:
				Contact contact2 = (Contact) data
						.getSerializableExtra("contact");
				contact2.setIndex(2);
				app.getDbWorker().addContact(contact2);
				showContact(btn2, contact2);

				break;
			case 3:
				Contact contact3 = (Contact) data
						.getSerializableExtra("contact");
				contact3.setIndex(3);
				app.getDbWorker().addContact(contact3);
				showContact(btn3, contact3);
				break;
			default:
				break;
			}
	}
}
