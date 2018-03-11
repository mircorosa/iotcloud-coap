package it.mr.jmdns;

/*
*
* @author Luca Davoli - <a href="mailto:lucadavo@gmail.com">lucadavo@gmail.com</a> - Department of Information Engineering - University of Parma
*
*/

import java.io.IOException;

import javax.jmdns.JmDNS;

public class JmDNSManager {
	
	private static JmDNSManager instance;
	private static JmDNS jmdns = null;
	
	private JmDNSManager() { /**/ }
	
	public static JmDNSManager getInstance() {
		if (instance == null) {
			instance = new JmDNSManager();
		}
		
		return instance;
	  }
	
	public JmDNS getJmDNS() {
		if (jmdns == null) {
			try {
				jmdns = JmDNS.create(NetworkInfo.getInetAddress(NetworkInfo.getNetworkAddress(NetworkInfo.IPv4)), "");
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return jmdns;
	}
	
	public void setJmDNS(JmDNS jmDNS) { jmdns = jmDNS; }

}
