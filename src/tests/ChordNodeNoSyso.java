package tests;

import java.io.IOException;
import java.net.*;

import chord.ChordNameService;
import chord.Helper;
import chord.Message;
import chord.Message.Type;

public class ChordNodeNoSyso implements ChordNameService {

    private MessageHandlerNoSyso _msgHandler;
    private ServerSocket _serverSocket;
    private int _port;

    private InetSocketAddress _myAddress;
    private InetSocketAddress _predecessor;
    private InetSocketAddress _successor;
    private InetSocketAddress _connectedAt;

    private boolean _isJoining;
    private boolean _isRunning;

    public long _startTimeOfJoin;
    public long _endTimeOfJoin;

    public ChordNodeNoSyso(int port) {
        _msgHandler = new MessageHandlerNoSyso();
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
        int tmp = name.hashCode()*1073741651 % 2147483647;
        if (tmp < 0) { tmp = -tmp; }
        return tmp;
    }

    public void createGroup() {
        _predecessor = _myAddress;
        _successor = _myAddress;
        _isJoining = false;
        _isRunning = true;
    }

    public void joinGroup(InetSocketAddress knownPeer) {
        _isJoining = true;
        _connectedAt = knownPeer;
        _isRunning = true;
    }

    public InetSocketAddress getChordName() {
        return _myAddress;
    }

    public void leaveGroup() {
        try {
            _serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        _isRunning = false;
    }

    public InetSocketAddress succ() {
        return _successor;
    }

    public InetSocketAddress pred() {
        return _predecessor;
    }

    public InetSocketAddress lookup(int key, InetSocketAddress origin) {

        if (_predecessor.equals(_myAddress)) {
            return _myAddress;
        }

        if (_successor.equals(origin)) {
            return origin;
        }

        if (Helper.between(key, keyOfName(_myAddress), keyOfName(_successor))) {
            return _successor;
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

        Message msg = new Message(Message.Type.LOOKUP, key, null);
        msg.origin = origin;
        _msgHandler.sendMessage(s, msg);


        InetSocketAddress result = _msgHandler.receiveMessage(s).result;

        return result;

    }

    public void run() {

        if (_isJoining) {
            joinTheChordRing();
        }

        while(_isRunning) {
            Socket s = null;
            try {
                s = _serverSocket.accept();
            } catch (SocketException e) {
                System.err.println("ID: " + _myAddress + " :: the socket was forcefully closed");
                break;
            } catch (IOException e) {
                System.err.println("Could not establish connection");
                System.err.println(e);
            }

            Message incomingMsg = _msgHandler.receiveMessage(s);

            if (incomingMsg.type.equals(Message.Type.LOOKUP)) {
                _msgHandler.sendMessage(s, new Message(Message.Type.LOOKUP, incomingMsg.key, lookup(incomingMsg.key, incomingMsg.origin)));

            } else if (incomingMsg.type.equals(Message.Type.GET_PREDECESSOR)) {
                _msgHandler.sendMessage(s, new Message(Message.Type.GET_PREDECESSOR, incomingMsg.key, _predecessor));

            } else if (incomingMsg.type.equals(Message.Type.SET_PREDECESSOR)) {
                _predecessor = incomingMsg.result;

            } else if (incomingMsg.type.equals(Message.Type.SET_SUCCESSOR)) {
                _successor = incomingMsg.result;

            }

            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        leaveTheChordRing();

    }

    public String toString() {
        return "ID: " + _myAddress + " :: KEY: " + keyOfName(_myAddress) + " :: SUCC: " + _successor + " :: PRED: " + _predecessor;
    }

    public String getGraphViz() {
        String result = "";
        result += keyOfName(getChordName()) + " -> " + keyOfName(succ()) + "\n";
        result += keyOfName(getChordName()) + " -> " + keyOfName(pred()) + "\n";
        return result;
    }

    private void leaveTheChordRing() {        
        Socket s = null;
        try {
            s = new Socket(_successor.getAddress(), _successor.getPort());
        } catch (IOException e) {
            System.err.println("Could not connect to our successor");
            System.err.println(e);
            System.exit(-1);
        }
        //send message to successor about setting his predecessor to my predecessor
        _msgHandler.sendMessage(s, new Message(Message.Type.SET_PREDECESSOR, 0, _predecessor));
        try {
            s = new Socket(_predecessor.getAddress(), _predecessor.getPort());
        } catch (IOException e) {
            System.err.println("Could not connect to our predecessor");
            System.err.println(e);
            System.exit(-1);
        }
        //send message to predecessor about changing his successor to my successor
        _msgHandler.sendMessage(s, new Message(Message.Type.SET_SUCCESSOR, 0, _successor));
    }

    private void joinTheChordRing() {

        _startTimeOfJoin = System.currentTimeMillis();

        Socket s = null;
        try {
            s = new Socket(_connectedAt.getAddress(), _connectedAt.getPort());
        } catch (IOException e) {
            System.err.println("Could not connect to our known peer");
            System.err.println(e);
            System.exit(-1);
        }
        //Send a message to our knownPeer so that we can get our new successor.
        Message msg = new Message(Message.Type.LOOKUP, keyOfName(getChordName()), null);
        //set the origin of the message so we can avoid circular wait
        msg.origin = _connectedAt;
        _msgHandler.sendMessage(s, msg);
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

        _endTimeOfJoin = System.currentTimeMillis();

    }

}










