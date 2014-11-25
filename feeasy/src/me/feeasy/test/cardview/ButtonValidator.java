package me.feeasy.test.cardview;

import java.util.LinkedList;

import android.view.View;

public class ButtonValidator {
	private View button; 
	private int backgroundValid; 
	private int backgroundInvalid;
	
	private LinkedList<AbstractValidator> validators = new LinkedList<AbstractValidator>(); 
	
	private Runnable listener;
	
	public void bindButton(View button, int backgroundValid, int backgroundInvalid) {
		this.button = button;
		
		this.backgroundValid = backgroundValid;
		this.backgroundInvalid = backgroundInvalid;
		
		button.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				AbstractValidator failValidator = getFirstNotValid();
				
				if( failValidator!=null ) {
					failValidator.highlightError();
					
					return;
				}
				
				if( listener!=null ) {
					listener.run();
				}
			}
		});
		
		updateButton();
	}

	private Runnable validatorsListener = new Runnable() {
		@Override public void run() {
			updateButton();
		}
	}; 
	
	public void addValidator(AbstractValidator validator) {
		validators.add(validator);
		validator.addObserver(validatorsListener);
		updateButton();
	}
	
	private void updateButton() {
		if( button==null ) return;
		
		button.setBackgroundResource(isValid() ? backgroundValid : backgroundInvalid);
	}

	public void setAction(Runnable listener) {
		this.listener = listener;
	}
	
	public AbstractValidator getFirstNotValid() {
		for(AbstractValidator validator : validators) {
			if(!validator.isValid() ) return validator;
		}
		
		return null;
	}
	
	public boolean isValid() {
		return getFirstNotValid() == null;
	}
}
