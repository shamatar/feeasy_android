package me.feeasy.test.cardview;

import java.util.HashSet;

abstract public class AbstractValidator {
	private boolean lastValid;
	private boolean lastValidSet = false;
	
	public abstract boolean isValid();
	public abstract void highlightError();
	
	private HashSet<Runnable> observers = new HashSet<Runnable>();
	
	public void addObserver(Runnable observer) {
		observers.add(observer);
	}
	
	//MUST be called when validity can be changed
	protected void onChange() {
		boolean newValid = isValid();
		if( lastValidSet && newValid == lastValid ) return;
		lastValidSet = true;
		lastValid = newValid;
		
		for(Runnable observer:observers) {
			observer.run();
		}
	}
}
