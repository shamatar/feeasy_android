package me.feeasy.test.cardview;

import me.feeasy.test.CardNumber;

public class CardNumberValidator extends EditValidator {
	public CardNumber cardNumber = new CardNumber(); 
	
	@Override
	public boolean isComplete() {
		return cardNumber.hasMaxLength();
	}

	@Override
	public boolean isValid() {
		return cardNumber.isValid();
	}

	@Override public String correctString(String initial) {
		cardNumber.set(initial);
		return cardNumber.getPretty();
	}

	@Override public String errorText() {
		if( thisView==null ) return null;
		return thisView.getResources().getString(cardNumber.toError().getErrorResource());
	}

	public String getPEN() {
		return cardNumber.getString();
	}
}
