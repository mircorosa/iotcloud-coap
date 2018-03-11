package it.mr.fetcher.types.cloud;

import java.util.ArrayList;

/**
 * Created by mirco on 01/05/16.
 */
public class CloudResource extends CloudObject {
	private String name = null;
	private String uri = null;
	private ArrayList<String> types = null;
	private ArrayList<CloudResource> resChildren = new ArrayList<>();

	public CloudResource(String name, String uri, ArrayList<String> types) {
		this.name = name;
		this.uri = uri;
		this.types = types;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getURI() {
		return uri;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public ArrayList<String> getTypes() {
		return types;
	}

	public void setTypes(ArrayList<String> types) {
		this.types = types;
	}

	public ArrayList<CloudResource> getResChildren() {
		return resChildren;
	}

	public void setResChildren(ArrayList<CloudResource> resChildren) {
		this.resChildren = resChildren;
	}
}
