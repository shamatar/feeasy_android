package me.feeasy.test;

public class SumValidator extends EditValidator {
	String text = "";
	
	float getNumber() {
		try {
			return Float.parseFloat(text);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	
	@Override
	public boolean isComplete() {
		return text.length() == 6;
	}

	@Override
	public boolean isValid() {
		float number = getNumber();
		
		return number>=100 && number <=500;
	}

	@Override public String correctString(String initial) {
		text = initial;		
		return text;
	}
}
