package me.feeasy.test.cardview;

import me.feeasy.test.R;

public class SumValidator extends EditValidator {
	String text = "";
	
	public float getNumber() {
		try {
			return Float.parseFloat(text);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	
	public int getCents() {
		return (int)Math.floor(getNumber()*100);
	}
	
	@Override
	public boolean isComplete() {
		return text.length() >= 3;
	}

	@Override
	public boolean isValid() {
		float number = getNumber();
		
		return number>=100 && number <=500;
	}

	@Override public String correctString(String initial) {
		text = initial;
		text.replace(",", ".");
		
		if( isValid() ) {
			if( text.indexOf(".")<0 ) {
				text += ".00";
			}
		}
		
		return text;
	}
	
	@Override public String errorText() {
		if( thisView==null ) return null;
		return thisView.getResources().getString(R.string.error_sum);
	}
}
