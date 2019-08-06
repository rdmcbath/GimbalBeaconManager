package com.mcbath.rebecca.gimbalbeacondemo.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mcbath.rebecca.gimbalbeacondemo.Models.Beacon;
import com.mcbath.rebecca.gimbalbeacondemo.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Rebecca McBath
 * on 2019-08-06.
 */
public class BeaconAdapter extends RecyclerView.Adapter<BeaconAdapter.ViewHolder> {
	private static final String TAG = "ManageBeaconActivity";

	private List<Beacon> beaconList;

	public BeaconAdapter(List<Beacon> beacons) {
		beaconList = beacons;
	}

	// Provide a direct reference to each of the views within a data item
	// Used to cache the views within the item layout for fast access
	public class ViewHolder extends RecyclerView.ViewHolder {
		// Your holder should contain a member variable
		// for any view that will be set as you render a row
		public TextView nameTextView;

		// We also create a constructor that accepts the entire item row
		// and does the view lookups to find each subview
		public ViewHolder(View itemView) {
			// Stores the itemView in a public final member variable that can be used
			// to access the context from any ViewHolder instance.
			super(itemView);
			nameTextView = itemView.findViewById(R.id.beacon_name);
		}
	}

	@NonNull
	@Override
	public BeaconAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		Context context = parent.getContext();
		LayoutInflater inflater = LayoutInflater.from(context);

		// Inflate the custom layout
		View contactView = inflater.inflate(R.layout.beacon_row_item, parent, false);

		// Return a new holder instance
		return new ViewHolder(contactView);
	}

	@Override
	public void onBindViewHolder(@NonNull BeaconAdapter.ViewHolder holder, int position) {

		holder.nameTextView.setText(beaconList.get(position).getName());
	}

	@Override
	public int getItemCount() {
		if (beaconList == null) {
			return 0;
		} else {
			Log.d(TAG, "getItemCount = " + beaconList.size());
			return beaconList.size();
		}
	}
}
