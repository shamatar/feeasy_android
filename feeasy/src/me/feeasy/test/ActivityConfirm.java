package me.feeasy.test;

import java.util.HashMap;

import me.feeasy.test.payapi_access.FeeasyApiSession;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityConfirm extends Activity {
	protected static final int TAG_ACTIVITY_VALIDATE = 0;
	static String EXTRA_TAG_PAY_DATA = "payData";
	PayData payData = new PayData();

	static int EXTRA_STATUS_SUCCESS =  0;
	static int EXTRA_STATUS_ERROR   = -1;
	static int EXTRA_STATUS_CANCEL  =  1;
	
	static String EXTRA_TAG_ERROR_TEXT = "error";
	
	View payContainer;
	
	static HashMap<String, Integer> bankImages = new HashMap<String, Integer>();
	static {
		bankImages.put("alfa", R.drawable.bank_alfa);
	}
	
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
		
		setContentView(R.layout.confirm);
		
		payContainer = findViewById(R.id.payContainer);
		payContainer.setVisibility(View.INVISIBLE);
		
		new FeeasyApiSession(this, payData){
			@Override protected void onSuccess() {
				payContainer.setVisibility(View.VISIBLE);
				
				ImageView payimageCard = (ImageView)findViewById(R.id.payimageCard);
				TextView  paydataPan   = (TextView) findViewById(R.id.paydataPan);
				TextView  paydataMessage = (TextView) findViewById(R.id.paydataMessage);
				TextView  paydataSum   = (TextView) findViewById(R.id.paydataSum);
				ImageView payimageBank = (ImageView)findViewById(R.id.payimageBank);
				TextView  paydataFee   = (TextView) findViewById(R.id.paydataFee);
				TextView  paydataFullSum = (TextView) findViewById(R.id.paydataFullSum);
				
				payimageCard.setImageResource(getCardType().getCardImage());
				paydataPan.setText(getCardPattern());
				paydataSum.setText(prettySum(payData.sum));
				paydataFee.setText(prettySum(getFee()));
				paydataFullSum.setText(prettySum(payData.sum + getFee()));
				paydataMessage.setText(payData.getMessage(getResources()));
				
				Integer bankImage = bankImages.get(getBank());
				if( bankImage!=null ) payimageBank.setImageDrawable(
						getResources().getDrawable(bankImage) );
			}
			@Override protected void onError(ErrType err) {
				Intent resultIntent = new Intent();
				
				//String errorText = getResources().getString(code.descriptionResource);
				//if( errorTitle!=null && !errorTitle.equals("") ) errorText+=": "+errorTitle;
				//resultIntent.putExtra(EXTRA_TAG_ERROR_TEXT, errorText);
				
				setResult(EXTRA_STATUS_ERROR, resultIntent);
				
				finish();
			}
		}.checkRequest();
		
		findViewById(R.id.paybtn_confirm).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ActivityValidate.class);
				
				Bundle payDataBundle = new Bundle();
				payData.save(payDataBundle);
				
				intent.putExtra(ActivityValidate.EXTRA_TAG_PAY_DATA, payDataBundle);
				
				startActivityForResult(intent, TAG_ACTIVITY_VALIDATE);
			}
		});
	}
	
	@SuppressLint("DefaultLocale")
	public static String prettySum(int sum) {
		return String.format("%d.%02d ₽", sum/100, sum%100);
	}
	
	@Override public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if( resultCode==InitialActivity.TAG_KILL_ALL ) {
			setResult(InitialActivity.TAG_KILL_ALL);
			finish();
			
			return;
		}
	}
}
