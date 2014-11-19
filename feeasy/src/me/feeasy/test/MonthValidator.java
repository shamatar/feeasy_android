package me.feeasy.test;

public class MonthValidator extends EditValidator {
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
		int number = getNumber();
		if( number>=2 ) return true;
		
		return text.length() == 2;
	}

	@Override
	public boolean isValid() {
		int number = getNumber();
		return text.length() <= 2 && number > 0 && number <= 12;
	}

	@Override public String correctString(String initial) {
		text = initial;

		int number = getNumber();
		if( number>=2 && text.length()==1 ) {
			text = "0" + text;
		}
		
		return text;
	}
}
