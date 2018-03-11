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
 * Created by mirco on 27/04/16.
 */
public class HumiditySensor extends GenericCoapResource {

	private int humidity;

	public HumiditySensor(String name, String parentUri, GenericCoapServer server, boolean observable) {
		super(name,parentUri,server,observable,ResType.HUMIDITY_SENSOR.getTypes());

		Random rand = new Random();
		humidity = 75+rand.nextInt(10);

		Timer timer = new Timer();
		timer.schedule(new BehaviourTask(), MyPrefs.HUM_INITIAL_DELAY, MyPrefs.HUM_UPDATE_FREQUENCY);

		//LOG.info(name+" resource created in "+this.server.getName());
	}

	private class BehaviourTask extends TimerTask {
		@Override
		public void run() {
			Random rand = new Random();
			int nextValue = rand.nextInt(3);
			switch (nextValue) {
				case 0:
					if(humidity== MyPrefs.HUM_MIN_VALUE) break;
					humidity--;
					changed();
					break;
				case 1:
					break;
				case 2:
					if(humidity== MyPrefs.HUM_MAX_VALUE) break;
					humidity++;
					changed();
					break;
			}
		}
	}

	//It's a sensor, no POST implementation
	@Override
	public void handleGET(CoapExchange exchange) {
		String json = new Gson().toJson(new ResponseDescriptor(Integer.toString(humidity),getURI(),server.getName(),null));
		exchange.respond(CoAP.ResponseCode.CONTENT,json, MediaTypeRegistry.APPLICATION_JSON);
		//LOG.info(getName()+" on "+ server +" answered to GET with value "+humidity);
		System.out.println(getName()+" on "+ server.getName() +" is sending "+humidity);
	}
}
