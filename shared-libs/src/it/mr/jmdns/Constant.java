package it.mr.jmdns;

public interface Constant {
	
	public static final String PROTOCOL_COAP = "coap";
	public static final String PROTOCOL_HTTP = "http";
	
	public static final int DEFAULT_COAP_SERVICE_PORT = 5683;
	
	public static final String DEFAULT_JMDNS_COAP_UDP_LOCAL = "_coap._udp.local.";
	public static final String DEFAULT_JMDNS_HTTP_TCP_LOCAL = "_http._tcp.local.";
	
	public static final int NODE_NOT_FOUND = -1;

}
