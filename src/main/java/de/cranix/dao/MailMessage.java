package de.cranix.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MailMessage {

	private String folder = "";
	private long uid = 0;
	private String messageId = "";
	private String from = "";
	private List<String> to = new ArrayList<String>();
	private List<String> cc = new ArrayList<String>();
	private String subject = "";
	private Date date;
	private boolean seen = false;
	private boolean flagged = false;
	private boolean hasAttachments = false;
	private int size = 0;
	private String textBody = "";
	private String htmlBody = "";
	private List<MailAttachment> attachments = new ArrayList<MailAttachment>();

	public MailMessage() {
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public List<String> getTo() {
		return to;
	}

	public void setTo(List<String> to) {
		this.to = to;
	}

	public List<String> getCc() {
		return cc;
	}

	public void setCc(List<String> cc) {
		this.cc = cc;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isSeen() {
		return seen;
	}

	public void setSeen(boolean seen) {
		this.seen = seen;
	}

	public boolean isFlagged() {
		return flagged;
	}

	public void setFlagged(boolean flagged) {
		this.flagged = flagged;
	}

	public boolean isHasAttachments() {
		return hasAttachments;
	}

	public void setHasAttachments(boolean hasAttachments) {
		this.hasAttachments = hasAttachments;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getTextBody() {
		return textBody;
	}

	public void setTextBody(String textBody) {
		this.textBody = textBody;
	}

	public String getHtmlBody() {
		return htmlBody;
	}

	public void setHtmlBody(String htmlBody) {
		this.htmlBody = htmlBody;
	}

	public List<MailAttachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<MailAttachment> attachments) {
		this.attachments = attachments;
	}
}
