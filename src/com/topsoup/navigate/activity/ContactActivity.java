package com.topsoup.navigate.activity;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.topsoup.navigate.R;
import com.topsoup.navigate.base.BaseActivity;
import com.topsoup.navigate.model.Contact;
import com.topsoup.navigate.utils.SLUtils;

@ContentView(R.layout.activity_contact)
public class ContactActivity extends BaseActivity {
	@ViewInject(R.id.et_name)
	private EditText etPhoneName;
	@ViewInject(R.id.et_phone)
	private EditText etPhoneNumber;

	private String name, phone;
	private Contact contact;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getIntent().hasExtra("contact")) {
			contact = (Contact) getIntent().getSerializableExtra("contact");
			etPhoneName.setText(contact.getName());
			etPhoneNumber.setText(contact.getPhoneNumber());
		}
		showTitle("编辑联系人");
	}

	@Override
	public String[] buildOptionsMenu() {
		return new String[] { "选取联系人", "保存", "取消" };
	}

	@Override
	public void onOptionMenuSelect(DialogInterface dialog, int which) {
		switch (which) {
		case 0:
			startActivityForResult(new Intent(Intent.ACTION_PICK,
					ContactsContract.Contacts.CONTENT_URI), 1);
			break;
		case 1:
			name = etPhoneName.getText().toString().trim();
			phone = etPhoneNumber.getText().toString().trim();
			if (!TextUtils.isEmpty(phone)) {
				if (SLUtils.isNumeric(phone)) {
					Contact contact = new Contact(name, phone);
					setResult(RESULT_OK,
							new Intent().putExtra("contact", contact));
					finish();
				} else {
					showToast("无效号码");
				}
			} else {
				showToast("电话号码不能为空");
			}
			break;
		case 2:
			setResult(RESULT_CANCELED);
			finish();
			break;
		default:
			break;
		}
	}

	@Event({ R.id.left, R.id.right, R.id.center })
	private void onToolBarClick(View v) {
		switch (v.getId()) {
		case R.id.left:
			showOptions();
			break;
		case R.id.center:

			break;
		case R.id.right:
			setResult(RESULT_CANCELED);
			finish();
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
				phone = phone.replace(" ", "").replace("+86", "");
				c.close();
				etPhoneName.setText(name);
				etPhoneNumber.setText(phone);
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
