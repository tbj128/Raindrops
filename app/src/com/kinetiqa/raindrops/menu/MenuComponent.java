package com.kinetiqa.raindrops.menu;

public abstract class MenuComponent {
	public abstract int getVersionNumber();
	public abstract void printNames();
	public abstract String getID();
	public abstract int getMediaType();
	public abstract boolean isActivity();
	public abstract String getName();
	public abstract String getDesc();
	public abstract String getPath();
	public abstract String getRequires();
}
