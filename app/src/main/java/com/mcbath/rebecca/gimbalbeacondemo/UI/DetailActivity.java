package com.mcbath.rebecca.gimbalbeacondemo.UI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.mcbath.rebecca.gimbalbeacondemo.DAO.GimbalDAO;
import com.mcbath.rebecca.gimbalbeacondemo.Adapters.GimbalEventListAdapter;
import com.mcbath.rebecca.gimbalbeacondemo.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Rebecca McBath
 * on 2019-08-02.
 */
public class DetailActivity extends AppCompatActivity {
	private static final String TAG = "DetailActivity";

	private static final String STATE_KEY = "list_state";
	private DetailActivity.GimbalEventReceiver gimbalEventReceiver;
	private GimbalEventListAdapter adapter;
	private ListView listView;
	private ArrayList<String> list;
	private static final String EVENTS_KEY = "events";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_beacon_detail);

		Intent intent = getIntent();
		String id = intent.getStringExtra("beacon_id");
		Log.d(TAG, "Intent Extra Id is " + id);

		getSupportActionBar().setTitle("Detail for " + id);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		adapter = new GimbalEventListAdapter(this);

		listView = findViewById(R.id.list);
		listView.setAdapter(adapter);

		if (savedInstanceState != null){
			list=savedInstanceState.getStringArrayList(STATE_KEY);
		}
	}

	// --------------------
	// SETTINGS MENU
	// --------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.clear_list:
				// clear the list of events and restart
				GimbalDAO.clearPrefs();
				listView.setAdapter(null);
				adapter = new GimbalEventListAdapter(this);
				listView.setAdapter(adapter);
				adapter.setEvents(GimbalDAO.getEvents(getApplicationContext()));
				break;
			case R.id.home:
				finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();

		gimbalEventReceiver = new DetailActivity.GimbalEventReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(GimbalDAO.GIMBAL_NEW_EVENT_ACTION);
		registerReceiver(gimbalEventReceiver, intentFilter);

		adapter.setEvents(GimbalDAO.getEvents(getApplicationContext()));
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(gimbalEventReceiver);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		adapter.setEvents(GimbalDAO.getEvents(getApplicationContext()));
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		//onSaveInstanceState is called before going to another activity or orientation change
		outState.putStringArrayList(STATE_KEY, list);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		list = savedInstanceState.getStringArrayList(STATE_KEY);
	}

	// --------------------
	// EVENT RECEIVER
	// --------------------

	class GimbalEventReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() != null) {
				if (intent.getAction().equals(GimbalDAO.GIMBAL_NEW_EVENT_ACTION)) {
					adapter.setEvents(GimbalDAO.getEvents(getApplicationContext()));
				}
			}
		}
	}
}
