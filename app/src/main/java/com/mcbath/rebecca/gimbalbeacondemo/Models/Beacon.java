package com.mcbath.rebecca.gimbalbeacondemo.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Rebecca McBath
 * on 2019-08-06.
 */
public class Beacon {

	@SerializedName("id")
	@Expose
	private String id;
	@SerializedName("factory_id")
	@Expose
	private String factoryId;
	@SerializedName("icon_url")
	@Expose
	private String iconUrl;
	@SerializedName("name")
	@Expose
	private String name;
	@SerializedName("latitude")
	@Expose
	private Double latitude;
	@SerializedName("longitude")
	@Expose
	private Double longitude;
	@SerializedName("gimbal_latitude")
	@Expose
	private Double gimbalLatitude;
	@SerializedName("gimbal_longitude")
	@Expose
	private Double gimbalLongitude;
	@SerializedName("gimbal_location_last_updated_date")
	@Expose
	private String gimbalLocationLastUpdatedDate;
	@SerializedName("visibility")
	@Expose
	private String visibility;
	@SerializedName("battery_level")
	@Expose
	private String batteryLevel;
	@SerializedName("battery_updated_date")
	@Expose
	private String batteryUpdatedDate;
	@SerializedName("hardware")
	@Expose
	private String hardware;
	@SerializedName("owner")
	@Expose
	private String owner;
	@SerializedName("contact_emails")
	@Expose
	private List<String> contactEmails = null;
	@SerializedName("beacon_attributes")
	@Expose
	private BeaconAttributes beaconAttributes;
	@SerializedName("imported_sharing_keys")
	@Expose
	private List<String> importedSharingKeys = null;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFactoryId() {
		return factoryId;
	}

	public void setFactoryId(String factoryId) {
		this.factoryId = factoryId;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getGimbalLatitude() {
		return gimbalLatitude;
	}

	public void setGimbalLatitude(Double gimbalLatitude) {
		this.gimbalLatitude = gimbalLatitude;
	}

	public Double getGimbalLongitude() {
		return gimbalLongitude;
	}

	public void setGimbalLongitude(Double gimbalLongitude) {
		this.gimbalLongitude = gimbalLongitude;
	}

	public String getGimbalLocationLastUpdatedDate() {
		return gimbalLocationLastUpdatedDate;
	}

	public void setGimbalLocationLastUpdatedDate(String gimbalLocationLastUpdatedDate) {
		this.gimbalLocationLastUpdatedDate = gimbalLocationLastUpdatedDate;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public String getBatteryLevel() {
		return batteryLevel;
	}

	public void setBatteryLevel(String batteryLevel) {
		this.batteryLevel = batteryLevel;
	}

	public String getBatteryUpdatedDate() {
		return batteryUpdatedDate;
	}

	public void setBatteryUpdatedDate(String batteryUpdatedDate) {
		this.batteryUpdatedDate = batteryUpdatedDate;
	}

	public String getHardware() {
		return hardware;
	}

	public void setHardware(String hardware) {
		this.hardware = hardware;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public List<String> getContactEmails() {
		return contactEmails;
	}

	public void setContactEmails(List<String> contactEmails) {
		this.contactEmails = contactEmails;
	}

	public BeaconAttributes getBeaconAttributes() {
		return beaconAttributes;
	}

	public void setBeaconAttributes(BeaconAttributes beaconAttributes) {
		this.beaconAttributes = beaconAttributes;
	}

	public List<String> getImportedSharingKeys() {
		return importedSharingKeys;
	}

	public void setImportedSharingKeys(List<String> importedSharingKeys) {
		this.importedSharingKeys = importedSharingKeys;
	}
}
