package org.ct.plat.component;

public abstract interface LifeCycle {
	
	public abstract void start() throws Exception;

	public abstract void stop() throws Exception;

	public abstract boolean isStarted();

	public abstract boolean isStopped();
}
