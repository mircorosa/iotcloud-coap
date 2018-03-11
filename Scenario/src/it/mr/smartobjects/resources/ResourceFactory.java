package it.mr.smartobjects.resources;

import it.mr.smartobjects.servers.GenericCoapServer;
import it.mr.types.internal.ResType;

/**
 * Created by mirco on 27/04/16.
 */
public class ResourceFactory {

	public GenericCoapResource getResource(String name, String parentURI, ResType type, GenericCoapServer server, String... types) {
		switch (type) {
			case TEMPERATURE_SENSOR:
				return new TemperatureSensor(name, parentURI, server, type.isObservable());
			case TWO_WAY_SWITCH:
				return new TwoWaySwitch(name, parentURI, server, type.isObservable());
			case HUMIDITY_SENSOR:
				return new HumiditySensor(name, parentURI, server, type.isObservable());
			case LIGHT_SENSOR:
				return new LightSensor(name, parentURI, server, type.isObservable());
			case SENSOR_SET:
				return new SensorSet(name, parentURI, server, type.isObservable());
			case STATUS_MONITOR:
				return new StatusMonitor(name, parentURI, server, type.isObservable());
			default:
				return null;
		}
	}
}
