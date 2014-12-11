package me.feeasy.test.cardview;

import java.util.HashSet;

import me.feeasy.test.CardType;

public class SavedCard {
	private HashSet<Runnable> changeListeners = new HashSet<Runnable>(); 
	
	protected boolean deleted = false;
	
	protected String id;
	protected String displayName;
	protected CardType displayType;
	protected String displayNumbers;
	protected int    iconDrawable;
	protected int    expMonth;
	protected int    expYear;
	
	public void onChangeRequested() {}
	
	public final void addChangeListener(Runnable listener) {
		changeListeners.add(listener);
	}
	
	public final void removeChangeListener(Runnable listener) {
		changeListeners.remove(listener);
	}
	
	public void invokeChangeListeners() {
		for(Runnable r : changeListeners) {
			r.run();
		}
	}
}
