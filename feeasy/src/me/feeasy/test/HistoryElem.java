package me.feeasy.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.os.Bundle;

import me.feeasy.test.CardType;
import me.feeasy.test.PayData;

public class HistoryElem {
	private static final String TAG_PAYDATA = "payData";
	private static final String TAG_DISPLAY_NAME = "displayName";
	private static final String TAG_DISPLAY_TYPE = "displayType";
	private static final String TAG_DATE = "date";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_TRANSACTION_ID = "transactionId";
	private static final String TAG_ID = "id";

	public static ArrayList<HistoryElem> history = new ArrayList<HistoryElem>();
	
	public PayData payData;
	public CardType senderCardType;
	public String senderCardName;
	public Date date;
	public boolean success;
	public String transactionId;
	public int id;
	public String error;
	
	public HistoryElem(PayData payData, 
			CardType senderCardType, 
			String   senderCardName, 
			String transactionId, 
			String error, boolean success) {
		this.payData = payData;
		this.senderCardType = senderCardType;
		this.senderCardName = senderCardName;
		this.date = new Date();
		this.transactionId = transactionId;
		this.id = history.size()*100;
		
		this.success = success;
	}
	
	public HistoryElem() {
	}
	
	public void save(Bundle bundle) {
		Bundle payBundle = new Bundle();
		payData.save(payBundle);
		payBundle.remove(PayData.TAG_CSC);
		
		bundle.putBundle(TAG_PAYDATA, payBundle);
		bundle.putString(TAG_DISPLAY_NAME, senderCardName);
		bundle.putString(TAG_DISPLAY_TYPE, senderCardType.getName());
		bundle.putLong(TAG_DATE, date.getTime());
		bundle.putBoolean(TAG_SUCCESS, success);
		bundle.putString(TAG_TRANSACTION_ID, transactionId);
		bundle.putInt(TAG_ID, id);
	}
	
	public void load(Bundle bundle) {
		this.payData = new PayData();
		
		this.payData.load(bundle.getBundle(TAG_PAYDATA));
		this.senderCardName = bundle.getString(TAG_DISPLAY_NAME);
		this.senderCardType = CardType.getById(bundle.getString(TAG_DISPLAY_TYPE));
		this.date = new Date(bundle.getLong(TAG_DATE));
		this.success = bundle.getBoolean(TAG_SUCCESS);
		this.transactionId = bundle.getString(TAG_TRANSACTION_ID);
		this.id = bundle.getInt(TAG_ID);
	}

	public String shortDate() {
		return Utility.timeInterval(date, new Date());
	}

	public void addAndSave() {
		history.add(this);
		FeeasyApp.instance.saveHistory();
	}
	
	static SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm\ndd.MM yyyy", Locale.US);
	public CharSequence formattedDate() {
		return dateFormat.format(date);
	}
}

