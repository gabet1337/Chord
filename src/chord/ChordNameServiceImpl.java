package chord;

import interfaces.ChordNameService;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ChordNameServiceImpl extends Thread implements ChordNameService  {

    private boolean joining;
    private int port;
    protected InetSocketAddress myName;
    protected int myKey;
    private InetSocketAddress suc;
    private InetSocketAddress pre;
    private InetSocketAddress connectedAt;

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
    protected InetSocketAddress _getMyName() {
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
	joining = false;
	this.port = port;
	myName = _getMyName();
	myKey = keyOfName(myName);
	start();
    }
	
    public void joinGroup(InetSocketAddress knownPeer, int port)  {
	joining = true;
	this.port = port;
	connectedAt = knownPeer;
	myName = _getMyName();
	myKey = keyOfName(myName);
	start();
    }

    public void leaveGroup() {
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
    
    public void run() {
	System.out.println("My name is " + myName + " and my key is " + myKey);

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
