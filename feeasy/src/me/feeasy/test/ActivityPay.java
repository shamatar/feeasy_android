package me.feeasy.test;

import me.feeasy.test.cardview.ButtonValidator;
import me.feeasy.test.cardview.CardFormView;
import me.feeasy.test.cardview.SumValidator;
import me.feeasy.test.cardview.CompoundButtonValidator;

import org.cryptonode.jncryptor.AES256JNCryptor;

import org.cryptonode.jncryptor.JNCryptor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

class ValueHolder<T> {
	public T value;
	
	ValueHolder() {}
	ValueHolder(T value) { this.value = value;}
}

public class ActivityPay extends FragmentActivity {
	public static final int TAG_ACTIVITY_PAY = 12300;

	//public static final String TAG_RECIPIENT_ID = "recipient_id";
	//public static final String TAG_RECIPIENT_MESSAGE = "recipient_message";
	public static final String TAG_URI = "uri";
	
	JNCryptor cryptor = new AES256JNCryptor();
	String PAN;
	
	CardFormView cardView;
	EditText sumView;
	
	Switch   switchView;
	CheckBox checkView;
	
	CompoundButton acceptView;
	
	String recipientId;
	String recipientMessage;
	
	public void hideSoftKeyboard() {
		View focus = getCurrentFocus();
		
		if( focus!=null ) {
		    InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
		    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}
	
	@Override public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		
		Bundle extras = getIntent().getExtras();
		FeeasyURI uri = null;
		
		if( extras!=null && extras.getString(TAG_URI)!=null) {
			uri = new FeeasyURI(extras.getString(TAG_URI));
		}
		if( getIntent().getAction() == Intent.ACTION_VIEW ) {
			uri = new FeeasyURI(getIntent().getData().toString());
		}
		if( uri!=null ) {
			if(!uri.error ) {
				recipientId = uri.cyphertoken;// extras.getString(TAG_RECIPIENT_ID);
				recipientMessage = uri.message;//extras.getString(TAG_RECIPIENT_MESSAGE);
			} else {
				Toast.makeText(getApplicationContext(), "Этот URL не может быть обработан", Toast.LENGTH_SHORT).show();
			}
		}
		
		//TODO: uncomment
		if( recipientId==null ) {
			setResult(InitialActivity.TAG_KILL_ALL);
			finish();
			Intent intent = new Intent(this, InitialActivity.class);
			startActivity(intent);
			return;
		}
		
		setContentView(R.layout.pay);
		
        cardView = (CardFormView)findViewById(R.id.pay_card);
        sumView  = (EditText    )findViewById(R.id.sum_holder);
        switchView = (Switch    )findViewById(R.id.accept_holder);
        checkView  = (CheckBox  )findViewById(R.id.accept_holder_check);
        
        FeeasyApp.addViewRurSign(sumView);
        
        if( recipientMessage!=null &&!recipientMessage.equals("")) { 
        	((TextView)findViewById(R.id.textMessage)).setText(Html.fromHtml("<font color=#A0A0A0>Сообщение получателя:</font> " + TextUtils.htmlEncode(recipientMessage)));
        }
        
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
				Intent intentPayProcess = new Intent(getApplicationContext(), ActivityConfirm.class);
				
				PayData payData = new PayData(
						cardView.getPEN(), 
						recipientId, 
						cardView.getCSC(), 
						cardView.getMonth(), 
						cardView.getYear(), 
						sumValidator.getCents(),
						recipientMessage
					);
				
				Bundle payDataBundle = new Bundle();
				payData.save(payDataBundle);
				
				intentPayProcess.putExtra(ActivityConfirm.EXTRA_TAG_PAY_DATA, payDataBundle);
				
				startActivityForResult(intentPayProcess, TAG_ACTIVITY_PAY);
			}
		});
        
	}
	
	@Override public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if( resultCode==InitialActivity.TAG_KILL_ALL ) {
			setResult(InitialActivity.TAG_KILL_ALL);
			finish();
			
			return;
		}
		
        if( requestCode == TAG_ACTIVITY_PAY ) {
        	if( resultCode == ActivityConfirm.EXTRA_STATUS_ERROR ) {
        		showErrorDialog(intent.getStringExtra(ActivityConfirm.EXTRA_TAG_ERROR_TEXT));
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
 