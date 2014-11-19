package me.feeasy.test;

import java.util.regex.Pattern;

public class CardNumber {
	private String number = "";
	static private Pattern toNumeric = Pattern.compile("\\D"); 
	
	public void set(String val) {
		this.number = toNumeric.matcher(val).replaceAll("");
	}
	
	public CardNumber(String val) {
		set(val);
	}
	
	public CardNumber() {}
	
	public CardType getType() {
		return CardType.getByPrefix(number);
	}
	
	public CardType toError() {
		return getType().toLengthError(number).toRangeError(number).toChecksumError(number);
	}
	
	public boolean isValid() {
		return !toError().isError();
	}
	
	public String getPretty() {
		return getType().toPrettyNumber(number);
	}
	
	public String getString() {
		return number;
	}
	
	public boolean hasMaxLength() {
		return getType().maxLength(number) == number.length();
	}
}
