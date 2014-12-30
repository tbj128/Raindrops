package com.kinetiqa.glacier.messaging;

import com.kinetiqa.glacier.utils.Utils;

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
	private String attachmentName;
	private String title;
	private String description;
	private Integer read;
	private Integer sent;

    public Message(String subjects, String body, String attachmentName, int messageType) {
        id = Utils.generateAlphaNumeric(10);
        this.title = subjects;
        this.description = body;
        this.attachmentName = attachmentName;
        this.type = messageType;
        this.read = Message.READ;
        this.sent = Message.UNSENT;
    }

    public Message(String id, String target, String date, int type, String attachmentName, String title, String description, int read, int sent) {
		this.id = id;
		this.target = target;
		this.date = date;
		this.type = type;
		this.attachmentName = attachmentName;
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

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
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

    public String getDescriptionPreview() {
        if (description.length() > 120) {
            return description.substring(0, 120);
        }
        return description;
    }

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isRead() {
		return read > 0;
	}

	public void setRead(Integer read) {
		this.read = read;
	}

	public boolean isSent() {
		return sent > 0;
	}

	public void setSent(Integer sent) {
		this.sent = sent;
	}

}
