package it.mr;

import it.mr.smartobjects.resources.GenericCoapResource;
import it.mr.smartobjects.servers.GenericCoapServer;
import it.mr.smartobjects.servers.ServerFactory;
import it.mr.smartobjects.servers.Thermometer;
import it.mr.types.internal.FetcherLogFormatter;
import it.mr.types.internal.NewResource;
import it.mr.types.internal.ResType;
import it.mr.types.internal.ServerType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScenarioSimulator {
	private static final Logger LOG = Logger.getLogger("SIMULATOR");

//	int portCount=0;
	private ArrayList<GenericCoapServer> realServers = new ArrayList<>();

	private ScenarioSimulator() {
		setupLogger(LOG, MyPrefs.SIMULATOR_LOG_LEVEL);
		//LOG.info(MyPrefs.SCENARIO_NAME+" simulation started");
	}

	private void addPresetServer(BufferedReader br) throws IOException {
		while(true) {
			printServerTypes();
			String serverType = br.readLine();
			if(serverType.equals("stop")) return;
			int serverIndex;
			try { serverIndex = Integer.parseInt(serverType); } catch (NumberFormatException e) { continue; }
			if(serverIndex<0 || serverIndex>=ServerType.values().length-1) {	//Excludes CustomServer
				System.out.println("Invalid index.");
				continue;
			}
			GenericCoapServer newServer = new ServerFactory().getServer(getSafeServerName(serverIndex),getSafeServerPort(),ServerType.values()[serverIndex],null);
			if(newServer==null) {    //Check if service name is already taken or if creation went wrong
				System.out.println("No server created.");
				return;
			}
			realServers.add(newServer);
			System.out.println(/*newServer.getName()+" added to Local Scenario\n"*/);
			return;
		}
	}

	private void addCustomServer(BufferedReader br) throws IOException {
		String server;
		String port;
		while(true) {
			System.out.print("Server Name: ");
			server = br.readLine();
			if(server.equals("stop")) return;
			boolean used = false;
			for(GenericCoapServer realServer : realServers) {
				if(server.equals(realServer.getName())) {
					System.out.print("Name already in use. ");
					used=true;
					break;
				}
			}
			if(used) continue;
			while(true) {
				System.out.print("Port: ");
				port = br.readLine();
				if(port.equals("stop")) return;
				used = false;
				int portNumber;
				try { portNumber = Integer.parseInt(port); } catch (NumberFormatException e) {	continue; }
				for(GenericCoapServer realServer : realServers) {
					if(portNumber==realServer.getPort()) {
						System.out.print("Port already in use. ");
						used=true;
						break;
					}
				}
				if(used) continue;
				ArrayList<NewResource> resources = new ArrayList<>();
				System.out.println("Insert resources (1st level only)");
				while(true) {
					System.out.print("Resource Name: ");
					String name = br.readLine();
					if(name.equals("stop")) break;
					used = false;
					for(NewResource resource : resources) {
						if(name.equals(resource.getName())) {
							System.out.print("Name already in use. ");
							used=true;
							break;
						}
					}
					if(used) continue;
					while(true) {
						printResourceTypes();
						String res = br.readLine();
						int resIndex;
						try { resIndex = Integer.parseInt(res); } catch (NumberFormatException e) {	continue; }
						if(resIndex<0 || resIndex>=ResType.values().length) {
							System.out.println("Invalid index.");
							continue;
						}
						resources.add(new NewResource(name,ResType.values()[resIndex]));
						break;
					}
				}
				GenericCoapServer newServer = new ServerFactory().getServer(server, Integer.parseInt(port),ServerType.CUSTOM_SERVER,resources);
				if(newServer==null) {    //Check if service name is already taken or if creation went wrong
					System.out.println("No server created.");
					return;
				}
				realServers.add(newServer);
				System.out.println("Server Added!");
				return;
			}
		}
	}

	private void removeServer(BufferedReader br) throws IOException {
		if(!realServers.isEmpty()) {
			String server;
			int serverIndex;
			while(true) {
				System.out.println("Servers:");
				printServers();
				server = br.readLine();
				if(server.equals("stop")) return;
				try{ serverIndex = Integer.parseInt(server); } catch (NumberFormatException e) {	continue; }
				if(serverIndex<0 || serverIndex>=realServers.size()) {
					System.out.println("Invalid index. ");
					continue;
				}
				GenericCoapServer serverToBeRemoved = realServers.get(serverIndex);
				serverToBeRemoved.stopServer();
				if(realServers.remove(serverToBeRemoved)) {
					System.out.println("Server Removed!");
					return;
				}
				else LOG.warning("Failed to remove server "+realServers.get(serverIndex).getName());
			}
		} else System.out.println("Server list is empty!");
	}

	private void addPresetResource(BufferedReader br) throws IOException {
		if(!realServers.isEmpty()) {
			String server, resource;
			int selectedServer, selectedResource;
			while(true) {
				System.out.println("Servers:");
				printServers();
				server = br.readLine();
				if(server.equals("stop")) return;
				try { selectedServer= Integer.parseInt(server); } catch (NumberFormatException e) {	continue; }
				if(selectedServer <0 || selectedServer>=realServers.size()) {
					System.out.println("Invalid index. ");
					continue;
				}
				GenericCoapResource parentRes = null;
				if(!realServers.get(selectedServer).getResources().isEmpty()) {
					System.out.println("Choose path:");
					while(true) {
						printResources(selectedServer);
						resource = br.readLine();
						if(resource.equals("stop"))	return;
						else if(resource.equals("here"))	break;
						try{ selectedResource = Integer.parseInt(resource); } catch (NumberFormatException e) {	continue; }
						if(selectedResource<0 || selectedResource>=realServers.get(selectedServer).getResources().size()) {
							System.out.println("Invalid index. ");
							continue;
						}
						parentRes= realServers.get(selectedServer).getResources().get(selectedResource);
						while(true) {
							if(parentRes.getResChildren().isEmpty()) break;
							printResChildren(parentRes);
							resource = br.readLine();
							if(resource.equals("stop"))	return;
							else if(resource.equals("here"))	break;
							try{ selectedResource = Integer.parseInt(resource); } catch (NumberFormatException e) {	continue; }
							if(selectedResource<0 || selectedResource>=parentRes.getResChildren().size()) {
								System.out.println("Invalid index. ");
								continue;
							}
							parentRes = parentRes.getResChildren().get(selectedResource);
						}
						break;
					}
				}
				while(true) {
					System.out.println("New Resource: ");
					printResourceTypes();
					String res = br.readLine();
					if(res.equals("stop"))	return;
					int resIndex;
					try { resIndex = Integer.parseInt(res); } catch (NumberFormatException e) {	continue; }
					if(resIndex<0 || resIndex>=ResType.values().length) {
						System.out.println("Invalid index.");
						continue;
					}
					if(parentRes==null)
						realServers.get(selectedServer).addResource(ResType.values()[resIndex]);
					else
						parentRes.addChildRes(ResType.values()[resIndex],false);
					System.out.println(ResType.values()[resIndex]+" added to "+realServers.get(selectedServer).getName()+"\n");
					return;
				}
			}
		} else System.out.println("Server list is empty!");
	}

	private void addCustomResource(BufferedReader br) throws IOException {
		if(!realServers.isEmpty()) {
			String server;
			String resource;
			int selectedServer;
			int selectedResource;
			while(true) {
				System.out.println("Servers:");
				printServers();
				server = br.readLine();
				if(server.equals("stop")) return;
				try{ selectedServer= Integer.parseInt(server); } catch (NumberFormatException e) {	continue; }
				if(selectedServer <0 || selectedServer>=realServers.size()) {
					System.out.println("Invalid index. ");
					continue;
				}
				GenericCoapResource parentRes = null;
				if(!realServers.get(selectedServer).getResources().isEmpty()) {
					System.out.println("Choose path:");
					while(true) {
						printResources(selectedServer);
						resource = br.readLine();
						if(resource.equals("stop"))	return;
						else if(resource.equals("here"))	break;
						try{ selectedResource = Integer.parseInt(resource); } catch (NumberFormatException e) {	continue; }
						if(selectedResource<0 || selectedResource>=realServers.get(selectedServer).getResources().size()) {
							System.out.println("Invalid index. ");
							continue;
						}
						parentRes= realServers.get(selectedServer).getResources().get(selectedResource);
						while(true) {
							if(parentRes.getResChildren().isEmpty()) break;
							printResChildren(parentRes);
							resource = br.readLine();
							if(resource.equals("stop"))	return;
							else if(resource.equals("here"))	break;
							try{ selectedResource = Integer.parseInt(resource); } catch (NumberFormatException e) {	continue; }
							if(selectedResource<0 || selectedResource>=parentRes.getResChildren().size()) {
								System.out.println("Invalid index. ");
								continue;
							}
							parentRes = parentRes.getResChildren().get(selectedResource);
						}
						break;
					}
				}
				while(true) {
					System.out.print("New Resource: ");
					String newResource = br.readLine();
					if(newResource.equals("stop")) return;
					boolean used = false;
					if((parentRes==null && !realServers.get(selectedServer).isNameEligible(newResource)) || (parentRes!= null && !parentRes.isNameEligible(newResource))) {
						System.out.print("Name already in use. ");
						used=true;
					}
					if(used) continue;
					while(true) {
						printResourceTypes();
						String res = br.readLine();
						if(res.equals("stop"))	return;
						int resIndex;
						try { resIndex = Integer.parseInt(res); } catch (NumberFormatException e) {	continue; }
						if(resIndex<0 || resIndex>=ResType.values().length) {
							System.out.println("Invalid index.");
							continue;
						}
						if(parentRes==null)
							realServers.get(selectedServer).addResource(newResource,ResType.values()[resIndex]);
						else
							parentRes.addChildRes(newResource,ResType.values()[resIndex],false);
						System.out.println("Resource Added!");
						return;
					}
				}
			}
		} else System.out.println("Server list is empty!");
	}

	private void removeResource(BufferedReader br) throws IOException {
		if(!realServers.isEmpty()) {
			String server, resource;
			int selectedServer, selectedResource;
			while(true) {
				System.out.println("Servers:");
				printServers();
				server = br.readLine();
				if(server.equals("stop")) return;
				try { selectedServer= Integer.parseInt(server); } catch (NumberFormatException e) {	continue; }
				if(selectedServer <0 || selectedServer>=realServers.size()) {
					System.out.println("Invalid index. ");
					continue;
				}
				GenericCoapResource parentRes = null, resToBeRemoved;
				if(realServers.get(selectedServer).getResources().isEmpty()) {
					System.out.println("Server has no resources!");
					return;
				}
				else {
					System.out.println("Choose path:");
					while(true) {
						printResources(selectedServer);
						resource = br.readLine();
						if(resource.equals("stop"))	return;
						try { selectedResource = Integer.parseInt(resource); } catch (NumberFormatException e) {	continue; }
						if(selectedResource<0 || selectedResource>=realServers.get(selectedServer).getResources().size()) {
							System.out.println("Invalid index. ");
							continue;
						}
						resToBeRemoved = realServers.get(selectedServer).getResources().get(selectedResource);
						while(true) {
							if(resToBeRemoved.getResChildren().isEmpty()) break;
							printResChildren(resToBeRemoved);
							resource = br.readLine();
							if(resource.equals("stop"))	return;
							else if(resource.equals("here"))	break;
							try { selectedResource = Integer.parseInt(resource); } catch (NumberFormatException e) {	continue; }
							if(selectedResource<0 || selectedResource>=resToBeRemoved.getResChildren().size()) {
								System.out.println("Invalid index. ");
								continue;
							}
							parentRes=resToBeRemoved;
							resToBeRemoved = parentRes.getResChildren().get(selectedResource);
						}
						break;
					}
				}
				if (parentRes==null){	//Resource on 1st level
					if(realServers.get(selectedServer).removeResource(resToBeRemoved)) {
						System.out.println(resToBeRemoved.getName()+" removed from "+realServers.get(selectedServer).getName()+"\n");
						return;
					} else LOG.warning("Failed to remove resource "+resToBeRemoved.getName());
				}
				else {
					if(parentRes.removeChildRes(resToBeRemoved)) {
						System.out.println("Resource Removed!");
						return;
					} else LOG.warning("Failed to remove resource "+resToBeRemoved.getName());
				}
			}
		} else System.out.println("Server list is empty!");
	}

	private void printScenario() {
		if (realServers.isEmpty()) {
			System.out.println("Scenario is empty!");
			return;
		}
		for(GenericCoapServer server : realServers) {
			System.out.println(server.getName()+"("+server.getPort()+")");
			for(GenericCoapResource res : server.getResources()) {
				if(res.getResParent()==null) {
					System.out.println("|-"+res.getName());
					printScenarioChildren("   ",res);
				}
			}
			System.out.println("----------");
		}
	}

	private void printScenarioChildren(String prefix, GenericCoapResource resource) {
		for(GenericCoapResource res : resource.getResChildren()) {
			System.out.println(prefix+"|-"+res.getName());
			printScenarioChildren((new String(new char[prefix.length()+3]).replace('\0',' ')),res);
		}
	}

	private void randomScenario() throws IOException {
//		clearScenario();
		SecureRandom sr = new SecureRandom();
		ServerFactory serverFactory = new ServerFactory();
//		for(int i = 0; i< MyPrefs.RANDOM_SCENARIO_SIZE; ++i) {
//			int rndServerType = sr.nextInt(ServerType.values().length-1);	//Excludes CustomServer
//			realServers.add(serverFactory.getServer(getSafeServerName(rndServerType),getSafeServerPort(),ServerType.values()[rndServerType],null));
//		}
//		System.out.println("New Random Scenario created!");

		realServers.add(serverFactory.getServer(getSafeServerName(2),getSafeServerPort(),ServerType.values()[2],null));
//		realServers.add(serverFactory.getServer(getSafeServerName(3),getSafeServerPort(),ServerType.values()[3],null));
		realServers.add(serverFactory.getServer(getSafeServerName(1),getSafeServerPort(),ServerType.values()[1],null));
	}

	private void clearScenario() throws IOException {
		for(GenericCoapServer server : realServers)
			server.stopServer();
		this.realServers.clear();
		System.out.println("Scenario cleared!");
	}

	private void printServers() {
		for(GenericCoapServer server : realServers) {
			System.out.println("("+realServers.indexOf(server)+") "+server.getName()+"("+server.getPort()+")");
		}
	}

	private void printResources(int serverIndex) {
		for(GenericCoapResource resource : realServers.get(serverIndex).getResources()) {
			System.out.println("("+realServers.get(serverIndex).getResources().indexOf(resource)+") "+resource.getName());
		}
	}

	private void printResChildren(GenericCoapResource resource) {
		for(GenericCoapResource res : resource.getResChildren()) {
			System.out.println("("+resource.getResChildren().indexOf(res)+") "+res.getName());
		}
	}

	private void printServerTypes() {
		for(ServerType server : ServerType.values()) {
			if(server.equals(ServerType.CUSTOM_SERVER)) continue;
			System.out.println("("+server.ordinal()+") "+server.toString());
		}
	}

	private void printResourceTypes() {
		for(ResType resType : ResType.values()) {
			System.out.println("("+resType.ordinal()+") "+resType.toString());
		}
	}

	private String getSafeServerName(int serverTypeIndex) {
		int nameCount = 1;
		for(int i=0; i<realServers.size(); i++) {
			if(realServers.get(i).getName().equals(ServerType.values()[serverTypeIndex].toString()+nameCount)) {
				++nameCount;
				i=0;
			}
		}
		return ServerType.values()[serverTypeIndex].toString()+nameCount;
	}

	private int getSafeServerPort() {
		int portCount=0;
		for(int i=0; i<realServers.size(); i++) {
			if(realServers.get(i).getPort()==(MyPrefs.SCENARIO_INITIAL_PORT+portCount)) {
				++portCount;
				i=-1;	//Loop restarts form 0
			}
		}
		return MyPrefs.SCENARIO_INITIAL_PORT+portCount;
	}

	private void setupLogger(Logger logger, Level level) {
		logger.setUseParentHandlers(false);
		FetcherLogFormatter formatter = new FetcherLogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(formatter);
		handler.setLevel(level);
		logger.addHandler(handler);
	}

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		ScenarioSimulator scenario = new ScenarioSimulator();
		int choice;

		StringBuilder builder = new StringBuilder();
		while(true) {
			builder.setLength(0);
			builder.append("+=================================================+\n")
					.append("|       ~ Server ~       |      ~ Resource ~      |\n")
					.append("|  (1)Preset  (2)Custom  |  (4)Preset  (5)Custom  |\n")
					.append("|       (3)Remove        |       (6)Remove        |\n")
					.append("|--------------------------+----------------------|\n")
					.append("|      (7)Status      (8)Random     (9)Clear      |\n")
					.append("|-------------------------------------------------|\n")
					.append("|        \"stop\" to interrupt       (0)Exit        |\n")
					.append("+=================================================+");
			System.out.println(builder.toString());

			try { choice = Integer.parseInt(br.readLine());	} catch (NumberFormatException e) {	choice = -1; }

			switch(choice) {
				case 1:
					System.out.println("|ADD PRESET SERVER");
					scenario.addPresetServer(br);
					break;
				case 2:
					System.out.println("|ADD CUSTOM SERVER");
					scenario.addCustomServer(br);
					break;
				case 3:
					System.out.println("|REMOVE SERVER");
					scenario.removeServer(br);
					break;
				case 4:
					System.out.println("|ADD PRESET RESOURCE");
					scenario.addPresetResource(br);
					break;
				case 5:
					System.out.println("|ADD CUSTOM RESOURCE");
					scenario.addCustomResource(br);
					break;
				case 6:
					System.out.println("|REMOVE RESOURCE");
					scenario.removeResource(br);
					break;
				case 7:
					System.out.println("|---- "+ MyPrefs.SCENARIO_NAME+" ----|");
					scenario.printScenario();
					break;
				case 8:
					System.out.println("|GENERATING NEW RANDOM SCENARIO");
					scenario.randomScenario();
					break;
				case 9:
					System.out.println("|CLEARING SCENARIO");
					scenario.clearScenario();
					break;
				case 0:
					scenario.clearScenario();
					System.out.println("GoodBye.");
					System.exit(0);
					break;
				default: break;
			}
		}
	}
}
