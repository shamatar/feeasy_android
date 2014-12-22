package me.feeasy.test;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class ActivityTerms extends Activity {
	@Override public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.terms);
		
		TextView text = (TextView)findViewById(R.id.termsText);
		text.setText(Html.fromHtml(getString(R.string.terms)));
		text.setMovementMethod(LinkMovementMethod.getInstance());
		
		FeeasyApp.instance.setupActivity(this);
	}
}
