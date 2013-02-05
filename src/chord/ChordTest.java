package chord;

import java.util.ArrayList;

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

    private static void testTime(int startPort, int limit) {
        ChordNodeNoSyso creator = new ChordNodeNoSyso(startPort);
        creator.createGroup();
        Thread t1 = new Thread(creator);
        t1.start();

        for (int i = 0; i < limit; i++) {
            ChordNodeNoSyso n = new ChordNodeNoSyso(startPort+i+1);
            n.joinGroup(creator.getChordName());
            Thread t = new Thread(n);
            t.start();

            try {
                Thread.sleep(100+i);
            } catch (InterruptedException e) {
                System.err.println("Couln't sleep the test thread");
                System.err.println(e);
            }

            System.out.println(i+1 + ", " + (n._endTimeOfJoin-n._startTimeOfJoin));

        }

    }

    private static void testGraph(int startPort, int limit) {

        ArrayList<ChordNodeNoSyso> nodes = new ArrayList<ChordNodeNoSyso>();

        ChordNodeNoSyso creator = new ChordNodeNoSyso(startPort);
        nodes.add(creator);
        creator.createGroup();
        Thread t1 = new Thread(creator);
        t1.start();

        for (int i = 0; i < limit; i++) {
            ChordNodeNoSyso n = new ChordNodeNoSyso(startPort+i+1);
            nodes.add(n);
            n.joinGroup(creator.getChordName());
            Thread t = new Thread(n);
            t.start();

            try {
                Thread.sleep(100+i);
            } catch (InterruptedException e) {
                System.err.println("Couln't sleep the test thread");
                System.err.println(e);
            }

        }

        System.out.println("digraph test {\n");
        System.out.println("size=\"50,50\" \n");
        System.out.println("layout=\"neato\"");
        System.out.println("nodesep=\"1\"");
        System.out.println("ranksep=\"2\"");
        
        for (ChordNodeNoSyso node : nodes) {
            System.out.println(node.keyOfName(node.getChordName()) + " [color=none; shape=plaintext; fontsize=10];");
        }
        
        for (ChordNodeNoSyso node : nodes) {
            System.out.println(node.getGraphViz());
        }

        System.out.println("}");

    }

    public void run() {
        //testTime(40000, 1000);
        testGraph(40000, 10);
    }

}
