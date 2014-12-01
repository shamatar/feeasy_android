package me.feeasy.test.payapi_access;

import me.feeasy.test.CardNumber;
import me.feeasy.test.R;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.webkit.WebView;

abstract public class PayApiBase {
	protected PayData payData;
	
	protected boolean verificationShowed = false;
	protected boolean backgroundPageShowed = false;
	
	public static int RUN_TIMEOUT = 60000;
	
	PayApiBase(PayData payData) {
		this.payData = payData;
	}
	
	public static enum Error {
		ERR_NOT_VALID        (R.string.error_not_valid),
		ERR_TIMEOUT          (R.string.error_timeout),
		ERR_PAGE_NOT_LOADED  (R.string.error_page_not_loaded),
		ERR_SERVICE_ERROR    (R.string.error_service_error),
		ERR_NOT_VERIFIED     (R.string.error_not_verified);
		
		public int descriptionResource;
		Error(int descriptionResource) {
			this.descriptionResource = descriptionResource;
		}
	}
	
	public static interface Observer {
		public void onShowVerification(WebView webView);
		public void onError(Error code, String errorTitle);
		public void onSuccess();
	}
	
	Observer observer;
	
	public void setObserver(Observer observer) {
		this.observer = observer;
	}
	
	public void process() {
		(new Handler()).postDelayed(new Runnable() {
			@Override public void run() {
				if(!verificationShowed ) {
					cancel();
					if( observer!=null ) {
						observer.onError(Error.ERR_TIMEOUT, "");
					}
				}
			}
		}, RUN_TIMEOUT);
		_process();
	}
	
	protected abstract void _process();
	public abstract void cancel();
}
