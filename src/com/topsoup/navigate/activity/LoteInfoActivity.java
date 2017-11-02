package com.topsoup.navigate.activity;

import java.util.Date;

import org.xutils.ex.DbException;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.topsoup.navigate.R;
import com.topsoup.navigate.base.BaseActivity;
import com.topsoup.navigate.model.MyLocation;
import com.topsoup.navigate.model.SOS;
import com.topsoup.navigate.utils.SLUtils;

@ContentView(R.layout.activity_locateinfo)
public class LoteInfoActivity extends BaseActivity {

	public static final void start(BaseActivity activity, SOS sos,
			boolean editEnable) {
		activity.startActivity(new Intent(activity, LoteInfoActivity.class)
				.putExtra("sos", sos).putExtra("editenable", editEnable));
	}

	public static final void start(BaseActivity activity, MyLocation locate,
			boolean editEnable) {
		activity.startActivity(new Intent(activity, LoteInfoActivity.class)
				.putExtra("locate", locate).putExtra("editenable", editEnable));
	}

	private boolean editEnable = false;

	@ViewInject(R.id.et_name)
	private EditText etName;

	@ViewInject(R.id.et_lat)
	private EditText etLat;
	@ViewInject(R.id.et_lon)
	private EditText etLon;

	@ViewInject(R.id.et_time)
	private EditText etTime;

	private MyLocation locate;
	private SOS sos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);
		Intent intent = getIntent();
		if (intent.hasExtra("locate")) {
			locate = (MyLocation) intent.getSerializableExtra("locate");
			if (locate != null && !TextUtils.isEmpty(locate.name)) {
				etName.setText(locate.name);
				etLat.setText(locate.lat + "");
				etLon.setText(locate.lon + "");
				etTime.setText(SLUtils.format(locate.createTime));
			} else {
				etTime.setText(SLUtils.format(new Date()));
			}
		} else if (intent.hasExtra("sos")) {
			sos = (SOS) intent.getSerializableExtra("sos");
			etName.setText(sos.getUser());
			etLat.setText(sos.lat + "");
			etLon.setText(sos.lon + "");
			etTime.setText(SLUtils.format(sos.createTime));
		} else {
			showToast("无效参数");
			finish();
		}
		editEnable = intent.getBooleanExtra("editenable", editEnable);
		etName.setEnabled(editEnable);
		etLat.setEnabled(editEnable);
		etLon.setEnabled(editEnable);
		etTime.setEnabled(false);
		etTime.setInputType(InputType.TYPE_NULL);
		showTitle(editEnable ? "编辑位置信息" : "位置详情");
		if (!editEnable) {
			etName.setInputType(InputType.TYPE_NULL);
			etLat.setInputType(InputType.TYPE_NULL);
			etLon.setInputType(InputType.TYPE_NULL);
		} else {
			etName.setInputType(InputType.TYPE_CLASS_TEXT);
			etLat.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
			etLon.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
		}
	}

	@Override
	public String[] buildOptionsMenu() {
		if (editEnable)
			return new String[] { "领航", "保存", "取消" };
		else
			return new String[] { "领航", "删除", "取消" };
	}

	@Override
	public void onOptionMenuSelect(DialogInterface dialog, int which) {
		switch (which) {
		case 1:
			if (editEnable) {
				save();
			} else {
				if (locate != null) {
					if (app.getDbWorker().removeMyLocateById(locate.id)) {
						showToast("删除成功");
						finish();
					}
				} else if (sos != null) {
					if (app.getDbWorker().removeSOSById(sos.id)) {
						showToast("删除成功");
						finish();
					}
				}
			}
			break;
		case 0:
			if (locate != null) {
				NavigateActivity.start(this, locate);
			} else if (sos != null) {
				NavigateActivity.start(this, sos);
			}
			finish();
			break;
		case 2:
			onBackPressed();
			break;
		default:
			break;
		}
	}

	private void save() {
		String name = etName.getText().toString();
		String lat = etLat.getText().toString();
		String lon = etLon.getText().toString();
		if (TextUtils.isEmpty(name))
			showToast("名称不能为空");
		else if (TextUtils.isEmpty(lat) || TextUtils.isEmpty(lon))
			showToast("经纬度信息不能为空");
		else {
			if (locate == null) {
				locate = new MyLocation();
				locate.createTime = System.currentTimeMillis();
			}
			locate.name = name;
			locate.lat = Double.parseDouble(lat);
			locate.lon = Double.parseDouble(lon);
			try {
				app.getDbWorker().addHistory(locate);
				showToast("保存成功");
				setResult(RESULT_OK);
				finish();
			} catch (DbException e) {
				e.printStackTrace();
			}
		}
	}

	@Event({ R.id.left, R.id.center, R.id.right })
	private void onToolBarClick(View v) {
		switch (v.getId()) {
		case R.id.left:
			showOptions();
			break;
		case R.id.center:
			if (editEnable)
				showToast("保存");
			onBackPressed();
			break;
		case R.id.right:
			if (editEnable)
				showToast("未保存");
			onBackPressed();
			break;
		default:
			break;
		}
	}
}
