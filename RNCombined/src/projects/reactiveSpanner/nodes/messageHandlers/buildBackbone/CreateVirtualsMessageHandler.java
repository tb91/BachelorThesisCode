package projects.reactiveSpanner.nodes.messageHandlers.buildBackbone;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sun.accessibility.internal.resources.accessibility;
import com.sun.java_cup.internal.runtime.virtual_parse_stack;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.TestGenerator;

import projects.defaultProject.nodes.timers.MessageTimer;
import projects.reactiveSpanner.Dijkstra;
import projects.reactiveSpanner.RoutingEntry;
import projects.reactiveSpanner.nodes.edges.DistEdge;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconMessageHandler;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.messages.BeaconRequestMessage;
import projects.reactiveSpanner.nodes.messages.BeaconReplyMessage;
import projects.reactiveSpanner.nodes.messages.NewBeaconReplyMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import projects.reactiveSpanner.nodes.timers.BeaconTimer;
import projects.reactiveSpanner.record.MessageRecord;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.nodes.edges.Edge;
import sinalgo.tools.logging.LogL;

/**
 * @author Tim
 * 
 */
public class CreateVirtualsMessageHandler extends BeaconMessageHandler<PhysicalGraphNode> implements BuildbackboneTopology {

	public static enum Phase {
		BROADCAST1, BROADCAST2, CREATEVIRTUALS, ADDROUTING, FINISHED;
	}

	Phase currentphase;

	/**
	 * stores all nodes from the 3 hop beaconing
	 */
	private HashMap<Integer, PhysicalGraphNode> collectedNodes = new HashMap<Integer, PhysicalGraphNode>();

	/**
	 * the virtualNodes this node is the controller of
	 */
	private HashMap<Integer, VirtualNode> myVirtuals = new HashMap<Integer, VirtualNode>();

	/**
	 * the virtualNodes this node knows of but is not the controller
	 */
	private HashMap<Integer, VirtualNode> ghostvirtuals = new HashMap<Integer, VirtualNode>();

	/**
	 * saves the routingTable of this protocol
	 */
	private HashMap<BuildbackboneTopology, ArrayList<RoutingEntry>> routingTable = new HashMap<BuildbackboneTopology, ArrayList<RoutingEntry>>();

	protected CreateVirtualsMessageHandler(UUID tcID, PhysicalGraphNode ownerNode, PhysicalGraphNode sourceNode, EStrategy strategy, Set<PhysicalGraphNode> consideredNeighbors) {
		super(tcID, ownerNode, sourceNode, strategy);
		for (PhysicalGraphNode p : consideredNeighbors) {
			knownNeighbors.add(p); // virtual Nodes will be created on intersection points created by these edges (this->p)
		}
	}

	public boolean drawConnections = false;

	public void start() {
		currentphase = Phase.BROADCAST1;
		new BeaconTimer(this, 1);
		new BeaconTimer(this, 5);
		new BeaconTimer(this, 9);
		new BeaconTimer(this, 10);

	}

	@Override
	public void receivedBeaconRequestMessage(BeaconRequestMessage brm2) {

	}

	public void receivedNewBeaconReplyMessage(NewBeaconReplyMessage brm2) {
		for (Integer i : brm2.nodes.keySet()) {
			PhysicalGraphNode p = brm2.nodes.get(i);

			if (p != this.node) {

				collectedNodes.put(i, p);
			}

		}
	}

	@Override
	public void beaconTimerFire() {
		nextPhase();
	}

	private void nextPhase() {
		if (collectedNodes.isEmpty()) {
			for (Edge e : this.node.outgoingConnections) { // outgoing connections must be used, because this node wants to know all of its 3-neighborhood
				collectedNodes.put(e.endNode.ID, ((PhysicalGraphNode) e.endNode));

			}
		}
		switch (currentphase) {
		case BROADCAST1:
			broadcastCollectedNodes();
			currentphase = Phase.BROADCAST2;
			break;
		case BROADCAST2:
			broadcastCollectedNodes();
			currentphase = Phase.CREATEVIRTUALS;
			break;
		case CREATEVIRTUALS:
			calculateVirtuals();
			currentphase = Phase.ADDROUTING;
			break;
		case ADDROUTING:
			addRouting();
			currentphase = Phase.FINISHED;
			break;
		case FINISHED:
			break;
		default:
			throw new RuntimeException("You probably forgot to change the phases from CreateVirtuals");// cannot happen

		}

	}

	private void addRouting() {
		for (VirtualNode virtual : myVirtuals.values()) {
			addRoutingFromVirtualNode(virtual);

		}
		addRoutingToNeighbors();
	}

