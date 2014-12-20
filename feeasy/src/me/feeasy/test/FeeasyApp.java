package me.feeasy.test;

import java.util.ArrayList;

import me.feeasy.test.cardview.SavedCard;
import me.feeasy.test.svgview.FontEncoder;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.net.Uri;

public class FeeasyApp extends Application {
	public static FeeasyApp instance = null;
	
	static final String TAG_PREFS = "feeasyPrefs";
	static final String TAG_SAVED_CARDS = "savedCards";
	static final String TAG_HISTORY = "history";
	
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
	
	@Override public void onCreate() {
		super.onCreate();
		instance = this;
		
		loadSavedCards();
	}
	
	@Override public void onTerminate() {
		instance = null;
		super.onTerminate();
	}
	
	public void loadSavedCards() {
		SavedCard.savedCards.clear();
		
		SharedPreferences prefs = getSharedPreferences(TAG_PREFS, MODE_PRIVATE);
		ArrayList<String> savedCards = Utility.stringToArray(prefs.getString(TAG_SAVED_CARDS, null));
		for(String card : savedCards) {
			Bundle bundle = Utility.bundleFromString(card);
			SavedCard savedCard = new SavedCard();
			savedCard.load(bundle);
			
			SavedCard.savedCards.add(savedCard);
		}
	}
	
	public void saveCards() {
		ArrayList<String> savedCards = new ArrayList<String>(SavedCard.savedCards.size());
		for(SavedCard savedCard : SavedCard.savedCards) {
			Bundle bundle = new Bundle();
			savedCard.save(bundle);
			
			savedCards.add(Utility.bundleToString(bundle));
		}
		
		SharedPreferences prefs = getSharedPreferences(TAG_PREFS, MODE_PRIVATE);
		prefs
			.edit()
			.putString(TAG_SAVED_CARDS, Utility.arrayToString(savedCards))
			.commit();
	}
	
	public void loadHistory() {
		HistoryElem.history.clear();
		
		SharedPreferences prefs = getSharedPreferences(TAG_PREFS, MODE_PRIVATE);
		ArrayList<String> history = Utility.stringToArray(prefs.getString(TAG_HISTORY, null));
		for(String historyElemStr : history) {
			Bundle bundle = Utility.bundleFromString(historyElemStr);
			HistoryElem historyElem = new HistoryElem();
			historyElem.load(bundle);
			
			HistoryElem.history.add(historyElem);
		}
	}
	
	public void saveHistory() {
		ArrayList<String> history = new ArrayList<String>(HistoryElem.history.size());
		for(HistoryElem historyElem : HistoryElem.history) {
			Bundle bundle = new Bundle();
			historyElem.save(bundle);
			
			history.add(Utility.bundleToString(bundle));
		}
		
		SharedPreferences prefs = getSharedPreferences(TAG_PREFS, MODE_PRIVATE);
		prefs
			.edit()
			.putString(TAG_HISTORY, Utility.arrayToString(history))
			.commit();
	}
	
	static abstract class DrawerMenuItem {
		public String name;
		public int    icon;

		DrawerMenuItem(String name, int icon) {
			this.name = name;
			this.icon = icon;
		}

		abstract public void fire(Activity context);
	}
	
	DrawerMenuItem leftMenuItems[] = {
		new DrawerMenuItem("Считать код", R.raw.l_qr) {
			@Override public void fire(Activity context) {
				Intent intent = new Intent(context, InitialActivity.class);
				context.startActivity(intent);
			}
		},

		new DrawerMenuItem("История операций", R.raw.history) {
			@Override public void fire(Activity context) {
				Intent intent = new Intent(context, ActivityHistory.class);
				context.startActivity(intent);
			}
		},
		
		new DrawerMenuItem("feeasy.me", R.raw.l_web) {
			@Override public void fire(Activity context) {
				String url = "https://feeasy.me";
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setData(Uri.parse(url));
				context.startActivity(intent);
			}
		},
		
		new DrawerMenuItem("Оценить", R.raw.l_rate) {
			@Override public void fire(Activity context) {
				final String appPackageName = getPackageName();

				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		},
		
		new DrawerMenuItem("Соглашение", R.raw.l_copyright) {
			@Override public void fire(Activity context) {
				Intent intent = new Intent(context, ActivityTerms.class);
				context.startActivity(intent);
			}
		},
	};

	public void setupActivity(final android.app.Activity activity) {
		activity.findViewById(R.id.headerTermsLink).setOnClickListener(
				new View.OnClickListener() {
			@Override public void onClick(View v) {
				Intent intent = new Intent(activity, ActivityTerms.class);
				activity.startActivity(intent);
			}
		});
		
		final ListView drawerList = (ListView) activity.findViewById(R.id.left_drawer);
		final DrawerLayout drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
		String labels[] = new String[leftMenuItems.length];
		for(int i=0;i<leftMenuItems.length;++i) {
			labels[i] = leftMenuItems[i].name;
		}
		
		View.OnClickListener openDrawer = new View.OnClickListener() {
			@Override public void onClick(View v) {
				if( drawerLayout.isDrawerOpen(drawerList) ) {
					drawerLayout.closeDrawer(drawerList);
				} else {
					drawerLayout.openDrawer(drawerList);
				}
			}
		};
		
		activity.findViewById(R.id.headDrawerIcon).setOnClickListener(openDrawer);
		activity.findViewById(R.id.headIcon).setOnClickListener(openDrawer);
		activity.findViewById(R.id.feeasyTitle).setOnClickListener(openDrawer);

        // Set the adapter for the list view
		drawerList.setAdapter(new ArrayAdapter<String>(activity,
                R.layout.drawer_list_item, labels));
        // Set the list's click listener
        drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
        	@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		leftMenuItems[position].fire(activity);
                drawerLayout.closeDrawer(drawerList);
            }
        });
	}
}
