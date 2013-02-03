package chord;

import interfaces.ChordNameService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChordNameServiceImpl extends Thread implements ChordNameService  {

	private boolean joining;
	private int port;
	protected InetSocketAddress myName;
	protected int myKey;
	private InetSocketAddress suc;
	private InetSocketAddress pre;
	private InetSocketAddress connectedAt;

	protected ServerSocket serverSocket;

	public int keyOfName(InetSocketAddress name)  {
		int tmp = name.hashCode()*1073741651 % 2147483647;
		if (tmp < 0) { tmp = -tmp; }
		return tmp;
	}

	public InetSocketAddress getChordName()  {
		return myName;
	}

	/**
	 * Computes the name of this peer by resolving the local host name
	 * and adding the current portname.
	 */
	protected InetSocketAddress getMyName() {
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			InetSocketAddress name = new InetSocketAddress(localhost, port);
			return name;
		} catch (UnknownHostException e) {
			System.err.println("Cannot resolve the Internet address of the local host.");
			System.err.println(e);
		}
		return null;
	}

	public void createGroup(int port) {
		this.joining = false;
		this.port = port;
		this.myName = getMyName();
		this.suc = getMyName();
		this.pre = getMyName();
		this.myKey = keyOfName(myName);
		start();
	}

	public void joinGroup(InetSocketAddress knownPeer, int port)  {
		this.joining = true;
		this.port = port;
		this.connectedAt = knownPeer;
		this.myName = getMyName();
		this.myKey = keyOfName(myName);
		start();
	}

	public void leaveGroup() {
		//System.out.println("My name is " + myName + " and my key is " + myKey + ": leaveGroup()");
		// More code needed here!
	}

	public InetSocketAddress succ() {
		return suc; // You might want to modify this.
	}

	public InetSocketAddress pred() {
		return pre; // You might want to modify this.
	}

	public InetSocketAddress lookup(int key) {
		/*
		 * The below works fine for singleton groups, but you might
		 * want to connect to the rest of the group to lookup the
		 * responsible if the group is larger.
		 */
		return myName; 
	}

	protected Socket waitForConnectionFromClient() {
		Socket res = null;
		try {
			res = serverSocket.accept();
		} catch (IOException e) {
			e.printStackTrace();
			// We return null on IOExceptions
		}
		return res;
	}

	protected void registerOnPort() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			serverSocket = null;
			System.err.println("Cannot open server socket on port number" + port);
			System.err.println(e);
			System.exit(-1);			
		}
	}

	protected Socket connectToServer(InetSocketAddress server) {
		Socket res = null;
		try {
			res = new Socket(server.getAddress(), server.getPort());
		} catch (IOException e) {
			e.printStackTrace();
			// We return null on IOExceptions
		}
		return res;
	}

	public enum command { LOOKUP, CHANGE_PRE, CHANGE_SUC }
	
	public InetSocketAddress[] send(InetSocketAddress to, command c, InetSocketAddress message) {

		//System.out.println("My name is " + myName + " and my key is " + myKey + ": send(" + connectedAt.getAddress() + ":" + connectedAt.getPort() + ")");

		Socket socket = connectToServer(to);

		if (socket != null) {
			//System.out.println("Connected to " + socket);
			try {
				PrintWriter toServer = new PrintWriter(socket.getOutputStream(),true);
				BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				toServer.println(c + " " + message.getAddress() + ":" + message.getPort());
				String response = fromServer.readLine();
				
				socket.close();
				
				//System.out.println(response);
				
				String nodeName = response.split(" ")[0].substring(0,response.split(" ")[0].indexOf(":"));
				int nodePort = Integer.parseInt(response.split(" ")[0].substring(response.split(" ")[0].indexOf(":") + 1));
				
				String preName = response.split(" ")[1].substring(0,response.split(" ")[1].indexOf(":"));
				int prePort = Integer.parseInt(response.split(" ")[1].substring(response.split(" ")[1].indexOf(":") + 1));
										
				InetSocketAddress[] res = new InetSocketAddress[2];
				res[0] = new InetSocketAddress(nodeName.substring(0,nodeName.indexOf("/")), nodePort);
				res[1] = new InetSocketAddress(preName.substring(0,preName.indexOf("/")), prePort);
				
				return res;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			//System.out.println("My name is " + myName + " and my key is " + myKey + ": socket is null");
		}
		return null;
	}

	public void run() {

		

		if (joining) {
			//System.out.println("My name is " + myName + " and my key is " + myKey + ": joining");
			InetSocketAddress[] m = send(connectedAt,command.LOOKUP,getMyName());
			//send(connectedAt,command.LOOKUP,getMyName());
			//send(connectedAt,command.LOOKUP,getMyName());
			//send(connectedAt,command.LOOKUP,getMyName());
			//send(connectedAt,command.LOOKUP,getMyName());
			
			//System.out.println("connectedAt: " + connectedAt.getPort());
			//System.out.println("m[1]: " + m[1].getHostName());
			
			//send(connectedAt,command.LOOKUP,getMyName());
			
			//System.out.println(getMyName().toString() + ": Should join on '" + m[0].toString() + "' with pre '" + m[1].toString() + "'");
			
			suc = m[0];
			pre = m[1];
		
			send(suc, command.CHANGE_PRE, getMyName());
			send(pre, command.CHANGE_SUC, getMyName());
			
		}

		System.out.println("My name is " + myName + " and my key is " + myKey + " and my pre is " + pre + " and my suc is " + suc);
		
		registerOnPort();

		while (true) {

			Socket socket = waitForConnectionFromClient();

			if (socket != null) {
				//System.out.println("Connection from " + socket);
				try {
					PrintWriter toClient = new PrintWriter(socket.getOutputStream(),true);
					BufferedReader fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String s = fromClient.readLine();
					// Read and print what the client is sending
					//while ((s = fromClient.readLine()) != null) { // Ctrl-D terminates the connection
					//	System.out.println("From the client: " + s);
					//}
					
					command c = command.valueOf(s.split(" ")[0]);
						
					//System.out.println(s.split(" ")[1]);
					
					String serverName = s.split(" ")[1].substring(0,s.split(" ")[1].indexOf(":"));
					int port = Integer.parseInt(s.split(" ")[1].substring(s.split(" ")[1].indexOf(":") + 1));
					
					InetSocketAddress message = new InetSocketAddress(serverName.substring(0,serverName.indexOf("/")), port); 
					
					if (c == command.LOOKUP) {
						//System.out.println("Trying lookup");
						//System.out.println("myKey:" + myKey);
						//System.out.println("preKey: " + keyOfName(pre));
						//System.out.println("message: " + keyOfName(message));
						if (pre.equals(myName) || Helper.between(keyOfName(message), keyOfName(pre), myKey)) {
							//System.out.println("was between!");
							toClient.println(getMyName() + " " + pre);
							//continue;
						} else {
							//System.out.println(keyOfName(pre) + " < " + keyOfName(message) + " <= " + myKey);
							//System.out.println(keyOfName(suc) + " = " + keyOfName(message));
							//System.out.println("was not between!");
							//toClient.println(getMyName());
							toClient.println(send(suc, command.LOOKUP, message));
							//continue;
						}
						//System.out.println("I think you asked for a lookup ?");
					}
					
					if (c == command.CHANGE_PRE) {
						pre = message;
						System.out.println("My name is " + myName + " and my key is " + myKey + " and my pre is " + pre + " and my suc is " + suc);
						//System.out.println(getMyName().toString() + " Change_Pre");
						//System.out.println("Change_Pre: " + message.toString());
						toClient.println(getMyName() + " " + getMyName());
					}
					
					if (c == command.CHANGE_SUC) {
						suc = message;
						System.out.println("My name is " + myName + " and my key is " + myKey + " and my pre is " + pre + " and my suc is " + suc);
						//System.out.println(getMyName().toString() + " Change_Suc");
						//System.out.println("Change_Suc: " + message.toString());
						toClient.println(getMyName() + " " + getMyName());
					}
					
					//toClient.println(getMyName());
					socket.close();
				} catch (IOException e) {
					// We report but otherwise ignore IOExceptions
					System.err.println(e);
				}
				//System.out.println("Connection closed by client.");
			} else {
				// We rather agressively terminate the server on the first connection exception
				break;
			}
		}

		/*
		 * If joining we should now enter the existing group and
		 * should at some point register this peer on its port if not
		 * already done and start listening for incoming connection
		 * from other peers who want to enter or leave the
		 * group. After leaveGroup() was called, the run() method
		 * should return so that the threat running it might
		 * terminate.
		 */
	}

	public static void main(String[] args) {
		ChordNameService peer1 = new ChordNameServiceImpl();
		ChordNameService peer2 = new ChordNameServiceImpl();
		ChordNameService peer3 = new ChordNameServiceImpl();

		peer1.createGroup(40009);
		peer2.joinGroup(peer1.getChordName(),40010);
		peer3.joinGroup(peer1.getChordName(),40011);

		//peer1.leaveGroup();
		//peer3.leaveGroup();
		//peer2.leaveGroup();
	}

}
