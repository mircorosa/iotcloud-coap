package it.mr.types.com;

public class MessageDescriptor {
	
	private String messageType=null, json=null;
	
	public MessageDescriptor() { }
	public MessageDescriptor(String messageType, String json) {
		this.messageType=messageType;
		this.json=json;
	}
	
	public String getMessageType() {
		return this.messageType;
	}
	public String getJson() {
		return this.json;
	}
	
	public void setMessageType(String messageType) {
		this.messageType=messageType;
	}
	public void setJson(String json) {
		this.json=json;
	}
	
}
