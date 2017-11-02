package com.topsoup.navigate.service;

import android.content.Context;

public interface IComPass {
	public void start(Context context, IComPassListener listener);

	public void stop();
}
