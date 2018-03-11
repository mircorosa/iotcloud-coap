package it.mr.fetcher.types.cloud;

import java.util.ArrayList;

/**
 * Created by mirco on 01/05/16.
 */
public class CloudServer extends CloudObject {
	private String name = null, address = null;
	private int port = 0;
	private ArrayList<CloudResource> resources = null;

	public CloudServer() {/*Empty Constructor*/}

	public CloudServer(String name) {
		this.name = name;
	}

	public CloudServer(String name, String address, int port, ArrayList<CloudResource> resources) {
		this.name = name;
		this.address = address;
		this.port = port;
		this.resources = resources;
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

	public ArrayList<CloudResource> getResources() {
		return resources;
	}

	public void setResources(ArrayList<CloudResource> resources) {
		this.resources = resources;
	}
}
