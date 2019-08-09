package com.mcbath.rebecca.gimbalbeacondemo.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.gimbal.android.Beacon;
import com.gimbal.android.BeaconEventListener;
import com.gimbal.android.BeaconManager;
import com.gimbal.android.BeaconSighting;
import com.gimbal.android.CommunicationManager;
import com.gimbal.android.Gimbal;
import com.gimbal.android.GimbalDebugger;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Visit;
import com.mcbath.rebecca.gimbalbeacondemo.R;

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
	private BeaconEventListener beaconEventListener;
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

		listView = findViewById(R.id.list);
		textView = findViewById(R.id.text);

		listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		listView.setAdapter(listAdapter);

		if (savedInstanceState != null) {
			list = savedInstanceState.getStringArrayList(STATE_KEY);
		}

		// this method checks to make sure bluetooth is turned on or asks permission to do it,
		// then calls the location permission check to start monitoring
		verifyBluetooth();
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
					verifyBluetooth();
					item.setTitle(getString(R.string.offMenuTitle));
					textView.setText(getString(R.string.listening_for_beacons));
					textView.setTextColor(getResources().getColor(R.color.green));
					Log.d(TAG, "Scan has been turned on in Settings");
				} else if (!monitoringNotOn()) {
					PlaceManager.getInstance().stopMonitoring();
					CommunicationManager.getInstance().stopReceivingCommunications();
					item.setTitle(getString(R.string.onMenuTitle));
					textView.setText(getString(R.string.monitoring_off));
					textView.setTextColor(getResources().getColor(R.color.red));
					Log.d(TAG, "Scan has been turned off in Settings");
				}
				break;
			case R.id.activation_setting:
				// start activation activity
				Intent intent = new Intent(MainActivity.this, ManageBeaconActivity.class);
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

	// --------------------
	// LOCATION PERMISSION
	// --------------------
	public void checkPermissionStartMontioring() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android M Permission check

			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				// Location permission was not granted already, so request
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
			} else {
				// Location permission was already granted
				Gimbal.start();
//				enableBeaconMonitoring();
				enablePlaceMonitoring();
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
			if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// permission granted
				Gimbal.start();
//				enableBeaconMonitoring();
				enablePlaceMonitoring();
			} else {
				// permission denied or request cancelled
			}
		}
	}

	// onBeaconSighting in BeaconManager only invoked when a beacon that's not associated to a Place, is sighted
	// This doesn't seem to be working on it's own. It will work when within the Place Monitoring
	private void listenForBeacons() {

		beaconManager = new BeaconManager();

		beaconEventListener = new BeaconEventListener(){
			@Override
			public void onBeaconSighting(BeaconSighting beaconSighting) {
				super.onBeaconSighting(beaconSighting);

				final String id = beaconSighting.getBeacon().getIdentifier();
				data.put(beaconSighting.getBeacon().getName(), beaconSighting);
				listAdapter.clear();
				List<String> collection = asString(data.values());
				listAdapter.addAll(collection);
				listAdapter.notifyDataSetChanged();

				Log.d(TAG, "Beacon Found in BeaconManager: " + beaconSighting.getBeacon().getName());
			}
		};
		Log.d(TAG, "BeaconEventListener started sucessfully...scanning for beacon sightings");

		beaconManager.addListener(beaconEventListener);
		beaconManager.startListening();
		CommunicationManager.getInstance().startReceivingCommunications();
	}

	// onBeaconSighting in BeaconManager only invoked when a beacon that's not associated to a Place, is sighted
	private void enableBeaconMonitoring() {

		beaconManager = new BeaconManager();
		beaconManager.addListener(new BeaconEventListener() {

			@Override
			public void onBeaconSighting(BeaconSighting beaconSighting) {
				super.onBeaconSighting(beaconSighting);

				final String id = beaconSighting.getBeacon().getIdentifier();
				data.put(beaconSighting.getBeacon().getName(), beaconSighting);
				listAdapter.clear();
				List<String> collection = asString(data.values());
				listAdapter.addAll(collection);
				listAdapter.notifyDataSetChanged();

				Log.d(TAG, "Beacon Found in BeaconManager: " + beaconSighting.getBeacon().getIdentifier() + ": " + beaconSighting.getBeacon().toString() + " ---> RSSI is " + beaconSighting.getRSSI().toString());

				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {

						listAdapter.getPosition(id);
						Intent intent = new Intent(MainActivity.this, DetailActivity.class);
						intent.putExtra("beacon_id", id);
						startActivity(intent);
					}
				});
			}
		});
		beaconManager.startListening();

		CommunicationManager.getInstance().startReceivingCommunications();

		Log.d(TAG, "==> ENABLED BEACON SIGHTING MONITORING");
	}

	// onBeaconSighting in PlaceManager will will be invoked when a beacon assigned to a Place within a current visit is sighted
	private void enablePlaceMonitoring(){

				placeEventListener = new PlaceEventListener() {

					@Override
					public void onBeaconSighting(BeaconSighting beaconSighting, List<Visit> list) {

						final String id = beaconSighting.getBeacon().getIdentifier();
						data.put(beaconSighting.getBeacon().getName(), beaconSighting);
						listAdapter.clear();
						final List<String> collection = asString(data.values());
						listAdapter.addAll(collection);
						listAdapter.notifyDataSetChanged();

						Log.d(TAG, "Beacon Found in PlaceManager: " + beaconSighting.getBeacon().getIdentifier() + ": " + beaconSighting.getBeacon().getName() + " ---> RSSI is " + beaconSighting.getRSSI().toString());

						listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
								listAdapter.getPosition(id);
								Intent intent = new Intent(MainActivity.this, DetailActivity.class);
								intent.putExtra("beacon_id", id);
								startActivity(intent);
							}
						});
					}
				};
					placeManager = PlaceManager.getInstance();
					placeManager.addListener(placeEventListener);
					placeManager.startMonitoring();

		CommunicationManager.getInstance().startReceivingCommunications();
		Log.d(TAG, "==> ENABLED PLACE MONITORING");
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
			double range = calculateAccuracy(beaconSighting.getRSSI());
			DecimalFormat df = new DecimalFormat("#.00");
			String format = (beacon.getName() + "\n" + "Distance: " + df.format(range) + " meters" + " " + "(RSSI: " + beaconSighting.getRSSI() + ")" + "\n" + "Battery level: " + beacon.getBatteryLevel());
			list.add(format);
		}

		return list;
	}

	// calculate range (distance from beacon)
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

	// --------------------
	// CHECK BLUETOOTH
	// --------------------
	private void verifyBluetooth() {

		// Initializes Bluetooth adapter
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		//  BT not activated, ask for permission to enable it
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			textView.setTextColor(getResources().getColor(R.color.red));
			textView.setText(getString(R.string.ble_not_supported));
		} else if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			textView.setTextColor(getResources().getColor(R.color.red));
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

		} else { //  BT is activated, we can start monitoring for beacons
			checkPermissionStartMontioring();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_LONG).show();
				checkPermissionStartMontioring();
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "User cancelled", Toast.LENGTH_LONG).show();
			}
		}
	}

	public boolean monitoringNotOn() {
		return !PlaceManager.getInstance().isMonitoring();
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Gimbal.stop();
		beaconManager.removeListener(beaconEventListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}
