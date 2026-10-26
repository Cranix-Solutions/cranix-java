package de.cranix.dao;

public class MailFolder {

	private String name = "";
	private String fullName = "";
	private String type = "";
	private int messageCount = 0;
	private int unreadCount = 0;

	public MailFolder() {
	}

	public MailFolder(String name, String fullName, String type, int messageCount, int unreadCount) {
		this.name = name;
		this.fullName = fullName;
		this.type = type;
		this.messageCount = messageCount;
		this.unreadCount = unreadCount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}

	public int getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}
}
