package temp;
import interfaces.*;

import java.net.*;
import java.io.*;
import java.util.*;

public class ChordNameServiceImpl implements ChordNameService {

    private InetSocketAddress succ;
    private InetSocketAddress pred;
    private int port;
    private int myKey;
    boolean isGroupCreator;
    
    @SuppressWarnings("unused")
	private HashMap<Integer, ChordObject> data;
    
    public ChordNameServiceImpl() {
        this.myKey = keyOfName(getChordName());
    }

    public int keyOfName(InetSocketAddress name) {
        return Math.abs(name.hashCode() * 1073741651 % 2147483647);
    }

    public void createGroup(int port) {
        this.port = port;
        isGroupCreator = true;
        succ = pred = this.getChordName();
        run();
    }

    public void joinGroup(InetSocketAddress knownPeer, int port) {
        this.port = port;
        isGroupCreator = false;
        run();
    }

    public InetSocketAddress getChordName() {
        try {
            return new InetSocketAddress(InetAddress.getLocalHost(), port);
        } catch (UnknownHostException e) {
            System.err.println("Could not resolve host name");
            System.err.println(e);
        }
        return null;
    }

    public void leaveGroup() {
        // TODO Auto-generated method stub

    }

    public InetSocketAddress succ() {
        return succ;
    }

    public InetSocketAddress pred() {
        return pred;
    }

    public InetSocketAddress lookup(int key) {
        if (keyOfName(pred) < key && key <= myKey) {
            return this.getChordName();
        } else {
            // need to use RMI or JMS here... Pros/cons.. This exercise is big
            return null;
        }
    }

    public void run() {
        @SuppressWarnings("unused")
		ServerSocket connection;
        try {
            connection = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("There was a problem during socket creation");
            System.err.println(e);
        }
        while (true) {
        }
    }

}