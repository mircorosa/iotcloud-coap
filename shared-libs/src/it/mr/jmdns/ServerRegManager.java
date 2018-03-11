package it.mr.jmdns;

/*
*
* @author Luca Davoli - <a href="mailto:lucadavo@gmail.com">lucadavo@gmail.com</a> - Department of Information Engineering - University of Parma
*
*/

import com.sun.corba.se.spi.activation.Server;
import javafx.beans.binding.StringBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class ServerRegManager {

	/*
	In this study case we have multiple servers running on the same machine, and this forces ServerRegManager
	(which is static) to handle service discovery for all servers. In real use cases each server has his
	serverRegManager object, so it's better to declare here class field such as service, port and serverName instead
	of taking them as parameters every time from server.
	*/

	JmDNS jmdns;
	
	public ServerRegManager() {
		jmdns = JmDNSManager.getInstance().getJmDNS();
	}
	
	public void registerOnFirstNetworkInterface(String serverName) {
		registerOnFirstNetworkInterface(Constant.DEFAULT_COAP_SERVICE_PORT, serverName);
	}
	
	public void registerOnFirstNetworkInterface(int servicePortCoAP, String serverName) {
		try {
			ServiceInfo service = ServiceInfo.create(Constant.DEFAULT_JMDNS_COAP_UDP_LOCAL, serverName, servicePortCoAP, 0, 0, "");
			jmdns.registerService(service);
			//System.out.println("SERVICE TO BE ADDED: "+service.getName());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean checkServiceName(String name, int port) {
		ServiceInfo[] list = jmdns.list(Constant.DEFAULT_JMDNS_COAP_UDP_LOCAL); //TODO create a copy of list and cycle on that, it maybe avoid slowdown (probably not)
		for(ServiceInfo info : list) {
			String serverName = info.getName();
			String pattern = "(\\S+):(\\S+)";	//name:port
			serverName=serverName.replaceAll(pattern,"$1 $2");
			String[] splitName = serverName.split("\\s");
			if(splitName[0].equals(name) && splitName[1].equals(port))
				return false;
		}
		return true;
	}

	public void unregisterService(String serverName) {
		unregisterService(Constant.DEFAULT_COAP_SERVICE_PORT, serverName);
	}

	public void unregisterService(int servicePortCoAP, String serverName) {
		ServiceInfo service = ServiceInfo.create(Constant.DEFAULT_JMDNS_COAP_UDP_LOCAL, serverName, servicePortCoAP, 0, 0, "");
		//System.out.println("SERVICE TO BE REMOVED: "+service.getName());
		jmdns.unregisterService(service);
	}

	public void addServiceListener(ServiceListener listener) {
		jmdns.list(Constant.DEFAULT_JMDNS_COAP_UDP_LOCAL);	//See ServiceListener on FetcherLauncher
		jmdns.addServiceListener(Constant.DEFAULT_JMDNS_COAP_UDP_LOCAL, listener);
	}
}