	private void addRoutingToNeighbors() {

		//===========add routing to virtual neighbors==========
		HashMap<Node, ArrayList<VirtualNode>> virtualsPerEdge = new HashMap<Node, ArrayList<VirtualNode>>();

		ArrayList<VirtualNode> allVirtuals = new ArrayList<VirtualNode>();
		for (VirtualNode vnode : ghostvirtuals.values()) {
			allVirtuals.add(vnode);
		}
		for (VirtualNode vnode : myVirtuals.values()) {
			allVirtuals.add(vnode);
		}

		for (VirtualNode vnode : allVirtuals) {// walk over all virtual nodes

			// find endNode of the edge which this virtual contains

			PhysicalGraphNode endNode = null;
			if (vnode.getStartnode1().equals(this.node)) {// one of the nodes must be this node or we do not care about this virtualnode
				endNode = vnode.getEndnode1();
			} else if (vnode.getEndnode1().equals(this.node)) {
				endNode = vnode.getStartnode1();
			} else if (vnode.getStartnode2().equals(this.node)) {
				endNode = vnode.getEndnode2();
			} else if (vnode.getEndnode2().equals(this.node)) {
				endNode = vnode.getStartnode2();
			} else {
				// we do not care
				continue;
			}

			ArrayList<VirtualNode> list = null;
			if (virtualsPerEdge.containsKey(endNode)) {
				list = virtualsPerEdge.get(endNode);
			} else {
				list = new ArrayList<VirtualNode>();
			}
			list.add(vnode);

			virtualsPerEdge.put(endNode, list);

		}

		// we sorted the virtual nodes depending on which edge they are.
		// so now we iterate over this set, find the nearest virtual node, if any
		// and add it to the routing table
		for (Node n : virtualsPerEdge.keySet()) {
			ArrayList<VirtualNode> vnodes = virtualsPerEdge.get(n);
			VirtualNode nearest = findNearestVirtual(vnodes, this.node);
			if (nearest == null) {// no virtual node is on this edge -> add endnode
				ArrayList<RoutingEntry> route = new ArrayList<RoutingEntry>();
				route.add(new RoutingEntry(n.getPosition(), n.ID));
				PhysicalGraphNode p = null;
				if (n instanceof PhysicalGraphNode) {
					p = (PhysicalGraphNode) n;
				} else {
					throw new RuntimeException("You should use PhysicalGraphNodes only!");
				}

				routingTable.put((CreateVirtualsMessageHandler) (p.messageHandlerMap.get(this.tcID)), route);
			} else {
				routingTable.put(nearest, Dijkstra.dijkstra(collectedNodes, this.node, nearest.getController()));
			}

		}
		
		
		//===========add routing to real neighbors==========
		for(PhysicalGraphNode p:knownNeighbors){
			if(!virtualsPerEdge.containsKey(p)){ //if there are edges without any virtual nodes, add them
				routingTable.put((CreateVirtualsMessageHandler) (p.messageHandlerMap.get(this.tcID)), Dijkstra.dijkstra(collectedNodes, this.node, p));
			}
		}
		
	}

	/**
	 * @param vNodes
	 *            the list of virtual nodes on one edge
	 * @param node
	 * @return the nearest virtual node to node
	 */
	private VirtualNode findNearestVirtual(ArrayList<VirtualNode> vNodes, Node node) {
		if (vNodes.size() == 0) {
			return null;
		}
		VirtualNode nearest = vNodes.get(0);
		for (VirtualNode vnode : vNodes) {

			if (node.getPosition().squareDistanceTo(vnode.getPosition()) < node.getPosition().squareDistanceTo(nearest.getPosition())) {
				nearest = vnode;
			}
		}
		return nearest;
	}

