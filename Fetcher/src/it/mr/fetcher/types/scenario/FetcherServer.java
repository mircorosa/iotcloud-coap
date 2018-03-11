package it.mr.fetcher.types.scenario;

import it.mr.MyPrefs;
import it.mr.fetcher.FetcherLauncher;
import it.mr.fetcher.types.cloud.CloudResource;
import it.mr.fetcher.types.cloud.CloudServer;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.WebLink;

import java.util.*;

/**
 * Created by mirco on 05/04/16.
 */
public class FetcherServer {
	private FetcherLauncher launcher;
	private String name, address;
	private int port;
	private ArrayList<FetcherResource> resources = new ArrayList<FetcherResource>();
	private CoapObserveRelation relation;
	private CoapHandler handler;

	public FetcherServer() {/*Empty Constructor*/}

	public FetcherServer(String name, String address, int port, ArrayList<FetcherResource> resources, CoapObserveRelation relation, CoapHandler handler, FetcherLauncher launcher) {
		this.name = name;
		this.address = address;
		this.port = port;
		this.resources = resources;
		this.relation = relation;
		this.handler = handler;
		this.launcher= launcher;
	}

	public String getCompleteURI() {
		return "coap://"+this.address+":"+this.port;
	}

	public String getAddressAndPort() {
		return "/"+this.address+":"+this.port;
	}

	public boolean addResource(FetcherResource resource) {
		//Checks if resource is already in list (relations could be different)
		for(FetcherResource res : resources) {
			if(resource.getName().equals(res.getName()))
				return false;
		}
		return this.resources.add(resource);
	}

	public boolean removeResource(FetcherResource resource) {
		//Direct removing doesn't work, relations could be different
		for(FetcherResource res : resources) {
			if(resource.getName().equals(res.getName()))
				return resources.remove(resource);
		}
		return false;
	}

	public FetcherResource getResource(FetcherResource resource) {
		for(FetcherResource res : resources) {
			if(resource.getName().equals(res.getName()))
				return res;
		}
		return null;
	}

	public CloudServer getCloudServer() {
		CloudServer cloudServer = new CloudServer(this.name,this.address,this.getPort(),null);
		ArrayList<CloudResource> cloudResources = new ArrayList<>();
		for(FetcherResource resource : resources)
			cloudResources.add(resource.getCloudResource());
		cloudServer.setResources(cloudResources);
		return cloudServer;
	}

	public void prettyPrint() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n|Server: ").append(this.name)
				.append("\n|Address: ").append(this.address)
				.append("\n|Port: ").append(this.port)
				.append("\n|URI: ").append(this.getAddressAndPort())
				.append("\n|Resources: ").append(getResourceCount()).append("\n");
		for(FetcherResource resource : resources) {
			builder.append("|-").append(resource.getName()).append("\ttypes: ");
			for(String type : resource.getTypes()) {
				builder.append(type).append(",");
			}
			builder.append("\tobs: ").append(resource.getObservationsCount()).append("\n");
			printChildren(resource,builder,"  ");
		}
		builder.append("\n");
		System.out.println(builder.toString());
	}

	private void printChildren(FetcherResource resource, StringBuilder builder, String prefix) {
		if(resource.getChildren()!=null) {
			for(FetcherResource res : resource.getChildren()) {
				builder.append(prefix).append("|--").append(res.getName()).append("\ttypes: ");
				for(String type : res.getTypes()) {
					builder.append(type).append(",");
				}
				builder.append("\tobs: ").append(res.getObservationsCount()).append("\n");
				printChildren(res,builder,(new String(new char[prefix.length()+3]).replace('\0',' ')));
			}
		}
	}

	public int getResourceCount() {
		int total = resources.size();
		for(FetcherResource resource : resources) {
			total+=resource.getChildCount();
		}
		return total;
	}

	public void updateFromCoAP(ArrayList<WebLink> webLinks, boolean firstTime) {
		int initialSize = webLinks.size();
		ListIterator<FetcherResource> resIterator = resources.listIterator();
		while(resIterator.hasNext()) {
			FetcherResource resource = resIterator.next();
			ArrayList<WebLink> resWebLinks = new ArrayList<>();
			ListIterator<WebLink> linkIterator = webLinks.listIterator();
			while(linkIterator.hasNext()) {
				WebLink webLink = linkIterator.next();
				if(webLink.getURI().substring(1).split("/")[0].equals(resource.getUri().substring(1).split("/")[0])) {
					resWebLinks.add(webLink);
					linkIterator.remove();
				}
			}
			if(resWebLinks.size()==0) {	//Resource to be deleted
				resource.removeChildren(false);
				resource.clearObservations();
				launcher.removeFromCloud(resource.getCloudURI());
				resIterator.remove();
			} else if(resWebLinks.size()==1) {	//Resource has no children
				resource.removeChildren(true);
			} else {    //WebLinks are sent to resource
				resWebLinks.remove(0);	//First webLink is the resource itself
				resource.updateFromCoAP(resWebLinks,true);
			}
		}
		boolean isCloudUpdateRoot = (webLinks.size()==initialSize && firstTime);	//Check if is first time
		//Adds remaining resources
		while(!webLinks.isEmpty()) {
			ArrayList<WebLink> resWebLinks = new ArrayList<>();
			while(webLinks.size()!=1 &&webLinks.get(0).getURI().substring(1).split("/")[0].equals(webLinks.get(1).getURI().substring(1).split("/")[0])) {
					resWebLinks.add(webLinks.get(1));
					webLinks.remove(1);
				}
			WebLink webLink = webLinks.get(0);
			FetcherResource resource = new FetcherResource(webLink.getAttributes().getTitle(),webLink.getURI(),this,new ArrayList<String>(webLink.getAttributes().getResourceTypes()),webLink.getAttributes().hasObservable(),null,new ArrayList<FetcherResource>());
			resources.add(resource);
			webLinks.remove(0);
			resource.updateFromCoAP(resWebLinks,false);
			if(!isCloudUpdateRoot)
				launcher.addToCloud("/"+MyPrefs.SCENARIO_NAME+"/"+getName(),resource.getCloudResource());
		}
		if(isCloudUpdateRoot && initialSize!=0)
			launcher.addToCloud("/"+MyPrefs.SCENARIO_NAME,getCloudServer());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ArrayList<FetcherResource> getResources() {
		return resources;
	}

	public void setResources(ArrayList<FetcherResource> resources) {
		this.resources = resources;
	}

	public boolean clearResources() {
		for(FetcherResource resource : resources) {
			resource.clearObservations();
		}
		this.resources.clear();
		return true;
	}

	public CoapObserveRelation getRelation() {
		return relation;
	}

	public void setRelation(CoapObserveRelation relation) {
		this.relation = relation;
	}

	public boolean cancelRelation() {
		this.relation.reactiveCancel();
		return true;
	}

	public CoapHandler getHandler() {
		return handler;
	}

	public void setHandler(CoapHandler handler) {
		this.handler = handler;
	}

	public FetcherLauncher getLauncher() {
		return launcher;
	}

	public void setLauncher(FetcherLauncher launcher) {
		this.launcher = launcher;
	}
}
