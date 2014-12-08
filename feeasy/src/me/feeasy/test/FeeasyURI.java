package me.feeasy.test;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

public class FeeasyURI {
	static final String URI_PREFIX = "FESY";
	private static final String TAG = "FeeasyURI";
	
	boolean error;
	String message;
	String cyphertoken;
	
	@SuppressLint("DefaultLocale")
	public FeeasyURI(String url) {
		this.error = true;
		
		if( url==null ) return;
		
    	try {
        	Uri uri = Uri.parse(url.toLowerCase());
        	
        	String sign = null;
        	
        	if( uri.getScheme().equals("http") || uri.getScheme().equals("https") ) {
        		if( uri.getHost().equals("feeasy.me") || uri.getHost().equals("feeasy.me.") ||
        			uri.getHost().equals("www.feeasy.me") || uri.getHost().equals("www.feeasy.me.")) {
        			if( uri.getPath()==null ) throw new Exception();
        			String[] path = uri.getPath().split("/");
        			if( path.length>=3 && 
        					( path[1].equals("a"))) {
        				sign = path[2];
        			}
        		}
        	} else if( uri.getScheme().equals("feeasy") ) {
        		if( uri.getHost().equals("a") )
        			sign = uri.getPath();
        	}
        	
        	if( sign==null ) {
        		throw new Exception();
        	}
        	
        	byte[] decodedSign = decryptSign(sign);
        	String cyphertoken = (char)(decodedSign[0]) + Utility.bytesToHex(decodedSign,1, 20);
        	String message = new String(decodedSign, 20, decodedSign.length - 20);
        	
        	this.message = message;
        	this.cyphertoken = cyphertoken;
        	this.error = false;
        } catch(Exception e) {
        	//Toast.makeText(getApplicationContext(), "URL не предназначен для feeasy", 
        	//		Toast.LENGTH_SHORT).show();
        }
	}
	
	@SuppressLint("TrulyRandom")
	public static byte[] decryptSign(String signCode) {
		try {
			//MessageDigest digester = MessageDigest.getInstance("SHA-256");
			//digester.update(query.getBytes());
			//byte[] hash = digester.digest();
			
			byte[] sign = Utility.b32decode(signCode.replace("0", "="));
			
			byte[] keyBytes = Base64.decode("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDIcRIyXNGGEPjDYKTlr6U2nAVZeFiQi2lBtoULYx9Gkn448H7BrpM74MXASiw6p4exk0+6P6CLZUksgpuMw44F5lCCQ5yA/IDmNGGY7+lpi8o1tMf4ijgxPDV47goBJeA9SA7g1YEE0JBaP8q/uqufQn554JBkjv8ys4iFSUc0vwIDAQAB", Base64.DEFAULT);
			
		    X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		    KeyFactory fact = KeyFactory.getInstance("RSA");
		    PublicKey key = fact.generatePublic(spec);
		    
		    Cipher rsa = Cipher.getInstance("RSA");
	        rsa.init(Cipher.ENCRYPT_MODE, key);
			
			byte[] result = rsa.doFinal(sign,0, 128);
			int firstNonZero, lastNoneZero;
			for(firstNonZero=0;firstNonZero<result.length;++firstNonZero) {
				if(result[firstNonZero]!=0) break;
			}
			for(lastNoneZero=result.length;lastNoneZero>0;--lastNoneZero) {
				if(result[lastNoneZero-1]!=0) break;
			}
			
			String prefix = new String(Arrays.copyOfRange(result,firstNonZero, firstNonZero + URI_PREFIX.length()));
			Log.d(TAG, "Decoded URL prefix: " + prefix);
			if(!URI_PREFIX.equals(prefix))
				return null;
			
			return Arrays.copyOfRange(result, firstNonZero + URI_PREFIX.length(), lastNoneZero);// strResult.substring(firstNonZero);
			
			//return new String(result).indexOf(new String(hash)) >= 0;
		} catch (NoSuchAlgorithmException e) {
		} catch (InvalidKeySpecException e) {
		} catch (InvalidKeyException e) {
		} catch (NoSuchPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		} catch (BadPaddingException e) { }
		
		return null;
	}
}

