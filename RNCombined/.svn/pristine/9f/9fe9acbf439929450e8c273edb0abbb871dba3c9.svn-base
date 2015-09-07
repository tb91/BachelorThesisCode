package projects.reactiveSpanner.nodes.messageHandlers.buildBackbone;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import projects.defaultProject.nodes.timers.MessageTimer;
import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.BridgePair;
import projects.reactiveSpanner.CustomGlobal;
import projects.reactiveSpanner.Utilities;
import projects.reactiveSpanner.nodes.messageHandlers.AbstractMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.AbstractTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconMessageHandler;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.messages.BeaconReplyMessage;
import projects.reactiveSpanner.nodes.messages.BeaconRequestMessage;
import projects.reactiveSpanner.nodes.messages.DataMessage;
import projects.reactiveSpanner.nodes.messages.DeliverBridgeNodesMessage;
import projects.reactiveSpanner.nodes.messages.DeliverSetMessage;
import projects.reactiveSpanner.nodes.messages.data.AdjacencyLists;
import projects.reactiveSpanner.nodes.messages.data.ID;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.timers.BeaconTimer;
import projects.reactiveSpanner.record.MessageRecord;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.nodes.edges.Edge;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

/**
 * @author Timmy
 *
 * Message Handler for the BuildBackbone protocol
 * knownneighbors contains only nodes which belong to the Backbone graph of the topologyControl
 * knownNeighborsTotal contains the backbone graph including all clustermembers
 */
public class BuildBackboneMessageHandler extends BeaconMessageHandler<PhysicalGraphNode> {

	/**
	 * the id which this belongs to
	 */
	final int gridCellID;
	/**
	 * the length of the diagonal each Gridcell has = Qr
	 */
	static float diagonal;
	private boolean isClusterHead = false;
	private boolean isBridgeNode = false;

	public static enum Phase {
		SELECT_CLUSTER_HEAD,
		BROADCAST_CLUSTERHEAD,
		BROADCAST_DISTINCT,
		BRIDGE_COMPUTATION,
		FINALIZING_BR,
		FINISHED;
	}

	/**
	 * saves the phase the protocol is currently in (all nodes always have the same Phase)
	 */
	private Phase currentPhase;

	private BuildBackboneMessageHandler clusterhead; // links to the clusterhead
														// of this Node

	protected static Logging logger = Logging.getLogger();

	/**
	 * a link to the topologyControl which created this messagehandler
	 */
	private final BuildBackbone control;

	/**
	 * saves distinct clusterheads c with nodes which have c as clusterhead to gain knowledge about bridgenodes;
	 * 
	 */
	private HashMap<BuildBackboneMessageHandler, BuildBackboneMessageHandler> distinctClusterheads;


	/**
	 * each clusterhead contains distinctClusterheads for each neighbor node
	 */
	private HashMap<BuildBackboneMessageHandler, HashMap<BuildBackboneMessageHandler, BuildBackboneMessageHandler>> bridgeNodes;

	/*
	 * each node contains three different WangLi neighbor-sets
	 */
	private HashSet<Node> knownNeighborsBlue = new HashSet<Node>();
	private HashSet<Node> knownNeighborsRed = new HashSet<Node>();
	private HashSet<Node> knownNeighborsGreen = new HashSet<Node>();
	
	
	/**
	 * knownNeighborsTotal contains the backbone graph including all clustermembers
	 */
	private HashSet<Node> knownNeighborsTotal= new HashSet<Node>();

	
	/**
	 * contains a list of bridgePairs which lead to cluster with corresponding ids
	 */
	private HashMap<Integer, BridgePair> completePaths = new HashMap<Integer, BridgePair>(); 

	/**
	 * contains the routing to adjacent clusters 
	 * bridgePair is used to store since this map is
	 * sent to other nodes and they need to look up
	 * whether or not they are the first nodes in this pair
	 */
	private HashMap<Integer, BridgePair> bridgeRouting = new HashMap<Integer, BridgePair>();

