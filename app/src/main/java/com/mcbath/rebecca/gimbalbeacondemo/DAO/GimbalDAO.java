package com.mcbath.rebecca.gimbalbeacondemo.DAO;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.mcbath.rebecca.gimbalbeacondemo.Models.GimbalEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Rebecca McBath
 * on 2019-08-01.
 */
public class GimbalDAO {
	public static final String GIMBAL_NEW_EVENT_ACTION = "GIMBAL_EVENT_ACTION";
	public static final String PLACE_MONITORING_PREFERENCE = "pref_place_monitoring";
	public static final String SHOW_OPT_IN_PREFERENCE = "pref_show_opt_in";
	private static final String EVENTS_KEY = "events";
	private static SharedPreferences prefs;

	// --------------
	// GIMBAL EVENTS
	// --------------

	public static List<GimbalEvent> getEvents(Context context) {

		List<GimbalEvent> events = new ArrayList<GimbalEvent>();
		try {
			prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
			String jsonString = prefs.getString(EVENTS_KEY, null);
			if (jsonString != null) {
				JSONArray jsonArray = new JSONArray(jsonString);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					GimbalEvent event = new GimbalEvent();
					event.setType(GimbalEvent.TYPE.valueOf(jsonObject.getString("type")));
					event.setTitle(jsonObject.getString("title"));
					event.setDate(new Date(jsonObject.getLong("date")));
					events.add(event);
				}
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return events;
	}

	public static void setEvents(Context context, List<GimbalEvent> events) {
		try {
			prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
			JSONArray jsonArray = new JSONArray();
			for (GimbalEvent event : events) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("type", event.getType().name());
				jsonObject.put("title", event.getTitle());
				jsonObject.put("date", event.getDate().getTime());
				jsonArray.put(jsonObject);
			}
			String jstr = jsonArray.toString();
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(EVENTS_KEY, jstr);
			editor.commit();

			// Notify activity
			Intent intent = new Intent();
			intent.setAction(GIMBAL_NEW_EVENT_ACTION);
			context.sendBroadcast(intent);

		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void clearPrefs() {
		SharedPreferences.Editor editor = prefs.edit();
		editor.clear();
		editor.commit();
	}
}

