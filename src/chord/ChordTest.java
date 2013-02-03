package chord;

public class ChordTest implements Runnable {

    public static void main(String[] args) {
        Thread t = new Thread(new ChordTest());
        t.start();
    }

    private static void test1() {
        ChordNode creator = new ChordNode(8000);
        creator.createGroup();
        System.out.println(creator.toString());
        System.out.println(creator.lookup(10, null));
    }
    
    private static void test2() {
        ChordNode creator = new ChordNode(4567);
        creator.createGroup();
        Thread t1 = new Thread(creator);
                
        ChordNode node1 = new ChordNode(4568);
        node1.joinGroup(creator.getChordName());
        Thread t2 = new Thread(node1);
        t1.start();
        t2.start();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(creator.toString());
        System.out.println(node1.toString());
    }
    
    private static void test3() {
        ChordNode creator = new ChordNode(4567);
        creator.createGroup();
        Thread t1 = new Thread(creator);
                
        ChordNode node1 = new ChordNode(4568);
        node1.joinGroup(creator.getChordName());
        Thread t2 = new Thread(node1);
        
        t1.start();
        t2.start();
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        
        System.out.println();
        System.out.println(creator.toString());
        System.out.println(node1.toString());
        System.out.println();
        
        ChordNode node2 = new ChordNode(4569);
        node2.joinGroup(creator.getChordName());
        Thread t3 = new Thread(node2);
        t3.start();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println(creator.toString());
        System.out.println(node1.toString());
        System.out.println(node2.toString());
    }
    
    /* 
     * test 4 peers in the ring
     */
    private static void test4() {
        ChordNode creator = new ChordNode(4567);
        creator.createGroup();
        Thread t1 = new Thread(creator);
                
        ChordNode node1 = new ChordNode(4568);
        node1.joinGroup(creator.getChordName());
        Thread t2 = new Thread(node1);
        
        t1.start();
        t2.start();
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        ChordNode node2 = new ChordNode(4569);
        node2.joinGroup(creator.getChordName());
        Thread t3 = new Thread(node2);
        t3.start();
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        ChordNode node3 = new ChordNode(4570);
        node3.joinGroup(creator.getChordName());
        Thread t4 = new Thread(node3);
        t4.start();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println(creator.toString());
        System.out.println(node1.toString());
        System.out.println(node2.toString());
        System.out.println(node3.toString());
    }
    
    /*
     * Leave the chord ring test.
     */
    private static void test5() {
        ChordNode creator = new ChordNode(4567);
        creator.createGroup();
        Thread t1 = new Thread(creator);
                
        ChordNode node1 = new ChordNode(4568);
        node1.joinGroup(creator.getChordName());
        Thread t2 = new Thread(node1);
        
        t1.start();
        t2.start();
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        ChordNode node2 = new ChordNode(4569);
        node2.joinGroup(creator.getChordName());
        Thread t3 = new Thread(node2);
        t3.start();
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        ChordNode node3 = new ChordNode(4570);
        node3.joinGroup(creator.getChordName());
        Thread t4 = new Thread(node3);
        t4.start();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println(creator.toString());
        System.out.println(node1.toString());
        System.out.println(node2.toString());
        System.out.println(node3.toString());
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("========");
        
        node3.leaveGroup();
                
        try {
            t4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println(creator.toString());
        System.out.println(node1.toString());
        System.out.println(node2.toString());
    }

    public void run() {
        test5();
    }

}
