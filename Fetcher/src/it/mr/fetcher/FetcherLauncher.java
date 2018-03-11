package it.mr.fetcher;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.mr.MyPrefs;
import it.mr.fetcher.types.cloud.CloudResource;
import it.mr.fetcher.types.cloud.CloudScenario;
import it.mr.fetcher.types.cloud.CloudServer;
import it.mr.jmdns.Constant;
import it.mr.jmdns.JmDNSManager;
import it.mr.jmdns.ServerRegManager;
import it.mr.fetcher.types.cloud.CloudObject;
import it.mr.types.com.MessageDescriptor;
import it.mr.types.com.RequestDescriptor;
import it.mr.types.com.ResponseDescriptor;
import it.mr.types.internal.FetcherLogFormatter;
import it.mr.fetcher.types.internal.NewObservation;
import it.mr.fetcher.types.scenario.FetcherObservation;
import it.mr.fetcher.types.scenario.FetcherResource;
import it.mr.fetcher.types.scenario.FetcherServer;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

/**
 * <h1>Fetcher</h1>
 * The it.mr.FetcherLauncher handles all the internal/external communications using HTTP or CoAP,
 * and creates the necessary instances of objects.
 * @author mirco
 */
public class FetcherLauncher {

	private static final Logger LOG = Logger.getLogger("LAUNCHER");

    private ArrayList<FetcherServer> smartObjects = new ArrayList<>();
    private FetcherHTTPServer fHttpServer;
    private FetcherCoapClient fCoapClient;
	private ServiceListener coAPServiceListener;
	private ServerRegManager regManager;

    public FetcherLauncher() throws IOException {
		setupLogger(LOG);
        fHttpServer = new FetcherHTTPServer(this);
        fCoapClient = new FetcherCoapClient(this);
		regManager = new ServerRegManager();

		removeFromCloud("/"+MyPrefs.SCENARIO_NAME);

		//Cloud sample
//		CloudScenario scenario = new CloudScenario("TestScenario");
//		ArrayList<CloudServer> servers = new ArrayList<>();
//		for(int i=1; i<=1; i++) {
//			CloudServer server = new CloudServer("Server"+i);
//			server.setAddress("192.168.0.something");
//			server.setPort(10000);
//			ArrayList<CloudResource> resources = new ArrayList<>();
//			for(int j=1; j<=1; j++) {
//				CloudResource res1 = new CloudResource("Resource"+j,"Resource/Uri/Here",new ArrayList<String>());
//				res1.getTypes().add("Type1");
//				res1.getTypes().add("Type2");
//				for(int k=1; k<=1; k++) {
//					CloudResource res2 = new CloudResource("Resource"+k,"Resource/Uri/Here/Deeper",new ArrayList<String>());
//					res2.getTypes().add("Type1");
//					res1.getResChildren().add(res2);
//				}
//				resources.add(res1);
//			}
//			server.setResources(resources);
//			servers.add(server);
//		}
//		scenario.setServers(servers);

//		addToCloud("",new CloudScenario(MyPrefs.SCENARIO_NAME));
		initDiscovery();
    }

    /**
     * Makes appropriate calls to CoAP clients based on the RequestDescriptor parameter,
     * returning data in a ResponseDescriptor.
     * @param reqDescriptor containing request data
     * @return the ResponseDescriptor with response data
     */
    public ResponseDescriptor handleHTTPGet(RequestDescriptor reqDescriptor) {
        for(FetcherServer server : smartObjects) {
            if (server.getName().equals(reqDescriptor.getServer()) ) {
                for(FetcherResource resource : server.getResources()) {
					if(resource.getName().equals(reqDescriptor.getResourceURI()[0])) {
						return resource.handleGet(reqDescriptor);
					}
				}
				LOG.warning("No Resource Found");
				return null;
            }
        }
		LOG.warning("No Server Found");
		return null;
    }

	public boolean handleHTTPPost(RequestDescriptor reqDescriptor) {
		for(FetcherServer server : smartObjects) {
			if (server.getName().equals(reqDescriptor.getServer()) ) {
				for(FetcherResource resource : server.getResources()) {
					if(resource.getName().equals(reqDescriptor.getResourceURI()[0]))
						return resource.handlePost(reqDescriptor);
				}
				LOG.warning("No Resource Found");
				return false;
			}
		}
		LOG.warning("No Server Found");
		return false;
	}

