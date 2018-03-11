package it.mr.fetcher.types.scenario;

import it.mr.MyPrefs;
import it.mr.fetcher.FetcherCoapClient;
import it.mr.fetcher.types.cloud.CloudResource;
import it.mr.fetcher.types.internal.NewObservation;
import it.mr.types.com.RequestDescriptor;
import it.mr.types.com.ResponseDescriptor;
import org.eclipse.californium.core.WebLink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.logging.Logger;

/**
 * Created by mirco on 06/04/16.
 */

public class FetcherResource {
	private String name, uri;
	private int treeDepth;
	private ArrayList<String> types;
	private boolean observable;
	private ArrayList<FetcherObservation> observations = new ArrayList<FetcherObservation>();
	private FetcherServer server;
	private FetcherResource parent;
	private ArrayList<FetcherResource> children;

	public FetcherResource() {/*Empty Constructor*/}

	public FetcherResource(String name, String uri, FetcherServer server, ArrayList<String> types, boolean observable, FetcherResource parent, ArrayList<FetcherResource> children) {
		this.name = name;
		this.uri = uri;
		this.server = server;
		this.types = types;
		this.observable = observable;
		this.parent = parent;
		this.children = children;
		this.treeDepth = uri.substring(1).split("/").length;
	}

	public String getCompleteURI() {
		return "coap://"+server.getAddress()+":"+server.getPort()+this.uri;
	}

	public String getCloudURI() {
		return "/"+MyPrefs.SCENARIO_NAME+"/"+getServer().getName()+getUri();
	}

	public CloudResource getCloudResource() {
		CloudResource resource = new CloudResource(this.name,this.uri,this.types);
		ArrayList<CloudResource> resChildren = new ArrayList<>();
		for(FetcherResource child : children) {
			resChildren.add(child.getCloudResource());
		}
		resource.setResChildren(resChildren);
		return resource;
	}

	public boolean addChildRes(String name, String uri, ArrayList<String> types, boolean observable) {
		return children.add(new FetcherResource(name,uri,this.server,types,observable,this,new ArrayList<FetcherResource>()));
	}

	public boolean removeChildRes(String name) {
		for(FetcherResource resource : children) {
			if(resource.getName().equals(name))
				return children.remove(resource);
		}
		return false;
	}

	public void removeChildren(boolean requiresCloudUpdate) {
		Iterator<FetcherResource> iterator = children.iterator();
		while(iterator.hasNext()) {
			FetcherResource resource = iterator.next();
			resource.removeChildren(false);
			resource.clearObservations();
			if(requiresCloudUpdate)	getServer().getLauncher().removeFromCloud(resource.getCloudURI());
			iterator.remove();
		}
	}

	public void updateFromCoAP(ArrayList<WebLink> webLinks, boolean fromCheck) {
		ListIterator<FetcherResource> resIterator = children.listIterator();
		while(resIterator.hasNext()) {
			FetcherResource resource = resIterator.next();
			ArrayList<WebLink> resWebLinks = new ArrayList<>();
			ListIterator<WebLink> linkIterator = webLinks.listIterator();
			while(linkIterator.hasNext()) {
				WebLink webLink = linkIterator.next();
				if(webLink.getURI().substring(1).split("/")[treeDepth].equals(resource.getUri().substring(1).split("/")[treeDepth])) {
					resWebLinks.add(webLink);
					linkIterator.remove();
				}
			}
			if(resWebLinks.size()==0) {	//Resource to be deleted
				resource.removeChildren(false);
				resource.clearObservations();
				if(fromCheck)	getServer().getLauncher().removeFromCloud(resource.getCloudURI());
				resIterator.remove();
			} else if(resWebLinks.size()==1) {	//Resource has no children
				resource.removeChildren(true);
			} else {    //WebLinks are sent to resource
				resWebLinks.remove(0);	//First webLink is the resource itself
				resource.updateFromCoAP(resWebLinks,true);
			}
		}
		//Adds remaining resources
		while(!webLinks.isEmpty()) {
			ArrayList<WebLink> resWebLinks = new ArrayList<>();
			while(webLinks.size()!=1 && webLinks.get(0).getURI().substring(1).split("/")[treeDepth].equals(webLinks.get(1).getURI().substring(1).split("/")[treeDepth])) {
				resWebLinks.add(webLinks.get(1));
				webLinks.remove(1);
			}
			WebLink webLink = webLinks.get(0);
			FetcherResource resource = new FetcherResource(webLink.getAttributes().getTitle(),webLink.getURI(),this.server,new ArrayList<String>(webLink.getAttributes().getResourceTypes()),webLink.getAttributes().hasObservable(),this,new ArrayList<FetcherResource>());
			children.add(resource);
			webLinks.remove(0);
			resource.updateFromCoAP(resWebLinks,false);
			if(fromCheck)
				getServer().getLauncher().addToCloud(getCloudURI(),resource.getCloudResource());
		}
	}

