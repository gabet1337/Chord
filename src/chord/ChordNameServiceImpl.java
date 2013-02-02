package chord;

import java.net.*;
import java.io.*;

import interfaces.ChordNameService;

public class ChordNameServiceImpl implements ChordNameService {

    private int myKey;
    private InetSocketAddress myAddress;
    private InetSocketAddress successor;
    private InetSocketAddress predecessor;
    private int port;
    private boolean isRunning;

    private Object isRunningLock = new Object();

    private ServerSocket serverSocket;

    public int keyOfName(InetSocketAddress name) {
        return Math.abs(name.hashCode()*1073741651 % 2147483647);
    }

    public void createGroup(int port) {
        System.out.println("Creating grp on port: " + port);
        synchronized(isRunningLock) {
            this.port = port;
            this.myAddress = getMyName();
            this.myKey = keyOfName(myAddress);
            this.successor = myAddress;
            this.predecessor = myAddress;
            isRunning = true;
        }
    }

    public void joinGroup(InetSocketAddress knownPeer, int port) {
        synchronized(isRunningLock) {
            this.port = port;
            this.myAddress = getMyName();
            this.myKey = keyOfName(myAddress);
            this.successor = getSuccessor(knownPeer);
            System.out.println("Got successor " + successor);
            this.predecessor = getPredecessor();

            sendMessage(new Message(Message.Type.SET_PREDECESSOR, 0, myAddress), successor);
            sendMessage(new Message(Message.Type.SET_SUCCESSOR, 0, myAddress), predecessor);
            isRunning = true;
        }
    }



    public InetSocketAddress getChordName() {
        return this.myAddress;
    }

    public void leaveGroup() {
        synchronized(isRunningLock) {
            isRunning = false;
        }
    }

    public InetSocketAddress succ() {
        return successor;
    }

    public InetSocketAddress pred() {
        return predecessor;
    }

    public InetSocketAddress lookup(int key) {
        if (Helper.between(key, keyOfName(predecessor), myKey)) {
            return myAddress;
        } else {
            Socket s = null;
            ObjectOutputStream output = null;
            ObjectInputStream input = null;
            try {
                s = new Socket(succ().getAddress(), succ().getPort());
                output = new ObjectOutputStream(s.getOutputStream());
                input = new ObjectInputStream(s.getInputStream());

                output.writeObject(new Message(Message.Type.LOOKUP, key, null));
                output.flush();
                output.close();

                InetSocketAddress result = (InetSocketAddress) input.readObject();
                return result;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }



    public void run() {
        System.out.println("ID: " + myAddress.toString() + " and my key is: " + myKey + " started running");

        while (isRunning) {

            Socket socket = waitForConnection();

            if (socket != null) {
                System.out.println("ID: " + myAddress + " has an incoming connection at: " + socket.getInetAddress() + ":" + socket.getPort());

                Message msg = getMessage(socket);

                switch (msg.type) {

                case LOOKUP : {
                    InetSocketAddress res = lookup(msg.key);
                    sendInetSocketAddress(getOOS(socket), res);
                    break;
                }
                case SET_PREDECESSOR : {
                    this.predecessor = msg.result;
                    break;
                }
                case SET_SUCCESSOR : {
                    this.successor = msg.result;
                    break;
                }
                case GET_PREDECESSOR : {
                    sendInetSocketAddress(getOOS(socket), predecessor);
                    break;
                }
                case GET_SUCCESSOR : {
                    sendInetSocketAddress(getOOS(socket), successor);
                    break;
                }
                default: {
                    //fail
                    System.err.println("UNKNOWN MESSAGE RECEIVED");
                }
                }
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        sendMessage(new Message(Message.Type.SET_PREDECESSOR, 0, predecessor), successor);
        sendMessage(new Message(Message.Type.SET_SUCCESSOR, 0, successor), predecessor);

    }

    public String toString() {
        return "ID: " + myAddress + " has succ: " + succ() + " and pred: " + pred() + " and my key is " + myKey;
    }

    private Socket waitForConnection() {
        System.out.println("ID: " + myAddress + " are now waiting for connections");
        Socket result = null;
        try {
            serverSocket = new ServerSocket(port);
            result = serverSocket.accept();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private ObjectInputStream getOIS(Socket socket) {
        System.out.println("Creating OIS for socket: " + socket.getLocalAddress() + ":" + socket.getLocalPort());
        ObjectInputStream res = null;
        try {
            res = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    private ObjectOutputStream getOOS(Socket socket) {
        ObjectOutputStream res = null;
        try {
            res = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    private synchronized Message getMessage(Socket s) {
        System.out.println("ID: " + myAddress + " is getting a message");
        try {
            ObjectInputStream ois = getOIS(s);
            System.out.println("reading");
            Message result = (Message) ois.readObject();
            ois.close();
            s.close();
            System.out.println("Read and sending it back");
            return result;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendInetSocketAddress(ObjectOutputStream oos, InetSocketAddress inet) {
        try {
            oos.writeObject(inet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InetSocketAddress getMyName() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            InetSocketAddress name = new InetSocketAddress(localhost, this.port);
            return name;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    private InetSocketAddress getPredecessor() {
        System.out.println("ID: " + myAddress + " is now asking for pred");
        Socket s = null;
        ObjectOutputStream output = null;
        ObjectInputStream input = null;
        try {
            s = new Socket(successor.getAddress(), successor.getPort(), myAddress.getAddress(), this.port);
            output = new ObjectOutputStream(s.getOutputStream());
            input = new ObjectInputStream(s.getInputStream());

            output.writeObject(new Message(Message.Type.GET_PREDECESSOR, 0, null));
            output.flush();
            output.close();

            return ((InetSocketAddress) input.readObject());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private InetSocketAddress getSuccessor(InetSocketAddress knownPeer) {
        System.out.println("ID: " + myAddress + " is now asking for succ of: " + knownPeer);
        Socket s = null;
        ObjectOutputStream output = null;
        ObjectInputStream input = null;
        try {
            s = new Socket(knownPeer.getAddress(), knownPeer.getPort(), this.myAddress.getAddress(), this.port);
            output = new ObjectOutputStream(s.getOutputStream());
            input = new ObjectInputStream(s.getInputStream());
            
            Message msg = new Message(Message.Type.LOOKUP, myKey, null);
            System.out.println("Sending message");
            output.writeObject(msg);
            output.flush();
            output.close();

            InetSocketAddress result = (InetSocketAddress) input.readObject();
            if (result != null)
                return result;
            
            throw new RuntimeException("FUCK");
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendMessage(Message msg, InetSocketAddress receiver) {
        try {
            Socket s = new Socket(receiver.getAddress(), receiver.getPort());
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            oos.writeObject(msg);
            oos.flush();
            oos.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



















