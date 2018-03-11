package it.mr.smartobjects.servers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.mr.MyPrefs;
import it.mr.jmdns.ServerRegManager;
import it.mr.smartobjects.resources.GenericCoapResource;
import it.mr.smartobjects.resources.ResourceFactory;
import it.mr.types.internal.FetcherLogFormatter;
import it.mr.types.internal.ResType;
import it.mr.types.internal.ServerType;
import org.eclipse.californium.core.CoapServer;

/*
 * Server Object: it instantiates resource objects and adds them,
 * but does not deal directly with requests (it acts like a relay node).
 */

public abstract class GenericCoapServer extends CoapServer {

	protected Logger LOG;
	protected ConsoleHandler handler;
	protected String name;
	protected int port;
	protected ArrayList<GenericCoapResource> resources = new ArrayList<>();	//1st tree level
	protected static ServerRegManager regManager = new ServerRegManager();

	protected GenericCoapServer(String name, int port, ServerType serverType) throws IOException {
		super(port);
		this.name=name;
		this.port=port;

		LOG = Logger.getLogger(name.toUpperCase());
		setupLogger(LOG, MyPrefs.SERVERS_LOG_LEVEL);

		if(serverType!=null) {
			StringBuilder builder = new StringBuilder(name+" created on port "+port);
			for(ResType resType : serverType.getResources()) {
				String resName = autoResName(resType);
				addResource(resName,resType);
//				builder.append("\n-").append(resName);
			}
			//LOG.info(builder.toString());
			System.out.println(builder.toString());

			this.start();
			//LOG.info(name+" started on port "+port);

			register();
		}
	}

	protected String autoResName(ResType type) {
		int count = 1;
		for(int i=0; i<resources.size(); i++) {
			if(resources.get(i).getName().equals(type.toString()+count)) {
				++count;
				i=0;
			}
		}
		return type.toString()+count;
	}

	public boolean isNameEligible(String name) {
		for(GenericCoapResource res : resources) {
			if(name.equals(res.getName()))
				return false;
		}
		return true;
	}

	public GenericCoapResource getResource(String name) {
		for(GenericCoapResource resource : resources) {
			if(resource.getName().equals(name))	return resource;
		}
		return null;
	}

	public boolean addResource(GenericCoapResource resource) {
		for(GenericCoapResource res : resources) {
			if(res.getName().equals(resource.getName()))
				return false;
		}
		this.add(resource);
		//LOG.info(resource.getName()+" resource added to "+this.name);
		return resources.add(resource);
	}

	public boolean addResource(String name, ResType resType) {
		GenericCoapResource resource = new ResourceFactory().getResource(name,"",resType,this);
		if(resource==null)	return false;
		for(GenericCoapResource res : resources) {	//Duplicates Check
			if(resource.getName().equals(res.getName()))
				return false;
		}
		this.add(resource);
		//LOG.info(resource.getName()+" resource added to "+this.name);
		return resources.add(resource);
	}

	public boolean addResource(ResType resType) {
		return addResource(autoResName(resType),resType);
	}

	public boolean removeResource(GenericCoapResource resource) throws IOException {
		for(GenericCoapResource res : resources) {
			if(resource.getName().equals(res.getName())) {
				res.removeChildren();
				this.remove(res);
				res.removeLogHandler();
				//LOG.info(resource.getName()+" resource removed from "+this.name);
				return resources.remove(res);
			}
		}
		return false;
	}

	public void stopServer() throws IOException {
		unregister();
		this.stop();  //Removing resources before stopping triggers well-known observation on Fetcher (bad for cloud)
		Iterator<GenericCoapResource> iterator = resources.iterator();
		while(iterator.hasNext()) {
			GenericCoapResource resource = iterator.next();
			resource.removeChildren();
			this.remove(resource);
			iterator.remove();
			//LOG.info(resource.getName()+" resource removed from "+this.name);
		}
		//LOG.info(this.name+" stopped on port "+port);
		LOG.removeHandler(handler);
	}

	public void unregister() throws IOException {
		regManager.unregisterService(name+":"+port);
		//LOG.info("mDNS: "+this.name+" service unregistered");
	}

	public void register() throws IOException {
		regManager.registerOnFirstNetworkInterface(name+":"+port);
		//LOG.info("mDNS: "+this.name+" service registered");
	}

	public void notifyWellKnown() {
		knowncore.changed();
	}

	public ArrayList<GenericCoapResource> getResources() {
		return this.resources;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	private void setupLogger(Logger logger, Level level) {
		logger.setUseParentHandlers(false);
		FetcherLogFormatter formatter = new FetcherLogFormatter();
		handler = new ConsoleHandler();
		handler.setFormatter(formatter);
		handler.setLevel(level);
		logger.addHandler(handler);
	}
}
