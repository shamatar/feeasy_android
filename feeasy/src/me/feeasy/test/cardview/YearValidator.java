package me.feeasy.test.cardview;

import java.util.Calendar;

import me.feeasy.test.R;

public class YearValidator extends EditValidator {
	String text = "";
	
	public int getNumber() {
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	
	@Override
	public boolean isComplete() {
		return text.length() == 2;
	}

	@Override
	public boolean isValid() {
		if( text.length() != 2 ) return false;
		
		return getNumber() >= getCurrentYear();
	}

	private int getCurrentYear() {
		return Calendar.getInstance().get(Calendar.YEAR) % 100;
	}

	@Override public String correctString(String initial) {
		text = initial;		
		return text;
	}
	
	@Override public String errorText() {
		if( thisView==null ) return null;
		return thisView.getResources().getString(R.string.error_year);
	}
}
