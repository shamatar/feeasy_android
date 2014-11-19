package me.feeasy.test;

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
}
