package me.feeasy.test.cardview;

import java.util.ArrayList;
import java.util.HashSet;

import android.os.Bundle;

import me.feeasy.test.CardNumber;
import me.feeasy.test.CardType;
import me.feeasy.test.FeeasyApp;
import me.feeasy.test.PayData;

public class SavedCard {
	public static ArrayList<SavedCard> savedCards = new ArrayList<SavedCard>();
	
	public SavedCard(String id, String displayName, CardType displayType,
			String displayNumbers, int expYear, int expMonth) {
		this.id = id;
		this.displayName = displayName;
		this.displayType = displayType;
		this.displayNumbers = displayNumbers;
		this.expYear = expYear;
		this.expMonth = expMonth;
		
		this.existing = true;
	}
	
	public SavedCard() {
		this.existing = false;
	}
	
	private HashSet<Runnable> changeListeners = new HashSet<Runnable>(); 
	
	protected boolean deleted = false;
	
	public boolean fromSavedCards = false;
	public boolean existing;
	
	public String id;
	public String displayName;
	public CardType displayType;
	public String displayNumbers;
	//protected int    iconDrawable;
	public int    expMonth;
	public int    expYear;
	
	static final String TAG_ID = "sc_id";
	static final String TAG_DISPLAY_NAME = "sc_name";
	static final String TAG_DISPLAY_TYPE = "sc_type";
	static final String TAG_DISPLAY_NUMBERS = "sc_numbers";
	static final String TAG_EXP_MONTH = "sc_month";
	static final String TAG_EXP_YEAR  = "sc_year";
	static final String TAG_EXISTING  = "sc_existing";
	
	public void onChangeRequested() {}
	
	public final void addChangeListener(Runnable listener) {
		changeListeners.add(listener);
	}
	
	public final void removeChangeListener(Runnable listener) {
		changeListeners.remove(listener);
	}
	
	public void invokeChangeListeners() {
		for(Runnable r : changeListeners) {
			r.run();
		}
	}
	
	public boolean isExisting() {
		return this.existing;
	}
	
	public void save(Bundle bundle) {
		bundle.putBoolean(TAG_EXISTING, existing);
		if(!existing ) return;
		
		bundle.putString(TAG_ID, id);
		bundle.putString(TAG_DISPLAY_NAME, displayName);
		bundle.putString(TAG_DISPLAY_TYPE, displayType.getName());
		bundle.putString(TAG_DISPLAY_NUMBERS, displayNumbers);
		bundle.putInt(TAG_EXP_MONTH, expMonth);
		bundle.putInt(TAG_EXP_YEAR, expYear);
	}
	
	public void load(Bundle bundle) {
		existing = bundle.getBoolean(TAG_EXISTING);
		if(!existing ) return;
		
		id = bundle.getString(TAG_ID);
		displayName = bundle.getString(TAG_DISPLAY_NAME);
		displayType = CardType.getById(bundle.getString(TAG_DISPLAY_TYPE));
		displayNumbers = bundle.getString(TAG_DISPLAY_NUMBERS);
		expMonth = bundle.getInt(TAG_EXP_MONTH);
		expYear = bundle.getInt(TAG_EXP_YEAR);
	}
	
	public static void saveNew(PayData payData, String id) {
		SavedCard foundCard = null;
		String numbers = "";
		
		if( payData.senderCard.length() > 4 ) 
			numbers = payData.senderCard.substring(payData.senderCard.length() - 4);
		
		CardNumber cardNumber = new CardNumber(payData.senderCard);
		
		for(SavedCard card : SavedCard.savedCards) {
			if( ( card.displayNumbers.equals(numbers) &&
				  card.displayType == cardNumber.getType() ) ||
				  card.id.equals(id) ) {
				foundCard = card;
				break;
			}
		}
		
		if( foundCard==null ) {
			foundCard=new SavedCard();
			foundCard.fromSavedCards = true;
			savedCards.add(foundCard);
		}
		
		foundCard.id = id;
		
		foundCard.existing = true;
		foundCard.displayNumbers = numbers;
		foundCard.displayType = cardNumber.getType();
		foundCard.displayName = "";
		
		foundCard.expMonth = payData.expMonth;
		foundCard.expYear  = payData.expYear;
		
		FeeasyApp.instance.saveCards();
	}

	public static void deleteCard(SavedCard savedCard) {
		savedCards.remove(savedCards.indexOf(savedCard));
		
		FeeasyApp.instance.saveCards();
	}

	public boolean isFromSavedCards() {
		return fromSavedCards;
	}
}

