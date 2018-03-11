package it.mr.types.internal;

/**
 * Created by mirco on 21/04/16.
 */
public class NewResource {  //TODO Implement this using Pair library
	private String name;
	private ResType type;

	public NewResource(String name, ResType type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ResType getType() {
		return type;
	}

	public void setType(ResType type) {
		this.type = type;
	}
}
