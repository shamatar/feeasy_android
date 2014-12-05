package me.feeasy.test;

import com.caverock.androidsvg.SVGImageView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ActivityResult extends Activity {
	static final String TAG_RESULT = "result";
	static final String TAG_TRANSACTION_ID = "transactionId";
	static final String TAG_ERROR = "errorText";
	
	boolean success;
	String  transactionId;
	String  error;
	
	@Override public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.result);
		
		Bundle extras = getIntent().getExtras();
		if( extras!=null ) {
			this.success = extras.getBoolean(TAG_RESULT, true);
			this.transactionId = extras.getString(TAG_TRANSACTION_ID);
			this.error         = extras.getString(TAG_ERROR);
		}
		
		SVGImageView result = (SVGImageView)findViewById(R.id.paysvgResult);
		result.setImageResource(this.success ? R.raw.icon_yes : R.raw.icon_no );
		TextView resultText = ((TextView)findViewById(R.id.paydataResult));
		resultText.setText(
				this.success ? "успешно":"ошибка" 
			);
		resultText.setTextColor(
				this.success 
					? getResources().getColor(R.color.yesColor)
					: getResources().getColor(R.color.noColor)
			);
		
		if( this.transactionId!=null &&!this.transactionId.equals("") ) {
			((TextView)findViewById(R.id.paydataCode)).setText(
					this.transactionId
				);
		} else {
			findViewById(R.id.paycaptionCode).setVisibility(View.GONE);
			findViewById(R.id.payholderCode ).setVisibility(View.GONE);
		}
		
		if( this.error!=null &&!this.error.equals("") ) {
			((TextView)findViewById(R.id.paydataError)).setText(
					this.error
				);
		} else {
			findViewById(R.id.paycaptionError).setVisibility(View.GONE);
			findViewById(R.id.payholderError ).setVisibility(View.GONE);
		}
		
		findViewById(R.id.paybtn_replay).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View arg0) {
				setResult(InitialActivity.TAG_SHOW_PAY);
				finish();
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
	
	@Override public void onBackPressed() {
		setResult(InitialActivity.TAG_KILL_ALL);
		finish();
		
		Intent intent = new Intent(getApplicationContext(),InitialActivity.class);
		startActivity(intent);
	}
}

