package me.feeasy.test.cardview;

import java.util.ArrayList;

import me.feeasy.test.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CardFormView extends LinearLayout {
	boolean cardSelected = false;
	SavedCard selectedCard = null;
	private ArrayList<SavedCardWidget> savedCards = new ArrayList<SavedCardWidget>();
	
	SavedCardWidget selectedTab;
	ViewPager pager;
	CardSlideAdapter pagerAdapter;
	
	class SavedCardWidget {
		//private static final long ANIMATION_DUR = 500;
		
		private CardFragment cardFragment;
		
		public SavedCardWidget(SavedCard savedCard, int index) {
			this.savedCard = savedCard;
			
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			this.view = inflater.inflate(R.layout.cardmenu_card, cardmenuNavbar, false);
			
			this.plusSign = this.view.findViewById(R.id.plusSign);
			this.cardDescription = this.view.findViewById(R.id.cardDescription);
			
			this.plusSign.setVisibility(!savedCard.isExisting() ? View.VISIBLE : View.GONE);
			this.cardDescription.setVisibility(!savedCard.isExisting() ? View.GONE : View.VISIBLE);
			
			if( savedCard.isExisting() ) {
				((TextView)(this.cardDescription.findViewById(R.id.cardName))).setText(savedCard.displayName);
				((ImageView)(this.cardDescription.findViewById(R.id.cardImage))).setImageResource(savedCard.displayType.getCardImage());
				((TextView)(this.cardDescription.findViewById(R.id.cardNumber))).setText(savedCard.displayNumbers);
			}
			
			cardmenuNavbar.addView(view, index);
			
			this.view.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					select(true);
				}
			});
			
			this.view.setOnLongClickListener(new View.OnLongClickListener() {
				@Override public boolean onLongClick(View v) {
					if(!SavedCardWidget.this.savedCard.isExisting() ) return false;
					
					showDeleteCardDialog();
					return true;
				}
			});
		}
		
		protected void showDeleteCardDialog() {
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        	if( isSelectedTab() ) {
			        		savedCards.get(getIndex() - 1).select(true);
			        	}
			        	cardmenuNavbar.removeView(view);
			            SavedCard.deleteCard(savedCard);
			            pagerAdapter.notifyChange();
			            pagerAdapter.notifyDataSetChanged();
			            break;

			        case DialogInterface.BUTTON_NEGATIVE:
			            break;
			        }
			    }
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setMessage("Удалить карту " + savedCard.displayType.getName() + " " + savedCard.displayNumbers + "?").setPositiveButton("Да", dialogClickListener)
			    .setNegativeButton("Нет", dialogClickListener).show();
		}
		
		private void onSelect() {
			if( cardFragment!=null ) {
				cardFragment.setFocus();
				if( buttonToAddValidators!=null )
					cardFragment.addValidators(buttonToAddValidators);
			}
		}
		private void onDeselect() {
			if( cardFragment!=null ) {
				if( buttonToAddValidators!=null )
					cardFragment.removeValidators(buttonToAddValidators);
			}
		}

		void select(boolean updatePager) {
			if( isSelectedTab() ) return;
			
			if( selectedTab!=null ) {
				selectedTab.deselect();
			}
			
			if( updatePager && pager!=null ) 
				pager.setCurrentItem(getIndex(), true);
			
			onSelect();
			
			view.setBackgroundResource(R.drawable.cardmenu_card_bg_selected);
			
			selectedTab = this;
		}
		
		int getIndex() {
			return savedCards.indexOf(this);
		}

		void deselect() {
			onDeselect();
			
			view.setBackgroundResource(R.drawable.cardmenu_card_bg);
		}
		
		/*void select() {
			if( cardSelected ) return;
			cardSelected = true;
			
			//hide all other widgets
			for(SavedCardWidget card: savedCards) {
				if( card==this ) continue;
				card.view.setVisibility(View.INVISIBLE);
			}
			
			//animate transformation to form view
			AnimationSet cardAnimation = new AnimationSet(true);
			float scaleX = payboxNewCard.getMeasuredWidth() / view.getMeasuredWidth();
			float scaleY = scaleX;//payboxNewCard.getMeasuredHeight() / view.getMeasuredHeight();
			
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)view.getLayoutParams();
			float newX = -params.leftMargin, newY = -params.topMargin;
			float oldX = 0, oldY = 0;
			
			float pivotX = (newX - oldX * scaleX) / (1 - scaleX);
			float pivotY = (newY - oldY * scaleY) / (1 - scaleY);
			cardAnimation.addAnimation(new ScaleAnimation(1.f, scaleX, 1.f, scaleY, ScaleAnimation.ABSOLUTE, pivotX, ScaleAnimation.ABSOLUTE, pivotY));
			cardAnimation.addAnimation(new AlphaAnimation(1.f,0.f));
			
			cardAnimation.setDuration(ANIMATION_DUR);
			
			AlphaAnimation formAnimation = new AlphaAnimation(0.f, 1.f);
			
			ViewGroup.MarginLayoutParams parentParams = (ViewGroup.MarginLayoutParams)payboxNewCard.getLayoutParams();
			ValueAnimator holderAnimator = ValueAnimator.ofInt(
					payboxCardMenu.getMeasuredHeight(), payboxNewCard.getMeasuredHeight() + parentParams.topMargin + parentParams.bottomMargin);
			holderAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override public void onAnimationUpdate(ValueAnimator animation) {
					payboxHolder.getLayoutParams().height = (Integer) animation.getAnimatedValue();
					payboxHolder.requestLayout();
				}
			});
			
			cardAnimation.setFillAfter(true);
			formAnimation.setFillAfter(true);
			
			cardAnimation.setDuration(ANIMATION_DUR);
			formAnimation.setDuration(ANIMATION_DUR);
			holderAnimator.setDuration(ANIMATION_DUR);
			
			view.startAnimation(cardAnimation);
			payboxNewCard.startAnimation(formAnimation);
			holderAnimator.start();
		}*/
		
		SavedCard savedCard;
		
		View view;
		View plusSign;
		View cardDescription;
		public CardFragment createFragment() {
			if( isSelectedTab() ) {
				onDeselect();
			}
			
			cardFragment = CardFragment.create(savedCard);
			
			if( isSelectedTab() ) {
				onSelect();
			}
			
			return cardFragment;
		}

		private boolean isSelectedTab() {
			return selectedTab == this;
		}
	}
	
	LinearLayout payboxNewCard;
	//RelativeLayout payboxCardMenu;
	//RelativeLayout payboxHolder;
	LinearLayout cardmenuNavbar;
	EditValidator validatorToBindBefore;
	private ButtonValidator buttonToAddValidators;
	
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
	
	/*@Override 
	protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
		ViewGroup.MarginLayoutParams parentParams = (ViewGroup.MarginLayoutParams)payboxCardMenu.getLayoutParams();
		int padding = getResources().getDimensionPixelSize(R.dimen.minStdPadding);
		int borderPadding = padding/2;
		int width  = (right-left - 2*borderPadding 
				- payboxCardMenu.getPaddingRight() - payboxCardMenu.getPaddingLeft()
				- parentParams.leftMargin - parentParams.rightMargin - padding)/2;
		
		int height = width * 53/85;//getResources().getDimensionPixelSize(R.dimen.fivefoldStdPadding);
		int cury  = borderPadding;
		
		int i = 0;
		
		for(SavedCardWidget widget : savedCards) {
			int x = borderPadding, y = cury;

			if( i++%2==1 ) {
				x = width + padding + borderPadding; 
				cury += height + padding;
			}
			
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)widget.view.getLayoutParams();
			
			params.leftMargin = x;
			params.topMargin  = y;
			params.width  = width;
			params.height = height;
			
			widget.view.setLayoutParams(params);
		}
		
		super.onLayout(changed, left, top, right, bottom);
	}*/
	
	static public class WrapContentHeightViewPager extends ViewPager {

	    /**
	     * Constructor
	     *
	     * @param context the context
	     */
	    public WrapContentHeightViewPager(Context context) {
	        super(context);
	    }

	    /**
	     * Constructor
	     *
	     * @param context the context
	     * @param attrs the attribute set
	     */
	    public WrapContentHeightViewPager(Context context, AttributeSet attrs) {
	        super(context, attrs);
	    }

	    @Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	        int height = 0;
	        
	        for(int i=0;i<getChildCount();++i) {
	        	// find the first child view
		        View view = getChildAt(i);
		        if (view != null) {
		            // measure the first child view with the specified measure spec
		            view.measure(widthMeasureSpec, heightMeasureSpec);
		        }
		        
		        height = Math.max(height, measureHeight(heightMeasureSpec, view));
	        }
	        
	        setMeasuredDimension(getMeasuredWidth(), height);
	        //super.onMeasure(MeasureSpec.EXACTLY | getMeasuredWidth(),MeasureSpec.EXACTLY | getMeasuredHeight());
	    }

	    /**
	     * Determines the height of this view
	     *
	     * @param measureSpec A measureSpec packed into an int
	     * @param view the base view with already measured height
	     *
	     * @return The height of the view, honoring constraints from measureSpec
	     */
	    private int measureHeight(int measureSpec, View view) {
	        int result = 0;
	        int specMode = MeasureSpec.getMode(measureSpec);
	        int specSize = MeasureSpec.getSize(measureSpec);

	        if (specMode == MeasureSpec.EXACTLY) {
	            result = specSize;
	        } else {
	            // set the height from the base view if available
	            if (view != null) {
	                result = view.getMeasuredHeight();
	            }
	            if (specMode == MeasureSpec.AT_MOST) {
	                result = Math.min(result, specSize);
	            }
	        }
	        return result;
	    }

	}

	void setup() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		inflater.inflate(R.layout.card_form, this, true);
		
		payboxNewCard  = (LinearLayout)findViewById(R.id.payboxNewCard );
		//payboxCardMenu = (RelativeLayout)findViewById(R.id.payboxCardMenu);
		//payboxHolder   = (RelativeLayout)findViewById(R.id.payboxHolder);
		cardmenuNavbar = (LinearLayout)findViewById(R.id.cardmenuNavbar);
		
		//savedCards.add(new SavedCardWidget(new SavedCard("id","Card",CardType.MAESTRO,"2222",14,12)));

		addSavedCard(new SavedCard());
		savedCards.get(0).select(true);
		
		for(SavedCard s : SavedCard.savedCards) {
			addSavedCard(s);
		}
		
		if( SavedCard.savedCards.isEmpty() ) {
			cardmenuNavbar.setVisibility(View.GONE);
		}

		//pager = (ViewPager) findViewById(R.id.paypagerCard);
		this.removeView(findViewById(R.id.paypagerCard));
		
		pager = new WrapContentHeightViewPager(getContext());
		pager.setId(R.id.paypagerCard);
		pager.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 
				LinearLayout.LayoutParams.WRAP_CONTENT ));
		
		this.addView(pager);
		
		pagerAdapter = new CardSlideAdapter(((FragmentActivity)getContext()).getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override public void onPageSelected(int position) {
            	if( position>= savedCards.size() || position<0 ) return;
            	savedCards.get(position).select(false);
            }
        });
	}

	public void setInitialSavedCard(SavedCard tmpSavedCard) {
		addSavedCard(tmpSavedCard,0);
		savedCards.get(0).select(true);
	}

	public String getPEN() {
		if( selectedTab.cardFragment==null ) return "";
		return selectedTab.cardFragment.getPEN();
	}

	public int getCSC() {
		if( selectedTab.cardFragment==null ) return 0;
		return selectedTab.cardFragment.getCSC();
	}

	public int getMonth() {
		if( selectedTab.cardFragment==null ) return 0;
		return selectedTab.cardFragment.getMonth();
	}

	public int getYear() {
		if( selectedTab.cardFragment==null ) return 0;
		return selectedTab.cardFragment.getYear();
	}
	
	public void addSavedCard(SavedCard savedCard) {
		addSavedCard(savedCard, savedCards.size());
	}
	
	public void addSavedCard(SavedCard savedCard, int index) {
		savedCards.add(index, new SavedCardWidget(savedCard, index));
		
		if( pagerAdapter!=null )
			pagerAdapter.notifyDataSetChanged();
		
		if( savedCards.size()==2 ) {
			cardmenuNavbar.setVisibility(View.VISIBLE);
		}
		
		//payboxCardMenu.setVisibility(cardSelected ? View.INVISIBLE : View.VISIBLE);
		//payboxNewCard .setVisibility(cardSelected ? View.VISIBLE : View.INVISIBLE);
	}
	
	private class CardSlideAdapter extends android.support.v4.app.FragmentPagerAdapter  {
		int baseId = 0;
		
        public CardSlideAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	CardFragment f = savedCards.get(position).createFragment();
        	f.bindBefore(validatorToBindBefore);
        	return f;
        }

        @Override public int getCount() {
            return savedCards.size();
        }
        
        @Override public int getItemPosition(Object object) {
            // refresh all fragments when data set changed
            return PagerAdapter.POSITION_NONE;
        }
        
        @Override public long getItemId(int position) {
            // give an ID different from position when position has been changed
            return baseId + position;
        }
        
        public void notifyChange() {
        	baseId += getCount() + 1;
        }
    }

	public void bindBefore(EditValidator validator) {
		validatorToBindBefore = validator;
	}

	public void addValidators(ButtonValidator buttonValidator) {
		buttonToAddValidators = buttonValidator;
		
		if( selectedTab.cardFragment!=null )
			selectedTab.cardFragment.addValidators(buttonValidator);
	}
}
