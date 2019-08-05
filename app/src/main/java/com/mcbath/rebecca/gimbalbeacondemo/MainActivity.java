package com.mcbath.rebecca.gimbalbeacondemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gimbal.android.Beacon;
import com.gimbal.android.BeaconEventListener;
import com.gimbal.android.BeaconManager;
import com.gimbal.android.BeaconSighting;
import com.gimbal.android.Communication;
import com.gimbal.android.CommunicationListener;
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

	private static final int PERMISSION_REQUEST_FINE_LOCATION = 99;
	private static final int REQUEST_ENABLE_BT = 1;
	public static final int TX_POWER = -60; // RSSI
	private static final String STATE_KEY = "list_state";

	private PlaceManager placeManager;
	private PlaceEventListener placeEventListener;
	private BeaconSighting beaconSighting;
	private BeaconManager beaconManager;
	private Beacon beacon;
	private Map<String, BeaconSighting> data = new HashMap<>();
	private ArrayAdapter<String> listAdapter;
	private ListView listView;
	private TextView textView;
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayList<String> list;
	private Menu menu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate MainActivity");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1);
		listView = findViewById(R.id.list);
		textView = findViewById(R.id.text);
		listView.setAdapter(listAdapter);

		textView = findViewById(R.id.text);

		if (savedInstanceState != null) {
			list = savedInstanceState.getStringArrayList(STATE_KEY);
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

		switch (item.getItemId()) {
			case R.id.monitoring_setting:
				// turn monitoring off/on
				if (monitoringNotOn()) {
					checkLocationPermissionAndMonitor();
					item.setTitle(getString(R.string.offMenuTitle));
					textView.setText(getString(R.string.listening_for_beacons));
					textView.setTextColor(getResources().getColor(R.color.green));
				} else if (!monitoringNotOn()) {
					PlaceManager.getInstance().stopMonitoring();
					CommunicationManager.getInstance().stopReceivingCommunications();
					item.setTitle(getString(R.string.onMenuTitle));
					textView.setText(getString(R.string.monitoring_off));
					textView.setTextColor(getResources().getColor(R.color.red));
				}
				break;
			case R.id.activation_setting:
				// start activation activity
				Intent intent = new Intent(MainActivity.this, ActivateBeaconActivity.class);
				startActivity(intent);
				break;
			case R.id.instance_settting:
				// This dissociates a device and data (place events) reported by the application running on that device.
				// The open place sightings get closed on server. Data on device also gets cleared due to this API invocation.
				Gimbal.resetApplicationInstanceIdentifier();
				Log.d(TAG, "Menu item: resetting the App Instance Id");

				return true;
		}
		return super.onOptionsItemSelected(item);
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
	}

	public void checkLocationPermissionAndMonitor() {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android M Permission check

			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				// Location permission was not granted already, so request
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
			} else {
				// Location permission was already granted
				verifyBluetoothAndStartMonitoring();
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
			if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// permission granted
				verifyBluetoothAndStartMonitoring();
			} else {
				// permission denied or request cancelled
			}
		}
	}

	private void verifyBluetoothAndStartMonitoring() {

		// onBeaconSighting in BeaconManager will be invoked when a beacon is sighted
//		beaconManager = new BeaconManager();
//		beaconManager.addListener(new BeaconEventListener() {
//			@Override
//			public void onBeaconSighting(BeaconSighting beaconSighting) {
//				super.onBeaconSighting(beaconSighting);
//
//				final String id = beaconSighting.getBeacon().getIdentifier();
//				data.put(beaconSighting.getBeacon().getName(), beaconSighting);
//				listAdapter.clear();
//				List<String> collection = asString(data.values());
//				listAdapter.addAll(collection);
//				listAdapter.notifyDataSetChanged();
//
//				Log.d(TAG, "Beacon Found in BeaconManager: " + beaconSighting.getBeacon().toString() + " ---> RSSI is " + beaconSighting.getRSSI().toString());
//
//				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//					@Override
//					public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
//
//						Intent intent = new Intent(MainActivity.this, DetailActivity.class);
//						intent.putExtra("beacon_id", id);
//						startActivity(intent);
//					}
//				});
//			}
//		});

		// create listener for beacon sightings within a place
		placeEventListener = new PlaceEventListener() {

			// onBeaconSighting in PlaceManager will will be invoked when a beacon assigned to a place within a current visit is sighted
			@Override
			public void onBeaconSighting(BeaconSighting beaconSighting, List<Visit> list) {

				final String id = beaconSighting.getBeacon().getIdentifier();
				data.put(beaconSighting.getBeacon().getName(), beaconSighting);
				listAdapter.clear();
				List<String> collection = asString(data.values());
				listAdapter.addAll(collection);
				listAdapter.notifyDataSetChanged();

				Log.d(TAG, "Beacon Found in PlaceManager: " + beaconSighting.getBeacon().getName() + " ---> RSSI is " + beaconSighting.getRSSI().toString());

				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {

						Intent intent = new Intent(MainActivity.this, DetailActivity.class);
						intent.putExtra("beacon_id", id);
						startActivity(intent);
					}
				});
			}
		};

		// Initializes Bluetooth adapter
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

		mBluetoothAdapter = bluetoothManager.getAdapter();

		//  BT not activated
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			textView.setTextColor(getResources().getColor(R.color.red));
			textView.setText(getString(R.string.ble_not_supported));
		} else if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			textView.setTextColor(getResources().getColor(R.color.red));
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			textView.setText(getString(R.string.ble_not_activated));
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

		} else { //  BT is activated, we can start monitoring for beacons
			placeManager = PlaceManager.getInstance();
			placeManager.addListener(placeEventListener);
			placeManager.startMonitoring();

//			beaconManager.startListening();

			CommunicationManager.getInstance().addListener(new CommunicationListener() {
				@Override
				public Collection<Communication> presentNotificationForCommunications(Collection<Communication> collection, Visit visit) {
					return super.presentNotificationForCommunications(collection, visit);
				}
			});
			CommunicationManager.getInstance().startReceivingCommunications();
		}

		Log.d(TAG, "Location enabled and bluetooth verified ==> START MONITORING");
	}

	private List<String> asString(Collection<BeaconSighting> v) {
		List<BeaconSighting> values = new ArrayList<>(v);
		Collections.sort(values, new Comparator<BeaconSighting>() {
			@Override
			public int compare(BeaconSighting lhs, BeaconSighting rhs) {
				return rhs.getRSSI().compareTo(lhs.getRSSI());
			}
		});

		list = new ArrayList<>();
		for (BeaconSighting beaconSighting : values) {
			beacon = beaconSighting.getBeacon();
			double accuracy = calculateAccuracy(beaconSighting.getRSSI());
			DecimalFormat df = new DecimalFormat("#.00");
			String format = String.format("Name: %S - ID: %s\nRange ~ %sm (%sdb)", beacon.getName(), beacon.getIdentifier(), df.format(accuracy), beaconSighting.getRSSI());
			list.add(format);
		}

		return list;
	}

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

	public boolean monitoringNotOn() {
		return !PlaceManager.getInstance().isMonitoring();
	}
}
