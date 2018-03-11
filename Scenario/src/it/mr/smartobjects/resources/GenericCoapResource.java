package it.mr.smartobjects.resources;

import it.mr.MyPrefs;
import it.mr.smartobjects.servers.GenericCoapServer;
import it.mr.types.internal.FetcherLogFormatter;
import it.mr.types.internal.ResType;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.Type;

import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Resource Object: deals with each requests autonomously, and
 * it must be added to a server to be used.
 */
public abstract class GenericCoapResource extends CoapResource{

	protected Logger LOG;
	protected ConsoleHandler handler;
	protected GenericCoapServer server;
	public GenericCoapResource resParent = null;
	protected ArrayList<GenericCoapResource> resChildren = new ArrayList<>();

	protected GenericCoapResource(String name, String parentUri, GenericCoapServer server, boolean observable, String... types) {
		super(name,true);
		this.server = server;
		//getURI() is not available when this constructor is called
		LOG = Logger.getLogger((server.getName()+parentUri+"/"+name).toUpperCase());
		setupLogger(LOG, MyPrefs.RESOURCE_LOG_LEVEL);

		setObservable(observable);
		setObserveType(Type.CON); //CON use ACKs, NON don't (resources with high frequency updating should be NON)
		for(String type: types)
			getAttributes().addResourceType(type);
		getAttributes().setTitle(name);
		if(observable)
			getAttributes().setObservable();
	}

	public ArrayList<GenericCoapResource> getResChildren() {
		return resChildren;
	}

	public void setResChildren(ArrayList<GenericCoapResource> resChildren) {
		this.resChildren = resChildren;
	}

	public GenericCoapResource getResParent() {
		return resParent;
	}

	public void setResParent(GenericCoapResource resParent) {
		this.resParent = resParent;
	}

	public boolean addChildRes(GenericCoapResource resource, boolean silent) {
		if(resource==null)	return false;
		for(GenericCoapResource res : resChildren) {
			if(res.getName().equals(resource.getName()))
				return false;
		}
		this.add(resource);
		if(!silent)
			server.notifyWellKnown();
		//LOG.info(resource.getName()+" resource added to "+getName());
		resource.setResParent(this);
		return this.resChildren.add(resource);
	}

	public boolean addChildRes(ResType type, boolean silent) {
		return addChildRes(autoResName(type),type,silent);
	}

	public boolean addChildRes(String name, ResType type, boolean silent) {
		GenericCoapResource resource = new ResourceFactory().getResource(name,getURI(),type, server);
		if(resource==null)	return false;
		for(GenericCoapResource res : resChildren) {
			if(resource.getName().equals(res.getName()))
				return false;
		}
		this.add(resource);
		if(!silent)
			server.notifyWellKnown();
		//LOG.info(resource.getName()+" resource added to "+getName());
		resource.setResParent(this);
		return resChildren.add(resource);
	}

	public boolean removeChildRes(GenericCoapResource resource) {
		for(GenericCoapResource res : resChildren) {
			if(res.getName().equals(resource.getName())) {
				res.removeChildren();
				this.remove(res.getName());
				res.removeLogHandler();
				server.notifyWellKnown();
				//LOG.info(resource.getName()+" resource removed from "+this.getName());
				return resChildren.remove(res);
			}
		}
		return false;
	}

	public boolean removeChildren() {
		for(GenericCoapResource resource : resChildren) {
			if(resource.removeChildren()) {
				this.remove(resource.getName());
				resource.removeLogHandler();
				//LOG.info(resource.getURI()+" resource removed");
			}
		}
		resChildren.clear();
		return true;
	}

	protected String autoResName(ResType type) {
		int count = 1;
		for(int i=0; i<resChildren.size(); i++) {
			if(resChildren.get(i).getName().equals(type.toString()+count)) {
				++count;
				i=0;
			}
		}
		return type.toString()+count;
	}

	public boolean isNameEligible(String name) {
		for(GenericCoapResource res : resChildren) {
			if(name.equals(res.getName()))
				return false;
		}
		return true;
	}

	private void setupLogger(Logger logger, Level level) {
		logger.setUseParentHandlers(false);
		FetcherLogFormatter formatter = new FetcherLogFormatter();
		handler = new ConsoleHandler();
		handler.setFormatter(formatter);
		handler.setLevel(level);
		logger.addHandler(handler);
	}

	public void removeLogHandler() {
		LOG.removeHandler(handler);
	}
}
