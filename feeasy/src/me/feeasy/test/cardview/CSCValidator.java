package me.feeasy.test.cardview;

import me.feeasy.test.CardNumber;
import me.feeasy.test.R;

public class CSCValidator extends EditValidator {
	String text = "";
	CardNumber cardNumber;
	
	CSCValidator(CardNumber cardNumber) {
		this.cardNumber=cardNumber;
	}
	
	public int getNumber() {
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	
	@Override
	public boolean isComplete() {
		return text.length() == cardNumber.getType().getCSCLength();
	}

	@Override
	public boolean isValid() {
		int number = getNumber();
		return number >= 0 && isComplete();
	}

	@Override public String correctString(String initial) {
		return text = initial;
	}
	
	@Override public String errorText() {
		if( thisView==null ) return null;
		return thisView.getResources().getString(R.string.error_csc);
	}
}
