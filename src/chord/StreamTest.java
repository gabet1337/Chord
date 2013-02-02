package chord;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class StreamTest {

    class Client implements Runnable {

        public void run() {
            Message msg = new Message(Message.Type.LOOKUP, 1010, null);
            try {
                Socket s = new Socket(InetAddress.getLocalHost(), 5001, InetAddress.getLocalHost(), 6001);
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
    
    class Server implements Runnable {
        
        public void run() {
            try {
                ServerSocket s = new ServerSocket(5001);
                Socket in = s.accept();
                
                ObjectInputStream ois = new ObjectInputStream(in.getInputStream());
                
                Message msg = (Message) ois.readObject();
                
                System.out.println(msg.type);
                
                ois.close();
                in.close();
                s.close();
                
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
    
    public static void main(String[] args) {
        StreamTest lol = new StreamTest();
        Thread t1 = new Thread(lol.new Server());
        Thread t2 = new Thread(lol.new Client());
        t1.start();
        t2.start();
    }
    
}
