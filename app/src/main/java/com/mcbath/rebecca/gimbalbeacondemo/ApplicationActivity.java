package com.mcbath.rebecca.gimbalbeacondemo;

import android.app.Application;
import android.util.Log;

import com.gimbal.android.CommunicationListener;
import com.gimbal.android.CommunicationManager;
import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;

/**
 * Created by Rebecca McBath
 * on 2019-08-01.
 */
public class ApplicationActivity extends Application {

	private static final Boolean ENABLE_PUSH_MESSAGING = null;
	private CommunicationListener communicationListener;
	private PlaceEventListener placeEventListener;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("-----", "Application created");

		Gimbal.setApiKey(this, getString(R.string.gimbal_api_key));
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		PlaceManager.getInstance().removeListener(placeEventListener);
		CommunicationManager.getInstance().removeListener(communicationListener);
	}
}
