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
        System.out.println(creator.lookup(10));
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

    public void run() {
        test2();

    }

}
