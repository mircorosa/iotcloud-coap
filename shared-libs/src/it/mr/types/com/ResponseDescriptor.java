package it.mr.types.com;

public class ResponseDescriptor {
	
	private String resource, server, value;
	private byte[] token = new byte[8];
	
	public ResponseDescriptor() {}
	public ResponseDescriptor(String value, String resource, String server, byte[] token) {
		this.value=value;
		this.resource=resource;
		this.server=server;
		this.token=token;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public byte[] getToken() {
		return token;
	}

	public void setToken(byte[] token) {
		this.token = token;
	}
}
