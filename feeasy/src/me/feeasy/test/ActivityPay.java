package me.feeasy.test;

import me.feeasy.test.cardview.ButtonValidator;
import me.feeasy.test.cardview.CardFormView;
import me.feeasy.test.cardview.SumValidator;

import org.cryptonode.jncryptor.AES256JNCryptor;

import org.cryptonode.jncryptor.CryptorException;
import org.cryptonode.jncryptor.InvalidHMACException;
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
	
	View acceptView;
	
	public void hideSoftKeyboard() {
	    InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	}
	
	@Override public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		
		//WebView webview = new WebView(this);
		//setContentView(webview);
		
		setContentView(R.layout.pay);
		
		Intent intentPayProcess = new Intent(getApplicationContext(), ActivityPayProcess.class);
		
		intentPayProcess.putExtra(ActivityPayProcess.TAG_SENDER_CARD, "4444333322221111");
		intentPayProcess.putExtra(ActivityPayProcess.TAG_PRECIPIENT_CARD, "4444333322221111");
		intentPayProcess.putExtra(ActivityPayProcess.TAG_CSC, 777);
		intentPayProcess.putExtra(ActivityPayProcess.TAG_EXP_MONTH, 9); 
		intentPayProcess.putExtra(ActivityPayProcess.TAG_EXP_YEAR, 15);
		intentPayProcess.putExtra(ActivityPayProcess.TAG_SUM, 10000);
		
		startActivityForResult(intentPayProcess, TAG_ACTIVITY_PAY);
		
		/*WebView webview = (WebView)findViewById(R.id.webview);
		
		AlphaWebEmuilation apiAccess = new AlphaWebEmuilation(
				new PayApiBase.PayData(
						new CardNumber("4444333322221111"), 
						new CardNumber("4444333322221111"), 
						271, 9, 17, 10000),
				webview );
		
		apiAccess.process();*/
