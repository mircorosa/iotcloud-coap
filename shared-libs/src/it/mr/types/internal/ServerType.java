package it.mr.types.internal;

/**
 * Created by mirco on 23/04/16.
 */
public enum ServerType {
	LAMP(ResType.TWO_WAY_SWITCH),
	THERMOSTAT(ResType.TEMPERATURE_SENSOR, ResType.TEMPERATURE_SENSOR, ResType.HUMIDITY_SENSOR),
	THERMOMETER(ResType.TEMPERATURE_SENSOR),
	ROOM_MONITOR(ResType.SENSOR_SET, ResType.SENSOR_SET),
	GREETING_DISPENSER(ResType.TWO_WAY_SWITCH, ResType.STATUS_MONITOR),
	CUSTOM_SERVER();

	private final ResType[] resources;

	ServerType(ResType... resources) {
		this.resources=resources;
	}
	@Override
	public String toString() {
		String name = name();
		String[] splitName = name.split("_");
		StringBuilder builder = new StringBuilder();
		for(String word : splitName)
			builder.append(word.charAt(0)).append(word.substring(1).toLowerCase());
		return builder.toString();
	}

	public ResType[] getResources() {
		return resources;
	}
}
