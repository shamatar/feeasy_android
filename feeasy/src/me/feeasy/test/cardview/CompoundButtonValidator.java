package me.feeasy.test.cardview;

import android.os.Build;
import android.os.Handler;
import android.widget.CompoundButton;

public class CompoundButtonValidator extends AbstractValidator {
	CompoundButton button; boolean goodValue; String errorText;
	public CompoundButtonValidator(CompoundButton button, boolean goodValue, String errorText) {
		this.button     = button;
		this.goodValue  = goodValue;
		this.errorText  = errorText;

		if( Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ) return;
		button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				CompoundButtonValidator.this.onChange();
			}
		});
	}

	@Override
	public boolean isValid() {
		if( Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ) return true;
		return this.button.isChecked() == goodValue;
	}

	@Override
	public void highlightError() {
		if( Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ) return;
			
		button.setError(errorText);
		if(!button.hasFocus() ) button.requestFocus();
		new Handler().postDelayed(new Runnable() {
			@Override public void run() {
				button.setError(null);
			}
		}, 2000);
	}

}
