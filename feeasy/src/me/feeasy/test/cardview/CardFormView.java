package me.feeasy.test.cardview;

import me.feeasy.test.CardType;
import me.feeasy.test.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class CardFormView extends LinearLayout {
	public interface CardEntryListener{
		void onCardNumberInputComplete();
		void onEdit();
	}
	
	public CardFormView(Context context) {
		super(context);
		setup();
	}
	
	public CardFormView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup();
	}

	@SuppressLint("NewApi")
	public CardFormView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup();
	}

	void setup() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.card_form, this, true);
		
		payimageCard  = (ImageView) findViewById(R.id.payimageCard );
		
		payfieldMonth = (EditText ) findViewById(R.id.payfieldMonth);
		payfieldYear  = (EditText ) findViewById(R.id.payfieldYear );
		payfieldCV    = (EditText ) findViewById(R.id.payfieldCV   );
		payfieldPEN   = (EditText ) findViewById(R.id.payfieldPEN  );
		
		penValidator.bindToView(payfieldPEN);
		monthValidator.bindToView(payfieldMonth);
		yearValidator.bindToView(payfieldYear);
		cscValidator.bindToView(payfieldCV);
		
		penValidator  .setNextValidator(monthValidator);
		monthValidator.setNextValidator(yearValidator );
		yearValidator .setNextValidator(cscValidator  );
		
		penValidator.addEditListener(new EditValidator.EditListener() {
			@Override public void onTextEdited() {
				CardType type = penValidator.cardNumber.getType();
				payimageCard.setImageDrawable(getResources().getDrawable(type.getCardImage()));
			}
		});
		
		//payfieldPEN.setCardEntryListener(cardEntryListener);
		
		/*mCardHolder = (CardNumHolder) findViewById(R.id.card_num_holder);
		mCardIcon = (CardIcon) findViewById(R.id.card_icon);
		mExtraFields = (LinearLayout) findViewById(R.id.extra_fields);
		mExpirationEditText = (ExpirationEditText) findViewById(R.id.expiration);
		mCVVEditText = (CVVEditText) findViewById(R.id.security_code);
		mCardHolder.setCardEntryListener(mCardEntryListener);
		
		setupViews();*/
	}

	private ImageView payimageCard ;
	private EditText  payfieldMonth;
	private EditText  payfieldYear ;
	private EditText  payfieldCV   ;
	private EditText  payfieldPEN  ;
	
	private CardNumberValidator penValidator = new CardNumberValidator();
	private MonthValidator monthValidator = new MonthValidator();
	private YearValidator yearValidator = new YearValidator();
	private CSCValidator cscValidator = new CSCValidator(penValidator.cardNumber);
	
	public void bindBefore(EditValidator sumValidator) {
		cscValidator.setNextValidator(sumValidator);
	}

	public void addValidators(ButtonValidator buttonValidator) {
		buttonValidator.addValidator(penValidator.validator);
		buttonValidator.addValidator(monthValidator.validator);
		buttonValidator.addValidator(yearValidator.validator);
		buttonValidator.addValidator(cscValidator.validator);
	}
}
