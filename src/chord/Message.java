package chord;

import java.io.Serializable;
import java.net.*;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public Type type; //contains the type of the message
    
    public int key; //contains the key we are looking for
    
    /*
     * contains the result we are looking for.
     * Only set this if the type is RESULT
     */
    public InetSocketAddress result; 
    
    
    public enum Type {
        LOOKUP, SET_PREDECESSOR, SET_SUCCESSOR, GET_PREDECESSOR, GET_SUCCESSOR;
    }
    
    public Message(Type type, int key, InetSocketAddress addr) {
        this.type = type;
        this.key = key;
        this.result = addr;
    }
    
}
