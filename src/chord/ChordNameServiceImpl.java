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
		System.out.println("My name is " + myName + " and my key is " + myKey + ": leaveGroup()");
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
			// We return null on IOExceptions
		}
		return res;
	}

	public enum command { LOOKUP }
	
	public InetSocketAddress send(InetSocketAddress to, command c, String message) {

		System.out.println("My name is " + myName + " and my key is " + myKey + ": send(" + connectedAt.getAddress() + ":" + connectedAt.getPort() + ")");

		Socket socket = connectToServer(to);

		if (socket != null) {
			System.out.println("Connected to " + socket);
			try {
				PrintWriter toServer = new PrintWriter(socket.getOutputStream(),true);
				BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				toServer.println(c + message);
				String answer = fromServer.readLine();
				
				socket.close();
				
				String serverName = answer.substring(0,answer.indexOf(":"));
				int port = Integer.parseInt(answer.substring(answer.indexOf(":") + 1));
				
				return new InetSocketAddress(serverName, port);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("My name is " + myName + " and my key is " + myKey + ": socket is null");
		}
		return null;
	}

	public void run() {

		System.out.println("My name is " + myName + " and my key is " + myKey + ": run()");

		if (joining) {
			System.out.println("My name is " + myName + " and my key is " + myKey + ": joining");
			
			System.out.println(send(connectedAt,command.LOOKUP,"key"));
		}

		registerOnPort();

		while (true) {

			Socket socket = waitForConnectionFromClient();

			if (socket != null) {
				System.out.println("Connection from " + socket);
				try {
					PrintWriter toClient = new PrintWriter(socket.getOutputStream(),true);
					BufferedReader fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String s = fromClient.readLine();
					// Read and print what the client is sending
					//while ((s = fromClient.readLine()) != null) { // Ctrl-D terminates the connection
					//	System.out.println("From the client: " + s);
					//}
					System.out.println(s);
					toClient.println(getMyName());
					socket.close();
				} catch (IOException e) {
					// We report but otherwise ignore IOExceptions
					System.err.println(e);
				}
				System.out.println("Connection closed by client.");
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

		peer1.createGroup(40001);
		peer2.joinGroup(peer1.getChordName(),40002);
		peer3.joinGroup(peer2.getChordName(),40003);

		peer1.leaveGroup();
		peer3.leaveGroup();
		peer2.leaveGroup();
	}

}
