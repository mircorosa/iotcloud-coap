package it.mr.smartobjects.servers;

import it.mr.types.internal.ServerType;

import java.io.IOException;

/**
 * Created by mirco on 23/04/16.
 */
public class Thermometer extends GenericCoapServer {

	public Thermometer(String name, int port) throws IOException {
		super(name,port,ServerType.THERMOMETER);
	}

	public static Thermometer newNameSafeServer(String name, int port) throws IOException {
		if(regManager.checkServiceName(name,port))
			return new Thermometer(name,port);	//Service name available
		else
			return null;	//Service name already taken
	}
}
