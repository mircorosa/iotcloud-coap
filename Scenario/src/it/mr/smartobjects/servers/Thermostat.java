package it.mr.smartobjects.servers;

import it.mr.types.internal.ServerType;

import java.io.IOException;

/**
 * Created by mirco on 27/04/16.
 */
public class Thermostat extends GenericCoapServer {

	public Thermostat(String name, int port) throws IOException {
		super(name,port,ServerType.THERMOSTAT);
	}

	public static Thermostat newNameSafeServer(String name, int port) throws IOException {
		if(regManager.checkServiceName(name,port))
			return new Thermostat(name,port);	//Service name available
		else
			return null;	//Service name already taken
	}
}
