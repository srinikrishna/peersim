package example.gossip;

import java.util.ArrayList;
import java.util.List;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.core.CommonState;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;


/**
 * @author Lucas Provensi
 * 
 * Basic Shuffling protocol template
 * 
 * The basic shuffling algorithm, introduced by Stavrou et al in the paper: "A Lightweight, Robust
 * P2P System to Handle Flash Crowds", is a simple peer-to-peer communication model. It forms an
 * overlay and keeps it connected by means of an epidemic algorithm. The protocol is extremely
 * simple: each peer knows a small, continuously changing set of other peers, called its neighbors,
 * and occasionally contacts a random one to exchange some of their neighbors.
 * 
 * This class is a template with instructions of how to implement the shuffling algorithm in
 * PeerSim.  Should make use of the classes Entry and GossipMessage: Entry - Is an entry in the
 * cache, contains a reference to a neighbor node and a reference to the last node this entry was
 * sent to.  GossipMessage - The message used by the protocol. It can be a shuffle request, reply or
 * reject message. It contains the originating node and the shuffle list.
 *
 */
public class BasicShuffle  implements Linkable, EDProtocol, CDProtocol{
	
	private static final String PAR_CACHE = "cacheSize";
	private static final String PAR_L = "shuffleLength";
	private static final String PAR_TRANSPORT = "transport";

	private final int tid;

	// The list of neighbors known by this node, or the cache.
	private List<Entry> cache;
	
	// The maximum size of the cache.
	private final int size;
	
	// The maximum length of the shuffle exchange.
	private final int l;

    // The status of the node waiting for a response from a shuffling operation.
    private boolean waiting;

    // The number of entries present in the cache.
    private int cacheCount;

    // The subset of the cache that has been sent to a neighbor node.
    private List<Entry> subset;

	/**
	 * Constructor that initializes the relevant simulation parameters and other class variables.
	 * 
	 * @param n simulation parameters
	 */
	public BasicShuffle(String n)
	{
		this.size = Configuration.getInt(n + "." + PAR_CACHE);
		this.l = Configuration.getInt(n + "." + PAR_L);
		this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);

