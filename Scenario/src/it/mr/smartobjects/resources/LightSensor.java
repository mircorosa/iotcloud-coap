package it.mr.smartobjects.resources;

import com.google.gson.Gson;
import it.mr.MyPrefs;
import it.mr.smartobjects.servers.GenericCoapServer;
import it.mr.types.com.ResponseDescriptor;
import it.mr.types.internal.ResType;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by mirco on 23/04/16.
 */
public class LightSensor extends GenericCoapResource {

	private int lux;

	public LightSensor(String name, String parentUri, GenericCoapServer server, boolean observable) {
		super(name,parentUri,server,observable,ResType.LIGHT_SENSOR.getTypes());

		Random rand = new Random();
		lux = 100+rand.nextInt(150);

		Timer timer = new Timer();
		timer.schedule(new BehaviourTask(), MyPrefs.LIGHT_INITIAL_DELAY, MyPrefs.LIGHT_UPDATE_FREQUENCY);

		//LOG.info(name+" resource created in "+this.server.getName());
	}

	//Data changing simulation
	private class BehaviourTask extends TimerTask {
		@Override
		public void run() {
			Random rand = new Random();
			int nextValue = rand.nextInt(2);
			switch (nextValue) {
				case 0:
					if(lux== MyPrefs.LIGHT_MIN_VALUE) break;
					lux--;
					changed();
					break;
				case 1:
					if(lux== MyPrefs.LIGHT_MAX_VALUE) break;
					lux++;
					changed();
					break;
			}
		}
	}

	//It's a sensor, no POST implementation
	@Override
	public void handleGET(CoapExchange exchange) {
		String json = new Gson().toJson(new ResponseDescriptor(Integer.toString(lux),getURI(),server.getName(),null));
		exchange.respond(CoAP.ResponseCode.CONTENT,json, MediaTypeRegistry.APPLICATION_JSON);
		//LOG.info(getName()+" on "+ server +" answered to GET with value "+lux);
		System.out.println(getName()+" on "+ server.getName() +" is sending "+lux);
	}

}
