package me.feeasy.test.cardview;

import java.util.HashSet;

import me.feeasy.test.CardType;
import me.feeasy.test.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;

public class CardFragment extends Fragment {
	SavedCard savedCard;
	
	private ImageView payimageCard ;
	private EditText  payfieldMonth;
	private EditText  payfieldYear ;
	private EditText  payfieldCV   ;
	private EditText  payfieldPEN  ;
	
	private CardNumberValidator penValidator = new CardNumberValidator();
	private MonthValidator monthValidator = new MonthValidator();
	private YearValidator yearValidator = new YearValidator();
	private CSCValidator cscValidator = new CSCValidator(penValidator.cardNumber);

	private ViewPager pager;
	private View content;
	private View payBox;
	
	HashSet<ButtonValidator> validators = new HashSet<ButtonValidator>(); 
	
	public void bindBefore(EditValidator sumValidator) {
		cscValidator.setNextValidator(sumValidator);
	}

	public void addValidators(ButtonValidator buttonValidator) {
		if( savedCard==null || penValidator==null ) {
			validators.add(buttonValidator);
			return;
		}
		addValidatorsExistingCard(buttonValidator);
	}
	
	private void addValidatorsExistingCard(ButtonValidator buttonValidator) {
		if(!savedCard.isExisting() ) { 
			buttonValidator.addValidator(penValidator.validator);
			buttonValidator.addValidator(monthValidator.validator);
			buttonValidator.addValidator(yearValidator.validator);
		}
		buttonValidator.addValidator(cscValidator.validator);
	}

	public void removeValidators(ButtonValidator buttonValidator) {
		if( savedCard==null || penValidator==null ) {
			validators.remove(buttonValidator);
			return;
		}
		if(!savedCard.isExisting() ) { 
			buttonValidator.removeValidator(penValidator.validator);
			buttonValidator.removeValidator(monthValidator.validator);
			buttonValidator.removeValidator(yearValidator.validator);
		}
		buttonValidator.removeValidator(cscValidator.validator);
	}

	static CardFragment create(SavedCard savedCard) {
		CardFragment fragment = new CardFragment();
        Bundle args = new Bundle();
        savedCard.save(args);
        fragment.setArguments(args);
        return fragment;
	}
	
	@Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedCard = new SavedCard();
        savedCard.load(getArguments());
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        content = inflater.inflate(R.layout.card_fragment, container, false);
        payBox = content.findViewById(R.id.payboxNewCard);
        //container.addView(content);
        
        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
			@Override
            public void onGlobalLayout() {
            	updatePager();
            	content.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                //int height = content.getHeight();
            }
        });
        
        payimageCard  = (ImageView)((View)container.getParent()).findViewById(R.id.payimageCard );
		
		payfieldMonth = (EditText ) content.findViewById(R.id.payfieldMonth);
		payfieldYear  = (EditText ) content.findViewById(R.id.payfieldYear );
		payfieldCV    = (EditText ) content.findViewById(R.id.payfieldCV   );
		payfieldPEN   = (EditText ) content.findViewById(R.id.payfieldPEN  );
		
		cscValidator.bindToView(payfieldCV);
		
		if(!savedCard.isExisting() ) { 
			//set up fields validators for detect and display input errors
			penValidator.bindToView(payfieldPEN);
			monthValidator.bindToView(payfieldMonth);
			yearValidator.bindToView(payfieldYear);
			
			//set up fields order for focus automatically movement to the next field
			penValidator  .setNextValidator(monthValidator);
			monthValidator.setNextValidator(yearValidator );
			yearValidator .setNextValidator(cscValidator  );
		} else {
			payimageCard.setImageResource(savedCard.displayType.getCardImage());
			
			payfieldPEN.setEnabled(false);
			payfieldMonth.setEnabled(false);
			payfieldYear.setEnabled(false);
			
			payfieldPEN.setText("**** " + savedCard.displayNumbers);
			payfieldMonth.setText(Integer.toString(savedCard.expMonth));
			payfieldYear.setText(Integer.toString(savedCard.expYear));
		}
		
		penValidator.addEditListener(new EditValidator.EditListener() {
			@Override public void onTextEdited() {
				CardType type = penValidator.cardNumber.getType();
				payimageCard.setImageDrawable(getResources().getDrawable(type.getCardImage()));
			}
		});

        for(ButtonValidator button : validators ) {
        	addValidatorsExistingCard(button);
        }

        return content;
    }
    
    public String getPEN() {
    	if(!savedCard.isExisting() ) 
    		return penValidator.getPEN();
    	else return savedCard.id;
	}

	public int getCSC() {
		return cscValidator.getNumber();
	}

	public int getMonth() {
		if(!savedCard.isExisting() ) 
    		return monthValidator.getNumber();
		else return savedCard.expMonth;
	}

	public int getYear() {
		if(!savedCard.isExisting() ) 
    		return yearValidator.getNumber();
		else return savedCard.expYear;
	}

	public void setPager(ViewPager pager) {
		this.pager = pager;
		updatePager();
	}

	private void updatePager() {
		if( pager==null ) return;
		if( content==null || payBox.getHeight()==0 ) return;
		
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)payBox.getLayoutParams();
		pager.getLayoutParams().height = payBox.getHeight() + params.topMargin + params.bottomMargin;
		pager.requestLayout();
	}

	public void setFocus() {
		if( savedCard==null ) return;
		
		if( savedCard.isExisting() && payfieldCV!=null ) payfieldCV.requestFocus();
		else if( payfieldPEN!=null ) payfieldPEN.requestFocus();
		
		if( payimageCard!=null ) {
			if(!savedCard.isExisting() ) {
				CardType type = penValidator.cardNumber.getType();
				payimageCard.setImageDrawable(getResources().getDrawable(type.getCardImage()));
			} else {
				payimageCard.setImageResource(savedCard.displayType.getCardImage());
			}
		}
	}
}