	/**
	 * @param virtual
	 *            gets routingEntries from it to all surrounding neighbors, the virtual nodes on the non controller edge, which might be invisible to the controller are not being considered
	 */
	private void addRoutingFromVirtualNode(VirtualNode virtual) {

		ArrayList<VirtualNode> nodesOnSameEdge = new ArrayList<VirtualNode>();

		for (VirtualNode vnode : myVirtuals.values()) {
			if (sameFirstEdge(virtual, vnode) && !virtual.equals(vnode)) {// find
				nodesOnSameEdge.add(vnode);
			}
		}

		for (VirtualNode vnode : ghostvirtuals.values()) {
			if ((sameFirstEdge(virtual, vnode) ||sameSecondEdge(virtual,vnode)) && !virtual.equals(vnode)) {
				nodesOnSameEdge.add(vnode);
			}
		}

	
		
		if (!nodesOnSameEdge.isEmpty()) {
			// ------edge which contains the controller---------
			VirtualNode minDist = getNearestNode(virtual, nodesOnSameEdge);
			VirtualNode secMinDist = getSecondNearestNode(virtual, nodesOnSameEdge);

			if (secMinDist.equals(minDist)) {// if there are 2 virtual nodes
				virtual.addRoutingEntry(minDist, Dijkstra.dijkstra(this.collectedNodes, virtual.getController(), minDist.getController()));

				// O1----V1---V2----O2 if O1 to V2 > 01 to V1 take V1 and O2 else V1 and O1
				if (virtual.getStartnode1().getPosition().squareDistanceTo(virtual.getPosition()) > virtual.getStartnode1().getPosition().squareDistanceTo(minDist.getPosition())) {
					virtual.addRoutingEntry((CreateVirtualsMessageHandler) (virtual.getEndnode1().messageHandlerMap.get(this.tcID)), Dijkstra.dijkstra(this.collectedNodes, virtual.getController(), virtual.getEndnode1()));
				} else {
					virtual.addRoutingEntry((CreateVirtualsMessageHandler) (virtual.getStartnode1().messageHandlerMap.get(this.tcID)), Dijkstra.dijkstra(this.collectedNodes, virtual.getController(), virtual.getStartnode1()));
				}

			} else {// if there are at least 3 virtual nodes on the same edge X--|--X
				if (!this.node.equals(virtual.getController())) {
					throw new RuntimeException("Change this collectedNodes to virtual.getcontroller. ... .getcollectedNodes");
				}
				//minDist must be added to the routing list
				virtual.addRoutingEntry(minDist, Dijkstra.dijkstra(this.collectedNodes, virtual.getController(), minDist.getController()));
				
				//secMinDist must only be added to the routing list, if virtual is between minDist and SecMinDist
				//check whether startnode 1 or endnode 1 are closer to virtual (hop distance)
				PhysicalGraphNode consideredNode=null;
				if(virtual.getStartnode1().getPosition().squareDistanceTo(virtual.getPosition())<virtual.getStartnode1().getPosition().squareDistanceTo(minDist.getPosition())){
					consideredNode=virtual.getStartnode1();
				}else{
					consideredNode=virtual.getEndnode1();
				}

				//compare the distance from consideredNode to virtual to the distance from consideredNode to mindist
				if(consideredNode.getPosition().squareDistanceTo(virtual.getPosition())<consideredNode.getPosition().squareDistanceTo(minDist.getPosition()) &&
						consideredNode.getPosition().squareDistanceTo(minDist.getPosition())<consideredNode.getPosition().squareDistanceTo(secMinDist.getPosition())){
				
					virtual.addRoutingEntry((CreateVirtualsMessageHandler)(consideredNode.messageHandlerMap.get(this.tcID)), Dijkstra.dijkstra(this.collectedNodes, virtual.getController(), consideredNode));
				}else{
					virtual.addRoutingEntry(secMinDist, Dijkstra.dijkstra(this.collectedNodes, virtual.getController(), secMinDist.getController()));					
				}


			}
		} else {
			// if there are not more than 1 virtual node on this edge
						
			virtual.addRoutingEntry((CreateVirtualsMessageHandler) (virtual.getStartnode1().messageHandlerMap.get(this.tcID)), Dijkstra.dijkstra(this.collectedNodes, virtual.getController(), virtual.getStartnode1()));
			virtual.addRoutingEntry((CreateVirtualsMessageHandler) (virtual.getEndnode1().messageHandlerMap.get(this.tcID)), Dijkstra.dijkstra(this.collectedNodes, virtual.getController(), virtual.getEndnode1()));

		}

		// ignore virtuals on the non controller edge since there might be some virtuals which are not known anyway
		virtual.addRoutingEntry((CreateVirtualsMessageHandler) (virtual.getStartnode2().messageHandlerMap.get(this.tcID)), Dijkstra.dijkstra(this.collectedNodes, virtual.getController(), virtual.getStartnode2()));
		virtual.addRoutingEntry((CreateVirtualsMessageHandler) (virtual.getEndnode2().messageHandlerMap.get(this.tcID)), Dijkstra.dijkstra(this.collectedNodes, virtual.getController(), virtual.getEndnode2()));

		// System.out.println("Virtual mit id:" + virtual.getId() + " and controller:" + virtual.getController());
//		System.out.println(virtual.getId() + ":  " + virtual.getRoutingTable());
	}

