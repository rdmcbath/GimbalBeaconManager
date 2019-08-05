package com.mcbath.rebecca.gimbalbeacondemo;

import android.app.Application;
import android.util.Log;

import com.gimbal.android.Gimbal;

/**
 * Created by Rebecca McBath
 * on 2019-08-01.
 */
public class ApplicationActivity extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("-----", "ApplicationActivity created");

		GimbalIntegration.init(this).onCreate();
		Gimbal.start();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		GimbalIntegration.init(this).onTerminate();
		Gimbal.stop();
	}
}