	/**
	 * Do you want to see the neighbors of each node?
	 */
	public boolean showNeighbors = true;

	static {
		try {
			diagonal = (float) Configuration.getDoubleParameter("QUDG/rMin");
		} catch (CorruptConfigurationEntryException e) {
			logger.logln(LogL.WARNING, "Parameter QUDG/rMin could not be read!");
			e.printStackTrace();

		}

	}

	protected BuildBackboneMessageHandler(UUID tcID, PhysicalGraphNode ownerNode, PhysicalGraphNode sourceNode, EStrategy strategy, BuildBackbone control) {
		super(tcID, ownerNode, sourceNode, strategy);

		this.control = control;

		gridCellID = calculateGridCellID(this.node.getPosition());

		distinctClusterheads = new HashMap<BuildBackboneMessageHandler, BuildBackboneMessageHandler>();
		bridgeNodes = new HashMap<BuildBackboneMessageHandler, HashMap<BuildBackboneMessageHandler, BuildBackboneMessageHandler>>();
		

		isClusterHead = true;
		clusterhead = this;

		currentPhase = Phase.SELECT_CLUSTER_HEAD;
		new BeaconTimer(this, 5); // add timers to call nextPhase(),
		new BeaconTimer(this, 10);
		new BeaconTimer(this, 15);
		new BeaconTimer(this, 20);
		new BeaconTimer(this, 25);
		new BeaconTimer(this, 28);

	
	}

	/**
	 * @param pos
	 *            the position of the node s
	 * @return the cellId of the node s
	 */
	public static int calculateGridCellID(Position pos) {
		float gridSize = (float) (diagonal / Math.sqrt(2));

		return (int) ((pos.xCoord / gridSize) + 1) + (int) ((Configuration.dimX / gridSize) + 1) * (int) (pos.yCoord / gridSize);
	}

	/**
	 * @param pos
	 *            the position of the node
	 * @param deltaX
	 *            the x adjustment
	 * @param deltaY
	 *            the y adjustment
	 * @return the id which is associated to the translated coordinates.
	 */
	public static int calculateGridCellID(Position pos, double delta) {
		float gridSize = (float) (diagonal / Math.sqrt(2));
		double x = pos.xCoord - delta + gridSize;
		double y = pos.yCoord - delta + gridSize;

		int idx = (int) ((x / gridSize) + 1);
		int idy = (int) (((Configuration.dimX + gridSize) / gridSize) + 1) * (int) (y / gridSize);

		return idx + idy;
	}

	@Override
	protected void executeTimerQueue() {
		super.executeTimerQueue();

	}

	@Override
	public void receivedMessage(AbstractMessage msg) {
		if (msg instanceof BeaconRequestMessage) {
			throw new RuntimeException("Nodes should not receive a BeaconRequestMessage using Buildbackbone");
		} else if (msg instanceof BeaconReplyMessage) {
			receivedBeaconReplyMessage((BeaconReplyMessage) msg);
		} else if (msg instanceof DeliverSetMessage) {
			receivedDeliverSetMessage((DeliverSetMessage) msg);
		} else if (msg instanceof DeliverBridgeNodesMessage) {
			receivedDeliverBridgeNodesMessage((DeliverBridgeNodesMessage) msg);
		} else if (msg instanceof DataMessage) {
			receivedDataMessage((DataMessage<?>) msg);//TODO: add different datas here
		} else {
			throw new RuntimeException("Message type is not supoorted: " + msg.toString());
		}
		
//		System.out.println(this.node + " received a message from: " + msg.getTransmitter() + " (" + msg.getClass() + ")");

	}

