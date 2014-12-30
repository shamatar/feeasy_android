package me.feeasy.test;

import com.caverock.androidsvg.SVGImageView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityResult extends Activity {
	static final String TAG_HIST_ELEM = "helem";
	//static final String TAG_TRANSACTION_ID = "transactionId";
	//static final String TAG_ERROR = "errorText";
	
	//boolean success;
	//String  transactionId;
	//String  error;
	
	HistoryElem historyElem;
	
	@Override public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.result);
		FeeasyApp.instance.setupActivity(this);
		
		historyElem = new HistoryElem();
		
		Bundle extras = getIntent().getExtras();
		if( extras!=null ) {
			historyElem.load(extras.getBundle(TAG_HIST_ELEM));
		}
		
		SVGImageView result = (SVGImageView)findViewById(R.id.paysvgResult);
		result.setImageResource(historyElem.success ? R.raw.icon_yes : R.raw.icon_no );
		TextView resultText = ((TextView)findViewById(R.id.paydataResult));
		resultText.setText(
				historyElem.success ? "успешно":"ошибка" 
			);
		resultText.setTextColor(
				historyElem.success 
					? getResources().getColor(R.color.yesColor)
					: getResources().getColor(R.color.noColor)
			);
		
		if( historyElem.transactionId!=null &&!historyElem.transactionId.equals("") ) {
			((TextView)findViewById(R.id.resdataTransaction)).setText(
					"Транзакция № " + historyElem.transactionId
				);
		} else {
			//findViewById(R.id.paycaptionCode).setVisibility(View.GONE);
			findViewById(R.id.resdataTransaction).setVisibility(View.GONE);
		}
		
		if( historyElem.error!=null &&!historyElem.error.equals("") ) {
			((TextView)findViewById(R.id.resdataErr)).setText(
					historyElem.error
				);
		} else {
			findViewById(R.id.resholderErr).setVisibility(View.GONE);
			//findViewById(R.id.payholderError ).setVisibility(View.GONE);
		}
		
		((TextView)findViewById(R.id.resdataCard)).setText(
				"*" + historyElem.senderCardName
			);
		
		((ImageView)findViewById(R.id.resdataCardImage)).setImageResource(
				historyElem.senderCardType.getCardImage()
			);
		
		((TextView)findViewById(R.id.resdataDate)).setText(
				historyElem.formattedDate()
			);
		
		((TextView)findViewById(R.id.resdataSum)).setText(
				Utility.prettySum(historyElem.payData.sum)
			);
		
		FeeasyApp.addViewRurSign((TextView)findViewById(R.id.resdataSum));
		
		String feeText = Utility.prettySum(historyElem.payData.fee);
		if( historyElem.payData.fee!=0 && historyElem.payData.payFee )
			feeText += ", оплачена";
		
		((TextView)findViewById(R.id.resdataFee)).setText(
				feeText
			);
		
		FeeasyApp.addViewRurSign((TextView)findViewById(R.id.resdataFee));
		
		TextView userMessageView = (TextView)findViewById(R.id.resdataUserMessage);
		userMessageView.setVisibility(historyElem.payData.hasUserMessage() ? View.VISIBLE : View.GONE);
		if( historyElem.payData.hasUserMessage() ) {
			userMessageView.setText(
					Html.fromHtml("<b>Ваше сообщение:</b> " + 
						TextUtils.htmlEncode(historyElem.payData.userMessage))
				);
		}
		
		((TextView)findViewById(R.id.resdataMessage)).setText(
				historyElem.payData.message
			);
		
		findViewById(R.id.paybtn_replay).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View arg0) {
				Bundle histBundle = new Bundle();
				historyElem.save(histBundle);
				
				Intent intent = new Intent(getApplicationContext(), ActivityPay.class);
				intent.putExtra(ActivityPay.TAG_HISTORY, histBundle);
				
				startActivity(intent);
				//setResult(InitialActivity.TAG_SHOW_PAY);
				//finish();
			}
		});
		
		findViewById(R.id.paybtn_continue).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View arg0) {
				setResult(InitialActivity.TAG_KILL_ALL);
				finish();
				
				Intent intent = new Intent(getApplicationContext(),InitialActivity.class);
				startActivity(intent);
			}
		});
	}
	
	/*@Override public void onBackPressed() {
		setResult(InitialActivity.TAG_KILL_ALL);
		finish();
		
		Intent intent = new Intent(getApplicationContext(),InitialActivity.class);
		startActivity(intent);
	}*/
}