	private VirtualNode getSecondNearestNode(VirtualNode virtual, ArrayList<VirtualNode> nodesOnSameEdge) {
		if (nodesOnSameEdge.size() == 0) {// no additional virtual node is on this edge
			return null;
		} else if (nodesOnSameEdge.size() == 1) {// one additional node is on this edge
			return nodesOnSameEdge.get(0);
		}
		VirtualNode minDist = getNearestNode(virtual, nodesOnSameEdge);
		nodesOnSameEdge.remove(minDist);
		return getNearestNode(virtual, nodesOnSameEdge);
	}

	/**
	 * @param virtual
	 *            a virtual node
	 * @param nodesOnSameEdge
	 *            all virtual nodes on the same edge as virtual
	 * @return the virtual node with the smallest distance to virtual
	 * 
	 */
	private VirtualNode getNearestNode(VirtualNode virtual, ArrayList<VirtualNode> nodesOnSameEdge) {

		if (nodesOnSameEdge.size() == 0) {// no additional virtual node is on this edge
			return null;
		} else if (nodesOnSameEdge.size() == 1) {// one additional node is on this edge
			return nodesOnSameEdge.get(0);
		}
		VirtualNode minDist = nodesOnSameEdge.get(0);
		for (VirtualNode vnode : nodesOnSameEdge) {
			// search the nearest virtual nodes on this edge
			if (vnode.getPosition().squareDistanceTo(virtual.getPosition()) < minDist.getPosition().squareDistanceTo(virtual.getPosition())) {

				minDist = vnode;
			}
		}
		return minDist;

	}
	
	private boolean sameSecondEdge(VirtualNode v1, VirtualNode v2) {
		if (v1.getStartnode1().equals(v2.getStartnode2()) && v1.getEndnode1().equals(v2.getEndnode2())) {
			return true;
		}
		return false;
	}

	/**
	 * @param v1
	 *            one virtual node
	 * @param v2
	 *            another virtual node
	 * @return true, if the first edge of both virtual nodes is the same edge (the edge which contains the controller of v1 and v2) false, otherwise
	 */
	private boolean sameFirstEdge(VirtualNode v1, VirtualNode v2) {
		if (v1.getStartnode1().equals(v2.getStartnode1()) && v1.getEndnode1().equals(v2.getEndnode1())) {
			return true;
		}
		return false;
	}

	private boolean checkIfEdgeAdded(HashSet<DistEdge> set, DistEdge current) {
		for (DistEdge de : set) {
			if (de.startNode.equals(current.startNode) && de.endNode.equals(current.endNode) || de.startNode.equals(current.endNode) && de.endNode.equals(current.startNode)) {
				return true;

			}
		}
		return false;

	}

	/**
	 * this function finds all virtual nodes this node can possibly see and saves them in myvirtuals if this is the controller or in ghostvirtuals if not.
	 */
	private void calculateVirtuals() {

		HashSet<DistEdge> checkingEdges = new HashSet<DistEdge>();
		for (PhysicalGraphNode p : collectedNodes.values()) {

			for (Node n : p.getMessageHandler(this.tcID).getKnownNeighbors()) { // <<-check: may not work since requestsubgraph does not work properly
				// for (Node n : ((CreateVirtualsMessageHandler) p.getMessageHandler(tcID)).knownNeighbors) {
				if (n instanceof PhysicalGraphNode) {
					if (p.equals(n)) {
						continue; // there cannot be an edge between p and p himself
					}
					DistEdge cvmhedge = new DistEdge();//little 
					cvmhedge.startNode = p;
					cvmhedge.endNode = n;

					if (!checkIfEdgeAdded(checkingEdges, cvmhedge)) { // since we use bidirectional edges
																		// we need to check if the edge was
																		// already added the other way round

						checkingEdges.add(cvmhedge);

					}

				} else {
					logger.logln(LogL.ERROR_DETAIL, "Dist-Edges should be used only.");
				}

			}

		}
		for (PhysicalGraphNode n : knownNeighbors) { // for each outgoing connection (based on this topology control) find and create virtual nodes
			if (n instanceof PhysicalGraphNode) {
				PhysicalGraphNode p = n;
				for (DistEdge other : checkingEdges) {
					if (other.startNode.equals(other.endNode)) {// there cannot be an edge between p and p himself
						continue;
					}
					if (other.endNode.equals(n) || other.endNode.equals(this.node)) {
						// the connection from another node to this node must be filtered too
						continue;
					}
					if (n.equals(other.startNode)) {// filter the two edges which start in the endNode connected with this.
						continue;
					}
					// find edge intersection
					Position po = DistEdge.getIntersectionWith(this.node.getPosition(), n.getPosition(), other.startNode.getPosition(), other.endNode.getPosition());

					if (po != null) {// if there is an intersection

						Node max = getMaxNode(this.node, p, other.startNode, other.endNode);
						if (max.equals(this.node)) {
							try {
								VirtualNode vnode = new VirtualNode(po, this.node, this.node, p, (PhysicalGraphNode) other.startNode, (PhysicalGraphNode) other.endNode);
								myVirtuals.put(vnode.getId(), vnode); // each node has a different ID although it is the same node, but another instance.
								// System.out.println(this.node + " creates a virtual node: "+ vnode);
							} catch (ClassCastException ex) {
								ex.printStackTrace();
								logger.logln(LogL.ERROR_DETAIL, "Use PhysicalGraphNodes only. ");
							}
						} else {
							if (max.equals(this.node) || max.equals(p)) {
								VirtualNode vnode = new VirtualNode(po, (PhysicalGraphNode) max, this.node, p, (PhysicalGraphNode) other.startNode, (PhysicalGraphNode) other.endNode);
								ghostvirtuals.put(vnode.getId(), vnode); // each node has a different ID although it is the same node.
							} else if (max.equals(other.startNode) || max.equals(other.endNode)) {
								VirtualNode vnode = new VirtualNode(po, (PhysicalGraphNode) max, (PhysicalGraphNode) other.startNode, (PhysicalGraphNode) other.endNode, this.node, p);
								ghostvirtuals.put(vnode.getId(), vnode); // each node has a different ID although it is the same node.
							} else {
								throw new RuntimeException("no valid controller could be determined! " + max + " -> " + this.node + ", " + p + ", " + other.startNode + ", " + other.endNode);
							}

						}
					}

				}

			} else {
				logger.logln(LogL.ERROR_DETAIL, "PhysicalGraphNodes should be used only. " + n.toString());
			}
		}

	}

