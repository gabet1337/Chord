package chord;

import java.util.*;

public class ChordTest {
    
    private ArrayList<ChordNameServiceImpl> nodes = new ArrayList<ChordNameServiceImpl>();
    
    public void test1() {
        ChordNameServiceImpl creator = new ChordNameServiceImpl();
        creator.createGroup(4000);
        nodes.add(creator);
        Thread t = new Thread(creator);
        t.start();
        
        System.out.println(creator.toString());
        
        ChordNameServiceImpl node1 = new ChordNameServiceImpl();
        node1.joinGroup(creator.getChordName(), 4001);
        nodes.add(node1);
        Thread t1 = new Thread(node1);
        t1.start();
        t.yield();
        
        System.out.println(node1.toString());
//        
//        System.out.println(node1.toString());
//        
//        
//        ChordNameServiceImpl node2 = new ChordNameServiceImpl();
//        node2.joinGroup(creator.getChordName(), 8002);
//        nodes.add(node2);
//        Thread t2 = new Thread(node2);
//        t2.start();
        
        
    }
    
    public static void main(String[] args) {
        new ChordTest().test1();
    }
    
}
