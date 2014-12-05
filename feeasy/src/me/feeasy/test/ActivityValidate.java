package me.feeasy.test;

import me.feeasy.test.payapi_access.FeeasyApiSession;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class ActivityValidate extends Activity {
	static String EXTRA_TAG_PAY_DATA = "payData";
	
	static int EXTRA_STATUS_SUCCESS =  0;
	static int EXTRA_STATUS_ERROR   = -1;
	static int EXTRA_STATUS_CANCEL  =  1;
	
	PayData payData = new PayData();
	WebView webview;
	
	String validateUrl = null;
	FeeasyApiSession session;
	
	@Override public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		
		Bundle extras = getIntent().getExtras();
		Bundle payDataBundle = null;
		
		if( extras!=null ) 
			payDataBundle = extras.getBundle(EXTRA_TAG_PAY_DATA);
		
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
				Intent resultIntent = resultIntent();
				
				resultIntent.putExtra(ActivityResult.TAG_RESULT, true);
				resultIntent.putExtra(ActivityResult.TAG_TRANSACTION_ID, transactionId);
				
				//setResult(EXTRA_STATUS_ERROR, resultIntent);
				
				startActivityForResult(resultIntent, 0);
				
				//finish();
			}
			@Override protected void onError(ErrType err, String errMessage) {
				if( err==ErrType.ERR_Canceled ) {
					finish();
				} else {
					Intent resultIntent = resultIntent();
					
					resultIntent.putExtra(ActivityResult.TAG_RESULT, false);
					resultIntent.putExtra(ActivityResult.TAG_ERROR, errMessage);
					if( transactionId!=null )
						resultIntent.putExtra(ActivityResult.TAG_TRANSACTION_ID, transactionId);

					startActivityForResult(resultIntent, 0);
					//setResult(EXTRA_STATUS_ERROR, resultIntent);
				}
				
				//startActivity(resultIntent);
				
				//finish();
			}
		};
		session.transferRequest(webview);
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
