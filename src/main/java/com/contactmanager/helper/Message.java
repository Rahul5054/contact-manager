package com.contactmanager.helper;

public class Message {
	
	//ye class banaya gaya hai message ko show karne ke liye suppose sabkuch sahi hua toh result success show ho
	
	private String content;
	private String type;
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Message(String content, String type) {
		super();
		this.content = content;
		this.type = type;
	}
	public Message() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		return "Message [content=" + content + ", type=" + type + "]";
	}
	

}