	private static Node getMaxNode(Node n1, Node n2, Node n3, Node n4) {
		Node result = n1;
		if (n2.ID > result.ID) {
			result = n2;
		}
		if (n3.ID > result.ID) {
			result = n3;
		}
		if (n4.ID > result.ID) {
			result = n4;
		}
		return result;
	}

	private void broadcastCollectedNodes() {
		this.enqueue(new MessageTimer(new NewBeaconReplyMessage(tcID, this.node, planarSubgraphCreationStrategy, collectedNodes)));
		this.executeTimerQueue();
	}

	@Override
	public void receivedMessage(AbstractMessage msg) {
		if (msg instanceof BeaconRequestMessage) {
			throw new RuntimeException("Nodes should not receive a BeaconRequestMessage using CreateVirtuals");
		} else if (msg instanceof NewBeaconReplyMessage) {
			receivedNewBeaconReplyMessage((NewBeaconReplyMessage) msg);
		} else {
			throw new RuntimeException("Message type is not supoorted: " + msg.toString());
		}

	}

	/*
	 * @Override public void executeTimerQueue() { MessageTimer msgt; if((msgt=messageTimerQueue.poll())!=null){ } }
	 */

	@Override
	public void drawNode(Graphics g, PositionTransformation pt) {
		// just needed for debugging
		// pt.translateToGUIPosition(this.node.getPosition());
		// int x = pt.guiX;
		// int y = pt.guiY;
		for (Node n : this.knownNeighbors) {
			g.setColor(Color.gray);
			pt.drawLine(g, this.node.getPosition(), n.getPosition());

		}
		for (VirtualNode vn : myVirtuals.values()) {
			// pt.translateToGUIPosition(vn.getPosition());
			pt.drawCircle(g, vn.getPosition(), 1);
			pt.translateToGUIPosition(vn.getPosition().xCoord, vn.getPosition().yCoord, 0);
			g.drawString(vn.getId() + "", pt.guiX, pt.guiY);
		}
		g.setColor(Color.black);
		pt.translateToGUIPosition(this.getNode().getPosition().xCoord + 2, this.getNode().getPosition().yCoord, 0);
		g.drawString(this.getNode().ID + "", pt.guiX, pt.guiY);

	}

	@Override
	public MessageRecord getCurrentMessageRecord() {

		return null;
	}

	@Override
	public HashMap<BuildbackboneTopology, ArrayList<RoutingEntry>> getRoutingTable() {
		return routingTable;
	}

	@Override
	public PhysicalGraphNode getNode() {
		return this.node;
	}

	@Override
	public HashMap<Integer, PhysicalGraphNode> getCollectedNodes() {
		return collectedNodes;
	}

	@Override
	public String toString() {
		return this.node.toString();
	}

}
