package me.feeasy.test;

public class CSCValidator extends EditValidator {
	String text = "";
	CardNumber cardNumber;
	
	CSCValidator(CardNumber cardNumber) {
		this.cardNumber=cardNumber;
	}
	
	int getNumber() {
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
}
