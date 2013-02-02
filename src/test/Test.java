package test;

import interfaces.Node;

import java.util.*;
import chord.*;

/**
 * @author mw
 * Simple test-suite for NodeV1.  By inheriting and modifying the createNode
 * factory method can be used to test other concrete implementations of Node.
 */
public class Test {
	List<Node> allNodes;
	
	void reset() {
		allNodes = new ArrayList<Node>();
	}

	private Test() {
		reset();
	}

	Node createNode(int k) {
		Node newNode = new NodeV1(k);
		allNodes.add(newNode);
		return newNode;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("=======================================\n");
		for (Node n : allNodes) {
			sb.append(n);
			sb.append("\n");
		}
		sb.append("=======================================");

		return sb.toString();
	}

	public void run() {
		Node known = createNode(7);
		createNode(12).join(known);
		createNode(4).join(known);
		createNode(1).join(known);
		createNode(15).join(known);
		System.out.println(this);
		for (int i = 0; i < 20; i++) {
			known.lookup(i).put(i, i);
		}
		System.out.println(this);

		reset();
		known = createNode(7);
		for (int i = 0; i < 20; i++) {
			known.lookup(i).put(i, i);
		}
		System.out.println(this);
		Node n12 = createNode(12);
		n12.join(known);
		Node n4 = createNode(4);
		n4.join(known);
		Node n1 = createNode(1);
		n1.join(known);
		Node n15 = createNode(15);
		n15.join(known);
		System.out.println(this);
		n1.leave();
		System.out.println(this);
		n15.leave();
		System.out.println(this);

	}

	public static void main(String[] args) {
		new Test().run();
	}
}
