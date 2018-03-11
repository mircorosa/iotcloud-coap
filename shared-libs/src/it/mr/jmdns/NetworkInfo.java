package it.mr.jmdns;

import java.util.Enumeration;
import java.net.*;

public class NetworkInfo {

	public static final boolean IPv4 = true;
	public static final boolean IPv6 = false;

	public static String getNetworkAddress(boolean ipv4){
		try{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()){
				NetworkInterface current = interfaces.nextElement();
				if(!current.isLoopback()){
					if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
					Enumeration<InetAddress> addresses = current.getInetAddresses();
					while (addresses.hasMoreElements()){
						InetAddress current_addr = addresses.nextElement();
						if (current_addr.isLoopbackAddress()) continue;
						if (current_addr instanceof Inet4Address){
							if(ipv4 == IPv4) return current_addr.getHostAddress();
						}
						else if (current_addr instanceof Inet6Address){
							if(ipv4 == IPv6) return current_addr.getHostAddress();
						}
					}
				}
			}
		} catch(SocketException e){}
		return null;
	}
	
	public static InetAddress getInetAddress(String ip){
		if(ip == null) return null;
		InetAddress address[];
		try {
			address = InetAddress.getAllByName(ip);
			return address[0];
		} catch (UnknownHostException e) {
			return null;
		}
	}

	public static final void printInterfaces(){
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()){
				NetworkInterface current = interfaces.nextElement();
				System.out.println("Interface:" + current.getDisplayName());
				Enumeration<InetAddress> addresses = current.getInetAddresses();
				while (addresses.hasMoreElements()){
					InetAddress current_addr = addresses.nextElement();
					if(current_addr instanceof Inet4Address){
						System.out.println("\t[IPv4]: " + current_addr);
					}
					else if(current_addr instanceof Inet6Address){
						System.out.println("\t[IPv6]: " + current_addr);
					}
				}
			}
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
	}
	
}
