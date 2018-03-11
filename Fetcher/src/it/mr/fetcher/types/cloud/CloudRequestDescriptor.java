package it.mr.fetcher.types.cloud;

import it.mr.fetcher.types.cloud.CloudResource;
import it.mr.fetcher.types.cloud.CloudScenario;
import it.mr.fetcher.types.cloud.CloudServer;

import java.util.ArrayList;

//TODO To be deleted.
public class CloudRequestDescriptor {
	private CloudScenario scenario;
	private CloudServer server;
	private ArrayList<CloudResource> resources;

	public CloudRequestDescriptor() {/*Empty Constructor*/}

	public CloudRequestDescriptor(CloudScenario scenario, CloudServer server, ArrayList<CloudResource> resources) {
		this.scenario = scenario;
		this.server = server;
		this.resources = resources;
	}

	public CloudScenario getScenario() {
		return scenario;
	}

	public void setScenario(CloudScenario scenario) {
		this.scenario = scenario;
	}

	public CloudServer getServer() {
		return server;
	}

	public void setServer(CloudServer server) {
		this.server = server;
	}

	public ArrayList<CloudResource> getResources() {
		return resources;
	}

	public void setResources(ArrayList<CloudResource> resources) {
		this.resources = resources;
	}
}

/*	private String scenario=null, server=null, resource=null;
	private ArrayList<SmartObjectDescriptor> serverList=null;
	private ArrayList<String> resources=null;

	public CloudRequestDescriptor(){}
	public CloudRequestDescriptor(ArrayList<SmartObjectDescriptor> serverList, String... parameters) {
		this.serverList=serverList;
		int size=parameters.length;
		switch (size) {
		case 3: this.resource=parameters[2];
		case 2: this.server=parameters[1];
		case 1: this.scenario=parameters[0];
		default:break;
		}
	}

	public String getScenario() {
		return scenario;
	}
	public String getServer() {
		return server;
	}
	public String getResourceURI() {
		return resource;
	}
	public ArrayList<SmartObjectDescriptor> getServerList() {
		return serverList;
	}
	public ArrayList<String> getResources() {
		return this.resources;
	}

	public void setScenario(String scenario) {
		this.scenario=scenario;
	}
	public void setServer(String server) {
		this.server=server;
	}
	public void setResourceURI(String resource) {
		this.resource=resource;
	}
	public void setServerList(ArrayList<SmartObjectDescriptor> serverList) {
		this.serverList=serverList;
	}
	public void setResources(ArrayList<String> resources) {
		this.resources=resources;
	}*/
