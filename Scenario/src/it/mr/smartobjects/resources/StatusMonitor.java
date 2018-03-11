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
public class StatusMonitor extends GenericCoapResource {

	private String status = "LOAD";
	private static final String statuses[] = {"STOP","RUN","WAIT","OFF","LOAD"};

	public StatusMonitor(String name, String parentUri, GenericCoapServer server, boolean observable) {
		super(name,parentUri,server,observable, ResType.STATUS_MONITOR.getTypes());

		Timer timer = new Timer();
		timer.schedule(new BehaviourTask(), MyPrefs.MON_INITIAL_DELAY, MyPrefs.MON_UPDATE_FREQUENCY);

		//LOG.info(name+" resource created in "+this.server.getName());
	}

	private class BehaviourTask extends TimerTask {
		@Override
		public void run() {
			String newGreeting = statuses[new Random().nextInt(statuses.length)];
			if(!status.equals(newGreeting)) {
				status=newGreeting;
				changed();
			}
		}
	}

	//It's a monitor, no POST implementation
	@Override
	public void handleGET(CoapExchange exchange) {
		String json = new Gson().toJson(new ResponseDescriptor(status,getURI(),server.getName(),null));
		exchange.respond(CoAP.ResponseCode.CONTENT,json, MediaTypeRegistry.APPLICATION_JSON);
		//LOG.info(getName()+" on "+ server +" answered to GET with value "+status);
		System.out.println(getName()+" on "+ server.getName() +" is sending "+status);
	}
}
