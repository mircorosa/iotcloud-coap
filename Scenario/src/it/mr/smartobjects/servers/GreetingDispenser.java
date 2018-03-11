package it.mr.smartobjects.servers;

import it.mr.types.internal.ServerType;

import java.io.IOException;

/**
 * Created by mirco on 27/04/16.
 */
public class GreetingDispenser extends GenericCoapServer {

	public GreetingDispenser(String name, int port) throws IOException {
		super(name,port,ServerType.GREETING_DISPENSER);
	}

	public static GreetingDispenser newNameSafeServer(String name, int port) throws IOException {
		if(regManager.checkServiceName(name,port))
			return new GreetingDispenser(name,port);	//Service name available
		else
			return null;	//Service name already taken
	}
}
