package com.kinetiqa.raindrops.components;


public class Bookmark {
	private String date;
	private String itemId;
	private String path;

	public Bookmark(String date, String itemId, String path) {
		this.date = date;
		this.itemId = itemId;
		this.path = path;
	}

	public String getDate() {
		return date;
	}

	public String getItemId() {
		return itemId;
	}

	public String getPath() {
		return path;
	}

}
