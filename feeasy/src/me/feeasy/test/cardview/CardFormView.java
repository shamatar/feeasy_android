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
		
		//set up fields validators for detect and display input errors
		penValidator.bindToView(payfieldPEN);
		monthValidator.bindToView(payfieldMonth);
		yearValidator.bindToView(payfieldYear);
		cscValidator.bindToView(payfieldCV);
		
		//set up fields order for focus automatically movement to the next field
		penValidator  .setNextValidator(monthValidator);
		monthValidator.setNextValidator(yearValidator );
		yearValidator .setNextValidator(cscValidator  );
		
		penValidator.addEditListener(new EditValidator.EditListener() {
			@Override public void onTextEdited() {
				CardType type = penValidator.cardNumber.getType();
				payimageCard.setImageDrawable(getResources().getDrawable(type.getCardImage()));
			}
		});
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

	public String getPEN() {
		return penValidator.getPEN();
	}

	public int getCSC() {
		return cscValidator.getNumber();
	}

	public int getMonth() {
		return monthValidator.getNumber();
	}

	public int getYear() {
		return yearValidator.getNumber();
	}
}
