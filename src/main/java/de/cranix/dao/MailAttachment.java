package de.cranix.dao;

public class MailAttachment {

	private int partIndex = 0;
	private String fileName = "";
	private String contentType = "";
	private int size = 0;

	public MailAttachment() {
	}

	public MailAttachment(int partIndex, String fileName, String contentType, int size) {
		this.partIndex = partIndex;
		this.fileName = fileName;
		this.contentType = contentType;
		this.size = size;
	}

	public int getPartIndex() {
		return partIndex;
	}

	public void setPartIndex(int partIndex) {
		this.partIndex = partIndex;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}
