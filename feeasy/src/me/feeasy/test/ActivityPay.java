package me.feeasy.test;

import org.cryptonode.jncryptor.AES256JNCryptor;
import org.cryptonode.jncryptor.CryptorException;
import org.cryptonode.jncryptor.InvalidHMACException;
import org.cryptonode.jncryptor.JNCryptor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class ActivityPay extends Activity {
	JNCryptor cryptor = new AES256JNCryptor();
	String PAN;
	
	CardFormView cardView;
	EditText sumView;
	
	Switch   switchView;
	CheckBox checkView;
	
	View acceptView;
	
	public void hideSoftKeyboard() {
	    InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	}
	
	@Override public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		
		/*Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);*/
        
        setContentView(R.layout.pay);
        cardView = (CardFormView)findViewById(R.id.pay_card);
        sumView  = (EditText    )findViewById(R.id.sum_holder);
        switchView = (Switch    )findViewById(R.id.accept_holder);
        checkView  = (CheckBox  )findViewById(R.id.accept_holder_check);
        
        acceptView = switchView == null ? checkView : switchView;
        
        SumValidator sumValidator = new SumValidator();
        sumValidator.bindToView(sumView);
        sumValidator.setNextView(acceptView);
        cardView.bindBefore(sumValidator);
        
        acceptView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override public void onFocusChange(View v, boolean hasFocus) {
				if( hasFocus ) {
					hideSoftKeyboard();
				}
			}
		});
        
        findViewById(R.id.payContainer).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				hideSoftKeyboard();
			}
		});
        //sumValidator.setNextView(findViewById(R.id.payContainer));
	}
	
	@Override public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) { 
            	String contents = intent.getStringExtra("SCAN_RESULT");
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
            	}

            	String code = contents.substring(usePrefix.length());
            	char secretKey[] = {'s','e','c','r','e','t','k','e','y'};

            	try {
					PAN = new String(cryptor.decryptData(Base64.decode(code, Base64.DEFAULT), secretKey));
				} catch (InvalidHMACException e) {
					// TODO error
				} catch (CryptorException e) {
					// TODO error
				}
            	
            	
                // Handle successful scan   
            } else if (resultCode == RESULT_CANCELED) {
               // Handle cancel
               Log.i("App","Scan unsuccessful");
            }
        }
    }
}
 