package me.feeasy.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Base64;

public class Utility {
	//private static final String base32Chars =
    //    "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int[] base32Lookup =
    { 0xFF,0xFF,0x1A,0x1B,0x1C,0x1D,0x1E,0x1F, 
      0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF, 
      0xFF,0x00,0x01,0x02,0x03,0x04,0x05,0x06, 
      0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E, 
      0x0F,0x10,0x11,0x12,0x13,0x14,0x15,0x16, 
      0x17,0x18,0x19,0xFF,0xFF,0xFF,0xFF,0xFF, 
      0xFF,0x00,0x01,0x02,0x03,0x04,0x05,0x06, 
      0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E, 
      0x0F,0x10,0x11,0x12,0x13,0x14,0x15,0x16, 
      0x17,0x18,0x19,0xFF,0xFF,0xFF,0xFF,0xFF  
    };

    /**
     * Decodes the given Base32 String to a raw byte array.
     *
     * @param base32
     * @return Decoded <code>base32</code> String as a raw byte array.
     */
    static public byte[] b32decode(final String base32) {
        int i, index, lookup, offset, digit;
        byte[] bytes = new byte[base32.length() * 5 / 8];

        for (i = 0, index = 0, offset = 0; i < base32.length(); i++) {
            lookup = base32.charAt(i) - '0';

            /* Skip chars outside the lookup table */
            if (lookup < 0 || lookup >= base32Lookup.length) {
                continue;
            }

            digit = base32Lookup[lookup];

            /* If this digit is not in the table, ignore it */
            if (digit == 0xFF) {
                continue;
            }

            if (index <= 3) {
                index = (index + 5) % 8;
                if (index == 0) {
                    bytes[offset] |= digit;
                    offset++;
                    if (offset >= bytes.length)
                        break;
                } else {
                    bytes[offset] |= digit << (8 - index);
                }
            } else {
                index = (index + 5) % 8;
                bytes[offset] |= (digit >>> index);
                offset++;

                if (offset >= bytes.length) {
                    break;
                }
                bytes[offset] |= digit << (8 - index);
            }
        }
        return bytes;
    }
	
	final private static char[] hexArray = "0123456789abcdef".toCharArray();
	public static String bytesToHex(byte[] bytes, int start, int end) {
	    char[] hexChars = new char[(end-start) * 2];
	    for ( int j = start; j < end; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[(j-start) * 2] = hexArray[v >>> 4];
	        hexChars[(j-start) * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static String bundleToString(Bundle in) {
	    Parcel parcel = Parcel.obtain();

        in.writeToParcel(parcel, 0);
        String serialized = Base64.encodeToString(parcel.marshall(), 0);
        parcel.recycle();
        
        return serialized;
	}

	public static Bundle bundleFromString(String str) {
		Bundle bundle = new Bundle();
	    if (str != null) {
	        Parcel parcel = Parcel.obtain();
	        try {
	            byte[] data = Base64.decode(str, 0);
	            parcel.unmarshall(data, 0, data.length);
	            parcel.setDataPosition(0);
	            bundle = parcel.readBundle();
	        } finally {
	            parcel.recycle();
	        }
	    }
	    return bundle;
	}
	
	public static<T> String arrayToString(List<T> data) {
		Parcel parcel = Parcel.obtain();

        parcel.writeList(data);
        String serialized = Base64.encodeToString(parcel.marshall(), 0);
        parcel.recycle();
        
        return serialized;
	}
	
	public static<T> ArrayList<T> stringToArray(String str) {
		ArrayList<T> result = new ArrayList<T>();
		if( str==null ) return result;
		
		Parcel parcel = Parcel.obtain();
        try {
            byte[] data = Base64.decode(str, 0);
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            parcel.readList(result, null);
        } finally {
            parcel.recycle();
        }
        
        return result;
	}

	public static String prettyNum(int q, String one, String two,
			String five) {
		if( q<0 ) q = -q;
		
		if( q%10 == 1 && ((q%100) / 10) !=1 ) {
			return one;
		}
		
		if( (q + 9)%10 + 1 >= 5 || ((q%100) / 10) ==1 ) {
			return five;
		}
		
		return two;
	}

	@SuppressLint("DefaultLocale")
	public static String prettySum(int sum) {
		return String.format("%d.%02d", sum/100, sum%100);
	}
	
	static SimpleDateFormat dateFormat=new SimpleDateFormat("dd.MM yyyy", Locale.US);

	public static String timeInterval(Date old, Date cur) {
		long millisGone = cur.getTime() - old.getTime();
		
		Calendar cnew = Calendar.getInstance(), 
				 cold = Calendar.getInstance(),
				 ctmp = Calendar.getInstance();
		
		cnew.setTime(cur);
		cold.setTime(old);
		
		if( millisGone < 1000L * 60 * 3)
			return "только что";
		
		if( millisGone < 1000L * 60 * 60) {
			int minutes = (int)(millisGone / (1000L * 60)); 
			return minutes + " " + Utility.prettyNum(minutes, "минуту назад", "минуты назад", "минут назад");
		}
		
		if( millisGone < 1000L * 60 * 60 * 2)
			return "час назад";
		
		ctmp.setTime(new Date(cur.getTime() - 1000L * 60 * 60 * 24));
		if( cold.get(Calendar.DAY_OF_YEAR) == ctmp.get(Calendar.DAY_OF_YEAR) &&
			cold.get(Calendar.YEAR)        == ctmp.get(Calendar.YEAR) ) {
			return "вчера";
		}
		
		if( millisGone < 1000L * 60 * 60 * 24) {
			int hours = (int)(millisGone / (1000L * 60 * 60)); 
			return hours + " " + Utility.prettyNum(hours, "час назад", "часа назад", "часов назад");
		}
		
		if( millisGone < 1000L * 60 * 60 * 24 * 7 ) {
			int days = (int)(millisGone / (1000L * 60 * 60 * 7));
			return days + " " + Utility.prettyNum(days, "день назад", "дня назад", "дней назад");
		}
		
		return dateFormat.format(old);
	}
}