	private void receivedDataMessage(DataMessage<?> msg) {
		Object payload = msg.getPayload();
		if (payload instanceof ID) {
			Integer foreignID = ((ID) payload).getId();
			if (gridCellID == calculateGridCellID(msg.getTransmitter().getPosition())) {

				if (foreignID < clusterhead.node.ID) {

					setClusterhead(msg.getTransmitter().getMessageHandler(tcID));
				}

			}

		} else if (payload instanceof AdjacencyLists) {
			// System.out.println(this.node + " received a wanglimsg from: " + msg.getTransmitter());
			AdjacencyLists wangLiNeighbors = (AdjacencyLists) payload;
			if (knownNeighborsBlue.isEmpty()) {
				Set<Node> blue = wangLiNeighbors.getBlueWangLi().get(this.node);
				if (blue != null) {
					for (Node n : blue) {
						knownNeighborsBlue.add(n);
					}
				} else {
					// System.out.println(this.node + " did not found himself in blue list from: " + msg.getTransmitter());
				}

			} else {

				// // =========else branch is testing whether or not all clusterheads compute the same WangLiNeighbors=======
				//
				// Set<Node> neu = wangLiNeighbors.getBlueWangLi().get(this.node);
				// if (neu != null) {
				// if (neu.size() != knownNeighborsBlue.size()) {
				// throw new RuntimeException(this.node + " got a new Message with blue WangLiNeighbors with different size (from: " + msg.getTransmitter());
				// }
				// for (Node n : neu) {
				// if (!knownNeighborsBlue.contains(n)) {
				// throw new RuntimeException(this.node + " got a new Message with WangLiNeighbors where " + n + " is not in the blue neighbors");
				// }
				// }
				// } else {
				// // System.out.println(this.node + " did not found himself in blue list from: " + msg.getTransmitter() + " while checking");
				// }

			}
			if (knownNeighborsRed.isEmpty()) {
				Set<Node> red = wangLiNeighbors.getRedWangLi().get(this.node);
				if (red != null) {
					for (Node n : red) {
						knownNeighborsRed.add(n);
					}
				} else {
					// System.out.println(this.node + " did not found himself in red list from: " + msg.getTransmitter());
				}

			}
			if (knownNeighborsGreen.isEmpty()) {
				Set<Node> green = wangLiNeighbors.getGreenWangLi().get(this.node);
				if (green != null) {
					for (Node n : green) {
						knownNeighborsGreen.add(n);
					}
				} else {
					// System.out.println(this.node + " did not found himself in Green list from: " + msg.getTransmitter());
				}

			}

		} else {
			throw new RuntimeException("Data Message contains payload of illegal type: " + msg);
		}
	}

	@Override
	public void receivedBeaconRequestMessage(BeaconRequestMessage brm) {
		// not needed

	}

	@Override
	public void receivedBeaconReplyMessage(BeaconReplyMessage brm) {
		// logger.logln(LogL.INFO, this.node + " received replyMessage from " +
		// brm.getTransmitter());

		// knownNeighbors.add(brm.getTransmitter());

		if (currentPhase == Phase.SELECT_CLUSTER_HEAD) {
			throw new RuntimeException("should not happen");
		} else if (currentPhase == Phase.FINISHED) {

			if (isClusterHead || isBridgeNode) {// transmitter must be a clusterhead or bridgenode so check whether this is a clusterhead or a bridgenode too
				knownNeighbors.add(brm.getTransmitter());
			    knownNeighborsTotal.add(brm.getTransmitter());
			}
		}

	}

	/**
	 * will be called as soon as nextPhase switches to SELECT_BRIDGE_NODES it broadcasts the nodes clusterheads to (1 hop) neighborhood
	 */
	protected void broadcastClusterhead() {

		HashMap<BuildBackboneMessageHandler, BuildBackboneMessageHandler> wrappedClusterhead = new HashMap<BuildBackboneMessageHandler, BuildBackboneMessageHandler>();
		wrappedClusterhead.put(this, clusterhead);
		this.enqueue(new MessageTimer(new DeliverSetMessage(super.tcID, this.node, planarSubgraphCreationStrategy, wrappedClusterhead)));
		this.executeTimerQueue();

		if (isClusterHead) { // a clusterhead does not receive a message from itself so we simulate this message
								// and add it do distinct clusterheads
			// distinctClusterheads.put(this, this); //not needed anymore since outgoingconnections from the clusterhead are being prefered

		}

		// knownNeighbors.add(clusterhead.node);//add this if a clusterhead must add himself as a neighbor for some reason
	}

