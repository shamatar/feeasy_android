package me.feeasy.test.payapi_access;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AlphaWebEmuilation extends PayApiBase{
	private WebView webview;
	private boolean canceled = false;

	private boolean alpha = false;
	private boolean valid;
	
	public AlphaWebEmuilation(PayData payData, WebView webview) {
		super(payData);
		this.webview = webview;
	}
	
	@SuppressLint("DefaultLocale")
	private String formatSum(int sum) {
		if(!alpha ) sum -= 3000;
		
		return String.format("%d.%02d", sum / 100, sum%100);
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override protected void _process() {
		final Handler handler = new Handler();

    	webview.addJavascriptInterface(new Object() {
    		@JavascriptInterface
    		public void validNotify(String json) {
    			JSONArray result;
				try {
					result = new JSONArray(json);
					valid = result.getBoolean(0);
				} catch (JSONException e) {}
    			
    			Log.d("NavigateWebView", "Valid: " + json);
    			
    			handler.postAtFrontOfQueue(new Runnable() {
					@Override public void run() {
						if( canceled ) return;
						if( valid ) {
		    				webview.loadUrl("javascript:(function(){angular.element(document.querySelector('.transaction__actions')).scope().submit();})()");
		    			} else {
		    				cancel();
							observer.onError(Error.ERR_NOT_VALID, "");
		    			}
					}
				});
    		}
    		
    		@JavascriptInterface
    		public void err(final String message, final String text) {
    			Log.d("NavigateWebView", "Error: " + message);
    			
    			handler.postAtFrontOfQueue(new Runnable() {
					@Override public void run() {
		    			if( observer!=null ) {
		    				cancel();
		    				observer.onError(Error.ERR_SERVICE_ERROR, message);
		    			}
					}
				});
    		}
    		
    		@JavascriptInterface
    		public void alphaNotify(String json) {
    			JSONArray result;
				try {
					result = new JSONArray(json);
					alpha = result.getBoolean(0) && result.getBoolean(1);
				} catch (JSONException e) {}
    			
    			Log.d("NavigateWebView", "Alpha: " + json);
    			
    			handler.postAtFrontOfQueue(new Runnable() {
					@Override public void run() {
						if( canceled ) return;
						
		    			webview.loadUrl("javascript:(function(){$('#sender__amount').val('" + formatSum(payData.sum) + "')})()");
				    	webview.loadUrl("javascript:(function(){$('#sender__amount').triggerHandler('input')})()");
				    	
				    	webview.loadUrl("javascript:(function(){feeasy.validNotify(JSON.stringify([angular.element(document.querySelector('.transaction__actions')).scope().transaction__form.$valid]))})()");	
					}
				});
    		}
    	}, "feeasy");
		
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebChromeClient(new WebChromeClient());
		webview.setWebViewClient(new WebViewClient(){
		    @Override
		    public boolean shouldOverrideUrlLoading(WebView view, String url){
		    	Log.d("NavigateWebView", "Loading: " + url);
		    	
		    	if( url.startsWith("javascript:") ) return false;
		    	
		    	URL urlObj;
		    	try {
					urlObj = new URL(url);
				} catch (MalformedURLException e) {
					cancel();
	    			Log.d("NavigateWebView", "Malformed URL: " + url);
					if( observer!=null ) {
						observer.onError(Error.ERR_PAGE_NOT_LOADED, "");
					}
					return true;
				}
		    	
		    	if( verificationShowed && urlObj.getHost().equals("alfabank.ru") ) {
		    		String query = urlObj.getQuery();
		    		boolean error = true;
		    		if( query!=null ) {
		    			Uri uri=Uri.parse(query);
		    			String errorParam = uri.getQueryParameter("error");
		    			error = (errorParam == null) || errorParam.equals("true");  
		    		}
		    		
		    		Log.d("NavigateWebView", "Vefied. Error: " + error);
		    		
		    		cancel();
		    		if( observer!=null ) {
		    			if( error ) {
		    				observer.onError(Error.ERR_NOT_VERIFIED, "");
		    			} else {
		    				observer.onSuccess();
		    			}
		    		}
		    		
		    		return true;
		    	}
		    	
		    	webview.loadUrl(url);
		    	
		    	//webview.loadUrl("javascript:(function(){document.body.style.background=\"red\"})()");
		    	//Toast.makeText(getApplicationContext(),url,Toast.LENGTH_SHORT).show();
		    	
		    	return true;
		    }

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				cancel();
				Log.d("NavigateWebView", "Loading faul: " + description + ", on url " + failingUrl);
				if( observer!=null ) {
					observer.onError(Error.ERR_PAGE_NOT_LOADED, description);
				}
		    }
		    
		    @Override
		    public void onPageFinished(WebView view, String url) {
		    	Log.d("NavigateWebView", "Finish: " + url);
		    	if( canceled ) return;
		    	
		    	URL urlObj;
		    	try {
					urlObj = new URL(url);
				} catch (MalformedURLException e) {
					return;
				}
		    	
		    	if( backgroundPageShowed ) {
		    		if(!verificationShowed &&!urlObj.getHost().equals("alfabank.ru") ) {
			    		if( observer!=null ) {
			    			observer.onShowVerification(view);
			    		}
			    		verificationShowed = true;
			    	}
		    		return;
		    	}
		    	backgroundPageShowed = true;
		    	
		    	webview.loadUrl("javascript:(function(){s=angular.element(document.querySelector('.transaction__actions')).injector().get('Progress');m=s.message;s.message = function(t,r){feeasy.err(t,r);m(t,r)}})()");

		    	webview.loadUrl("javascript:(function(){$('#sender__card_number').val('" + payData.senderCard + "')})()");
		    	webview.loadUrl("javascript:(function(){$('#sender__card_number').triggerHandler('blur')})()");
		    	
		    	webview.loadUrl("javascript:(function(){$('#recipient__card_number').val('" + payData.recipientCard + "')})()");
		    	webview.loadUrl("javascript:(function(){$('#recipient__card_number').triggerHandler('blur')})()");
		    	
		    	webview.loadUrl("javascript:(function(){feeasy.alphaNotify(JSON.stringify([angular.element(document.querySelector('.transaction__actions')).scope().transaction.recipient__card_number_alfa,angular.element(document.querySelector('.transaction__actions')).scope().transaction.sender__card_number_alfa]))})()");
		    	
		    	webview.loadUrl("javascript:(function(){$('#sender__card_expiration').val('" + payData.formatExpDate() + "')})()");
		    	webview.loadUrl("javascript:(function(){$('#sender__card_expiration').triggerHandler('blur')})()");
		    	
		    	webview.loadUrl("javascript:(function(){$('#sender__card_cvv').val(" + payData.formatCVC() + ")})()");
		    	webview.loadUrl("javascript:(function(){$('#sender__card_cvv').triggerHandler('input')})()");
		    	
		    	webview.loadUrl("javascript:(function(){angular.element(document.querySelector('.transaction__actions')).scope().transaction__form.sender__accept.$valid;})()");
		    	
		    	webview.loadUrl("javascript:(function(){$('#sender__accept').click()})()");
		    	
		    	//webview.loadUrl("javascript:(function(){angular.element(document.querySelector('.transaction__actions')).scope().submit();})()");
	        }
		});
		Log.d("NavigateWebView", "Load");
		webview.loadUrl("https://alfabank.ru/retail/cardtocard/alfaperevod/#/");
	}

	@Override public void cancel() {
		canceled = true;
		webview.loadUrl("about:blank");
	}

}
