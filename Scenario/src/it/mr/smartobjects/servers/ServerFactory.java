package it.mr.smartobjects.servers;

import it.mr.smartobjects.resources.GenericCoapResource;
import it.mr.types.internal.NewResource;
import it.mr.types.internal.ServerType;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by mirco on 27/04/16.
 */
public class ServerFactory {
	public GenericCoapServer getServer(String name, int port, ServerType serverType, ArrayList<NewResource> customResources) throws IOException {
		switch(serverType) {
			case THERMOMETER:
				return Thermometer.newNameSafeServer(name, port);
			case LAMP:
				return Lamp.newNameSafeServer(name, port);
			case THERMOSTAT:
				return Thermostat.newNameSafeServer(name, port);
			case ROOM_MONITOR:
				return RoomMonitor.newNameSafeServer(name,port);
			case GREETING_DISPENSER:
				return GreetingDispenser.newNameSafeServer(name, port);
			case CUSTOM_SERVER:
				return CustomServer.newNameSafeServer(name,port,customResources);
			default:
				return null;
		}
	}
}