	protected void receivedDeliverSetMessage(DeliverSetMessage msg) {
		switch (currentPhase) {
		case BROADCAST_CLUSTERHEAD:
			BuildBackboneMessageHandler transmittersClusterhead = msg.getDistinctIDs().get(msg.getTransmitter().getMessageHandler(tcID));
			if (transmittersClusterhead == null) {
				logger.logln(projects.reactiveSpanner.LogL.ERROR_DETAIL, "transmittersClusterhead must not be empty");
			}
			
			if (!distinctClusterheads.containsValue(transmittersClusterhead)) {
				distinctClusterheads.put((BuildBackboneMessageHandler) msg.getTransmitter().getMessageHandler(tcID), transmittersClusterhead);// Typecast is save

				/*
				 * if (this.node.ID == 1) { logger.logln(LogL.INFO, transmittersClusterhead.toString()); logger.logln(LogL.INFO, this.node + " adds Clusterhead " + transmittersClusterhead + " to it's distinctClusterheads"); }
				 */
				// / just needed for debugging
			}

			break;
		case BROADCAST_DISTINCT:
			handleDistinctSet(msg);
			break;
		default:
			logger.logln(LogL.ERROR_DETAIL, "Received a DeliverSetMessage in the wrong phase: " + currentPhase);
			break;
		}
	}

	/**
	 * broadcasts the set of distinct clusterheads
	 */
	protected void broadcastDistinctSet() {

		this.enqueue(new MessageTimer(new DeliverSetMessage(super.tcID, this.node, planarSubgraphCreationStrategy, distinctClusterheads)));
		this.executeTimerQueue();
	}

	/**
	 * @param msg
	 *            the message containing the distinct set of IDs of clusterheads
	 * 
	 */
	private void handleDistinctSet(DeliverSetMessage msg) {
		if (isClusterHead) {
			bridgeNodes.put((BuildBackboneMessageHandler) msg.getTransmitter().getMessageHandler(tcID), msg.getDistinctIDs()); // Typecast is save

		}

	}

