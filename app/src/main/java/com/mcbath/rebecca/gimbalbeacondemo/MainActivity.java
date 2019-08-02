package com.mcbath.rebecca.gimbalbeacondemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gimbal.android.Beacon;
import com.gimbal.android.BeaconSighting;
import com.gimbal.android.CommunicationManager;
import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Visit;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";

	private static final int PERMISSION_REQUEST_COARSE_LOCATION = 99;
	private static final int REQUEST_ENABLE_BT = 1;
	public static final int TX_POWER = -60; // RSSI
	private static final String STATE_KEY = "list_state";

	private GimbalEventReceiver gimbalEventReceiver;
	private GimbalEventListAdapter adapter;

	private PlaceManager placeManager;
	private PlaceEventListener placeEventListener;
	private Map<String, BeaconSighting> data = new HashMap<>();
	private ArrayAdapter<String> listAdapter;
	private ListView listView;
	private TextView textView;
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayList<String> list;
	private Menu menu;
	private String onMenuTitle = "Turn on monitoring";
	private String offMenuTitle = "Turn off monitoring";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1);
//		listView = findViewById(R.id.list);
//		textView = findViewById(R.id.text);
//		listView.setAdapter(listAdapter);

		textView = findViewById(R.id.text);
		adapter = new GimbalEventListAdapter(this);

		ListView listView = findViewById(R.id.list);
		listView.setAdapter(adapter);

		if (savedInstanceState != null){
			list=savedInstanceState.getStringArrayList(STATE_KEY);
		}

		checkLocationPermissionAndMonitor();
	}

	// --------------------
	// SETTINGS MENU
	// --------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {

		// item title is already set to "Turn monitoring off" at first, meaning the monitoring should be happening
		switch (item.getItemId()) {
			case R.id.monitoring_setting:
				// turn monitoring off/on
					if (monitoringNotOn()){
						checkLocationPermissionAndMonitor();
						item.setTitle(offMenuTitle);
						textView.setText(getString(R.string.listening_for_beacons));
						textView.setTextColor(getResources().getColor(R.color.green));
					} else if (!monitoringNotOn()) {
						PlaceManager.getInstance().stopMonitoring();
						CommunicationManager.getInstance().stopReceivingCommunications();
						item.setTitle(onMenuTitle);
						textView.setText(getString(R.string.monitoring_off));
						textView.setTextColor(getResources().getColor(R.color.red));
					}

//			case R.id.instance_settting:
//				// This dissociates a device and data (place events) reported by the application running on that device.
//				// The open place sightings get closed on server. Data on device also gets cleared due to this API invocation.
//				Gimbal.resetApplicationInstanceIdentifier();
//				Log.d(TAG, "Menu item: resetting the App Instance Id");
				return true;
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

		gimbalEventReceiver = new GimbalEventReceiver();
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

	public void checkLocationPermissionAndMonitor() {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android M Permission check

			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				// Location permission was not granted already, so request
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
			} else {
				// Location permission was already granted
				verifyBluetoothAndStartGimbal();
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
			if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// permission granted
				verifyBluetoothAndStartGimbal();
			} else {
				// permission denied or request cancelled
			}
		}
	}

	private void verifyBluetoothAndStartGimbal() {

//		placeEventListener = new PlaceEventListener() {
//			@Override
//			public void onVisitStart(Visit visit) {
//				Toast.makeText(getApplicationContext(), String.format("Start Visit for %s", visit.getPlace().getName()), Toast.LENGTH_LONG).show();
//				textView.setText(getString(R.string.beacon_in_range));
//				textView.setTextColor(getResources().getColor(R.color.green));
//			}
//
//			@Override
//			public void onVisitEnd(Visit visit) {
//				Toast.makeText(getApplicationContext(), String.format("End Visit for %s", visit.getPlace().getName()), Toast.LENGTH_LONG).show();
//				textView.setText(getString(R.string.beacon_out_of_range));
//				textView.setTextColor(getResources().getColor(R.color.red));
//			}
//
//			@Override
//			public void onBeaconSighting(BeaconSighting beaconSighting, List<Visit> list) {
//				data.put(beaconSighting.getBeacon().getName(), beaconSighting);
//
//				Log.d("onBeaconSighting: ", "getName is "+ beaconSighting.getBeacon().getName());
//
//				listAdapter.clear();
//				List<String> collection = asString(data.values());
//				listAdapter.addAll(collection);
//				listAdapter.notifyDataSetChanged();
//			}
//		};

		// Initializes Bluetooth adapter
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

		if (bluetoothManager != null) {
			mBluetoothAdapter = bluetoothManager.getAdapter();
		}

		//  BT not activated
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			textView.setTextColor(getResources().getColor(R.color.red));
			textView.setText(getString(R.string.ble_not_supported));
		} else if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			textView.setTextColor(getResources().getColor(R.color.red));
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			textView.setText(getString(R.string.ble_not_activated));
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

		} else {
//			placeManager = PlaceManager.getInstance();
//			placeManager.addListener(placeEventListener);
//			placeManager.startMonitoring();
//			CommunicationManager.getInstance().startReceivingCommunications();

			enableGimbalMonitoring();
		}
	}

//	private List<String> asString(Collection<BeaconSighting> v) {
//		List<BeaconSighting> values = new ArrayList<>(v);
//		Collections.sort(values, new Comparator<BeaconSighting>() {
//			@Override
//			public int compare(BeaconSighting lhs, BeaconSighting rhs) {
//				return rhs.getRSSI().compareTo(lhs.getRSSI());
//			}
//		});
//
//		list = new ArrayList<>();
//		for (BeaconSighting beaconSighting : values) {
//			Beacon beacon = beaconSighting.getBeacon();
//			double accuracy = calculateAccuracy(beaconSighting.getRSSI());
//			DecimalFormat df = new DecimalFormat("#.00");
//			String format = String.format("Name: %S - ID: %s\nRange ~ %sm (%sdb)", beacon.getName(), beacon.getIdentifier(), df.format(accuracy), beaconSighting.getRSSI());
//			list.add(format);
//		}
//
//		return list;
//	}

	protected static double calculateAccuracy(double rssi) {
		if (rssi == 0) {
			return -1.0; // if we cannot determine accuracy, return -1.
		}

		double ratio = rssi * 1.0 / MainActivity.TX_POWER;
		if (ratio < 1.0) {
			return Math.pow(ratio, 10);
		} else {
			return (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
		}
	}

	//    private String renderRange(Integer rssi) {
	//        if (rssi < -100) return "    ";
	//        else if (rssi > -100 && rssi <= -80) return "+   ";
	//        else if (rssi > -80 && rssi <= -55) return "++  ";
	//        else if (rssi > -55 && rssi <= -35) return "+++ ";
	//        else return "++++";
	//    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//onSaveInstanceState is called before going to another activity or orientation change
		outState.putStringArrayList(STATE_KEY, list);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		list = savedInstanceState.getStringArrayList(STATE_KEY);
	}

	private void enableGimbalMonitoring() {
		Gimbal.start();
	}

	private boolean monitoringNotOn() {

		return !PlaceManager.getInstance().isMonitoring();
	}


	// --------------------
	// EVENT RECEIVER
	// --------------------

	class GimbalEventReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() != null) {
				if (intent.getAction().compareTo(GimbalDAO.GIMBAL_NEW_EVENT_ACTION) == 0) {
					adapter.setEvents(GimbalDAO.getEvents(getApplicationContext()));
				}
			}
		}
	}
}
