package tests;

import java.io.IOException;
import java.net.*;

import chord.Message;
import chord.MessageHandler;
import chord.Message.Type;

public class MessageHandlerTest {

    class Server implements Runnable {

        private ServerSocket serverSocket;
        private MessageHandler msgHandler;

        public Server() {
            msgHandler = new MessageHandler();
            try {
                serverSocket = new ServerSocket(4000);              
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (true) {
                try {
                    Socket conn = serverSocket.accept();

                    Message msg = msgHandler.receiveMessage(conn);

                    conn.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    
    class Client implements Runnable {
        
        private MessageHandler msgHandler;
        
        
        public Client() {
            msgHandler = new MessageHandler();
        }

        public void run() {
            
            try {
                Socket s = new Socket("localhost", 4000);
                
                Message msg = new Message(Message.Type.LOOKUP, 1000, null);
                
                msgHandler.sendMessage(s, msg);
                
                msgHandler.sendMessage(s, new Message(null, 0, null));
                
                s.close();
                
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        
    }
    
    public static void main(String[] args) {
        MessageHandlerTest mht = new MessageHandlerTest();
        Thread t1 = new Thread(mht.new Server());
        Thread t2 = new Thread(mht.new Client());
        Thread t3 = new Thread(mht.new Client());
        Thread t4 = new Thread(mht.new Client());
        t1.start();
        t2.start();
        t3.start();
        t4.start();
    }

}