	/**
	 * each clusterhead calculates one bridgeEdge in each surrounding gridCell and saves it in completePaths
	 */
	public void computeBridgeEdges() {
		if (isClusterHead) {// every clusterhead needs to compute the bridgeEdges
			for (Edge e : this.node.outgoingConnections) {
				AbstractMessageHandler amh = null;
				if (e.endNode instanceof PhysicalGraphNode && (amh = ((PhysicalGraphNode) e.endNode).getMessageHandler(this.tcID)) instanceof BuildBackboneMessageHandler) {

					BuildBackboneMessageHandler bbmh = (BuildBackboneMessageHandler) amh;

					if (!completePaths.containsKey(bbmh.gridCellID) && bbmh.gridCellID != this.gridCellID) { // second condition is needed to prevent
																												// clusterheads to declare a node as bridgenode
																												// which leads to its own cluster

						completePaths.put(bbmh.gridCellID, new BridgePair(this.node, e.endNode, this.tcID));
					}
				} else {
					logger.logln(LogL.WARNING, "The following Node was not considered in computing the bridge edges since it is either " + "no PhysicalGraphNode or it has no BuildBackboneMh: " + e.endNode + " id:" + this.tcID);
				}
				
			}
			// System.out.println(this.node + " computes bridges");
			for (BuildBackboneMessageHandler current : bridgeNodes.keySet()) {
				HashMap<BuildBackboneMessageHandler, BuildBackboneMessageHandler> distinctClusters = bridgeNodes.get(current);
				for (BuildBackboneMessageHandler bbmh : distinctClusters.keySet()) {// for each bbbMH in distinctClusters do

					if (bbmh.gridCellID != this.gridCellID) {// filter the entry in which the cell is the computing clusterhead's cell

						if (!completePaths.containsKey(bbmh.gridCellID)) {// if no mapping exists yet
							BridgePair bp = null;
							if (!current.equals(bbmh)) {

								if (current.gridCellID == this.gridCellID) { // a Clusterhead should not create bridgeEdges which do not start in its cluster
									bp = new BridgePair(current, bbmh);
									completePaths.put(bbmh.gridCellID, bp);
									/*
									 * if (this.node.ID == 1) System.out.println("putting " + bbmh.gridCellID + " :   " + current + "  " + bbmh);
									 */
								} else {//this case is wrong, do not use i
									// bp = new BridgePair(this, current);// special case: Xc X | Xc  
									// completePaths.put(current.gridCellID, bp);
									// System.out.println("found a special case: clusterhead " + this + " found path to: " + current);
								}
								// if(this.node.ID==43)System.out.println("added: " + bp);
							}

						} else {// if a bridge Edge already leads to this cell, check whether or not the new edge ends at a clusterhead
							if (bbmh.isClusterHead) { // edges from a clusterhead to a clusterhead will be preferred
								if (this.node.outgoingConnections.contains(this.node, bbmh.node)) {// check whether this has a connection to the new clusterhead

									BridgePair pair = completePaths.get(bbmh.gridCellID);
									// System.out.println("changed " + bbmh.gridCellID + " " + pair);
									if (this.equals(pair.first)) {// ensures that this node has a connection to the endnode
										// not needed since all outgoing connections are already considered
										pair.second = bbmh;
										// completePaths.put(bbmh.gridCellID, pair);
									}
								}

							}
						}

					}

				}

			}
			publishBridgeNodeList();
		}

	}

	/**
	 * each clusterhead broadcasts it's list of elected bridgeEdges
	 */
	private void publishBridgeNodeList() {
		this.enqueue(new MessageTimer(new DeliverBridgeNodesMessage(this.tcID, this.node, this.planarSubgraphCreationStrategy, completePaths)));
		this.executeTimerQueue();

	}

	/**
	 * @param msg
	 *            nodes receive a message containing all bridge nodes twice first from it's clusterhead second from a possible bridgeNodeNeighbor
	 */
	private void receivedDeliverBridgeNodesMessage(DeliverBridgeNodesMessage msg) {
		if (currentPhase == Phase.BRIDGE_COMPUTATION) {

			BuildBackboneMessageHandler check = (BuildBackboneMessageHandler) msg.getTransmitter().getMessageHandler(tcID);// Typecast is save

			if (!isClusterHead && check.gridCellID == this.gridCellID) { // if this is no clusterhead and the message comes from this' cluterhead do
//				System.out.println(this.node + " f�gt " + check.node + " zu den known neighbors hinzu");
				knownNeighborsTotal.add(check.node); // add clusterhead to knownneighborsTotal
				
				HashMap<Integer, BridgePair> bbmhMap = msg.getBridgeNodes();

				for (Integer gridID : bbmhMap.keySet()) { // search bridgeNodes to find this as second entry in the bbmhmap (which means: this is a bridge node)

					BridgePair bridgepair = bbmhMap.get(gridID);
					// System.out.println(bridgepair.first.node + ", " + bridgepair.second.node);
					if (bridgepair.first.node.equals(this.node)) {// this must be a start node if it is a bridge node (since the message comes from its clusterhead...)
						isBridgeNode = true;
						// System.out.println(this.node.toString() + " becomes a bridgeNode");

						
						bridgeRouting.put(gridID, bridgepair);//this node must remember
					} else {

						// bbmhMap.remove(gridID); // remove current entry from list, so this can send it further soon

					}
				}

			}

		} else if (currentPhase == Phase.FINALIZING_BR) {
			HashMap<Integer, BridgePair> bbmhMap = msg.getBridgeNodes();
			for (Integer gridID : bbmhMap.keySet()) { // search bridgeNodes to find this as first or second entry in the bbmhmap (which means: this is a bridge node)
				BridgePair bridgepair = bbmhMap.get(gridID);
				if (bridgepair.first.node.equals(this.node) || bridgepair.second.node.equals(this.node)) {
					isBridgeNode = true;
					// System.out.println(this.node.toString() + " becomes a bridgeNode");
					// knownNeighbors.add(bridgepair.first.node);
					bridgeRouting.put(gridID, new BridgePair(bridgepair.second, bridgepair.first));// need to change the gridcellid but if i do this. There can be more than one (maximum 2) ways to route a message to cluster x
				}

			}


		}
	}