	public ResponseDescriptor handleGet(RequestDescriptor reqDescriptor) {
		if(treeDepth==reqDescriptor.getResourceURI().length) {	//Process GET request
			if(!reqDescriptor.isObserving())	//Simple GET request
				return server.getLauncher().getCoapClient().doCoapGet(this);
			else if (this.getObservationsCount()!=0) {
				for(FetcherObservation observation : this.getObservations()){
					if(Arrays.equals(observation.getToken(),reqDescriptor.getToken())) { //Resource already observed
						server.getLauncher().getLog().info("Stopping observing with token "+new String(reqDescriptor.getToken()));
						this.removeObservation(observation);
						server.getLauncher().getLog().info("## "+this.getObservationsCount()+" clients observing on "+server.getName()+"/"+this.getName()+" ##");
						return server.getLauncher().getCoapClient().doCoapGet(this);	//Last fresh value returned
					}
				}
				server.getLauncher().getLog().info("Starting observing ("+(this.getObservationsCount()+1)+")...");
				NewObservation obs = server.getLauncher().getCoapClient().doCoapObserve(this);
				if(!this.addObservation(obs.getObservation())) {
					server.getLauncher().getLog().warning("Failed adding observation");
					return null;
				}
				server.getLauncher().getLog().info("## "+this.getObservationsCount()+" clients observing on "+server.getName()+"/"+this.getName()+" ##");
				return obs.getRespDescriptor();	//New observing relation is created
			}
			else {	//First Observation
				server.getLauncher().getLog().info("Starting observing ("+(this.getObservationsCount()+1)+")...");
				NewObservation obs = server.getLauncher().getCoapClient().doCoapObserve(this);
				if(!this.addObservation(obs.getObservation())) {
					server.getLauncher().getLog().warning("Failed adding observation");
					return null;
				}
				server.getLauncher().getLog().info("## "+this.getObservationsCount()+" clients observing on "+server.getName()+"/"+this.getName()+" ##");
				return obs.getRespDescriptor();	//New observing relation is created
			}
		} else {
			for(FetcherResource child : children) {
				if(child.getName().equals(reqDescriptor.getResourceURI()[treeDepth]))
					return child.handleGet(reqDescriptor);
			}
		}
		return null;
	}

	public boolean handlePost(RequestDescriptor reqDescriptor) {
		if(treeDepth==reqDescriptor.getResourceURI().length) {
			return server.getLauncher().getCoapClient().doCoapPost(this,reqDescriptor.getValue());
		} else {
			for(FetcherResource child : children) {
				if(child.getName().equals(reqDescriptor.getResourceURI()[treeDepth]))
					return child.handlePost(reqDescriptor);
			}
		}
		return false;
	}

	public int getChildCount() {
		int count = children.size();
		for(FetcherResource resource : children) {
			count+=resource.getChildCount();
		}
		return count;
	}

	public boolean addObservation(FetcherObservation observation) {
		//Checks for duplicates
		for(FetcherObservation obs : observations) {
			if(obs.getToken().equals(observation.getToken())) {
				return false;
			}
		}
		this.observations.add(observation);
		return true;
	}

	public FetcherObservation getObservation(byte[] token) {
		for(FetcherObservation obs : observations) {
			if(obs.getToken().equals(token)) {
				return obs;
			}
		}
		return null;
	}

	public boolean removeObservation(FetcherObservation observation) {
		observation.getRelation().reactiveCancel();
		return observations.remove(observation);
	}

	public int getObservationsCount() {
		return observations.size();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getTypes() {
		return types;
	}

	public void setTypes(ArrayList<String> types) {
		this.types = types;
	}

	public ArrayList<FetcherObservation> getObservations() {
		return observations;
	}

	public void clearObservations() {
		for(FetcherObservation obs : observations) {
			obs.getRelation().reactiveCancel();
		}
		observations.clear();
	}

	public FetcherServer getServer() {
		return server;
	}

	public void setServer(FetcherServer server) {
		this.server = server;
	}

	public boolean isObservable() {
		return observable;
	}

	public void setObservable(boolean observable) {
		this.observable = observable;
	}

	public FetcherResource getParent() {
		return parent;
	}

	public void setParent(FetcherResource parent) {
		this.parent = parent;
	}

	public ArrayList<FetcherResource> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<FetcherResource> children) {
		this.children = children;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}
