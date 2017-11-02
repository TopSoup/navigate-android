package com.topsoup.navigate.activity;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.topsoup.navigate.R;
import com.topsoup.navigate.base.BaseActivity;
import com.topsoup.navigate.model.MyLocation;
import com.topsoup.navigate.model.SOS;

@ContentView(R.layout.activity_sendsms)
public class SendLocateActivity extends BaseActivity {

	public static final void start(BaseActivity activity, Location locate) {
		activity.startActivity(new Intent(activity, SendLocateActivity.class)
				.putExtra("location", locate));
	}

	public static final void start(BaseActivity activity, MyLocation locate) {
		activity.startActivity(new Intent(activity, SendLocateActivity.class)
				.putExtra("location", locate));
	}

	private String name, phone;

	@ViewInject(R.id.et_name)
	private EditText etName;

	@ViewInject(R.id.et_sms)
	private EditText etSMS;
	private Location location;
	private MyLocation myLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getIntent().hasExtra("location")) {
			location = (Location) getIntent().getParcelableExtra("location");
			etSMS.setText(SOS.buildSOS(location));
		} else if (getIntent().hasExtra("mylocate")) {
			myLocation = (MyLocation) getIntent().getSerializableExtra(
					"mylocate");
			etSMS.setText(SOS.buildSOS(myLocation));
		} else {
			showToast("参数错误");
			finish();
		}
		etName.setEnabled(false);
	}

	@Override
	public String[] buildOptionsMenu() {
		return new String[] { "发送", "选择联系人", "取消" };
	}

	@Override
	public void onOptionMenuSelect(DialogInterface dialog, int which) {
		switch (which) {
		case 0:
			String msg = etSMS.getText().toString();
			if (TextUtils.isEmpty(msg))
				showToast("消息内容不能为空");
			else if (TextUtils.isEmpty(phone))
				showToast("请选择联系人");
			else {
				SOS.sendSOS(phone, msg);
				showToast("正在发送");
				finish();
			}
			break;
		case 1:
			startActivityForResult(new Intent(Intent.ACTION_PICK,
					ContactsContract.Contacts.CONTENT_URI), 1);
			break;
		case 2:
			onBackPressed();
			break;
		default:
			break;
		}
	}

	@Event({ R.id.left, R.id.center, R.id.right })
	private void onToolBarClick(View v) {
		switch (v.getId()) {
		case R.id.left:
			showOptions();
			break;
		case R.id.right:
			onBackPressed();
			break;
		default:
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			Uri uri = data.getData();
			if (uri != null) {
				Cursor c = getContentResolver().query(uri, null, null, null,
						null);
				c.moveToFirst();
				name = c.getString(c
						.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
				phone = getContactPhone(c);
				c.close();
				etName.setText(name + "(" + phone + ")");
			}
		}
	}

	// 获取联系人电话
	private String getContactPhone(Cursor cursor) {

		int phoneColumn = cursor
				.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
		int phoneNum = cursor.getInt(phoneColumn);
		String phoneResult = "";
		// System.out.print(phoneNum);
		if (phoneNum > 0) {
			// 获得联系人的ID号
			int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
			String contactId = cursor.getString(idColumn);
			// 获得联系人的电话号码的cursor;
			Cursor phones = getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
							+ contactId, null, null);
			// int phoneCount = phones.getCount();
			// allPhoneNum = new ArrayList<String>(phoneCount);
			if (phones.moveToFirst()) {
				// 遍历所有的电话号码
				for (; !phones.isAfterLast(); phones.moveToNext()) {
					int index = phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
					int typeindex = phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
					int phone_type = phones.getInt(typeindex);
					String phoneNumber = phones.getString(index);
					switch (phone_type) {
					case 2:
						phoneResult = phoneNumber;
						break;
					}
					// allPhoneNum.add(phoneNumber);
				}
				if (!phones.isClosed()) {
					phones.close();
				}
			}
		}
		return phoneResult;
	}
}
