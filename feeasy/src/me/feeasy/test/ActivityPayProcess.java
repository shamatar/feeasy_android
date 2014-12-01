package me.feeasy.test;

import me.feeasy.test.payapi_access.AlphaWebEmuilation;
import me.feeasy.test.payapi_access.PayApiBase;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class ActivityPayProcess extends Activity {
	WebView webview;
	AlphaWebEmuilation apiAccess;
	
	static int EXTRA_STATUS_SUCCESS =  0;
	static int EXTRA_STATUS_ERROR   = -1;
	static int EXTRA_STATUS_CANCEL  =  1;
	
	static String EXTRA_TAG_ERROR_TEXT = "error";
	
	@Override public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		
		setContentView(R.layout.payprocess);
		webview = (WebView)findViewById(R.id.webview);
	}
	
	@Override public void onResume() {
		super.onResume();
		
		Intent intent = getIntent();
		
		apiAccess = new AlphaWebEmuilation(
				new PayApiBase.PayData(
						intent.getExtras().getString(TAG_SENDER_CARD), 
						intent.getExtras().getString(TAG_PRECIPIENT_CARD),
						intent.getExtras().getInt(TAG_CSC, 0),
						intent.getExtras().getInt(TAG_EXP_MONTH, 0), 
						intent.getExtras().getInt(TAG_EXP_YEAR, 0), 
						intent.getExtras().getInt(TAG_SUM, 0)),
				webview );
		
		apiAccess.setObserver(new AlphaWebEmuilation.Observer() {
			@Override public void onShowVerification(WebView webview) {
				webview.setVisibility(View.VISIBLE);
			}
			@Override public void onError(AlphaWebEmuilation.Error code, String errorTitle) {
				Intent resultIntent = new Intent();
				
				String errorText = getResources().getString(code.descriptionResource);
				if( errorTitle!=null && !errorTitle.equals("") ) errorText+=": "+errorTitle;
				resultIntent.putExtra(EXTRA_TAG_ERROR_TEXT, errorText);
				
				setResult(EXTRA_STATUS_ERROR, resultIntent);
				finish();
			}
			@Override public void onSuccess() {
				setResult(EXTRA_STATUS_SUCCESS);
				finish();
			}
		});
		apiAccess.process();
	}
	
	@Override public void onPause() {
		super.onPause();
		
		apiAccess.cancel();
	}
	
	@Override
	public void onBackPressed() {
		setResult(EXTRA_STATUS_CANCEL);
	    super.onBackPressed();
	}
}
