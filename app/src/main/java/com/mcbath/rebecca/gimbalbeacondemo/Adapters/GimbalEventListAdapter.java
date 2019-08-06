package com.mcbath.rebecca.gimbalbeacondemo.Adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mcbath.rebecca.gimbalbeacondemo.Models.GimbalEvent;
import com.mcbath.rebecca.gimbalbeacondemo.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Rebecca McBath
 * on 2019-08-02.
 */
public class GimbalEventListAdapter  extends BaseAdapter {

	private Activity activity;
	private List<GimbalEvent> events = new ArrayList<GimbalEvent>();
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, hh:mm:ss a", Locale.getDefault());

	public GimbalEventListAdapter(Activity activity) {
		this.activity = activity;
	}

	public void setEvents(List<GimbalEvent> events) {
		this.events.clear();
		this.events.addAll(events);
		notifyDataSetChanged();
	}

	public void addEvent(GimbalEvent event) {
		events.add(event);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return events.size();
	}

	@Override
	public Object getItem(int position) {
		return events.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GimbalEvent event = events.get(position);
		View view = convertView;
		if (view == null) {
			view = activity.getLayoutInflater().inflate(R.layout.event_row_item, null);
		}

		ImageView icon = view.findViewById(R.id.icon);
		Integer iconRes = iconRes(event.getType());
		if (iconRes != null) {
			icon.setImageResource(iconRes);
		}
		else {
			icon.setImageDrawable(null);
		}

		TextView title = view.findViewById(R.id.title);
		title.setText(event.getTitle());

		TextView subtitle = view.findViewById(R.id.date);
		subtitle.setText(dateFormat.format(event.getDate()));

		return view;
	}

	private int iconRes(GimbalEvent.TYPE type) {
		switch (type) {
			case PLACE_ENTER:
				return R.drawable.place_enter;

			case PLACE_ENTER_DELAY:
				return R.drawable.place_enter_delay;

			case PLACE_EXIT:
				return R.drawable.place_exit;

			case COMMUNICATION_PRESENTED:
				return R.drawable.comm_presented;

			case COMMUNICATION_ENTER:
				return R.drawable.comm_enter;

			case COMMUNICATION_EXIT:
				return R.drawable.comm_exit;

			case COMMUNICATION_INSTANT_PUSH:
				return R.drawable.comm_presented;

			case COMMUNICATION_TIME_PUSH:
				return R.drawable.comm_presented;

			case NOTIFICATION_CLICKED:
				return R.drawable.comm_enter;

			default:
				return R.drawable.place_enter;
		}
	}
}
