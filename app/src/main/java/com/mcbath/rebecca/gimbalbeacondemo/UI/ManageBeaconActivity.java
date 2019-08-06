package com.mcbath.rebecca.gimbalbeacondemo.UI;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mcbath.rebecca.gimbalbeacondemo.Adapters.BeaconAdapter;
import com.mcbath.rebecca.gimbalbeacondemo.Models.Beacon;
import com.mcbath.rebecca.gimbalbeacondemo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.msebera.android.httpclient.Header;

/**
 * Created by Rebecca McBath
 * on 2019-07-30.
 */
public class ManageBeaconActivity extends AppCompatActivity {
	private static final String TAG = "ManageBeaconActivity";

	private EditText editText;
	private static final String BASE_URL = "https://manager.gimbal.com/api/beacons";
	private static final String BASE_URL_PARAMS = "https://manager.gimbal.com/api/beacons/?";
	private String inputText;
	private RecyclerView rvBeacons;
	private List<Beacon> beacons;
	private BeaconAdapter adapter;
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_beacon);

		getSupportActionBar().setTitle("Activate Beacon");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		editText = findViewById(R.id.edit_text);
		progressBar = findViewById(R.id.progress_circular);
		rvBeacons = findViewById(R.id.recycler_view);
		rvBeacons.setLayoutManager(new LinearLayoutManager(this));

		editText.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void afterTextChanged(Editable s) {
					if (editText.length() == 4) {
						editText.append("-");
					}
				}
			});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_activate, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	public void viewBeacons(View v) {
		AsyncHttpClient client = new AsyncHttpClient();
		client.addHeader("Content-type", "application/json");
		client.addHeader("AUTHORIZATION", "Token token=350707dfebb9a52ae0d76d3cd34ae89f");
		client.get(BASE_URL, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				// Hide Progress Dialog
				progressBar.setVisibility(View.GONE);

				beacons = new ArrayList<>();
				adapter = new BeaconAdapter(beacons);

				try {
					String JSONString = new String(responseBody, StandardCharsets.UTF_8);

					// JSON Object
					JSONArray objs = new JSONArray(JSONString);
					Log.d(TAG, "Json payload:" + JSONString);

					for (int i = 0; i < objs.length(); i++) {
						String beaconName = objs.getJSONObject(i).get("name").toString();

						Beacon mBeacon = new Beacon();
						mBeacon.setName(beaconName);
						beacons.add(mBeacon);

						Log.d(TAG, "Beacon Name: " + beaconName);
					}

					populateList();

				} catch (JSONException e) {
					Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
					e.printStackTrace();

				}
			}

			// When the response returned by REST has Http response code other than '200'
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				// When Http response code is '404'
				if (statusCode == 404) {
					Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
				}
				// When Http response code is '500'
				else if (statusCode == 500) {
					Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
				}
				// When Http response code other than 404, 500
				else if (statusCode == 401) {
					Toast.makeText(getApplicationContext(), "Unauthorized", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	public void Activate(View v) {
		inputText = editText.getText().toString();
		Log.d(TAG, "user input the following: " + inputText);
		AsyncHttpClient client = new AsyncHttpClient();
		client.addHeader("Content-type", "application/json");
		client.addHeader("AUTHORIZATION", "Token token=350707dfebb9a52ae0d76d3cd34ae89f");
		RequestParams params = new RequestParams();
		params.put("factory_id", inputText);
		params.put("name", "Beacon added from mobile app");
		//		params.put("latitude", "");
		//		params.put("longitude", "");
		//		params.put("config_id", "100");
		//		params.put("visibility", "private");
		//		params.put("contact_emails", "rdmcbath@outlook.com");
		client.post(BASE_URL_PARAMS, params, new AsyncHttpResponseHandler() {

			// When the response returned by REST has Http response code '200'
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

				String jsonResponse = Arrays.toString(responseBody);
				Log.i(TAG, "onSuccess: " + jsonResponse);
				// Hide Progress Dialog
				Toast.makeText(getApplicationContext(), "Success - Activated Beacon!", Toast.LENGTH_LONG).show();
			}

			// When the response returned by REST has Http response code other than '200'
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

				// When Http response code is '404'
				if (statusCode == 404) {
					Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
				}
				// When Http response code is '500'
				else if (statusCode == 500) {
					Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
				}
				// When Http response code other than 404, 500
				else if (statusCode == 401) {
					Toast.makeText(getApplicationContext(), "Unauthorized", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	public void DeActivate(View v) {
		inputText = editText.getText().toString();
		AsyncHttpClient client = new AsyncHttpClient();
		client.addHeader("Content-type", "application/json");
		client.addHeader("AUTHORIZATION", "Token token=350707dfebb9a52ae0d76d3cd34ae89f");
		client.delete(BASE_URL + inputText, new AsyncHttpResponseHandler() {

			// When the response returned by REST has Http response code '200'
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				// Hide Progress Dialog
				Toast.makeText(getApplicationContext(), "Success - Deactivated Beacon!", Toast.LENGTH_LONG).show();

				try {
					String JSONString = new String(responseBody, StandardCharsets.UTF_8);

					// JSON Object
					JSONObject obj = new JSONObject(JSONString);
					Log.d(TAG, JSONString + "JSONObject is null? " + obj.get("name").toString());
					Log.d(TAG, String.format("%s\n", obj.toString()));

				} catch (JSONException e) {
					Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
					e.printStackTrace();

				}
			}

			// When the response returned by REST has Http response code other than '200'
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				// When Http response code is '404'
				if (statusCode == 404) {
					Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
				}
				// When Http response code is '500'
				else if (statusCode == 500) {
					Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
				}
				// When Http response code other than 404, 500
				else if (statusCode == 401) {
					Toast.makeText(getApplicationContext(), "Unauthorized", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	public void populateList(){
		adapter = new BeaconAdapter(beacons);
		rvBeacons.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
}