    /**
     * Sends all the necessary values from observing resource to PhantomJS Server, including the
     * token of the current relation.
     * @param responseDesc containing request data
     */
    public void handleObserving(ResponseDescriptor responseDesc) {
        //Sends values from observing to PhantomJS Server.
        try {
            // Define the server endpoint to send the HTTP request to
            URL serverUrl = new URL("http://localhost:"+MyPrefs.PHANTOMJS_PORT);
            HttpURLConnection urlConnection = (HttpURLConnection)serverUrl.openConnection();

            // Indicate that we want to write to the HTTP request body
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Writing the post data to the HTTP request body
//            MessageDescriptor message = new MessageDescriptor("value", new Gson().toJson(responseDesc)); //Message encapsulation
            BufferedWriter httpRequestBodyWriter = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
            httpRequestBodyWriter.write(new Gson().toJson(responseDesc));
            httpRequestBodyWriter.close();

            // Reading from the HTTP response body (sent by PhantomJS)
            Scanner httpResponseScanner = new Scanner(urlConnection.getInputStream());
            /*while(httpResponseScanner.hasNextLine()) {
            	LOG.info(httpResponseScanner.nextLine());
            }*/
            httpResponseScanner.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

		LOG.info("Sending value "+responseDesc.getValue()+" from "+responseDesc.getServer()+"/"+responseDesc.getResource()+" (token:"+ new String(responseDesc.getToken())+")");
    }

	//Server Discovery
	private void initDiscovery() {
		coAPServiceListener = new ServiceListener() {
			public void serviceResolved(ServiceEvent ev) {
				//For some reason if we already have services registered, on first "discovery"
				//jmdns won't resolve the first service if the number of services is >1 .
				//Practical workaround: query jmdns for services list before adding ServiceListener (see ServerRegManager)
				String name = ev.getName();
				String pattern = "(\\S+):(\\S+)";	//name:port
				name=name.replaceAll(pattern, "$1 $2");
				String[] splitName = name.split("\\s");

				addSmartObject(splitName[0],ev.getInfo().getHostAddresses()[0],Integer.valueOf(splitName[1]));
				LOG.info("mDNS: Resolved Service " + ev.getName());
			}

			public void serviceRemoved(ServiceEvent ev) {
				String name = ev.getName();
				String pattern = "(\\S+):(\\S+)";
				name=name.replaceAll(pattern, "$1 $2");
				String[] splitName = name.split("\\s");

				/*#####IMPORTANT##### in serviceRemoved, ev.getInfo().getHostAddresses()[0] doesn't always work: probably because it's an "asynchronous call" and the object observed is already gone at the check*/
				removeSmartObject(splitName[0],Integer.valueOf(splitName[1]));
				LOG.info("mDNS: Removed Service "+ev.getName());
			}

			public void serviceAdded(ServiceEvent event) {
				JmDNSManager.getInstance().getJmDNS().requestServiceInfo(event.getType(), event.getName(), 60000);
				LOG.info("mDNS: Added Service " + event.getName());
			}
		};

		regManager.addServiceListener(coAPServiceListener);
		LOG.info("mDNS: Listener initialized on "+Constant.DEFAULT_JMDNS_COAP_UDP_LOCAL);
	}

	private boolean addSmartObject(String name, String address, int port) {
		for(FetcherServer server : smartObjects) {
			if(server.getName().equals(name) && server.getAddress().equals(address) && server.getPort()==port)
				return false;
		}

		smartObjects.add(fCoapClient.doServerInit(name,address,port));
		LOG.info("ADD SERVER TO CLOUD!");
		return true;
	}

	private boolean removeSmartObject(String name, int port) {
		for(FetcherServer server : smartObjects) {
			if(server.getName().equals(name) && server.getPort()==port) {
				server.clearResources();
				server.cancelRelation();
				removeFromCloud("/"+MyPrefs.SCENARIO_NAME+"/"+server.getName());
				smartObjects.remove(server);
				LOG.info("REMOVE SERVER FROM CLOUD!");
				return true;
			}
		}
		return false;
	}

	//Cloud uses URI to evaluate request type, payload contains additional information
	public <T extends CloudObject> boolean addToCloud(String URI, T payload) {
		Gson gson = new GsonBuilder().serializeNulls().create();
        try {
            URL serverUrl = new URL("http://localhost:"+MyPrefs.PHANTOMJS_PORT+URI);
            HttpURLConnection urlConnection = (HttpURLConnection)serverUrl.openConnection();

            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("PUT");
            urlConnection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");

            // Writing data to the HTTP request body
            BufferedWriter httpRequestBodyWriter = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
            httpRequestBodyWriter.write(gson.toJson(payload)); //String going into PhantomJS
            httpRequestBodyWriter.close();

            // Reading from the HTTP response body
            Scanner httpResponseScanner = new Scanner(urlConnection.getInputStream());
            while(httpResponseScanner.hasNextLine()) {
                System.out.println(httpResponseScanner.nextLine());
            }
            httpResponseScanner.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
			return false;
        } catch (ProtocolException e) {
            e.printStackTrace();
			return false;
        } catch (IOException e) {
            e.printStackTrace();
			return false;
        }
		return true;
	}

	public boolean removeFromCloud(String URI) {
//		Gson gson = new GsonBuilder().serializeNulls().create();
		try {
			URL serverUrl = new URL("http://localhost:"+MyPrefs.PHANTOMJS_PORT+URI);
			HttpURLConnection urlConnection = (HttpURLConnection)serverUrl.openConnection();

			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod("DELETE");

			// Reading from the HTTP response body
			Scanner httpResponseScanner = new Scanner(urlConnection.getInputStream());
			while(httpResponseScanner.hasNextLine()) {
				System.out.println(httpResponseScanner.nextLine());
			}
			httpResponseScanner.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		} catch (ProtocolException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public Logger getLog() {
		return LOG;
	}

	public FetcherCoapClient getCoapClient() {
		return fCoapClient;
	}

	private void setupLogger(Logger logger) {
		logger.setUseParentHandlers(false);
		FetcherLogFormatter formatter = new FetcherLogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(formatter);
		logger.addHandler(handler);
	}

    public static void main(String[] args) throws IOException {
        FetcherLauncher launcher = new FetcherLauncher();
    }

}
