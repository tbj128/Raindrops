package com.kinetiqa.raindrops.components;

public class Message {

	public static final int READ = 1;
	public static final int UNREAD = 0;
	
	public static final int SENT = 1;
	public static final int UNSENT = 0;
	
	public static final int TYPE_TEXT = 0;
	public static final int TYPE_AUDIO = 1;
	public static final int TYPE_VIDEO = 2; 
	
	private boolean isInbox;
	private String id;
	private String target;
	private String date;
	private int type;
	private String location;
	private String title;
	private String description;
	private Integer read;
	private Integer sent;

	public Message(String id, String target, String date, int type, String location, String title, String description, int read, int sent) {
		this.id = id;
		this.target = target;
		this.date = date;
		this.type = type;
		this.location = location;
		this.title = title;
		this.description = description;
		this.read = read;
		this.sent = sent;
	}

	public boolean isInbox() {
		return isInbox;
	}

	public void setInbox(boolean isInbox) {
		this.isInbox = isInbox;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getRead() {
		return read;
	}

	public void setRead(Integer read) {
		this.read = read;
	}

	public Integer getSent() {
		return sent;
	}

	public void setSent(Integer sent) {
		this.sent = sent;
	}

}
