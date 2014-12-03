package me.feeasy.test;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.google.zxing.client.android.CaptureActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class InitialActivity extends Activity {
	private static final String TAG_RESTORED = "restored";
	public static final int TAG_KILL_ALL = 8888;
	public static final int TAG_SHOW_PAY = 8889;
	
	@SuppressLint("TrulyRandom")
	static boolean checkSign(String query, String signCode) {
		try {
			MessageDigest digester = MessageDigest.getInstance("SHA-256");
			digester.update(query.getBytes());
			byte[] hash = digester.digest();
			
			byte[] sign = Base64.decode(signCode.replace("~", "="), Base64.URL_SAFE);
			
			byte[] keyBytes = Base64.decode("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDIcRIyXNGGEPjDYKTlr6U2nAVZeFiQi2lBtoULYx9Gkn448H7BrpM74MXASiw6p4exk0+6P6CLZUksgpuMw44F5lCCQ5yA/IDmNGGY7+lpi8o1tMf4ijgxPDV47goBJeA9SA7g1YEE0JBaP8q/uqufQn554JBkjv8ys4iFSUc0vwIDAQAB", Base64.DEFAULT);
			
		    X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		    KeyFactory fact = KeyFactory.getInstance("RSA");
		    PublicKey key = fact.generatePublic(spec);
		    
		    Cipher rsa = Cipher.getInstance("RSA");
	        rsa.init(Cipher.ENCRYPT_MODE, key);
			
			byte[] result = rsa.doFinal(sign);
			
			return new String(result).indexOf(new String(hash)) >= 0;
		} catch (NoSuchAlgorithmException e) {
		} catch (InvalidKeySpecException e) {
		} catch (InvalidKeyException e) {
		} catch (NoSuchPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		} catch (BadPaddingException e) { }
		
		return false;
	}

	@Override protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.initial);
		
		findViewById(R.id.logoHolder).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				runQr();
			}
		});
		
		if( savedState==null ||!savedState.getBoolean(TAG_RESTORED,false) ) {
	        // run QR scanner
	        runQr();
		}
	}
	
	private void runQr() {
		Intent intent = new Intent(getApplicationContext(), CaptureActivity.class);
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
	}
	
	@Override protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(TAG_RESTORED, true);
		super.onSaveInstanceState(outState);
	}
	
	@Override public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if( resultCode==TAG_KILL_ALL ) {
			setResult(TAG_KILL_ALL);
			finish();
			
			return;
		}
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
            	try {
	            	Uri uri = Uri.parse(intent.getStringExtra("SCAN_RESULT"));
	            	
	            	String sign = null;
	            	
	            	if( uri.getScheme().equals("http") || uri.getScheme().equals("https") ) {
	            		if( uri.getHost().equals("feeasy.me") || uri.getHost().equals("feeasy.me.") ||
	            			uri.getHost().equals("www.feeasy.me") || uri.getHost().equals("www.feeasy.me.")) {
	            			if( uri.getPath()==null ) throw new Exception();
	            			String[] path = uri.getPath().split("/");
	            			if( path.length<3 || 
	            					(!path[1].equals("fee") && !path[1].equals("sign"))) {
	            				throw new Exception();
	            			}
	            			
	            			sign = path[2].substring(1);
	            		}
	            	} else if( uri.getScheme().equals("feeasy") ) {
	            		if( uri.getHost().equals("fee") )
	            			sign = uri.getPath().substring(1);
	            	}
	            	
	            	if( sign==null ) {
	            		throw new Exception();
	            	}
	            	
	            	String query = uri.getQuery();
	            	
	            	if(!checkSign(query, sign) ) {
	            		throw new Exception();
	            	}
	            	
	            	String message = new String(
	            			Base64.decode(uri.getQueryParameter("d").replace("~","="), Base64.URL_SAFE)
	            		);
	            	String cyphertoken = "t" + uri.getQueryParameter("t");
	            	 
	            	Intent payIntent = new Intent(getApplicationContext(), ActivityPay.class);
	            	payIntent.putExtra(ActivityPay.TAG_RECIPIENT_MESSAGE, message);
	            	payIntent.putExtra(ActivityPay.TAG_RECIPIENT_ID, cyphertoken);
	            	startActivity(payIntent);
	            } catch(Exception e) {
	            	Toast.makeText(getApplicationContext(), "QR не предназначен для feeasy", 
	            			Toast.LENGTH_SHORT).show();
	            }
            	
            	/*String contents = intent.getStringExtra("SCAN_RESULT");
            	String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

            	Toast.makeText(getApplicationContext(), format + ":" + contents, Toast.LENGTH_SHORT).show();

            	String possiblePrefix[] = {"feeasy://", "http://feeasy.com/?q1=", "https://feeasy.com/?q1="
            			, "http://www.feeasy.com/?q1=", "https://www.feeasy.com/?q1="};

            	String usePrefix = null;

            	for(String prefix : possiblePrefix) {
            		if( contents.startsWith(prefix) ) {
            			usePrefix = prefix;
            		}
            	}

            	if( usePrefix==null ) {
            		//TODO: error
            	} else {
	            	String code = contents.substring(usePrefix.length());
	            	char secretKey[] = {'s','e','c','r','e','t','k','e','y'};
	
	            	try {
						PAN = new String(cryptor.decryptData(Base64.decode(code, Base64.DEFAULT), secretKey));
					} catch (CryptorException e) {
						// TODO error
					}
            	}*/
            	
                // Handle successful scan   
            } else if (resultCode == RESULT_CANCELED) {
               // Handle cancel
               Log.i("App","Scan unsuccessful");
            }
        }
    }
}
