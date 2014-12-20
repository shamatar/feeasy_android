package me.feeasy.test;

import java.util.HashSet;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityHistory extends android.app.Activity {
	ListView listView;
	HashSet<DataSetObserver> listObservers = new HashSet<DataSetObserver>();
	
	@Override public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		
		setContentView(R.layout.history);
		listView = (ListView)findViewById(R.id.historyList);
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@Override public void onItemClick(AdapterView<?> p, View view, int pos,
					long id) {
				HistoryElem histElem = ActivityHistory.this.adapter.getItem(pos);
				showResult(histElem);
			}
		});
		
		FeeasyApp.instance.setupActivity(this);
	}
	
	protected void showResult(HistoryElem histElem) {
		Bundle histBundle = new Bundle();
		histElem.save(histBundle);
		
		Intent intent = new Intent(getApplicationContext(), ActivityResult.class);
		intent.putExtra(ActivityResult.TAG_HIST_ELEM, histBundle);
		
		startActivity(intent);
	}

	@Override public void onResume() {
		super.onResume();
		
		FeeasyApp.instance.loadHistory();
		for(DataSetObserver o : listObservers) {
			o.onChanged();
		}
		
		findViewById(R.id.viewEmpty).setVisibility(adapter.isEmpty() ? View.VISIBLE : View.INVISIBLE);
	}
	
	class AdapterClass implements ListAdapter {
		@Override public void unregisterDataSetObserver(DataSetObserver observer) {
			listObservers.remove(observer);
		}
		
		@Override public void registerDataSetObserver(DataSetObserver observer) {
			listObservers.add(observer);
		}
		
		@Override public boolean isEmpty() {
			return HistoryElem.history.isEmpty();
		}
		
		@Override public boolean hasStableIds() {
			return true;
		}
		
		@Override public int getViewTypeCount() {
			return 1;
		}
		
		@Override public View getView(int position, View view, ViewGroup parent) {
			if( view==null ) {
				view = getLayoutInflater().inflate(R.layout.history_elem, parent, false);
			}
			
			final HistoryElem item = getItem(position);
			((TextView)(view.findViewById(R.id.hist_message))).setText(item.payData.message);
			((TextView)(view.findViewById(R.id.hist_paysum))).setText(Utility.prettySum(item.payData.sum));
			FeeasyApp.addViewRurSign((TextView)(view.findViewById(R.id.hist_paysum)));
			((TextView)(view.findViewById(R.id.hist_paydate))).setText(item.shortDate());
			((TextView)(view.findViewById(R.id.hist_cardnum))).setText("*" + item.senderCardName);
			((ImageView)(view.findViewById(R.id.hist_cardimg))).setImageResource(item.senderCardType.getCardImage());
			
			view.setBackgroundColor(getResources().getColor(item.success ? R.color.histSuccess:R.color.histError) );
			view.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					showResult(item);
				}
			});
			
			return view;
		}
		
		@Override public int getItemViewType(int position) {
			return 0;
		}
		
		@Override public long getItemId(int pos) {
			return getItem(pos).id;
		}
		
		@Override public HistoryElem getItem(int pos) {
			return HistoryElem.history.get(HistoryElem.history.size() - pos - 1);
		}
		
		@Override public int getCount() {
			return HistoryElem.history.size();
		}
		
		@Override public boolean isEnabled(int position) {
			return true;
		}
		
		@Override public boolean areAllItemsEnabled() {
			return true;
		}
	};
	
	AdapterClass adapter = new AdapterClass(); 
}
