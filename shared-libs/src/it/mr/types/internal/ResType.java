package it.mr.types.internal;

/**
 * Created by mirco on 23/04/16.
 */
public enum ResType {
	TEMPERATURE_SENSOR(true,"sensor"),
	HUMIDITY_SENSOR(true,"sensor"),
	LIGHT_SENSOR(true,"sensor"),
	SENSOR_SET(false,"set"),
	TWO_WAY_SWITCH(true,"switch","interactive"),
	STATUS_MONITOR(true,"status");

	private final String[] types;
	private final boolean observable;

	ResType(boolean observable, String... types) {  //TODO Check observable usages
		this.observable=true;
		this.types = types;
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

	public String[] getTypes() {
		return types;
	}

	public boolean isObservable() {
		return observable;
	}
}
