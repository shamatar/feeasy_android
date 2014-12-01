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
		
		new FeeasyApiSession(this, payData){
			@Override protected void onError(ErrType err) {
				Intent resultIntent = new Intent();
				
				setResult(EXTRA_STATUS_ERROR, resultIntent);
				
				finish();
			}
		}.transferRequest(webview);
	}
	
	@Override public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if( resultCode==InitialActivity.TAG_KILL_ALL ) {
			setResult(InitialActivity.TAG_KILL_ALL);
			finish();
			
			return;
		}
	}
	
	/*void showWebView() {
		webview.
	}*/
}
