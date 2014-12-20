package me.feeasy.test;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;

public class PayData {
	public String senderCard;
	public String recipientCard;
	
	public int cvc;
	public int expMonth;
	public int expYear;
	
	public int sum; //times 100
	
	public String message;
	public int fee = 0;
	public boolean payFee=false;
	
	public static String TAG_SENDER_CARD = "sender_card";
	public static String TAG_PRECIPIENT_CARD = "recipient_card";
	public static String TAG_CSC = "csc";
	public static String TAG_EXP_MONTH = "exp_month";
	public static String TAG_EXP_YEAR  = "exp_year";
	public static String TAG_SUM       = "sum";
	public static String TAG_FEE       = "fee";
	public static String TAG_PAY_FEE       = "pay_fee";
	public static String TAG_MESSAGE   = "message";
	
	public PayData(String senderCard, String recipientCard, 
			int cvc, int expMonth, int expYear, int sum, String message) {
		this.senderCard = senderCard;
		this.recipientCard = recipientCard;
		this.cvc = cvc;
		this.expMonth = expMonth;
		this.expYear = expYear;
		this.sum = sum;
		
		this.message = message;
	}

	public PayData() {
		// TODO Auto-generated constructor stub
	}

	@SuppressLint("DefaultLocale")
	public String formatExpDate() {
		return String.format("%02d/%02d", expMonth, expYear);
	}

	@SuppressLint("DefaultLocale")
	public String formatCVC() {
		return String.format("%03d", cvc);
	}
	
	public String getMessage(Resources resources) {
		if( message!=null &&!message.equals("") ) return message;
		return resources.getString(R.string.label_recipient_message_not_specified);
	}
	
	public void save(Bundle bundle) {
		bundle.putString(TAG_SENDER_CARD, senderCard);
		bundle.putString(TAG_PRECIPIENT_CARD, recipientCard);
		bundle.putInt(TAG_CSC, cvc);
		bundle.putInt(TAG_EXP_MONTH, expMonth); 
		bundle.putInt(TAG_EXP_YEAR, expYear);
		bundle.putInt(TAG_SUM, sum);
		bundle.putInt(TAG_FEE, fee);
		bundle.putBoolean(TAG_PAY_FEE, payFee);
		bundle.putString(TAG_MESSAGE, message);
	}
	
	public void load(Bundle bundle) {
		senderCard = bundle.getString(TAG_SENDER_CARD);
		recipientCard = bundle.getString(TAG_PRECIPIENT_CARD);
		
		cvc      = bundle.getInt(TAG_CSC);
		expMonth = bundle.getInt(TAG_EXP_MONTH);
		expYear  = bundle.getInt(TAG_EXP_YEAR);
		sum      = bundle.getInt(TAG_SUM);
		fee      = bundle.getInt(TAG_FEE);
		payFee   = bundle.getBoolean(TAG_PAY_FEE);
		message  = bundle.getString(TAG_MESSAGE);
	}

	public boolean senderIdentifyedByToken() {
		if( senderCard.length() == 0 ) return false;
		char first = senderCard.charAt(0);
		
		return first < '0' || first > '9';
	}
}
