package me.feeasy.test;

import me.feeasy.test.cardview.ButtonValidator;
import me.feeasy.test.cardview.CardFormView;
import me.feeasy.test.cardview.SumValidator;
import me.feeasy.test.cardview.CompoundButtonValidator;

import org.cryptonode.jncryptor.AES256JNCryptor;

import org.cryptonode.jncryptor.CryptorException;
import org.cryptonode.jncryptor.JNCryptor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

class ValueHolder<T> {
	public T value;
	
	ValueHolder() {}
	ValueHolder(T value) { this.value = value;}
}

public class ActivityPay extends Activity {
	private static final int TAG_ACTIVITY_PAY = 12300;
	
	JNCryptor cryptor = new AES256JNCryptor();
	String PAN;
	
	CardFormView cardView;
	EditText sumView;
	
	Switch   switchView;
	CheckBox checkView;
	
	CompoundButton acceptView;
	
	CardNumber recipientCard = new CardNumber();
	
	public void hideSoftKeyboard() {
		View focus = getCurrentFocus();
		
		if( focus!=null ) {
		    InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
		    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}
	
	@Override public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		
		setContentView(R.layout.pay);
		
        cardView = (CardFormView)findViewById(R.id.pay_card);
        sumView  = (EditText    )findViewById(R.id.sum_holder);
        switchView = (Switch    )findViewById(R.id.accept_holder);
        checkView  = (CheckBox  )findViewById(R.id.accept_holder_check);
        
        acceptView = switchView == null ? checkView : switchView;

		//set up fields validators for detect and display input errors
        final SumValidator sumValidator = new SumValidator();
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
        
        // set validity conditions for "Pay" button
        ButtonValidator buttonValidator = new ButtonValidator();
        cardView.addValidators(buttonValidator);
        buttonValidator.addValidator(sumValidator.validator);
        
        buttonValidator.addValidator(new CompoundButtonValidator(
        	acceptView, true, getResources().getString(R.string.error_accept)));
        
        buttonValidator.bindButton(findViewById(R.id.paybtn_commit), 
        		R.drawable.button_bg, R.drawable.button_bg_err);
        
        // set action for button if it is valid
        buttonValidator.setAction(new Runnable() {
			@Override public void run() {
				Intent intentPayProcess = new Intent(getApplicationContext(), ActivityPayProcess.class);
				
				intentPayProcess.putExtra(ActivityPayProcess.TAG_SENDER_CARD, cardView.getPEN());
				intentPayProcess.putExtra(ActivityPayProcess.TAG_PRECIPIENT_CARD, recipientCard.getString());
				intentPayProcess.putExtra(ActivityPayProcess.TAG_CSC, cardView.getCSC());
				intentPayProcess.putExtra(ActivityPayProcess.TAG_EXP_MONTH, cardView.getMonth()); 
				intentPayProcess.putExtra(ActivityPayProcess.TAG_EXP_YEAR, cardView.getYear());
				intentPayProcess.putExtra(ActivityPayProcess.TAG_SUM, sumValidator.getCents());
				
				startActivityForResult(intentPayProcess, TAG_ACTIVITY_PAY);
			}
		});
        
        // run QR scanner
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
	}
	
	@Override public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
        	recipientCard.set("5486742777221135");
        	
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
            	} else {
	            	String code = contents.substring(usePrefix.length());
	            	char secretKey[] = {'s','e','c','r','e','t','k','e','y'};
	
	            	try {
						PAN = new String(cryptor.decryptData(Base64.decode(code, Base64.DEFAULT), secretKey));
					} catch (CryptorException e) {
						// TODO error
					}
            	}
            	
                // Handle successful scan   
            } else if (resultCode == RESULT_CANCELED) {
               // Handle cancel
               Log.i("App","Scan unsuccessful");
            }
        } else if(requestCode == TAG_ACTIVITY_PAY ) {
        	if( resultCode == ActivityPayProcess.EXTRA_STATUS_ERROR ) {
        		showErrorDialog(intent.getStringExtra(ActivityPayProcess.EXTRA_TAG_ERROR_TEXT));
        	}
        }
    }

	private void showErrorDialog(String stringExtra) {
    	AlertDialog alertDialog = new AlertDialog.Builder(this).create();

	    // Setting Dialog Title
	    alertDialog.setTitle("Ошибка");
	
	    // Setting Dialog Message
	    alertDialog.setMessage(stringExtra);
	
	    // Setting Icon to Dialog
	    alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
	
	    // Setting OK Button
	    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
		            // Write your code here to execute after dialog closed
		            //Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
	            }
	    });
	
	    // Showing Alert Message
	    alertDialog.show();
	}
}
 