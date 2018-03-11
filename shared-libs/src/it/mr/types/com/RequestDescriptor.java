package it.mr.types.com;

public class RequestDescriptor {
	
	private String server;
	private String[] resourceURI;
	private boolean observing;
	private byte[] token = new byte[8];
	private String value = "";
	
	public RequestDescriptor() {}
	public RequestDescriptor(String server, String[] resourceURI, boolean observing, byte[] token) {
		this.server=server;
		this.resourceURI = resourceURI;
		this.observing=observing;
		this.token=token;
	}
	
	public RequestDescriptor(String URI) {		
		String getPattern = "/([^/]+)/([^/]+)/(\\S+)\\?(\\S+)";
		String postPattern = "/([^/]+)/([^/]+)/(\\S+)";
		String newUri = URI.replaceAll(getPattern, "$1 $2 $3 $4");
		if(URI.equals(newUri))
			newUri = URI.replaceAll(postPattern, "$1 $2 $3");
		//Output: 0.HTTPServerName  1.CoAP Server  2.Resource  (3.Query string)
		String[] splitURI = newUri.split("\\s");
		//Query string splitting
		if(splitURI.length==3) {
			this.server=splitURI[1];
			this.resourceURI =splitURI[2].split("/");
			this.observing=false;
			this.token=null;
			return;
		}
		String[] splitQuery = splitURI[3].split("&");

		this.server=splitURI[1];
		this.resourceURI =splitURI[2].split("/");
		if(splitQuery[0].equals("observing=true")) {
			setObserving(true);
			if(splitQuery.length==2) {
				this.token = (splitQuery[1].split("="))[1].getBytes();
			}
		} else if(splitQuery[0].equals("observing=false")) {
			setObserving(false);
			this.token = null;
		}
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String[] getResourceURI() {
		return resourceURI;
	}

	public void setResourceURI(String[] resourceURI) {
		this.resourceURI = resourceURI;
	}

	public boolean isObserving() {
		return observing;
	}

	public String getUriString() {
		StringBuilder builder = new StringBuilder();
		for(String res : resourceURI)
			builder.append(res).append("/");
		String uri = builder.toString();
		return uri.substring(0,uri.length()-1);
	}

	public void setObserving(boolean observing) {
		this.observing = observing;
	}

	public byte[] getToken() {
		return token;
	}

	public void setToken(byte[] token) {
		this.token = token;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
