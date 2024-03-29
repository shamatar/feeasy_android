package me.feeasy.test.payapi_access;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import me.feeasy.test.CardType;
import me.feeasy.test.PayData;
import me.feeasy.test.R;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class FeeasyApiSession {
	private static final int RETRYPOLICY_TIMEOUT_CHECK = 15000;
	private static final int RETRYPOLICY_TIMEOUT_TRANSFER = 35000;
	private static final int RETRYPOLICY_ATTEMPTS = 2;
	
	final String apiUrl = "https://feeasy.me/payapi";
	//final String apiUrl = "http://37.252.124.233:5000/payapi";
	//final String apiUrl = "http://192.168.157.15:5000/payapi";
	
	Context context;
	RequestQueue queue;
	PayData payData;
	
	private int fee1 = 0, fee2 = 0, sum1 = 0, sum2 = 0;
	private String bank = null;
	private String api_id = null;
	private String fullMessage;
	private String cardPattern = null;
	private String cypherToken = null;
	private CardType cardType = CardType.UNKNOWN_CARD;
	
	private boolean verificationCanceled = false;
	private boolean verificationShowed = false;
	
	protected String transactionId;
	
	public enum ErrType {
		ERR_Network,
		ERR_Data, 
		ERR_Verification, 
		ERR_Canceled,
	}
	
	protected void onSuccess() {}
	protected void onRequestComplete() {}
	protected void onError(ErrType err, String reason) {}
	protected void onVerificationComplete(String transactionId) {}
	
	public int getFee1() { return fee1; }
	public int getFee2() { return fee2; }
	public int getSum1() { return sum1; }
	public int getSum2() { return sum2; }
	
	public String getBank() { return bank; }
	public String getApiId() { return api_id; }
	public String getCardPattern() { return cardPattern; }
	public CardType getCardType() { return cardType; }
	public String getFullMessage() { return fullMessage; }
	
	public String getCypherToken() { return cypherToken; }
	
	public FeeasyApiSession(Context context, PayData payData) {
		this.context = context;
		this.payData = payData;
	}
	
	private void onCheckSuccess(String response) {
    	JSONObject data = null;
		try {
			data = new JSONObject(response);
	    	if(!data.getBoolean("error") ) {
	    		if( data.has("history_id") ) {
	    			payData.historyId = data.getString("history_id");
	    		}
	    		if( data.has("message") ) {
	    			payData.message = data.getString("message");
	    		}
	    		
	    		FeeasyApiSession.this.fee1  = data.getInt("fee");
	    		FeeasyApiSession.this.sum1  = data.getInt("sum");
	    		FeeasyApiSession.this.fee2  = data.getInt("fee2");
	    		FeeasyApiSession.this.sum2  = data.getInt("sum2");
	    		
	    		FeeasyApiSession.this.bank = data.getJSONObject("bank")
	    				.getString("id");
	    		FeeasyApiSession.this.api_id = data.getJSONObject("bank")
	    				.getString("api_id");
	    		FeeasyApiSession.this.fullMessage = data.getString("message");
	    		
	    		FeeasyApiSession.this.cardPattern = data.getString("sender_card");
	    		FeeasyApiSession.this.cardType = CardType.getById(data.getString("sender_card_type"));
	    		
	    		onSuccess();
	    		onRequestComplete();
	    		return;
	    	}
		} catch (JSONException e) {}
		
		onError(ErrType.ERR_Data, context.getResources().getString(R.string.err_badresponse));
		onRequestComplete();
	}
	
	public void checkRequest() {
		if( queue==null )
			queue = Volley.newRequestQueue(context);

		// Request a string response from the provided URL.
		StringRequest stringRequest = new StringRequest(Request.Method.POST, apiUrl,
		    new Response.Listener<String>() {
			    @Override
			    public void onResponse(String response) {
			    	onCheckSuccess(response);
			    }
			}, new Response.ErrorListener() {
			    @Override
			    public void onErrorResponse(VolleyError error) {
					onError(ErrType.ERR_Network, context.getResources().getString(R.string.err_network) + ": " + error.getLocalizedMessage());
					onRequestComplete();
			    }
			}
		) {
			@Override protected Map<String, String> getParams() {
		        Map<String, String> params = getPayParams();
		        params.put("method", "check");
		        return params;
		    }
		};
		
		stringRequest.setTag(FeeasyApiSession.this);
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(
				RETRYPOLICY_TIMEOUT_CHECK, 
                RETRYPOLICY_ATTEMPTS,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) );
		
		// Add the request to the RequestQueue.
		queue.add(stringRequest);
		queue.start();
	}
	
	public void transferRequest(final WebView webView, final String api_id) {
		if( queue==null )
			queue = Volley.newRequestQueue(context);

		// Request a string response from the provided URL.
		StringRequest stringRequest = new StringRequest(Request.Method.POST, apiUrl,
		    new Response.Listener<String>() {
			    @Override
			    public void onResponse(String response) {
			    	JSONObject data = null;
					try {
						data = new JSONObject(response);
				    	if(!data.getBoolean("error") ) {
				    		cypherToken = data.getString("cyphertoken");
				    		showValidationWindow(webView, data.getString("url"));
				    		return;
				    	}
					} catch (JSONException e) {}
					
					onError(ErrType.ERR_Data, context.getResources().getString(R.string.err_badresponse));
					onRequestComplete();
			    }
			}, new Response.ErrorListener() {
			    @Override
			    public void onErrorResponse(VolleyError error) {
					onError(ErrType.ERR_Network, context.getResources().getString(R.string.err_network) + ": " + error.getLocalizedMessage());
					onRequestComplete();
			    }
			}
		) {
			@Override protected Map<String, String> getParams() {
		        Map<String, String> params = getPayParams();
		        params.put("method", "transfer");
		        params.put("recipientfee", payData.payFee ? "n" : "y");
		        params.put("api_id", api_id);
		        
		        return params;
		    }
		};
		
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                RETRYPOLICY_TIMEOUT_TRANSFER, 
                RETRYPOLICY_ATTEMPTS, 
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) );
		stringRequest.setTag(FeeasyApiSession.this);
		
		// Add the request to the RequestQueue.
		queue.add(stringRequest);
		queue.start();
	}
	
	public void verificationCancel() {
		verificationCanceled = true;
		//onError(ErrType.ERR_Canceled);
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	protected void showValidationWindow(final WebView webview, String url) {
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebChromeClient(new WebChromeClient());
		webview.setWebViewClient(new WebViewClient(){
		    @Override
		    public boolean shouldOverrideUrlLoading(WebView view, String url){
		    	Log.d("NavigateWebView", "Loading: " + url);
		    	
		    	if( url.startsWith("javascript:") ) return false;
		    	
				Uri urlObj = Uri.parse(url);
				if( urlObj.getPath().endsWith("/verification-result") ) {
					String successParam = urlObj.getQueryParameter("success");
					
					boolean success=successParam==null ? false : successParam.equals("true");
					FeeasyApiSession.this.transactionId = urlObj.getQueryParameter("transactionid");
					
					if( success && transactionId!=null ) {
						onVerificationComplete(transactionId);
						onRequestComplete();
					} else {
						if(!verificationCanceled ) {
							onError(ErrType.ERR_Verification, context.getResources().getString(R.string.err_verification));
							onRequestComplete();
						}
					}
				}
		    	
		    	if(!verificationCanceled )
		    		webview.loadUrl(url);
		    	
		    	//webview.loadUrl("javascript:(function(){document.body.style.background=\"red\"})()");
		    	//Toast.makeText(getApplicationContext(),url,Toast.LENGTH_SHORT).show();
		    	
		    	return true;
		    }

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				verificationCancel();
				Log.d("NavigateWebView", "Loading fail: " + description + ", on url " + failingUrl);
				if(!verificationCanceled ) { 
					onError(ErrType.ERR_Network, context.getResources().getString(R.string.err_network) + ": " + description);
					onRequestComplete();
				}
		    }
		    
		    @Override
		    public void onPageFinished(WebView view, String url) {
		    	Log.d("NavigateWebView", "Finish: " + url);
		    	if( verificationCanceled ) return;
		    	
		    	URL urlObj, serverUrlObj;
		    	try {
					urlObj = new URL(url);
					serverUrlObj = new URL(apiUrl);
				} catch (MalformedURLException e) {
					return;
				}
		    	
		    	if(!verificationShowed && !urlObj.getHost().equals(serverUrlObj.getHost()) ) {
		    		webview.setVisibility(View.VISIBLE);
		    		verificationShowed = true;
		    	}
		    	
		    	if( verificationShowed && urlObj.getHost().equals(serverUrlObj.getHost()) ) {
		    		
		    	}
	        }
		});
		Log.d("NavigateWebView", "Load");
		webview.loadUrl(url);
	}
	protected Map<String, String> getPayParams() {
		Map<String, String> params = new HashMap<String, String>();
		
        params.put("recipient_card", payData.recipientCard);
        params.put("sender_card", payData.senderCard);
        params.put("sender_exp_year",  Integer.toString(payData.expYear));
        params.put("sender_exp_month", Integer.toString(payData.expMonth));
        params.put("sender_csc", Integer.toString(payData.cvc));
        params.put("sum", Integer.toString(payData.sum));
        
        params.put("user_id", 
        		Secure.getString(context.getContentResolver(), Secure.ANDROID_ID));
        
        if( payData.historyId!=null )
        	params.put("history_id", payData.historyId);
        
        if( payData.userMessage!=null )
        	params.put("sender_message", payData.userMessage);
        
        return params;
	}
	public void stop() {
		if( queue==null ) return;
		
		queue.cancelAll(FeeasyApiSession.this);
	}
}