	/**
	 * @param n
	 *            the node which should become this' clusterhead
	 */
	protected void setClusterhead(AbstractMessageHandler n) {

		if (n instanceof BuildBackboneMessageHandler) {
			if (n.equals(this.node)) {
				isClusterHead = true;
			} else {
				isClusterHead = false;
			}
			clusterhead = (BuildBackboneMessageHandler) n;
		} else {
			logger.logln(LogL.WARNING, "Tried to set a non-PhysicalGraphNode with wrong MessageHandler as clusterhead");
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BuildBackboneMessageHandler) {
			BuildBackboneMessageHandler bbmh = (BuildBackboneMessageHandler) obj;
			if (bbmh.tcID == this.tcID && this.node.equals(bbmh.node)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void drawNode(Graphics g, PositionTransformation pt) {
		if (currentPhase == Phase.FINISHED) {
			if (CustomGlobal.showWangLiBlue) {
				for (Node n : knownNeighborsBlue) {

					g.setColor(Color.blue);
					pt.drawLine(g, this.node.getPosition(), n.getPosition());

				}
			}
			if (CustomGlobal.showWangLiRed) {
				for (Node n : knownNeighborsRed) {
					g.setColor(Color.red);
					pt.drawLine(g, this.node.getPosition(), n.getPosition());

				}
			}

			if (CustomGlobal.showWangLiGreen) {
				for (Node n : knownNeighborsGreen) {
					g.setColor(Color.green);
					pt.drawLine(g, this.node.getPosition(), n.getPosition());

				}

			}
		} else {
			if (isClusterHead) {
				this.node.setColor(Color.red);

				pt.translateToGUIPosition(new Position(Configuration.dimX, Configuration.dimY, 0));
				final int startX = (int) (pt.guiX - Configuration.dimX * pt.getZoomFactor());
				final int startY = (int) (pt.guiY - Configuration.dimY * pt.getZoomFactor());
				g.drawString(gridCellID + "", (int) (startX + this.node.getPosition().xCoord * pt.getZoomFactor()), (int) (startY + this.node.getPosition().yCoord * pt.getZoomFactor()));

			} else if (isBridgeNode) {

				this.node.setColor(Color.pink);

			} else {
				this.node.setColor(Color.blue);

			}

			if (this.showNeighbors) {

				for (Node n : knownNeighborsTotal) {
					BuildBackboneMessageHandler bbmh = (BuildBackboneMessageHandler) ((PhysicalGraphNode)n).getMessageHandler(this.tcID);
					g.setColor(Color.black);
					if (!this.isBridgeNode && !this.isClusterHead && bbmh.isClusterHead) {
						g.setColor(Color.gray);
						pt.drawDottedLine(g, this.node.getPosition(), n.getPosition());
					} else if (bbmh.gridCellID != this.gridCellID) {
						g.setColor(Color.green);
						pt.drawLine(g, this.node.getPosition(), bbmh.node.getPosition());

					} else {
						pt.drawLine(g, this.node.getPosition(), bbmh.node.getPosition());
					}

				}
			}
		}
		g.setColor(Color.black);
	}

	@Override
	public MessageRecord getCurrentMessageRecord() {
		
		return null;
	}

	@Override
	public String toString() {
		return this.node.toString(); // + " (clusterhead:" + isClusterHead + "; bridgenode:" + isBridgeNode + ") ";
	}

	@Override
	public void beaconTimerFire() {
		nextPhase();

	}

	/**
	 * switches to the next phase: SELECT_CLUSTER_HEAD -> BROADCAST_CLUSTERHEAD -> BROADCAST_DISTINCT -> BRIDGE_COMPUTATION -> FINALIZING_BR -> ALMOST_FINISHED -> FINISHED;
	 */
	public void nextPhase() {

		switch (currentPhase) {
		case SELECT_CLUSTER_HEAD:
			currentPhase = Phase.BROADCAST_CLUSTERHEAD;
			this.broadcastClusterhead();
			break;
		case BROADCAST_CLUSTERHEAD:
			currentPhase = Phase.BROADCAST_DISTINCT;
			this.broadcastDistinctSet();
			break;
		case BROADCAST_DISTINCT:
			currentPhase = Phase.BRIDGE_COMPUTATION;
			this.computeBridgeEdges();
			// needed to show routing tables
			/*
			 * if (this.node.ID == 1) { for (Integer i : completePaths.keySet()) { System.out.print(i + ": "); for (BuildBackboneMessageHandler bbmh : completePaths.get(i)) { System.out.print(bbmh + " -> "); } System.out.println(); } }
			 */
			break;
		case BRIDGE_COMPUTATION:
			currentPhase = Phase.FINALIZING_BR;
			bridgeNodeBroadcast();
			break;
		case FINISHED:
			break;
		case FINALIZING_BR:
			requestedNodes.clear();// so the replymessage will be accepted;
			lastBroadcast();
			currentPhase = Phase.FINISHED;
			break;
		default:
			break;
		}
	}

	protected void loadWangLiNeighbors() {
		// store prior computed WangLiNeighbors
		Set<Node> blue = control.adjacencyListBlue.get(this.node);
		for (Node n : blue) {
			knownNeighborsBlue.add(n);
		}
		Set<Node> red = control.adjacencyListRed.get(this.node);
		for (Node n : red) {
			knownNeighborsRed.add(n);
		}

		Set<Node> green = control.adjacencyListGreen.get(this.node);
		for (Node n : green) {
			knownNeighborsGreen.add(n);
		}
	}

	/**
	 * this is the last broadcast the algorithm does. This method is needed so every node sends its actual role: clusterhead or BridgeNode every nodes who receives this message checks whether it is a clusterhead or bridgenode too and add it to it's known neighbors
	 */
	private void lastBroadcast() {

		if (isBridgeNode || isClusterHead) {

			this.enqueue(new MessageTimer(new BeaconReplyMessage(tcID, this.node, planarSubgraphCreationStrategy, null)));// null indicates broadcast
			this.executeTimerQueue();
		}

	}

	/**
	 * this is a broadcast to tell the second node in a bridgePair which might be no clusterhead that he is a bridgeNode for instance: C1---N1--|--N2---C2
	 */
	private void bridgeNodeBroadcast() {
		if (isBridgeNode && !isClusterHead) {// defensive programming
			this.enqueue(new MessageTimer(new DeliverBridgeNodesMessage(this.tcID, this.node, planarSubgraphCreationStrategy, bridgeRouting)));
			this.executeTimerQueue();
		} else if (isClusterHead) {
			this.enqueue(new MessageTimer(new DeliverBridgeNodesMessage(this.tcID, this.node, planarSubgraphCreationStrategy, completePaths)));
			this.executeTimerQueue();
		}

	}

	/**
	 * sends the nodes id to all 1-hop neighbors ID needs to be wrapped in a helper class
	 */
	public void broadcastID() {
		ID id = new ID(this.node.ID);
		this.enqueue(new MessageTimer(new DataMessage<ID>(tcID, this.node, planarSubgraphCreationStrategy, id)));
		this.executeTimerQueue();


	}
	
	public HashSet<Node> getKnownNeighborsTotal(){
		return knownNeighborsTotal;
	}

}
