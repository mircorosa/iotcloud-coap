package it.mr.fetcher.types.cloud;

import java.util.ArrayList;

/**
 * Created by mirco on 01/05/16.
 */

//TODO Class to be used in multiple scenarios implementation

public class CloudScenario extends CloudObject {
	private String name = null;
	private ArrayList<CloudServer> servers = new ArrayList<>();

	public CloudScenario() {/*Empty Constructor*/}

	public CloudScenario(String name) {
		this.name = name;
	}

	public CloudScenario(String name, ArrayList<CloudServer> servers) {
		this.name = name;
		this.servers = servers;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<CloudServer> getServers() {
		return servers;
	}

	public void setServers(ArrayList<CloudServer> servers) {
		this.servers = servers;
	}
}
