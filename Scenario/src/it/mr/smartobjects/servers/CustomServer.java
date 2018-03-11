package it.mr.smartobjects.servers;

import it.mr.types.internal.NewResource;
import it.mr.types.internal.ResType;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by mirco on 28/04/16.
 */
public class CustomServer extends GenericCoapServer {

	public CustomServer(String name, int port, ArrayList<NewResource> resources) throws IOException {
		super(name,port,null);

		StringBuilder builder = new StringBuilder("\n"+name+" created on port "+port);
		for(NewResource resource: resources) {
			addResource(resource.getName(),resource.getType());
			builder.append("\n-").append(resource.getName());
		}
		//LOG.info(builder.toString());

		this.start();
		//LOG.info(name+" started on port "+port);

		register();
	}

	public static CustomServer newNameSafeServer(String name, int port, ArrayList<NewResource> resources) throws IOException {
		if(regManager.checkServiceName(name,port))
			return new CustomServer(name,port,resources);	//Service name available
		else
			return null;	//Service name already taken
	}
}
