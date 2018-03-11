package it.mr.fetcher;

import it.mr.types.com.ResponseDescriptor;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import it.mr.types.internal.FetcherLogFormatter;
import it.mr.fetcher.types.internal.NewObservation;
import it.mr.fetcher.types.scenario.FetcherObservation;
import it.mr.fetcher.types.scenario.FetcherResource;
import it.mr.fetcher.types.scenario.FetcherServer;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.network.CoAPEndpoint;

import com.google.gson.Gson;

public class FetcherCoapClient extends CoapClient {

	private static final Logger LOG = Logger.getLogger("COAP");

	FetcherLauncher launcher;
	SecureRandom sr = new SecureRandom();

	public FetcherCoapClient(FetcherLauncher launcher) {
		super();
		this.setEndpoint(new CoAPEndpoint());

		setupLogger(LOG);
		this.launcher=launcher;
	}

	/**
	 * Makes a GET request to the specified client, returning the response immediately
	 * @param resource containing request data
	 * @return the ResponseDescriptor with response data
	 */
	public ResponseDescriptor doCoapGet(FetcherResource resource) {
		//Set CoAP Client URI to request destination
		this.setURI(resource.getCompleteURI());
		final ResponseDescriptor responseDesc=new ResponseDescriptor();

		Request getRequest = new Request(Code.GET);
		getRequest.setURI(resource.getCompleteURI()); //May be optional

		//Semaphore used in order to wait for the response of the server before ending 
		//the java process
		final Semaphore semaphore = new Semaphore(0);

		CoapHandler handler = new CoapHandler() {
			@Override
			public void onLoad(CoapResponse response) {
				ResponseDescriptor desc = new Gson().fromJson(response.getResponseText(), ResponseDescriptor.class);
				responseDesc.setResource(desc.getResource());
				responseDesc.setServer(desc.getServer());
				responseDesc.setValue(desc.getValue());
//				responseDesc.setToken(null);
				semaphore.release(1);
			}

			@Override
			public void onError() {
				LOG.severe("GET Failed");
				System.err.println("COAP: Failed");
			}
		};
		this.advanced(handler, getRequest);

		// Wait until all requests finished
		try {
			semaphore.acquire(1);
		} catch (InterruptedException e) {}

		this.setURI(null);
		return responseDesc;
	}

	public boolean doCoapPost(FetcherResource resource, String value) {
		//Set CoAP Client URI to request destination
		this.setURI(resource.getCompleteURI());
		final BooleanWrapper result = new BooleanWrapper();

		Request postRequest = new Request(Code.POST);
		postRequest.setURI(resource.getCompleteURI()); //May be optional
		postRequest.setPayload(value);

		//Semaphore used in order to wait for the response of the server before ending
		//the java process
		final Semaphore semaphore = new Semaphore(0);

		CoapHandler handler = new CoapHandler() {
			@Override
			public void onLoad(CoapResponse response) {
				if(response.getCode().equals(CoAP.ResponseCode.CHANGED))
					result.set(true);
				else
					result.set(false);
				semaphore.release(1);
			}
			@Override
			public void onError() {
				LOG.severe("POST Failed");
				System.err.println("COAP: Failed");
			}
		};
		this.advanced(handler, postRequest);

		// Wait until all requests finished
		try {
			semaphore.acquire(1);
		} catch (InterruptedException e) {}

		this.setURI(null);
		return result.get();
	}

	/**
	 * Makes a GET request with Observe flag to the specified client, registering the latter to
	 * the specified resource.
	 * @param resource containing request data
	 * @return the ResponseDescriptor with response data
	 */
	public NewObservation doCoapObserve(FetcherResource resource) {
		this.setURI(resource.getCompleteURI());
		final ResponseDescriptor responseDesc=new ResponseDescriptor();

		Request getRequest = new Request(Code.GET);
		getRequest.setObserve();
		getRequest.setURI(resource.getCompleteURI()); //May be optional
		final byte[] token = new byte[8];
		for(int i=0; i<8; i++) {
			token[i]=(byte)((char)(sr.nextInt(26)+'a'));  //Alphabetical is good for URLs and enough for now
		}
		getRequest.setToken(token);

		//Semaphore used in order to wait for the response of the server before ending 
		//the java process
		final Semaphore semaphore = new Semaphore(0);

		CoapHandler handler = new CoapHandler() {
			@Override
			public void onLoad(CoapResponse response) {
				ResponseDescriptor desc = new Gson().fromJson(response.getResponseText(), ResponseDescriptor.class);
				responseDesc.setResource(desc.getResource());
				responseDesc.setServer(desc.getServer());
				responseDesc.setValue(desc.getValue());
				//Sending Tokens, same resource can be observed from more clients 
				//Check occurs at GCM level (message sorting, better) or Android level (Token checked directly)
				responseDesc.setToken(token);
				launcher.handleObserving(responseDesc);

				semaphore.release(1);
			}

			@Override
			public void onError() {
				LOG.severe("Failed Observing");
				System.err.println("COAP: Failed observing");
			}
		};
		CoapObserveRelation relation = this.observe(getRequest, handler);

		// Wait until all requests finished
		try {
			semaphore.acquire(1);
		} catch (InterruptedException e) {}

		this.setURI(null);
		return new NewObservation(new FetcherObservation(token,handler,relation),responseDesc);
	}


	//Observing Scenario
	public FetcherServer doServerInit(String name, String address, int port) {
		CoapObserveRelation relation;

		this.setURI("coap://"+address+":"+port);

		Request getRequest = new Request(Code.GET);
		getRequest.setObserve();
		getRequest.setURI("coap://"+address+":"+port);
		getRequest.getOptions().clearUriPath().clearUriQuery().setUriPath("/.well-known/core");
		getRequest.getOptions().setUriQuery("rt=*"); //Takes all resource types

		final byte[] token = new byte[8];
		sr.nextBytes(token);
		getRequest.setToken(token);

		final FetcherServer server = new FetcherServer(name,address,port,new ArrayList<FetcherResource>(),null,null,launcher);	//Server Object
		//Semaphore used in order to wait for the response of the server before ending 
		//the java process
		final Semaphore semaphore = new Semaphore(0);

		final BooleanWrapper isFirstTime = new BooleanWrapper(true);

		CoapHandler handler = new CoapHandler() {
			@Override
			public void onLoad(CoapResponse response) {
				ArrayList<WebLink> webLinks = new ArrayList<>(LinkFormat.parse(response.getResponseText()));
				System.out.println("Resource update received.");
				server.updateFromCoAP(webLinks,isFirstTime.use());
				server.prettyPrint();
				semaphore.release(1);
			}

			@Override
			public void onError() {
				LOG.severe("Failed Observing Server");
				System.err.println("COAP: Failed observing");
			}
		};

		relation = this.observe(getRequest, handler);

		server.setRelation(relation);
		server.setHandler(handler);

		// Wait until all requests finished
		try {
			semaphore.acquire(1);
		} catch (InterruptedException e) {}

		this.setURI(null);

		return server;
	}

	private class BooleanWrapper {
		boolean value;
		private BooleanWrapper() {}
		private BooleanWrapper(boolean value) { this.value = value; }
		private void set(boolean aBoolean) { this.value=aBoolean; }
		private boolean get() {return value;}
		private boolean use() {
			if(value) {
				value=false;
				return true;
			}
			return value;
		}
	}

	private void setupLogger(Logger logger) {
		logger.setUseParentHandlers(false);
		FetcherLogFormatter formatter = new FetcherLogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(formatter);
		logger.addHandler(handler);
	}
}
