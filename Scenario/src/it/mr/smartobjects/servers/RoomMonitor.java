package it.mr.smartobjects.servers;

import it.mr.smartobjects.resources.GenericCoapResource;
import it.mr.smartobjects.resources.ResourceFactory;
import it.mr.types.internal.ResType;
import it.mr.types.internal.ServerType;

import java.io.IOException;

/**
 * Created by mirco on 04/05/16.
 */
public class RoomMonitor extends GenericCoapServer {

	public RoomMonitor(String name, int port) throws IOException {
		super(name,port, ServerType.ROOM_MONITOR);
	}

	public static RoomMonitor newNameSafeServer(String name, int port) throws IOException {
		if(regManager.checkServiceName(name,port))
			return new RoomMonitor(name,port);	//Service name available
		else
			return null;	//Service name already taken
	}
}
