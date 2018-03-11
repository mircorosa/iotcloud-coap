package it.mr;

import java.util.logging.Level;

/**
 * Created by mirco on 19/04/16.
 */
public class MyPrefs {

	/*##### FETCHER PREFERENCES #####*/
	public static final int PHANTOMJS_PORT = 11000;
	public static final String HTTPSERVER_URI = "/HTTPServer";


	/*####### SCENARIO PREFERENCES #######*/

	/*-- RESOURCES --*/
	public static final Level RESOURCE_LOG_LEVEL = Level.ALL;

	/*- TemperatureSensor -*/
	public static final int TEMP_INITIAL_DELAY = InitialDelay.SHORT;
	public static final int TEMP_UPDATE_FREQUENCY = UpdateFrequency.SHORT;
	public static final int TEMP_MIN_VALUE = -20;
	public static final int TEMP_MAX_VALUE = 60;

	/*- HumiditySensor*/
	public static final int HUM_INITIAL_DELAY = InitialDelay.SHORT;
	public static final int HUM_UPDATE_FREQUENCY = UpdateFrequency.MEDIUM;
	public static final int HUM_MIN_VALUE = 0;
	public static final int HUM_MAX_VALUE = 100;

	/*- LightSensor*/
	public static final int LIGHT_INITIAL_DELAY = InitialDelay.SHORT;
	public static final int LIGHT_UPDATE_FREQUENCY = UpdateFrequency.SHORTEST;
	public static final int LIGHT_MIN_VALUE = 100;
	public static final int LIGHT_MAX_VALUE = 1000;

	/*- TwoWaySwitch*/
	public static final int SWITCH2_INITIAL_DELAY = InitialDelay.SHORT;
	public static final int SWITCH2_UPDATE_FREQUENCY = UpdateFrequency.LONG;

	/*- StatusMonitor -*/
	public static final int MON_INITIAL_DELAY = InitialDelay.MEDIUM;
	public static final int MON_UPDATE_FREQUENCY = UpdateFrequency.SHORT;


	/*-- SERVERS --*/
	public static final Level SERVERS_LOG_LEVEL = Level.ALL;

	/*-- SIMULATOR --*/
	public static final Level SIMULATOR_LOG_LEVEL = Level.ALL;
	public static final String SCENARIO_NAME = "TestScenario";
	public static final int RANDOM_SCENARIO_SIZE = ScenarioSize.SMALL;
	public static final int SCENARIO_INITIAL_PORT = 10000;




	private static final class InitialDelay {
		private static final int NO_DELAY = 0;
		private static final int SHORT = 1000;
		private static final int MEDIUM = 5000;
		private static final int LONG = 10000;
	}

	private static final class UpdateFrequency {
		private static final int SHORTEST = 500;
		private static final int SHORTER = 1000;
		private static final int SHORT = 3000;
		private static final int MEDIUM = 5000;
		private static final int LONG = 10000;
		private static final int LONGER = 30000;
		private static final int LONGEST = 60000;
	}

	private static final class ScenarioSize {
		private static final int PAIR = 2;
		private static final int SMALL = 3;
		private static final int MEDIUM = 5;
		private static final int BIG = 10;
		private static final int HUGE = 20;
	}
}