//		final Handler handler = new Handler();
//
//    	webview.addJavascriptInterface(new Object() {
//    		boolean alpha = false;
//    		boolean valid;
//    		
//    		@JavascriptInterface
//    		public void validNotify(String json) {
//    			JSONArray result;
//				try {
//					result = new JSONArray(json);
//					valid = result.getBoolean(0);
//				} catch (JSONException e) {}
//    			
//    			Log.d("NavigateWebView", "Valid: " + json);
//    			
//    			handler.postAtFrontOfQueue(new Runnable() {
//					@Override public void run() {
//						if( valid ) {
//		    				webview.loadUrl("javascript:(function(){angular.element(document.querySelector('.transaction__actions')).scope().submit();})()");
//		    			}	
//					}
//				});
//    		}
//    		
//    		@JavascriptInterface
//    		public void err(String message, String text) {
//    			Log.d("NavigateWebView", "Error: " + message);
//    		}
//    		
//    		@JavascriptInterface
//    		public void alphaNotify(String json) {
//    			JSONArray result;
//				try {
//					result = new JSONArray(json);
//					alpha = result.getBoolean(0) && result.getBoolean(1);
//				} catch (JSONException e) {}
//    			
//    			Log.d("NavigateWebView", "Alpha: " + json);
//    			
//    			handler.postAtFrontOfQueue(new Runnable() {
//					@Override public void run() {
//		    			webview.loadUrl("javascript:(function(){$('#sender__amount').val('150.00')})()");
//				    	webview.loadUrl("javascript:(function(){$('#sender__amount').triggerHandler('input')})()");
//				    	
//				    	webview.loadUrl("javascript:(function(){feeasy.validNotify(JSON.stringify([angular.element(document.querySelector('.transaction__actions')).scope().transaction__form.$valid]))})()");	
//					}
//				});
//    		}
//    	}, "feeasy");
//		
//		final ValueHolder<Integer> attempt = new ValueHolder<Integer>(0);
//		webview.getSettings().setJavaScriptEnabled(true);
//		webview.setWebChromeClient(new WebChromeClient());
//		webview.setWebViewClient(new WebViewClient(){
//		    @Override
//		    public boolean shouldOverrideUrlLoading(WebView view, String url){
//		    	Log.d("NavigateWebView", "Loading: " + url);
//		    	
//		    	if( url.startsWith("javascript:") ) return false;
//		    	
//		    	webview.loadUrl(url);
//		    	
//		    	//webview.loadUrl("javascript:(function(){document.body.style.background=\"red\"})()");
//		    	Toast.makeText(getApplicationContext(),url,Toast.LENGTH_SHORT).show();
//		    	
//		    	return true;
//		    }
//		    
//		    @Override
//		    public void onPageFinished(WebView view, String url) {
//				/*String user = ((EditText) findViewById(R.id.edit_text)).getText().toString();
//				if (user.isEmpty()) {
//					user = "World";
//				}
//	            String javascript="javascript: document.getElementById('msg').innerHTML='Hello "+user+"!';";
//	            view.loadUrl(javascript);*/
//		    	
//		    	Log.d("NavigateWebView", "Finish: " + url);
//		    	if( attempt.value++>0) return;
//		    	
//		    	webview.loadUrl("javascript:(function(){s=angular.element(document.querySelector('.transaction__actions')).injector().get('Progress');m=s.message;s.message = function(t,r){feeasy.err(t,r);m(t,r)}})()");
//
//		    	webview.loadUrl("javascript:(function(){$('#sender__card_number').val('4444333322221111')})()");
//		    	webview.loadUrl("javascript:(function(){$('#sender__card_number').triggerHandler('blur')})()");
//		    	
//		    	webview.loadUrl("javascript:(function(){$('#recipient__card_number').val('4279010011528366')})()");
//		    	webview.loadUrl("javascript:(function(){$('#recipient__card_number').triggerHandler('blur')})()");
//		    	
//		    	webview.loadUrl("javascript:(function(){feeasy.alphaNotify(JSON.stringify([angular.element(document.querySelector('.transaction__actions')).scope().transaction.recipient__card_number_alfa,angular.element(document.querySelector('.transaction__actions')).scope().transaction.sender__card_number_alfa]))})()");
//		    	
//		    	webview.loadUrl("javascript:(function(){$('#sender__card_expiration').val('09/15')})()");
//		    	webview.loadUrl("javascript:(function(){$('#sender__card_expiration').triggerHandler('blur')})()");
//		    	
//		    	webview.loadUrl("javascript:(function(){$('#sender__card_cvv').val('888')})()");
//		    	webview.loadUrl("javascript:(function(){$('#sender__card_cvv').triggerHandler('input')})()");
//		    	
//		    	webview.loadUrl("javascript:(function(){angular.element(document.querySelector('.transaction__actions')).scope().transaction__form.sender__accept.$valid;})()");
//		    	
//		    	webview.loadUrl("javascript:(function(){$('#sender__accept').click()})()");
//		    	
//		    	//webview.loadUrl("javascript:(function(){angular.element(document.querySelector('.transaction__actions')).scope().submit();})()");
//	        }
//		});
//		Log.d("NavigateWebView", "Load");
//		webview.loadUrl("https://alfabank.ru/retail/cardtocard/alfaperevod/#/");
		
		//String summary = "<html><body>You scored <b>192</b> points.</body></html>";
		//webview.loadData(summary, "text/html", null);
		
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
        
        ButtonValidator buttonValidator = new ButtonValidator();
        cardView.addValidators(buttonValidator);
        buttonValidator.addValidator(sumValidator.validator);
        
        buttonValidator.bindButton(findViewById(R.id.paybtn_commit), 
        		R.drawable.button_bg, R.drawable.button_bg_err);
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
        } else if(requestCode == TAG_ACTIVITY_PAY ) {
        	if( resultCode == ActivityPayProcess.EXTRA_STATUS_ERROR ) {
	        	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
	
			    // Setting Dialog Title
			    alertDialog.setTitle("Ошибка");
			
			    // Setting Dialog Message
			    alertDialog.setMessage(intent.getStringExtra(ActivityPayProcess.EXTRA_TAG_ERROR_TEXT));
			
			    // Setting Icon to Dialog
			    alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
			
			    // Setting OK Button
			    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int which) {
				            // Write your code here to execute after dialog closed
				            Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
			            }
			    });
			
			    // Showing Alert Message
			    alertDialog.show();
        	}
        }
    }
}
 