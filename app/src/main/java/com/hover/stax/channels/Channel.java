package com.hover.stax.channels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.hover.stax.utils.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Entity(tableName = "channels")
public class Channel {

	public Channel() {}

	public Channel(int _id, String addChannel) {
		this.id = _id;
		this.name = addChannel;
	}

	public Channel(JSONObject jsonObject, String rootUrl) {
		update(jsonObject, rootUrl);
	}

	Channel update(JSONObject jsonObject, String rootUrl) {
		try {
			id = jsonObject.getInt("id");
			name = jsonObject.getString("name");
			countryAlpha2 = jsonObject.getString("country_alpha2").toUpperCase();
			currency = jsonObject.getString("currency");
			hniList = jsonObject.getString("hni_list");
			logoUrl = rootUrl + jsonObject.getString("logo_url");
			institutionId = jsonObject.getInt("institution_id");
			primaryColorHex = jsonObject.getString("primary_color_hex");
			secondaryColorHex = jsonObject.getString("secondary_color_hex");
		} catch (JSONException e) {
			Log.d("exception", e.getMessage());
		}
		return this;
	}

	@PrimaryKey
	@NonNull
	public int id;

	@NonNull
	@ColumnInfo(name = "name")
	public String name;

	@NonNull
	@ColumnInfo(name = "country_alpha2")
	public String countryAlpha2;

	@NonNull
	@ColumnInfo(name = "currency")
	public String currency;

	@NonNull
	@ColumnInfo(name = "hni_list")
	public String hniList;

	@NonNull
	@ColumnInfo(name = "logo_url")
	public String logoUrl;

	@NonNull
	@ColumnInfo(name = "institution_id")
	public int institutionId;

	@NonNull
	@ColumnInfo(name = "primary_color_hex")
	public String primaryColorHex;

	@NonNull
	@ColumnInfo(name = "secondary_color_hex")
	public String secondaryColorHex;

	@NonNull
	@ColumnInfo(name = "selected", defaultValue = "0")
	public boolean selected;

	@NonNull
	@ColumnInfo(name = "defaultAccount", defaultValue = "0")
	public boolean defaultAccount;

	@ColumnInfo(name = "pin")
	public String pin;

	@ColumnInfo(name = "latestBalance")
	public String latestBalance;

	@ColumnInfo(name = "latestBalanceTimestamp", defaultValue = "CURRENT_TIMESTAMP")
	public Long latestBalanceTimestamp;

	@ColumnInfo(name = "account_no")
	public String accountNo;

	@Ignore
	public String spentThisMonth, spentDifferenceToLastMonth;

	public void setSpentThisMonth(String spentThisMonth) {
		this.spentThisMonth = spentThisMonth;
	}

	public void setSpentDifferenceToLastMonth(String spentDifferenceToLastMonth) {
		this.spentDifferenceToLastMonth = spentDifferenceToLastMonth;
	}

	public void updateBalance(HashMap<String, String> parsed_variables) {
		latestBalance = parsed_variables.get("balance");
		if (parsed_variables.containsKey("update_timestamp") && parsed_variables.get("update_timestamp") != null) {
			latestBalanceTimestamp = Long.parseLong(parsed_variables.get("update_timestamp"));
		} else {
			latestBalanceTimestamp = DateUtils.now();
		}
	}

	@Override
	public String toString() {
		return name;
	}
}
