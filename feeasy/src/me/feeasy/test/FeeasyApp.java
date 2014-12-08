package me.feeasy.test;

import me.feeasy.test.svgview.FontEncoder;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.widget.TextView;

public class FeeasyApp {
	private static Drawable rurSign = null;
	public static void addViewRurSign(TextView view) {
		if( rurSign==null ) {
			FontEncoder.Glyph rur = FontEncoder.createFromResouce(view.getResources(), R.raw.rur);
			rurSign = new PictureDrawable(rur.getPicture(
					null, view.getResources().getDimensionPixelSize(R.dimen.smallText), view.getResources().getColor(R.color.darkText),
					0, view.getResources().getDimensionPixelSize(R.dimen.minStdPadding)/2 , 0, 0));
		}
    	
    	view.setLayerType(TextView.LAYER_TYPE_SOFTWARE, null);
    	view.setCompoundDrawablesWithIntrinsicBounds(rurSign, null, null, null);
	}
}
