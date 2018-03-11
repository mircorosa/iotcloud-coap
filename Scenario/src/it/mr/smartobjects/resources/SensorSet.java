package it.mr.smartobjects.resources;

import it.mr.smartobjects.servers.GenericCoapServer;
import it.mr.types.internal.ResType;

/**
 * Created by mirco on 04/05/16.
 */
public class SensorSet extends GenericCoapResource {

	public SensorSet(String name, String parentUri, GenericCoapServer server, boolean observable) {
		super(name,parentUri,server,observable, ResType.SENSOR_SET.getTypes());

		for(ResType resType : ResType.values()) {
			for(String type : resType.getTypes()) {
				if(type.equals("sensor"))
					addChildRes(new ResourceFactory().getResource(autoResName(resType),parentUri+"/"+name,resType,server),true);
			}
		}

		//LOG.info(name+" resource created in "+this.server.getName());
	}
}
