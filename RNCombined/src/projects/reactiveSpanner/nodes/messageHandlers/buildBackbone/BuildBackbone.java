package projects.reactiveSpanner.nodes.messageHandlers.buildBackbone;

import static projects.reactiveSpanner.nodes.messageHandlers.buildBackbone.BuildBackboneMessageHandler.calculateGridCellID;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import projects.reactiveSpanner.Utilities;
import projects.reactiveSpanner.WangLiSpanner;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconTopologyControl;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.timers.BeaconTopologyTimer;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.tools.logging.LogL;

/**
 * @author Tim This is the BuildBackbone topology control for the BuildBackbone algorithm from K. Lillis et al. in 2008 You can run the simulation, generate some nodes (ensure that this graph is connected, hit "Graph Connectivity") and hit "BuildBackbone" With every Round which passes on you can see the progress the algorithm makes. First, you observe several local broadcasts as described in the corresponding paper. The meaning of the colored Nodes is the following: red : clusterhead (is contained in the backbone graph) pink : bridgenode (also contained in the backbone graph) blue : clustermember (not contained in the backbone graph)
 * 
 *         (There is one additional broadcast due to an error in this paper ("At this point every node then knows whether it is a cluster head or a bridge node." This is wrong!) In the following rounds several edges appear and 27 rounds after you have clicked the "BuildBackbone" button the first part of the protocol is finished. Green edges : edges which cross grid borders Black edges : edges from a clusterhead to one of its bridge-starting-or-ending-nodes Gray dotted edges: edges from a clusterhead to its clustermembers which do not have a specific role.
 * 
 *         Now, buildbackbone invokes the creation of the virtualNodes by creating a CreateVirtuals topology control.
 * 
 */
public class BuildBackbone extends BeaconTopologyControl {

	/**
	 * stores the blue Grids WangliNeighbors
	 */
	HashMap<Node, Set<Node>> adjacencyListBlue;

	/**
	 * stores the red Grids WangliNeighbors
	 */
	HashMap<Node, Set<Node>> adjacencyListRed;

	/**
	 * stores the green Grids WangliNeighbors
	 */
	HashMap<Node, Set<Node>> adjacencyListGreen;

	public static enum TopologyPhase {
		BUILDBACKBONE, CREATEVIRTUALS, CREATE_WANGLI;
	}

	private TopologyPhase currentTopologyPhase;

	/**
	 * the link to the CreateVirtuals topologyControl
	 */
	private CreateVirtuals cv;

	public BuildBackbone(PhysicalGraphNode sourceNode) {
		super(EStrategy.BUILD_BACKBONE, sourceNode);
		// System.out.println("NORMAL");
		// System.out.println(getTopologyControlID());
		adjacencyListBlue = new HashMap<Node, Set<Node>>();
		adjacencyListRed = new HashMap<Node, Set<Node>>();
		adjacencyListGreen = new HashMap<Node, Set<Node>>();
		new BeaconTopologyTimer(this, 34);
		new BeaconTopologyTimer(this, 44);
		_init();
		// start();
	}

	@Override
	public void start() {
		// logger.logln(LogL.INFO, "adding BuildBackbone messageHandler to one Node. Forwarder is: " + sourceNode); // all Nodes know when they should start

		// create MessageHandlers for each node
		for (PhysicalGraphNode p : Utilities.getNodeCollectionByClass(PhysicalGraphNode.class)) {
			// give every node a new messageHandler with same TopologyControlID

			BuildBackboneMessageHandler bbmh = new BuildBackboneMessageHandler(super.getTopologyControlID(), p, p, super.getStrategyType(), this);
			// if (!p.equals(this.sourceNode)){

			// sourceNode.subgraphStrategyFactory.request(EStrategy.BUILD_BACKBONE,this.getTopologyControlID());

			// }
			if (!p.equals(sourceNode)) {
				p.subgraphStrategyFactory.handOverSubgraphStrategy(this);
			}

			p.messageHandlerMap.put(super.getTopologyControlID(), bbmh);

			bbmh.broadcastID();

		}
	}

	// BuildBackboneMessageHandler bbmh = new BuildBackboneMessageHandler(super.getTopologyControlID(), sourceNode, sourceNode, super.getStrategyType(), this);
	// System.out.println(super.getTopologyControlID());
	// sourceNode.putNewForwarderMessageHandler(super.getTopologyControlID(), bbmh);
	// bbmh.broadcastID();

	// }

	@Override
	public void topologyTimerFire() {

		finished();
	}

