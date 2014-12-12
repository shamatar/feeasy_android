package me.feeasy.test;

import java.util.ArrayList;

import me.feeasy.test.cardview.SavedCard;
import me.feeasy.test.svgview.FontEncoder;
import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.widget.TextView;

public class FeeasyApp extends Application {
	public static FeeasyApp instance = null;
	
	static final String TAG_PREFS = "feeasyPrefs";
	static final String TAG_SAVED_CARDS = "savedCards";
	
	private static Drawable rurSign = null;
	public static void addViewRurSign(TextView view) {
		if( rurSign==null ) {
			FontEncoder.Glyph rur = FontEncoder.createFromResouce(view.getResources(), R.raw.rur);
			rurSign = new PictureDrawable(rur.getPicture(
					null, view.getResources().getDimensionPixelSize(R.dimen.smallText), view.getResources().getColor(R.color.darkText),
					0, view.getResources().getDimensionPixelSize(R.dimen.minStdPadding)/2 , 0, 0));
		}
    	
    	view.setLayerType(TextView.LAYER_TYPE_SOFTWARE, null);
    	view.setCompoundDrawablesWithIntrinsicBounds(rurSign, null, null, null);
	}
	
	@Override public void onCreate() {
		super.onCreate();
		instance = this;
		
		loadSavedCards();
	}
	
	@Override public void onTerminate() {
		instance = null;
		super.onTerminate();
	}
	
	public void loadSavedCards() {
		SavedCard.savedCards.clear();
		
		SharedPreferences prefs = getSharedPreferences(TAG_PREFS, MODE_PRIVATE);
		ArrayList<String> savedCards = Utility.stringToArray(prefs.getString(TAG_SAVED_CARDS, null));
		for(String card : savedCards) {
			Bundle bundle = Utility.bundleFromString(card);
			SavedCard savedCard = new SavedCard();
			savedCard.load(bundle);
			
			SavedCard.savedCards.add(savedCard);
		}
	}
	
	public void saveCards() {
		ArrayList<String> savedCards = new ArrayList<String>(SavedCard.savedCards.size());
		for(SavedCard savedCard : SavedCard.savedCards) {
			Bundle bundle = new Bundle();
			savedCard.save(bundle);
			
			savedCards.add(Utility.bundleToString(bundle));
		}
		
		SharedPreferences prefs = getSharedPreferences(TAG_PREFS, MODE_PRIVATE);
		prefs
			.edit()
			.putString(TAG_SAVED_CARDS, Utility.arrayToString(savedCards))
			.commit();
	}
}
