package chord;

import java.net.*;
import java.io.*;

public class MessageHandlerNoSyso {
    
    public Message receiveMessage(Socket s) {
        
        Message result = null;
        
        try {
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            
            result = (Message) ois.readObject();
            //ois.close();
        } catch (SocketTimeoutException e) {
            System.err.println("Connection timed out");
            System.err.println(e);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public void sendMessage(Socket s, Message msg) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            
            oos.writeObject(msg);
            
            oos.flush();
            //oos.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String getLocalSocketNameAndPort(Socket s) {
        return s.getLocalAddress() + ":" + s.getLocalPort();
    }
    
    private String getSocketNameAndPort(Socket s) {
        return s.getInetAddress() + ":" + s.getPort();
    }
}
