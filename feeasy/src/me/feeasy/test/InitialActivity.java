package me.feeasy.test;

import com.google.zxing.client.android.CaptureActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class InitialActivity extends Activity {
	private static final String TAG_RESTORED = "restored";
	public static final int TAG_KILL_ALL = 8888;

	@Override protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		
		if( savedState==null ||!savedState.getBoolean(TAG_RESTORED,false) ) {
	        // run QR scanner
	        Intent intent = new Intent(getApplicationContext(), CaptureActivity.class);
	        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
	        startActivityForResult(intent, 0);
		}
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
            	Intent payIntent = new Intent(getApplicationContext(), ActivityPay.class);
            	startActivity(payIntent);
            	
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
