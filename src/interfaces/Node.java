package interfaces;

import java.util.Map;

/**
 * @author mw
 * Interface for a node in Chord
 */
public interface Node {
	/**
	 * @return successor node in ring
	 */
	Node successor();
	
	/**
	 * @return predecessor node in ring
	 */
	Node predecessor();
	
	/**
	 * @return the id of this node
	 */
	int getId();
	
	/**
	 * Assign a new successor. Does not update predecessor of old/new successor.
	 */
	void changeSuccessor(Node n);
	
	/**
	 * Assign a new predecessor.  Does not update successor of old/new predecessor.
	 */
	void changePredecessor(Node n);

	/**
	 * Look up key k in the ring.  Will call recursively if needed.
	 */
	Node lookup(int k);
	
	/**
	 * Join a Chord ring given a single known node.  Will insert itself correctly
	 * into the ring as well as transfer all data items this node becomes responsible
	 * for.
	 */
	void join(Node known);
	
	/**
	 * Leave a Chord ring.  Will remove itself correctly from the ring and transfer
	 * all data items belonging to this node to the new owner.
	 */
	void leave();

	/**
	 * Return data item k.  Will NOT call recursively, so you will need to call
	 * lookup to find the correct owner.
	 */
	Object get(int k);
	
	/**
	 * Get all data items from exlcusively k1 to incluseively k2.  Does not call
	 * recursively, so you may need to call lookup.  Mainly used for joining a ring.
	 */
	Map<Integer, Object> getAll(int k1, int k2);
	
	/**
	 * Store a data item.  Will not call recursively, so you will need to call
	 * lookup to find the correct node.
	 */
	void put(int k, Object v);
	
	/**
	 * Store all data items.  Does not call recursively, so you may need to call
	 * lookup.  Mainly used for leaving a ring.
	 */
	void putAll(Map<Integer, Object> values);
}

