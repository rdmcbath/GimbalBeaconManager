package com.mcbath.rebecca.gimbalbeacondemo.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Rebecca McBath
 * on 2019-08-06.
 */
public class BeaconAttributes {

	@SerializedName("ack")
	@Expose
	private String ack;

	public String getAck() {
		return ack;
	}

	public void setAck(String ack) {
		this.ack = ack;
	}
}
