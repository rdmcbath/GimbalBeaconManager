package com.mcbath.rebecca.gimbalbeacondemo;

import android.app.Application;
import android.util.Log;

/**
 * Created by Rebecca McBath
 * on 2019-08-01.
 */
public class ApplicationActivity extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("-----", "Application created");

		GimbalIntegration.init(this).onCreate();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		GimbalIntegration.init(this).onTerminate();
	}
}
