package it.mr.smartobjects.resources;

import com.google.gson.Gson;
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
public class TwoWaySwitch extends GenericCoapResource {

	private static final boolean ON = true, OFF = false;
	private boolean state;

	public TwoWaySwitch (String name, String parentUri, GenericCoapServer server, boolean observable) {
		super(name,parentUri,server,observable, ResType.TWO_WAY_SWITCH.getTypes());

		Timer timer = new Timer();
//		timer.schedule(new BehaviourTask(), MyPrefs.SWITCH2_INITIAL_DELAY, MyPrefs.SWITCH2_UPDATE_FREQUENCY);

		//LOG.info(name+" resource created in "+this.server.getName());
	}

	private class BehaviourTask extends TimerTask {
		@Override
		public void run() {
			Random rand = new Random();
			int nextState = rand.nextInt(2);
			switch (nextState) {
				case 0:
					if(isOn()) {
						turnOff();
						changed();
					}
					break;
				case 1:
					if(!isOn()) {
						turnOn();
						changed();
					}
					break;
			}
		}
	}

	//Coap methods
	@Override
	public void handleGET(CoapExchange exchange) {
		String json = new Gson().toJson(new ResponseDescriptor(String.valueOf(state),getURI(),server.getName(),null));
		exchange.respond(CoAP.ResponseCode.CONTENT,json, MediaTypeRegistry.APPLICATION_JSON);
		//LOG.info(getName()+" on "+ server +" answered to GET with value "+state);
	}

	@Override
	public void handlePOST(CoapExchange exchange) {
		boolean newValue = Boolean.parseBoolean(new Gson().fromJson(exchange.getRequestText(), String.class));
		exchange.respond(CoAP.ResponseCode.CHANGED);
		if(state!=newValue) {
			this.state=newValue;
			changed();
		}
		//LOG.info(getName()+" on "+ server.getName() +" answered to POST with value "+state);
	}

	private boolean isOn() {
		return state;
	}

	private void turnOn() {
		this.state = ON;
	}

	private void turnOff() {
		this.state = OFF;
	}
}
