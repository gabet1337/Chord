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
    public boolean isRunning;

    private Object isRunningLock = new Object();

    private ServerSocket serverSocket;


    public int keyOfName(InetSocketAddress name) {
        return Math.abs(name.hashCode()*1073741651 % 2147483647);
    }

    public void createGroup(int port) {
        synchronized(isRunningLock) {
            this.port = port;
            this.myAddress = getMyName();
            this.myKey = keyOfName(myAddress);
            this.successor = myAddress;
            this.predecessor = myAddress;
            isRunning = true;
            run();
        }
    }

    public void joinGroup(InetSocketAddress knownPeer, int port) {
        this.port = port;
        this.myAddress = getMyName();
        this.myKey = keyOfName(myAddress);
        this.successor = getSuccessor(knownPeer);
        this.predecessor = getPredecessor();

        sendMessage(new Message(Message.Type.SET_PREDECESSOR, 0, myAddress), successor);
        sendMessage(new Message(Message.Type.SET_SUCCESSOR, 0, myAddress), predecessor);
        isRunning = true;
        run();
    }



    public InetSocketAddress getChordName() {
        return myAddress;
    }

    public void leaveGroup() {
        isRunning = false;
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
        System.out.println("ID: " + myAddress.toString() + " and my key is: " + myKey);

        while (isRunning) {

            Socket socket = waitForConnection();

            if (socket != null) {
                System.out.println("MYID: " + myAddress.toString() + " has an incoming connection at: " + socket.getInetAddress());
                ObjectInputStream input = getOIS(socket);

                Message msg = getMessage(input);

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
        }
        sendMessage(new Message(Message.Type.SET_PREDECESSOR, 0, predecessor), successor);
        sendMessage(new Message(Message.Type.SET_SUCCESSOR, 0, successor), predecessor);

    }

    private Socket waitForConnection() {
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

    private Message getMessage(ObjectInputStream ois) {
        Message result = null;
        try {
            result = (Message) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
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
            InetSocketAddress name = new InetSocketAddress(localhost, port);
            return name;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    private InetSocketAddress getPredecessor() {
        Socket s = null;
        ObjectOutputStream output = null;
        ObjectInputStream input = null;
        try {
            s = new Socket(successor.getAddress(), successor.getPort());
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
        Socket s = null;
        ObjectOutputStream output = null;
        ObjectInputStream input = null;
        try {
            s = new Socket(successor.getAddress(), successor.getPort());
            output = new ObjectOutputStream(s.getOutputStream());
            input = new ObjectInputStream(s.getInputStream());

            output.writeObject(new Message(Message.Type.LOOKUP, myKey, null));
            output.flush();
            output.close();
            
            InetSocketAddress result = (InetSocketAddress) input.readObject();
            input.close();
            s.close();
            
            return result;
            
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



















