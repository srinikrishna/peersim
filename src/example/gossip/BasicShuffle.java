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

    // Turns on/off debugging printing.
    private boolean debug = false;
    
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
        if (debug) {
            System.out.println("nextCycle: Node " + node.getID() + " has cache:" +
                               listToString(cache));
        }

		// Let's name this node as P.
		// 1. If P is waiting for a response from a shuffling operation initiated in a previous
        // cycle, or (2) if P's cache is empty, return;
        if (waiting || cache.isEmpty()) {
            return;
        }

        waiting = true;
        
        // 3. Select a random neighbor (named Q) from P's cache to initiate the shuffling:
        //    - You should use the simulator's common random source to produce a random number,
        //      CommonState.r.nextInt(cache.size()).
        int neighborIndex = CommonState.r.nextInt(cache.size());
        Node neighbor = cache.get(neighborIndex).getNode();

		// 4. If P's cache is full, remove Q from the cache.
        if (cache.size() == size) {
            cache.remove(neighborIndex);
        }
        
		// 5. Select a subset of other l-1 random neighbors from P's cache;
		//	  - l is the length of the shuffle exchange
		//    - Do not add Q to this subset
        List<Entry> subset = new ArrayList<>(l);
        int count = 0;
        int r = CommonState.r.nextInt(cache.size());
        for (int i = 0; count <= l - 1 && i < cache.size(); i++) {            
            int j = (i + r) % cache.size();
            Entry entry = cache.get(j);
            if (entry.getNode().getID() != neighbor.getID()) {
                entry.setSentTo(neighbor);
                subset.add(new Entry(entry.getNode()));
                count++;
            }
        }

		// 6. Add P to the subset;
        subset.add(new Entry(node));

		// 8. Send a shuffle request to Q containing the subset;
        GossipMessage message = new GossipMessage(node, subset);
        message.setType(MessageType.SHUFFLE_REQUEST);
        Transport tr = (Transport) node.getProtocol(tid);
        tr.send(node, neighbor, message, protocolID);

        if (debug) {
            System.out.println("nextCycle: Node " + node.getID() + " sends subset " +
                               listToString(subset) + " to node " + neighbor.getID());
        }
        
		// 8. From this point on P is waiting for Q's response and will not initiate a new shuffle
		// operation.
        
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
                GossipMessage response = new GossipMessage(message.getNode(), new ArrayList<>());
                response.setType(MessageType.SHUFFLE_REJECTED);
                Transport tr = (Transport) node.getProtocol(tid);
                tr.send(node, message.getNode(), response, pid);

                if (debug) {
                    System.out.format("processEvent: Node %d rejects shuffle request from node %d\n",
                                      node.getID(), message.getNode().getID());
                }
            } else {
                // 2. Q selects a random subset size l of its own neighbors;
                List<Entry> subset = new ArrayList<>(l);
                int count = 0;
                int r = (cache.isEmpty()) ? 0 : CommonState.r.nextInt(cache.size());
                for (int i = 0; count <= l && i < cache.size(); i++) {
                    int j = (i + r) % cache.size();
                    Entry entry = cache.get(j);
                    if (entry.getNode().getID() != message.getNode().getID()) {
                        entry.setSentTo(message.getNode());
                        subset.add(new Entry(entry.getNode()));
                        count++;
                    }
                }

                // 3. Q reply P's shuffle request by sending back its own subset;
                GossipMessage reply = new GossipMessage(node, subset);
                reply.setType(MessageType.SHUFFLE_REPLY);
                Transport tr = (Transport) node.getProtocol(tid);
                tr.send(node, message.getNode(), reply, pid);

                if (debug) {
                    System.out.format("processEvent: Node %d replies to node %d with subset %s\n",
                                      node.getID(),message.getNode().getID(), listToString(subset));
                }

                // 4. Q updates its cache to include the neighbors sent by P;
                List<Entry> shuffle = message.getShuffleList();
                for (Entry entry : shuffle) {                    
                    updateCache(entry.getNode(), node.getID(), message.getNode().getID());
                }
                // reset entries sent to P;
                for (Entry entry : cache) {
                    Node n = entry.getSentTo();
                    if (n != null && n.getID() == message.getNode().getID()) {
                        entry.setSentTo(null);
                    }
                }
                
            }
			break;
		
		case SHUFFLE_REPLY:
            if (debug) {
                System.out.format("processEvent: Node %d received shuffle set %s from node %d\n",
                                  node.getID(), listToString(message.getShuffleList()),
                                  message.getNode().getID());
            }
            // In this case Q initiated a shuffle with P and is receiving a response containing a
            // subset of P's neighbors.
            //  1. Q updates its cache to include the neighbors sent by P;
            List<Entry> shuffle = message.getShuffleList();
            for (Entry entry: shuffle) {
                updateCache(entry.getNode(), node.getID(), message.getNode().getID());
            }
            // reset entries sent to P;
            for (Entry entry : cache) {
                Node n = entry.getSentTo();
                if (n != null && n.getID() == message.getNode().getID()) {
                    entry.setSentTo(null);
                }
            }
            
            // 2. Q is no longer waiting for a shuffle reply;
            waiting = false;
			break;
		
		case SHUFFLE_REJECTED:
            // 1. If P was originally removed from Q's cache, add it again to the cache.
            if (!inCache(message.getNode())) {
                updateCache(message.getNode(), node.getID(), message.getNode().getID());                
            }
            // reset entries sent to P;
            for (Entry entry : cache) {
                Node n = entry.getSentTo();
                if (n != null && n.getID() == message.getNode().getID()) {
                    entry.setSentTo(null);
                }
            }
            
            //	2. Q is no longer waiting for a shuffle reply;
            waiting = false;
			break;
			
		default:
			break;
		}
		
	}

    // Updates the cache with the following constraints:
    //    - No neighbor appears twice in the cache.
    //    - Use empty cache slots to add the new entries.
    //    - If the cache is full, you can replace entries among the ones sent to P with the new
    //      ones.
    private void updateCache(Node node, long thisNodeID, long destNodeID) {
        // DEBUG
        // System.out.format("updateCache: Adding node %d to cache %s of node %d\n", node.getID(),
        //                   listToString(cache), thisNodeID);
            
        if (inCache(node) || node.getID() == thisNodeID) {
            return;
        }
        
        if (cache.size() < size) {
            // There is an empty cache slot;
            cache.add(new Entry(node));
        } else if (cache.size() == size) {
            // The cache is full. Replace an entry in the cache that has just been sent;
            for (int i = 0; i < cache.size(); i++) {
                Node n = cache.get(i).getSentTo();
                if (n != null && n.getID() == destNodeID) {
                    cache.set(i, new Entry(node));
                    break;
                }
            }
        }
        // DEBUG
        // System.out.format("updateCache: New cache of %d: %s\n", thisNodeID, listToString(cache));
    }

    // Finds the index of the entry e in set and returns it. If the entry is not in the set, then -1
    // is returned.
    private boolean inCache(Node node) {
        for (Entry e : cache) {            
            if (node.getID() == e.getNode().getID()) {
                return true;
            }
        }
        return false;
    }

    private String listToString(List<Entry> list) {
        if (list.size() == 0) {
            return "{}";
        }
        
        String str = "{";

        for (int i = 0; i < list.size() - 1; i++) {
            str += list.get(i).getNode().getID() + ", ";
        }
        
        str += list.get(list.size() - 1).getNode().getID() + "}";
        
        return str;
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