	@Override
	public void _init() {
		currentTopologyPhase = TopologyPhase.BUILDBACKBONE;
	}

	/**
	 * 
	 */
	private void finished() {
		if (currentTopologyPhase == TopologyPhase.BUILDBACKBONE) {
			// logger.logln(LogL.INFO, "attempting to create Virtual Nodes");
			currentTopologyPhase = TopologyPhase.CREATEVIRTUALS;

			logger.logln(LogL.INFO, "Starting CreateVirtuals algorithm.");
			sourceNode.subgraphStrategyFactory.request(EStrategy.CREATE_VIRTUALS).start();

		} else if (currentTopologyPhase == TopologyPhase.CREATEVIRTUALS) {
			currentTopologyPhase = TopologyPhase.CREATE_WANGLI;

			// create WangLi spanners for each gridCell centrally

			adjacencyListBlue = createWangLi(Color.blue);
			adjacencyListRed = createWangLi(Color.red);
			adjacencyListGreen = createWangLi(Color.green); // each node can lookup it's entry in the adjacencyLists now
			for (PhysicalGraphNode p : Utilities.getNodeCollectionByClass(PhysicalGraphNode.class)) {
				BuildBackboneMessageHandler bbmh = (BuildBackboneMessageHandler) p.getMessageHandler(getTopologyControlID());
				bbmh.loadWangLiNeighbors();
			}
			System.out.println("Blue");
			for (Node n :

			adjacencyListBlue.keySet()) {
				System.out.println(n + " = " + adjacencyListBlue.get(n));
			}
			System.out.println();
			System.out.println("Red");
			for (Node n : adjacencyListRed.keySet()) {
				System.out.println(n + " = " + adjacencyListRed.get(n));
			}
			System.out.println();
			System.out.println("Green");
			for (Node n : adjacencyListGreen.keySet()) {
				System.out.println(n + " = " + adjacencyListGreen.get(n));
			}

		}

	}

	// ============================WangLi Creation===============================

	/**
	 * We simulate following behavior, to save computer resources: each Node creates the WangLi-Neighbors for it's grids
	 */
	public HashMap<Node, Set<Node>> createWangLi(Color c) {

		HashMap<Node, Set<Node>> wangLiNeighbors = new HashMap<Node, Set<Node>>();

		for (Set<Node> nodes : getNodes(c).values()) {
			HashMap<Node, Set<Node>> oneCell = WangLiSpanner.buildWangLiSpanner(nodes);
			for (Node n : oneCell.keySet()) {
				if (!wangLiNeighbors.containsKey(n)) {
					wangLiNeighbors.put(n, oneCell.get(n));
				}
				// else {
				// System.out.println("SAVED");
				// }

			}
		}
		return wangLiNeighbors;
	}

	// }

	/**
	 * @param c
	 *            the color of the grid
	 * @returns the nodes which are in this grid with the specified color
	 */
	private HashMap<Integer, Set<Node>> getNodes(Color c) {

		double offset = 0;
		if (c == Color.blue) {// determine offset for origin
			// no offset needed
		} else if (c == Color.red) {
			offset = BeaconTopologyControl.qr / (3 * Math.sqrt(2));
		} else if (c == Color.green) {
			offset = 2 * BeaconTopologyControl.qr / (3 * Math.sqrt(2));
		}

		HashMap<Integer, Set<Node>> nodesPerCell = new HashMap<Integer, Set<Node>>();

		for (Node n : Utilities.getNodeCollectionByClass(Node.class)) { // iterate over all nodes saving each node
																		// in a list with the nodes in the same gridcell
			int id = calculateGridCellID(n.getPosition(), offset);
			Set<Node> nodes = null;
			if (nodesPerCell.containsKey(id)) {
				nodes = nodesPerCell.get(id);
			} else {
				nodes = new HashSet<Node>();
			}
			nodes.add(n);
			nodesPerCell.put(id, nodes);

		}

		// System.out.println(p + " has following neighbors "+ c + " :");
		// System.out.println(nodesInCell);
		return nodesPerCell;
	}

	/**
	 * @return
	 */
	public UUID getVirtualsID() {
		return cv.getTopologyControlID();
	}

	@Override
	public void draw(Graphics g, PositionTransformation pt) {
		for (PhysicalGraphNode p : Utilities.getNodeCollectionByClass(PhysicalGraphNode.class)) {
			
			p.getMessageHandler(this.getTopologyControlID()).drawNode(g, pt);

		}// draw method of each BuildBackbone messagehandler

	}

	@Override
	protected void _start() {

	}

}
