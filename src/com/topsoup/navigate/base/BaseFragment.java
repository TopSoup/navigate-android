package com.topsoup.navigate.base;

import org.greenrobot.eventbus.EventBus;
import org.xutils.x;

import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.topsoup.navigate.AppConfig;

public abstract class BaseFragment extends Fragment {
	private boolean injected = false;
	protected AppConfig app;
	private Toast mToast;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		app = (AppConfig) x.app();
		injected = true;
		return x.view().inject(this, inflater, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (!injected) {
			x.view().inject(this, this.getView());
		}
	}

	protected void registerEvent(BaseFragment fragment) {
		if (!EventBus.getDefault().isRegistered(fragment))
			EventBus.getDefault().register(fragment);
	}

	protected void unRegisterEvent(BaseFragment fragment) {
		if (EventBus.getDefault().isRegistered(fragment))
			EventBus.getDefault().unregister(fragment);
	}

	protected void showToast(String msg) {
		if (mToast == null)
			mToast = Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG);
		else
			mToast.setText(msg);
		mToast.show();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}
}
