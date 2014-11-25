package me.feeasy.test;

import java.util.TreeMap;
import java.util.regex.Pattern;

public enum CardType {
	VISA(1, "Visa", false, 3, "^4\\d{15}$", new String[]{"4", "5"}) { 
		public CardType toLengthError(String number) {
			if( number.length() < 16 && number.length()!=13 ) return NOT_ENOUGH_DIGITS;
			if( number.length() > 16 ) return TOO_MANY_DIGITS;
			
			return this;
		}
		@Override public int maxLength(String number) {
			return 16;
		}
		@Override public int getCardImage() {
			return R.drawable.pk_card_visa;
		}
	},
	MASTERCARD(2, "Mastercard", false, 3, "^5[1-5]\\d{14}$", new String[]{"51", "56"}) {
		public CardType toLengthError(String number) {
			if( number.length() < 16 ) return NOT_ENOUGH_DIGITS;
			if( number.length() > 16 ) return TOO_MANY_DIGITS;
			
			return this;
		}
		@Override public int maxLength(String number) {
			return 16;
		}
		@Override public int getCardImage() {
			return R.drawable.pk_card_master;
		}
	}, 
	AMERICAN_EXPRESS(3, "American Express", false, 4, "^3[47]\\d{13}$", new String[]{"34", "38"})  {
		public CardType toLengthError(String number) {
			if( number.length() < 15 ) return NOT_ENOUGH_DIGITS;
			if( number.length() > 15 ) return TOO_MANY_DIGITS;
			
			return this;
		}
		@Override public int maxLength(String number) {
			return 15;
		}
		@Override public int getCardImage() {
			return R.drawable.pk_card_amex;
		}
		@Override public int getCSCLength() {
			return 4;
		}
	},
	DISCOVER(4,"Discover", false, 3, "^6(?:011\\d\\d|5\\d{4}|4[4-9]\\d{3}|22(?:1(?:2[6-9]|[3-9]\\d)|[2-8]\\d\\d|9(?:[01]\\d|2[0-5])))\\d{10}$"
			, new String[]{"6011", "6012", "622126", "622127", "622925", "622926", "644","66"}) {
		public CardType toLengthError(String number) {
			if( number.length() < 16 ) return NOT_ENOUGH_DIGITS;
			if( number.length() > 16 ) return TOO_MANY_DIGITS;
			
			return this;
		}
		@Override public int maxLength(String number) {
			return 16;
		}
		@Override public int getCardImage() {
			return R.drawable.pk_card_discover;
		}
	},
	JCB(5, "JCB", false, 3, "^35(?:2[89]|[3-8]\\d)\\d{12}$"
			, new String[]{"3528","359"}) {
		public CardType toLengthError(String number) {
			if( number.length() < 16 ) return NOT_ENOUGH_DIGITS;
			if( number.length() > 16 ) return TOO_MANY_DIGITS;
			
			return this;
		}
		@Override public int maxLength(String number) {
			return 16;
		}
	} ,
	DINERS_CLUB(6, "Diners Club", false, 3, "^$3(?:0[0-5]\\d|095|[689]\\d\\d)\\d{12}"
			, new String[]{"300","306", "309", "31", "36", "37", "38","4"}) {
		//300-305, 3095, 36, 38-39
		public CardType toLengthError(String number) {
			int expectedLength = maxLength(number);
			
			if( number.length() < expectedLength ) return NOT_ENOUGH_DIGITS;
			if( number.length() > expectedLength ) return TOO_MANY_DIGITS;
			
			return this;
		}
		@Override public int maxLength(String number) {
			if( number.length()>0 ) {
				if( number.charAt(0)=='3' ) return 14;
				if( number.charAt(0)=='2' ) return 15;
				if( number.charAt(0)=='5' ) return 16;
			}
			
			return 16;
		}
	},
	UNKNOWN_CARD(7, "Unknown", false, 3, "", new String[]{}) {
		public CardType toLengthError(String number) {
			if( number.length() < 9 ) return NOT_ENOUGH_DIGITS;
			if( number.length() > 19 ) return TOO_MANY_DIGITS;
			return this;
		}
		@Override public int maxLength(String number) {
			return 16;
		}
	},
	TOO_MANY_DIGITS(8, "Too Many Digits", true, 3, "", new String[]{}) {
		public CardType toLengthError(String number) {
			return this;
		}
		@Override public int maxLength(String number) {
			return 16;
		}
		@Override public int getErrorResource() {
			return R.string.error_card_too_long;
		}
	},
	NOT_ENOUGH_DIGITS(9, "Not Enough Digits", true, 3, "", new String[]{}) {
		public CardType toLengthError(String number) {
			return this;
		}
		@Override public int maxLength(String number) {
			return 16;
		}
		@Override public int getErrorResource() {
			return R.string.error_card_too_short;
		}
	},

