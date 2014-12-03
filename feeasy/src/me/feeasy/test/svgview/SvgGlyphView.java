 /*************************************************************************
  * 
  * XPianoTools
  * http://www.xpianotools.com
  * 
  *  [2014] XPianoTools 
  *  All Rights Reserved.
  * 
  * NOTICE:  All information contained herein is, and remains
  * the property of XPianoTools and its suppliers,
  * if any.
  * 
  * Developer: Georgy Osipov
  * 	developer@xpianotools.com
  * 	gaosipov@gmail.com
  * 
  * 2014-15-08
  *************************************************************************/

package me.feeasy.test.svgview;

import me.feeasy.test.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

public class SvgGlyphView extends View {
	private static int defaultSize;
	private static SparseArray<FontEncoder.Glyph> glyphByRes = new SparseArray<FontEncoder.Glyph>();
	private int    glyphRes;
	
	private Paint  paint = new Paint();
	private Paint  shadePaint = new Paint();
	
	private int    color = 0;
	private int    shadeColor = 0;
	
	private int    shadeOffsetX = 0;
	private int    shadeOffsetY = 0;
	
	@SuppressLint("NewApi")
	private void loadAttrs(AttributeSet attrs) {
		if( attrs!=null ) {
			TypedArray a = getContext().getTheme().obtainStyledAttributes(
			        attrs,
			        R.styleable.CompProgressView,
			        0, 0);
			
			float blurRadius = 0;
	
		    try { 
			    glyphRes   = a.getResourceId(R.styleable.CompProgressView_glyph, glyphRes);
		 	    color      = a.getColor(R.styleable.CompProgressView_color, color);
		 	    blurRadius = a.getFloat(R.styleable.CompProgressView_blur_radius, blurRadius);
		 	    shadeColor = a.getColor(R.styleable.CompProgressView_shade_color, shadeColor);
		 	    shadeOffsetX = a.getDimensionPixelOffset(R.styleable.CompProgressView_shade_offset_x, shadeOffsetX);
		 	    shadeOffsetY = a.getDimensionPixelOffset(R.styleable.CompProgressView_shade_offset_y, shadeOffsetY);
		    } finally {
		        a.recycle();
		    }
		    paint.setColor(color);
		    
		    if( blurRadius>0 )
		    	paint.setMaskFilter(new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL));
		    
		    shadePaint.setMaskFilter(new BlurMaskFilter(.2f, BlurMaskFilter.Blur.NORMAL));
		}
		
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && getLayerType() != View.LAYER_TYPE_SOFTWARE ) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
	}

	public SvgGlyphView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		loadAttrs(attrs);
	}
	
	public SvgGlyphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		loadAttrs(attrs);
	}
	
	public SvgGlyphView(Context context) {
		super(context);
		loadAttrs(null);
	}
	
	public static int extractSize(int measureSpec, int normal) {
		if( View.MeasureSpec.getMode(measureSpec) == View.MeasureSpec.EXACTLY || normal<=0)
	    	return View.MeasureSpec.getSize(measureSpec);
		if( View.MeasureSpec.getMode(measureSpec) == View.MeasureSpec.AT_MOST )
	    	return Math.min(normal, View.MeasureSpec.getSize(measureSpec));
		return normal;
	}
	
	public static boolean isExactly(int measureSpec) {
		return View.MeasureSpec.getMode(measureSpec) == View.MeasureSpec.EXACTLY;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    int width ;
	    int height;
	    
	    FontEncoder.Glyph glyph = getGlyph();
	    
	    float glyphw = glyph != null ? glyph.getWidth () : extractSize( widthMeasureSpec ,defaultSize);
	    float glyphh = glyph != null ? glyph.getHeight() : extractSize( heightMeasureSpec,defaultSize);
	    
	    if( isExactly(widthMeasureSpec) && isExactly(heightMeasureSpec) ) {
	    	int maxWidth  = extractSize( widthMeasureSpec,defaultSize);
	    	int maxHeight = extractSize(heightMeasureSpec,defaultSize);
	    	
	    	float gwidth  = glyphw;
	    	float gheight = glyphh;
	    	
	    	float scale = Math.min(maxWidth / gwidth, maxHeight / gheight);
	    	
	    	width  = (int) (gwidth  * scale);
	    	height = (int) (gheight * scale);
	    } else if( isExactly(widthMeasureSpec) ) {
	    	width  = extractSize( widthMeasureSpec,defaultSize);
	    	height = (int) (width * glyphh / glyphw);
	    	height = Math.min(height,View.MeasureSpec.getSize(heightMeasureSpec));
	    } else if( isExactly(heightMeasureSpec) ) {
	    	height = extractSize( heightMeasureSpec,defaultSize);
	    	width = (int) (height * glyphw / glyphh);
	    	width = Math.min(width,View.MeasureSpec.getSize(widthMeasureSpec));
	    } else {
	    	int maxWidth  = extractSize( widthMeasureSpec, getLayoutParams().width);
	    	int maxHeight = extractSize(heightMeasureSpec, getLayoutParams().height);
	    	
	    	float gwidth  = glyphw;
	    	float gheight = glyphh;
	    	
	    	float scale = Math.min(maxWidth / gwidth, maxHeight / gheight);
	    	
	    	width  = (int) (gwidth  * scale);
	    	height = (int) (gheight * scale);
	    }
	    
	    setMeasuredDimension(width, height);
	}
	
	private FontEncoder.Glyph getGlyph() {
		synchronized(glyphByRes) {
			FontEncoder.Glyph glyph = glyphByRes.get(glyphRes);
			if( glyph==null && glyphRes!=0 ) {
				glyph = FontEncoder.createFromResouce(getResources(), glyphRes);
				glyphByRes.put(glyphRes,glyph);
			}
			
			return glyph;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		FontEncoder.Glyph glyph = getGlyph();
		if( glyph==null ) return;
		
		canvas.save();
		try {
			Log.d("SVG", "width " + getWidth());
			Log.d("SVG", "height " + getHeight());
			canvas.scale(getWidth() / glyph.getWidth(), getHeight() / glyph.getHeight());
			paint.setAntiAlias(true);
			paint.setColor(color);
			
			if( (shadeColor&0xFF000000)!=0 ) {
				canvas.save();
				canvas.translate(shadeOffsetX / getWidth() * glyph.getWidth(), 
						shadeOffsetY / getHeight() * glyph.getHeight());
				
				glyph.draw(canvas, shadePaint);
				canvas.restore();
			}

			glyph.draw(canvas, paint);
		} finally {
			canvas.restore();
		}
	}
}
