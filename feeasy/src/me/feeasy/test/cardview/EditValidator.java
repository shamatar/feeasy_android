package me.feeasy.test.cardview;

import java.util.HashSet;

import me.feeasy.test.R;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public abstract class EditValidator {
	/** must return true only if sure that it is end of input */
	abstract public boolean isComplete();
	
	/** must return true if input can be valid */
	abstract public boolean isValid();
	
	abstract public String errorText();
	
	public interface EditListener {
		void onTextEdited();
	}
	
	public String correctString(String initial) {
		return initial;
	}
	
	public int correctCursor(String oldText, String newText, int oldPos) {
		int changedLetters = 0;
		
		//calculate the number of changed characters before oldPos
		for(int i=0;i<oldPos && i<oldText.length() && i<newText.length();++i) {
			if( oldText.charAt(i)!=newText.charAt(i) ) changedLetters++;
		}
		
		return Math.min(oldPos + changedLetters, newText.length());
	}
	
	public boolean isEmpty() {
		if( thisView==null ) return true;
		return thisView.getText().length()==0;
	}
	
	public void bindToView(final EditText edit) {
		thisView = edit;
		
		edit.setImeActionLabel("Далее", KeyEvent.KEYCODE_ENTER);
		
		edit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if( (actionId == EditorInfo.IME_NULL || 
					 actionId == EditorInfo.IME_ACTION_SEND ||
					 actionId == EditorInfo.IME_ACTION_NEXT ||
					 actionId == EditorInfo.IME_ACTION_DONE ||
					 actionId == KeyEvent.KEYCODE_ENTER ) 
						&& (event==null || event.getAction() == KeyEvent.ACTION_DOWN) ) {
					
					//if( thisView!=null ) thisView.clearFocus();
					//if( thisView!=null ) thisView.setCursorVisible(false);
					done();
					return true;
				}
				return false;
			}
		});
		
		edit.setOnKeyListener(new View.OnKeyListener() {                 
	        @Override
	        public boolean onKey(View v, int keyCode, KeyEvent event) {
	        	if( keyCode == KeyEvent.KEYCODE_DEL &&
	        			event.getAction() == KeyEvent.ACTION_DOWN && isEmpty() ) {
	        		focusTo(prevView);
	        	} else if( keyCode == KeyEvent.KEYCODE_ENTER &&
	        			event.getAction() == KeyEvent.ACTION_DOWN ) {
	        		
	    			//if( thisView!=null ) thisView.clearFocus();
	        		//if( thisView!=null ) thisView.setCursorVisible(false);
	        		done();
	        		return true;
	        	}
	        	
	        	return false;
	        }
	    });
		
		edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus ) setError();
			}
		});
		
		edit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
	            String oldText = s.toString();
	            String newText = correctString(oldText); 
	            
	            if(!oldText.equals(newText) ) {
		    	    edit.removeTextChangedListener(this);
	
		            int oldSelectionPositionEnd   = edit.getSelectionEnd  ();
		            int oldSelectionPositionStart = edit.getSelectionStart();
		            
		            replaceText(s, newText);
	
		            int newSelectionPositionEnd = correctCursor(oldText, newText, oldSelectionPositionEnd);
		            int newSelectionPositionStart = correctCursor(oldText, newText, oldSelectionPositionStart);
		            
		            edit.setSelection(newSelectionPositionStart, newSelectionPositionEnd);
		            edit.addTextChangedListener(this);
	            }
	            
	            textEdited();
			}
		});
	}

	protected void replaceText(Editable editable, String newString) {
        InputFilter[] filters = editable.getFilters();
        editable.setFilters(new InputFilter[] { });
        // We need to remove filters so we can add text with spaces.
        editable.replace(0, editable.length(), newString);
        editable.setFilters(filters);
	}
	
	public void textEdited() {
		setError(false);
		
		for(EditListener listener : editListeners)
			listener.onTextEdited();
		
        if( isComplete() ) {
        	done();
        }
	}
	
	public void done() {
		if( thisView==null ) return;

		setError();
		if(!isValid() ) {
			return;
		}
		
		focusTo(nextView);
	}
	
	private void focusTo(View nextView) {
		if( nextView!=null && !nextView.hasFocus() ) {
			if( thisView!=null && thisView!=nextView ) thisView.clearFocus();
			
			nextView.requestFocus();
			if( nextView instanceof TextView ) {
				((TextView)nextView).setCursorVisible(true);
			}
		}
	}
	
	public void setError() {
		setError(!isEmpty()&&!isValid());
	}
	
	public void setError(boolean error) {
		if( errorState==error ) return;
		
		if( thisView!=null )
			thisView.setBackgroundResource(
				error ? R.drawable.text_bg_err : R.drawable.text_bg);
		
		errorState=error;
	}

	public void setNextView(View nextView) {
		this.nextView = nextView;
	}
	public void setPrevView(View prevView) {
		this.prevView = nextView;
	}
	
	public void setNextValidator(EditValidator next) {
		this.nextView = next.thisView;
		next.prevView = this.thisView;
	}
	
	public void addEditListener(EditListener editListener) {
		this.editListeners.add(editListener);
	}
	
	private View nextView = null;
	private View prevView = null;
	
	protected EditText thisView = null;
	private boolean errorState = false;
	private HashSet<EditListener> editListeners = new HashSet<EditListener>();
	
	public AbstractValidator validator = new AbstractValidator() {
		@Override public boolean isValid() {
			return EditValidator.this.isValid();
		}
		
		@Override
		public void highlightError() {
			setError(!isValid());
			if( thisView!=null ) {
				thisView.setError(errorText());
				if(!thisView.hasFocus() ) thisView.requestFocus();
				new Handler().postDelayed(new Runnable() {
					@Override public void run() {
						thisView.setError(null);
					}
				}, 2000);
			}
		}
	};
	
	EditValidator() {
		addEditListener(new EditListener() {
			@Override public void onTextEdited() {
				validator.onChange();
			}
		});
	}
}