	MAESTRO(10, "Maestro", false, 3, "^(?:5[0678]\\d\\d|6304|6390|67\\d\\d)\\d{8,15}$"
			, new String[]{"50", "51", "56", "59", "6304", "6305", "6390", "6391" , "67", "68"}) {
		public CardType toLengthError(String number) {
			if( number.length() < 12 ) return NOT_ENOUGH_DIGITS;
			if( number.length() > 19 ) return TOO_MANY_DIGITS;
			
			return this;
		}
		
		@Override 
		public String toPrettyNumber(String number) {
			if( number.length() < 9) return new String(number);
			return number.substring(0,8) + " " + number.substring(8); 
		}
		
		@Override public int maxLength(String number) {
			return 19;
		}
		
		@Override public int getCardImage() {
			return R.drawable.pk_card_maestro;
		}
	},
	
	ERROR_CARD(11, "Error", true, 3, "", new String[]{}) {
		public CardType toLengthError(String number) {
			return this;
		}
		@Override public int maxLength(String number) {
			return 16;
		}
		@Override public int getErrorResource() {
			return R.string.error_card_invalid;
		}
	},
	;
	
	static {
		ranges = new CardRangeDetector();
		for(CardType cardType: CardType.values()) {
			cardType.register();
		}
	}

	private int mVal;
	private String mName;
	private boolean mIsError;
	
	private Pattern validationPattern;
	private String[] strRanges;
	static CardRangeDetector ranges;
	CardType(int val, String name, boolean isError, int maxCVVLength, String validationRegExp, String[] strRanges) {
		this.mVal = val;
		this.mName = name;
		this.mIsError = isError;
		
		this.validationPattern = Pattern.compile(validationRegExp);
		
		this.strRanges = strRanges;
	}
	
	private void register() {
		for(int i=0;i<strRanges.length-1;i+=2) {
			ranges.regRange(strRanges[i], strRanges[i+1], this);
		}
	}

	public static CardType getByPrefix(String prefix) {
		CardType result = ranges.find(prefix);
		if( result==null ) return UNKNOWN_CARD;
		
		return result;
	}

	public int getValue() {
		return mVal;
	}

	public String getName() {
		return mName;
	}

	public boolean isError() {
		return mIsError;
	}
	
	public int getCardImage() {
		return R.drawable.pk_default_card;
	}

	abstract public CardType toLengthError(String number);
	public CardType toRangeError (String number) {
		if( mIsError ) return this;
		if( validationPattern.matcher(number).matches() ) return this;
		
		return UNKNOWN_CARD;
	}
	public CardType toChecksumError(String number) {
		if( mIsError ) return this;
		if( luhnValid(number) ) return this;
		
		return ERROR_CARD;
	}
	
	static boolean luhnValid(String number) {
		//if( number.length()==0 ) return true;
		
		int checksum = 0;
		
		for(int i=0; i<number.length();++i) {
			int digit = number.charAt(number.length() - 1 - i) - '0';
			int add;
			if( i % 2 == 0 ) {
				add = digit;
			} else {
				add = digit * 2;
				if( add>9 ) add-=9;
			}
			checksum = (checksum + add) % 10;
		}
		
		return checksum == 0;
	}
	
	public String toPrettyNumber(String number) {
		String result = new String();
		for(int i=0;i<number.length();i+=4) {
			if( i>0 ) result+=" ";
			result += number.substring(i,Math.min(i+4,number.length()));
		}
		return result;
	}
	
	public int getErrorResource() {
		return R.string.error_card_success;
	}

	abstract public int maxLength(String number);

	public int getCSCLength() {
		return 3;
	}
}

class CardRangeDetector {
	private class Range {
		public Range(String end, CardType type) {
			this.end = end;
			this.value = type;
		}
		
		String end;
		CardType value;
	}
	
	public TreeMap<String,Range> ranges = new TreeMap<String,Range>();
	
	public void regRange(String start, String end, CardType type) {
		ranges.put(start,new Range(end, type));
	}
	
	public CardType find(String number) {
		TreeMap.Entry<String,Range> floor = ranges.floorEntry(number);
		if( floor==null ) return null;
		if( floor.getValue().end.compareTo(number) <=0 ) return null;
		
		return floor.getValue().value;
	}
}