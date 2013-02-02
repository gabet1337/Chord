package chord;

import interfaces.Node;

import java.util.*;

/**
 * @author mw
 * Simple local implementation of Node.  See Node.java for method docs.
 */
public class NodeV1 implements Node {
	private int id;
	Map<Integer, Object> data;

	public NodeV1(int id) { // Good for testing
		this.id = id;
		this.data = new HashMap<Integer, Object>();

		// Make each node its own network until it joins others
		this.successor = this;
		this.predecessor = this;
	}

	public NodeV1() {
		this(Helper.random());
	}

	public int getId() {
		return id;
	}

	private Node successor, predecessor;

	public Node successor() {
		return this.successor;
	}

	public Node predecessor() {
		return this.predecessor;
	}

	public void changeSuccessor(Node n) {
		this.successor = n;
	}

	public void changePredecessor(Node n) {
		this.predecessor = n;
	}

	public Node lookup(int k) {
		if (Helper.between(k, predecessor.getId(), getId())) {
			return this;
		}
		return successor().lookup(k);
	}

	public Object get(int k) {
		return data.get(k);
	}

	public Map<Integer, Object> getAll(int k1, int k2) {
		Map<Integer, Object> result = new HashMap<Integer, Object>();
		for (Map.Entry<Integer, Object> entry : data.entrySet()) {
			if (Helper.between(entry.getKey(), k1, k2)) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		for (Integer key : result.keySet())
			data.remove(key);
		return result;
	}

	public void put(int k, Object v) {
		data.put(k, v);
	}

	public void putAll(Map<Integer, Object> values) {
		data.putAll(values);
	}

	private Map<Integer, Object> getAll() {
		return data;
	}

	public void join(Node known) {
		Node successor = known.lookup(getId());
		Node predecessor = successor.predecessor();
		successor.changePredecessor(this);
		predecessor.changeSuccessor(this);
		changeSuccessor(successor);
		changePredecessor(predecessor);
		putAll(successor.getAll(predecessor.getId(), getId()));
	}

	public void leave() {
		successor().changePredecessor(predecessor());
		predecessor().changeSuccessor(successor());
		successor.putAll(data);

		// Make into own network again
		successor = predecessor = this;
		data.clear();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Node: ");
		sb.append(getId());
		sb.append(" (of ");
		sb.append(Helper.getMax());
		sb.append(")\n");
		if (successor == predecessor && predecessor == this) {
			sb.append(" Disconnected");
		} else {
			sb.append(" Successor: ");
			sb.append(successor().getId());
			sb.append("\n Predecessor: ");
			sb.append(predecessor().getId());
		}
		sb.append("\n Data: ");
		sb.append(getAll());

		return sb.toString();
	}
}
