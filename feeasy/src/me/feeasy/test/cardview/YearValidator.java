package me.feeasy.test.cardview;

import java.util.Calendar;

public class YearValidator extends EditValidator {
	String text = "";
	
	int getNumber() {
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
}
