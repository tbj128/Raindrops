package com.kinetiqa.raindrops.menu;


// Represents a 'leaf node' - the videos/activities residing in a folder
public class MenuLeaf extends MenuComponent {

	private int versionNumber = 1;
	private boolean isActivity;
	private int mediaType;
	private String id;
	private String desc;
	private String requires;
	private String path;

	private String name;
	
	public MenuLeaf(int versionNumber, boolean isActivity, int mediaType, String id, String desc, String requires, String path) {
		this.versionNumber = versionNumber;
		this.isActivity = isActivity;
		this.mediaType = mediaType;
		this.id = id;
		this.requires = requires;
		this.desc = desc;
		this.path = path;
	}
	
	@Override
	public void printNames() {
		System.out.println(path);
	}

	public void setName(String name) {
		this.name = name;
	}	
	
	@Override
	public int getMediaType() {
		return this.mediaType;
	}
	
	@Override
	public boolean isActivity() {
		return this.isActivity;
	}

	@Override
	public String getName() {
		return this.name;
	}	
	
	@Override
	public String getDesc() {
		return this.desc;
	}

	@Override
	public String getRequires() {
		return this.requires;
	}

	@Override
	public String getID() {
		return this.id;
	}

	@Override
	public int getVersionNumber() {
		return versionNumber;
	}
	
	@Override
	public String getPath() {
		return path;
	}
}