        cache = new ArrayList<Entry>(size);
        cacheCount = 0;
        waiting = false;
	}

	/* START YOUR IMPLEMENTATION FROM HERE
	 * 
	 * The simulator engine calls the method nextCycle once every cycle (specified in time units in
	 * the simulation script) for all the nodes.
	 * 
	 * You can assume that a node initiates a shuffling operation every cycle.
	 * 
	 * @see peersim.cdsim.CDProtocol#nextCycle(peersim.core.Node, int)
	 */
	@Override
	public void nextCycle(Node node, int protocolID) {
		// Let's name this node as P.
		// 1. If P is waiting for a response from a shuffling operation initiated in a previous
        // cycle, or (2) if P's cache is empty, return;
        if (waiting || cacheCount == 0) {
            return;
        }

        // 3. Select a random neighbor (named Q) from P's cache to initiate the shuffling:
        //    - You should use the simulator's common random source to produce a random number,
        //      CommonState.r.nextInt(cache.size()).
        Entry neighbor = null;
        int index = 0;
        while(neighbor == null) {
            index = CommonState.r.nextInt(cache.size());
            neighbor = cache.get(index);
        }

		// 4. If P's cache is full, remove Q from the cache.
        if (cacheCount == cache.size()) {
            cache.remove(index);
            cacheCount--;
        }
        
		// 5. Select a subset of other l-1 random neighbors from P's cache;
		//	  - l is the length of the shuffle exchange
		//    - Do not add Q to this subset
        int count = 0;
        int rand = CommonState.r.nextInt(cache.size());
        List<Entry> set = new ArrayList<>(l);
        // The cache might not have enough nodes to populate the whole subset
        for (int i = 0; count <= l - 1 || i < cache.size(); i++) {
            int j = (i + rand) % cache.size();
            Entry e = cache.get(j);
            // Checking against object reference should be correct
            if (e != null && !e.equals(neighbor)) {
                set.add(e);
                count++;
            }
        }
        
		// 6. Add P to the subset;
        set.add(new Entry(node));

        // 7. Keep track of the nodes sent to Q;
        subset = set;
        
		// 8. Send a shuffle request to Q containing the subset;
        GossipMessage message = new GossipMessage(node, subset);
        message.setType(MessageType.SHUFFLE_REQUEST);
        Transport tr = (Transport) node.getProtocol(tid);
        tr.send(node, neighbor.getNode(), message, protocolID);
        
		// 8. From this point on P is waiting for Q's response and will not initiate a new shuffle
		// operation.
        waiting = true;
        
		// The response from Q will be handled by the method processEvent.
	}

	/* The simulator engine calls the method processEvent at the specific time unit that an event
	 * occurs in the simulation.  It is not called periodically as the nextCycle method.
	 * 
	 * You should implement the handling of the messages received by this node in this method.
	 * 
	 * @see peersim.edsim.EDProtocol#processEvent(peersim.core.Node, int, java.lang.Object)
	 */
	@Override
	public void processEvent(Node node, int pid, Object event) {
		// Let's name this node as Q.
        // Q receives a message from P, and casts the event object to a message.
		GossipMessage message = (GossipMessage) event;
		
		switch (message.getType()) {
		case SHUFFLE_REQUEST:
            // 1. If Q is waiting for a response from a shuffling initiated in a previous cycle, send
            //    back to P a message rejecting the shuffle request;
            if (waiting) {
                GossipMessage response = new GossipMessage(message.getNode(), null);
                response.setType(MessageType.SHUFFLE_REJECTED);
                Transport tr = (Transport) node.getProtocol(tid);
                tr.send(node, message.getNode(), response, pid);
            }
            else {
                // 2. Q selects a random subset size l of its own neighbors;
                subset = getSubsetFromCache(l);

                // 3. Q reply P's shuffle request by sending back its own subset;
                GossipMessage response = new GossipMessage(node, subset);
                message.setType(MessageType.SHUFFLE_REPLY);
                Transport tr = (Transport) node.getProtocol(tid);
                tr.send(node, message.getNode(), response, pid);

                // 4. Q updates its cache to include the neighbors sent by P;
                List<Entry> shuffleList = message.getShuffleList();
                for (Entry e : shuffleList) {
                    updateCache(e);
                }
            }
			break;
		
		case SHUFFLE_REPLY:
            // In this case Q initiated a shuffle with P and is receiving a response containing a
            // subset of P's neighbors.
            //  1. Q updates its cache to include the neighbors sent by P;
            List<Entry> shuffleList = message.getShuffleList();
            for (Entry e : shuffleList) {
                updateCache(e);
            }
            // 2. Q is no longer waiting for a shuffle reply;
            waiting = false;
			break;
		
		// If the message is a shuffle rejection:
		case SHUFFLE_REJECTED:
            // 1. If P was originally removed from Q's cache, add it again to the cache.
            if (contains(message.getNode())) {
                updateCache(new Entry(message.getNode()));
            }
            //	2. Q is no longer waiting for a shuffle reply;
            waiting = false;
			break;
			
		default:
			break;
		}
		
	}
    
    private List<Entry> getSubsetFromCache(int size) {
        List<Entry> lst = new ArrayList<Entry>();
        int rand = CommonState.r.nextInt(cache.size());
        // The cache might not have enough nodes to populate the whole subset
        for (int i = 0; lst.size() <= size || i < cache.size(); i++) {
            int j = (i + rand) % cache.size();
            Entry e = cache.get(j);
            if (e != null) {
                lst.add(e);
            }
        }
        return lst;
    }

    // Updates the cache with the following constraints:
    //    - No neighbor appears twice in the cache.
    //    - Use empty cache slots to add the new entries.
    //    - If the cache is full, you can replace entries among the ones sent to P with the new
    //      ones (The ones in the subset sent to P is already removed from the cache).
    private void updateCache(Entry entry) {
        if (entry == null || indexOf(entry, cache) > -1) {
            // Entry is null or it is already in cache;
            return;
        } else if (cacheCount == cache.size()) {
            // The cache is full. Replace an entry in the cache that is also in the created subset;
            int index = -1;
            for (Entry e : subset) {
                index = indexOf(e, cache);
                if (index > -1) {
                    break;
                }
            }
            cache.set(index, entry);
        } else {
            // The cache is not full. Place the entry at the first empty slot;
            for (int i = 0; i < cache.size(); i++) {
                if (cache.get(i) == null) {
                    cache.set(i, entry);
                    cacheCount++;
                }
            }
        }
    }

    // Finds the index of the entry e in set and returns it. If the entry is not in the set, then -1
    // is returned.
    private int indexOf(Entry e, List<Entry> set) {
        for (int i = 0; i < set.size(); i++) {
            Entry setEntry = set.get(i);
            if (setEntry != null && e.getNode().getID() == setEntry.getNode().getID()) {
                return i;
            }
        }
        return -1;
    }
    /* The following methods are used only by the simulator and don't need to be changed */
	
	@Override
	public int degree() {
		return cache.size();
	}

	@Override
	public Node getNeighbor(int i) {
		return cache.get(i).getNode();
	}
    
	@Override
	public boolean addNeighbor(Node neighbour) {
		if (contains(neighbour))
			return false;

		if (cache.size() >= size)
			return false;

		Entry entry = new Entry(neighbour);
		cache.add(entry);

		return true;
	}

	@Override
	public boolean contains(Node neighbor) {
		return cache.contains(new Entry(neighbor));
	}

	public Object clone()
	{
		BasicShuffle gossip = null;
		try { 
			gossip = (BasicShuffle) super.clone(); 
		} catch( CloneNotSupportedException e ) {
			
		} 
		gossip.cache = new ArrayList<Entry>();

		return gossip;
	}

	@Override
	public void onKill() {
		// TODO Auto-generated method stub		
	}

	@Override
	public void pack() {
		// TODO Auto-generated method stub	
	}
}
