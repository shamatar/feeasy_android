package me.feeasy.test;

import java.util.HashMap;

import me.feeasy.test.payapi_access.FeeasyApiSession;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
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
	CompoundButton switchFee;
	CompoundButton switchSave;
	String apiId = "";
	
	String cardName;
	CardType cardType;
	
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
		FeeasyApp.instance.setupActivity(this);
		
		payContainer = findViewById(R.id.payContainer);
		payContainer.setVisibility(View.INVISIBLE);
		
		new FeeasyApiSession(this, payData){
			@Override protected void onSuccess() {
				payContainer.setVisibility(View.VISIBLE);
				
				payData.message = getFullMessage();
				payData.fee = payData.payFee ? getFee2() : getFee1();
				int resSum = payData.payFee ? getSum2() : getSum1();
				
				final ImageView payimageCard = (ImageView)findViewById(R.id.payimageCard);
				final TextView  paydataPan   = (TextView) findViewById(R.id.paydataPan);
				final TextView  paydataMessage = (TextView) findViewById(R.id.paydataMessage);
				final TextView  paydataUserMessage = (TextView) findViewById(R.id.paydataUserMessage);
				final TextView  paydataSum   = (TextView) findViewById(R.id.paydataSum);
				final ImageView payimageBank = (ImageView)findViewById(R.id.payimageBank);
				final TextView  paydataFee   = (TextView) findViewById(R.id.paydataFee);
				final TextView  paydataFullSum = (TextView) findViewById(R.id.paydataFullSum);
				
				FeeasyApp.addViewRurSign(paydataSum);
				FeeasyApp.addViewRurSign(paydataFee);
				FeeasyApp.addViewRurSign(paydataFullSum);
				
				payimageCard.setImageResource(getCardType().getCardImage());
				paydataPan.setText(getCardPattern());
				paydataSum.setText(Utility.prettySum(payData.sum));
				paydataFee.setText(Utility.prettySum(payData.fee));
				paydataFullSum.setText(Utility.prettySum(resSum));
				paydataMessage.setText(getFullMessage());
				
				findViewById(R.id.payholderUserMessage).setVisibility(payData.hasUserMessage() ? View.VISIBLE : View.GONE);
				findViewById(R.id.paycaptionUserMessage).setVisibility(payData.hasUserMessage() ? View.VISIBLE : View.GONE);
				if( payData.hasUserMessage() ) paydataUserMessage.setText(payData.userMessage);
				
				cardName = getCardPattern();
				if( cardName.length()>4 ) cardName = cardName.substring(cardName.length() - 4);
				cardType = getCardType();
				
				apiId = getApiId();
				
				final View paycheckFee  = findViewById(R.id.paycheckFee );
				
				switchFee = (CompoundButton)paycheckFee.findViewById(R.id.accept_holder);
				switchFee.setChecked(payData.payFee);
				
				if( getFee1()==0 ) paycheckFee.setVisibility(View.GONE);
				else {
					switchFee.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							payData.payFee = isChecked;
							payData.fee = isChecked ? getFee2() : getFee1();
							
							paydataFee.setText(Utility.prettySum(payData.fee));
							paydataFullSum.setText(Utility.prettySum(isChecked ? getSum2() : getSum1()));
							
							//hightlight sum widget
							
							int colorFrom = 0x00FFFFFF;
							int colorTo = 0x80FFFFFF;
							
							ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
							colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
							    @Override public void onAnimationUpdate(ValueAnimator animator) {
							    	paydataFullSum.setBackgroundColor((Integer)animator.getAnimatedValue());
							    }

							});
							colorAnimation.setDuration(100);
							colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
							colorAnimation.setRepeatCount(1);
							colorAnimation.start();
						}
					});
				}

				final View paycheckSave = findViewById(R.id.paycheckSave);
				switchSave = (CompoundButton)paycheckSave.findViewById(R.id.accept_holder);
				
				if( payData.senderIdentifyedByToken() ) {
					paycheckSave.setVisibility(View.GONE);
				}
				
				Integer bankImage = bankImages.get(getBank());
				if( bankImage!=null ) payimageBank.setImageDrawable(
						getResources().getDrawable(bankImage) );
			}
			@Override protected void onError(ErrType err, String message) {
				Intent resultIntent = new Intent();
				
				//String errorText = getResources().getString(code.descriptionResource);
				//if( errorTitle!=null && !errorTitle.equals("") ) errorText+=": "+errorTitle;
				
				setResult(EXTRA_STATUS_ERROR, resultIntent);
				resultIntent.putExtra(EXTRA_TAG_ERROR_TEXT, message);
				
				finish();
			}
		}.checkRequest();
		
		findViewById(R.id.paybtn_confirm).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ActivityValidate.class);
				
				Bundle payDataBundle = new Bundle();
				payData.save(payDataBundle);
				
				intent.putExtra(ActivityValidate.EXTRA_TAG_PAY_DATA, payDataBundle);
				//intent.putExtra(ActivityValidate.EXTRA_TAG_PAY_FEE, switchFee!=null ? switchFee.isChecked() : false);
				intent.putExtra(ActivityValidate.EXTRA_TAG_NO_SAVE, switchSave!=null ? switchSave.isChecked() : false);
				intent.putExtra(ActivityValidate.EXTRA_TAG_API_ID, apiId);
				intent.putExtra(ActivityValidate.EXTRA_TAG_SENDER_CARD_NAME, cardName);
				intent.putExtra(ActivityValidate.EXTRA_TAG_SENDER_CARD_TYPE, cardType.name());
				
				startActivityForResult(intent, TAG_ACTIVITY_VALIDATE);
			}
		});
	}
	
	@Override public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if( resultCode==InitialActivity.TAG_KILL_ALL ||
			resultCode==InitialActivity.TAG_SHOW_PAY ) {
			setResult(resultCode);
			finish();
			
			return;
		}
	}
}
