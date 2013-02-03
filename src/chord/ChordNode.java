package chord;

import java.io.IOException;
import java.net.*;

public class ChordNode implements ChordNameService {

    private MessageHandler _msgHandler;
    private ServerSocket _serverSocket;
    private int _port;

    private InetSocketAddress _myAddress;
    private InetSocketAddress _predecessor;
    private InetSocketAddress _successor;
    private InetSocketAddress _connectedAt;

    private boolean _isJoining;

    public ChordNode(int port) {
        _msgHandler = new MessageHandler();
        _port = port;
        try {
            _serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not create the server socket");
            e.printStackTrace();
        }
        _myAddress = Helper.getInetSocketAddress(port);
        _predecessor = null;
        _successor = null;
        _connectedAt = null;
    }

    public int keyOfName(InetSocketAddress name) {
        return Math.abs(name.hashCode()*1073741651 % 2147483647);
    }

    public void createGroup() {
        _predecessor = _myAddress;
        _successor = _myAddress;
        _isJoining = false;
    }

    public void joinGroup(InetSocketAddress knownPeer) {
        _isJoining = true;
        _connectedAt = knownPeer;
    }

    public InetSocketAddress getChordName() {
        return _myAddress;
    }

    public void leaveGroup() {
        // Send message to successor and predecessor about their new succ and pred
    }

    public InetSocketAddress succ() {
        return _successor;
    }

    public InetSocketAddress pred() {
        return _predecessor;
    }

    public InetSocketAddress lookup(int key) {

        if (_predecessor.equals(_myAddress)) {
            return _myAddress;
        }

        if (Helper.between(key, keyOfName(_predecessor), keyOfName(_myAddress))) {
            return _myAddress;
        }
        
        /*
         * If im not the holder of the key, I need to find out who it is. To do this
         * I need to ask my successor and wait for his reply. I will send a message of
         * type LOOKUP on a new socket to my successor.
         */
        
        Socket s = null;
        try {
            s = new Socket(_successor.getAddress(), _successor.getPort());
        } catch (IOException e) {
            System.err.println("Could not establish a connection to my successor");
            System.err.println(e);
        }
        
        _msgHandler.sendMessage(s, new Message(Message.Type.LOOKUP, key, null));
        
        return _msgHandler.receiveMessage(s).result;
        
    }

    public void run() {

        if (_isJoining) {
            joinTheChordRing();
        }
        
        System.out.println(this);
        
        while(true) {
            System.out.println("ID: " + _myAddress + " :: waiting for a connection...");
            Socket s = null;
            try {
                s = _serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Could not establish connection");
                System.err.println(e);
            }
            
            System.out.println("ID: " + _myAddress + " :: established a connection to " + s.getInetAddress() + ":" + s.getPort());
            
            Message incomingMsg = _msgHandler.receiveMessage(s);
            
            if (incomingMsg.type.equals(Message.Type.LOOKUP)) {
                System.out.println("ID: " + _myAddress + " :: in the LOOKUP handler");
                _msgHandler.sendMessage(s, new Message(Message.Type.LOOKUP, incomingMsg.key, lookup(incomingMsg.key)));
                
            } else if (incomingMsg.type.equals(Message.Type.GET_PREDECESSOR)) {
                System.out.println("ID: " + _myAddress + " :: in the GET_PREDECESSOR handler");
                _msgHandler.sendMessage(s, new Message(Message.Type.GET_PREDECESSOR, incomingMsg.key, _predecessor));
                
            } else if (incomingMsg.type.equals(Message.Type.SET_PREDECESSOR)) {
                System.out.println("ID: " + _myAddress + " :: in the SET_PREDECESSOR handler");
                _predecessor = incomingMsg.result;
                
            } else if (incomingMsg.type.equals(Message.Type.SET_SUCCESSOR)) {
                System.out.println("ID: " + _myAddress + " :: in the SET_SUCCESSOR handler");
                _successor = incomingMsg.result;
                
            }
            
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }

    }

    public String toString() {
        return "ID: " + _myAddress + " :: KEY: " + keyOfName(_myAddress) + " :: SUCC: " + _successor + " :: PRED: " + _predecessor;
    }
    
    private void joinTheChordRing() {
        System.out.println("ID: " + _myAddress + " :: started the joining process");
        Socket s = null;
        try {
            s = new Socket(_connectedAt.getAddress(), _connectedAt.getPort());
        } catch (IOException e) {
            System.err.println("Could not connect to our known peer");
            System.err.println(e);
            System.exit(-1);
        }
        //Send a message to our knownPeer so that we can get our new successor.
        _msgHandler.sendMessage(s, new Message(Message.Type.LOOKUP, keyOfName(getChordName()), null));
        //Receive the answer and store the result in _successor
        _successor = _msgHandler.receiveMessage(s).result;
        //Finally close the stream
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Now we need to find our predecessor. We ask our new successor for his previous predecessor:
        try {
            s = new Socket(_successor.getAddress(), _successor.getPort());
        } catch (IOException e) {
            System.err.println("Could not connect to our new successor");
            System.err.println(e);
            System.exit(-1);
        }
        //Send a message to successor
        _msgHandler.sendMessage(s, new Message(Message.Type.GET_PREDECESSOR, 0, null));
        //Wait for reply and set our new predecessor
        _predecessor = _msgHandler.receiveMessage(s).result;
        //Finally close the stream
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //At last we need to send messages to our new predecessor and successor of our arrival in the ring:
        try {
            s = new Socket(_successor.getAddress(), _successor.getPort());
        } catch (IOException e) {
            System.err.println("Could not connect to our new successor");
            System.err.println(e);
            System.exit(-1);
        }
        //Send the message to successor
        _msgHandler.sendMessage(s, new Message(Message.Type.SET_PREDECESSOR, 0, _myAddress));
        //Finally close the stream
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //
        try {
            s = new Socket(_predecessor.getAddress(), _predecessor.getPort());
        } catch (IOException e) {
            System.err.println("Could not connect to our new successor");
            System.err.println(e);
            System.exit(-1);
        }
        //Send the message to predecessor
        _msgHandler.sendMessage(s, new Message(Message.Type.SET_SUCCESSOR, 0, _myAddress));
        //Finally close the stream
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}










