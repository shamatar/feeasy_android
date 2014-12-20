package me.feeasy.test;

import me.feeasy.test.cardview.SavedCard;
import me.feeasy.test.payapi_access.FeeasyApiSession;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class ActivityValidate extends Activity {
	static String EXTRA_TAG_PAY_DATA = "payData";
	//static String EXTRA_TAG_PAY_FEE = "payFee";
	static String EXTRA_TAG_NO_SAVE = "noSave";
	static String EXTRA_TAG_API_ID = "apiId";
	static String EXTRA_TAG_SENDER_CARD_TYPE = "senderType";
	static String EXTRA_TAG_SENDER_CARD_NAME = "senderName";
	
	static int EXTRA_STATUS_SUCCESS =  0;
	static int EXTRA_STATUS_ERROR   = -1;
	static int EXTRA_STATUS_CANCEL  =  1;
	
	PayData payData = new PayData();
	WebView webview;
	
	String validateUrl = null;
	FeeasyApiSession session;

	//private boolean payFee = false;
	private boolean noSave = true;
	private String apiId = "";
	
	private CardType senderCardType;
	private String   senderCardName;
	
	private String errorText = null;
	
	@Override public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		
		Bundle extras = getIntent().getExtras();
		Bundle payDataBundle = null;
		
		senderCardType = CardType.UNKNOWN_CARD;
		senderCardName = "";
		
		if( extras!=null ) {
			payDataBundle = extras.getBundle(EXTRA_TAG_PAY_DATA);
			//payFee = extras.getBoolean(EXTRA_TAG_PAY_FEE);
			noSave = extras.getBoolean(EXTRA_TAG_NO_SAVE);
			apiId  = extras.getString(EXTRA_TAG_API_ID);
			senderCardType = CardType.getById(extras.getString(EXTRA_TAG_SENDER_CARD_TYPE));
			senderCardName = extras.getString(EXTRA_TAG_SENDER_CARD_NAME);
		}
		
		if( payDataBundle!=null )
			payData.load(payDataBundle);
		else {
			finish(); //TODO: error
			return;
		}
		
		setContentView(R.layout.validate);
		webview = (WebView)findViewById(R.id.webview);
		
		session = new FeeasyApiSession(this, payData){
			@Override protected void onVerificationComplete(String transactionId) {
				//Intent resultIntent = resultIntent();
				
				//resultIntent.putExtra(ActivityResult.TAG_RESULT, true);
				//resultIntent.putExtra(ActivityResult.TAG_TRANSACTION_ID, transactionId);
				
				//setResult(EXTRA_STATUS_ERROR, resultIntent);
				
				showResult(getCypherToken(),transactionId, true);
				
				//startActivityForResult(resultIntent, 0);
				
				if(!payData.senderIdentifyedByToken() && !noSave ) {
					SavedCard.saveNew(payData, getCypherToken());
				}
				
				//finish();
			}
			@Override protected void onError(ErrType err, String errMessage) {
				/*if(!payData.senderIdentifyedByToken() && !noSave ) {
					SavedCard.saveNew(payData, getCypherToken());
				}*/
				
				if( err==ErrType.ERR_Canceled ) {
					finish();
				} else {
					//Intent resultIntent = resultIntent();
					
					//resultIntent.putExtra(ActivityResult.TAG_RESULT, false);
					//transactionId="CC9357845786";
					//resultIntent.putExtra(ActivityResult.TAG_ERROR, errMessage);
					//if( transactionId!=null )
					//	resultIntent.putExtra(ActivityResult.TAG_TRANSACTION_ID, transactionId);
					
					ActivityValidate.this.errorText = errMessage;
					
					showResult(getCypherToken(),transactionId, false);

					//startActivityForResult(resultIntent, 0);
					//setResult(EXTRA_STATUS_ERROR, resultIntent);
				}
				
				//startActivity(resultIntent);
				
				//finish();
			}
		};
		session.transferRequest(webview, apiId);
	}
	

	private void showResult(String cypherToken, String transactionId, boolean success) {
		payData.senderCard = cypherToken;
		if( noSave || payData.senderCard==null ) payData.senderCard = "";
		if( transactionId==null ) transactionId="";
		if( errorText==null ) errorText="";
		
		Intent initialIntent = new Intent(this, InitialActivity.class);
		initialIntent.putExtra(InitialActivity.TAG_NO_SCAN, true);
		
		HistoryElem histElem = new HistoryElem(payData,senderCardType,senderCardName,transactionId, errorText, success);
		histElem.addAndSave();
		
		Bundle histBundle = new Bundle();
		histElem.save(histBundle);
		
		Intent intent = resultIntent();
		intent.putExtra(ActivityResult.TAG_HIST_ELEM, histBundle);
		
		finishActivity(InitialActivity.TAG_KILL_ALL);

		startActivity(initialIntent);
		startActivity(intent);
	}
	
	protected Intent resultIntent() {
		Intent intent = new Intent(getApplicationContext(), ActivityResult.class);
		
		//Bundle payDataBundle = new Bundle();
		//payData.save(payDataBundle);
		
		//intent.putExtra(ActivityValidate.EXTRA_TAG_PAY_DATA, payDataBundle);
		
		return intent;
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if( resultCode==InitialActivity.TAG_KILL_ALL ||
			resultCode==InitialActivity.TAG_SHOW_PAY ) {
			setResult(resultCode);
			finish();
			
			return;
		}
	}
	
	@Override public void onBackPressed() {
		if( session!=null ) session.verificationCancel();
		super.onBackPressed();
	}
	
	/*void showWebView() {
		webview.
	}*/
}
