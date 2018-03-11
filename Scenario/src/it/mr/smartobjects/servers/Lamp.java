package it.mr.smartobjects.servers;

import it.mr.types.internal.ServerType;

import java.io.IOException;

/**
 * Created by mirco on 27/04/16.
 */
public class Lamp extends GenericCoapServer {

	public Lamp(String name, int port) throws IOException {
		super(name,port,ServerType.LAMP);
	}

	public static Lamp newNameSafeServer(String name, int port) throws IOException {
		if(regManager.checkServiceName(name,port))
			return new Lamp(name,port);	//Service name available
		else
			return null;	//Service name already taken
	}
}
