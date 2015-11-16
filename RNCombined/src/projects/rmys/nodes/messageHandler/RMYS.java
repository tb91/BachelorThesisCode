package projects.rmys.nodes.messageHandler;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import projects.reactiveSpanner.TopologyControlObserver;
import projects.reactiveSpanner.nodes.messageHandlers.AbstractMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.reactivePDT.ReactivePDT;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import projects.rmys.nodes.messages.RequestMessage;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sun.awt.image.IntegerInterleavedRaster;

/**
 * @author timmy
 *
 * 
 */
public class RMYS extends BeaconlessTopologyControl {
	/*
	 * also korrektheitsbeweis MY(PDT(v))<=>RMYS(v)... erledigt
	 * 
	 * Beispiel aufschreiben rpdt im grundlagen rmys im kasten (abstrakt)
	 * erledigt rmys als beispiel
	 * 
	 * korrektheitsbeweis X erledigt
	 * 
	 * eigenschaften: planar zusammenhang des graphen kosntant node degree
	 * spanner vermutung aufschreiben mit gründen anzahl der nachrichten
	 * (nachrichtenkomplexität) (Nachrichtengröße) erledigt local rmys pdt
	 * mit 1hop beaconing(mit mys)
	 * 
	 * worst case szenario mit punkten immer weiter entfernt
	 * 
	 *----------------------------
	 *verbesserung dass letzte broadcast nicht gebraucht wird:
	 * Wenn kante einmal gefunden -> automatisch hinzuf�gen -> planarity bleibt erhalten
	 * weil pdt planaren graphen erzeugt und RMYS nur kanten wegnimmt (selbst wenn es alle hinzuf�gt)
	 * Spanning ratio kann nur besser werden
	 * einzige was man untersuchen muss ist contant node degree...
	 *	 */
	public static int k = -1;
	public static double cone_size = -1;
	public static double unit_radius = -1;

	static {
		try {
			k = Configuration.getIntegerParameter("RMYS/k_value");
			unit_radius = Configuration.getIntegerParameter("UDG/rMax");
			cone_size = 2 * Math.PI / k;
		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}

	ReactivePDT pdt;
	RMYSForwarderMessageHandler forwarderMh;

	public RMYS(NewPhysicalGraphNode sourceNode) {
		super(EStrategy.RMYS, sourceNode);

		_init();
	}

	@Override
	protected void _init() {
		if (sourceNode instanceof NewPhysicalGraphNode) {
			pdt = new ReactivePDT(sourceNode);
			pdt.addObserver(new TopologyControlObserver() {
				// 77654767234
				@Override
				public void onNotify(SubgraphStrategy topologyControl, EState event) {
					if (pdt.hasTerminated()) { // as soon as RMYS gets notified
												// thats rPDT has terminated it
												// starts Modified Yao Step

						forwarderMh.getKnownNeighbors().addAll(calculateMYS(
								(NewPhysicalGraphNode) sourceNode, 
								sourceNode.getMessageHandler(pdt.getTopologyControlID()).getKnownNeighbors()));
						checkIfEdgeIsBidirectional(forwarderMh);
					}

				}
			});

			// adds a RMYSMessageHandler for compatibility reasons to the
			// ReactiveSpanner Framework
			forwarderMh = new RMYSForwarderMessageHandler(getTopologyControlID(), sourceNode, pdt);
			forwarderMh.initializeKnownNeighborsSet();
			sourceNode.messageHandlerMap.put(super.getTopologyControlID(), forwarderMh);

			pdt.start();
		} else {
			throw new RuntimeException("RMYS can use NewPhysicalGraphNodes only.");
		}

	}

	public static Set<NewPhysicalGraphNode> calculateMYS(NewPhysicalGraphNode sourceNode, Set<? extends SimpleNode> pdtNeighbors) {
		// for each PDT neighbour calculate its cone id

		// NodeId -> coneId
		// HashMap<Integer, Integer> nodeIds = new HashMap<>();
		// for (SimpleNode n :
		// sourceNode.messageHandlerMap.get(pdt.getTopologyControlID()).getKnownNeighbors())
		// {
		// nodeIds.put(n.ID, calculateCone(n.getPosition()));
		// }
		System.out.println("Constructing neighborhood of node: " + sourceNode.toString());
		// coneId -> NewPhysicalGraphNode
		HashMap<Integer, ArrayList<NewPhysicalGraphNode>> cones = new HashMap<>();
		for (int l = 0; l < RMYS.k; l++) {// for easier calculations initialize
											// hashmap with all possible coneIds
			cones.put(l, new ArrayList<NewPhysicalGraphNode>());
		}
		// coneId 0 is the one starting at 3 o'clock running counterclockwise
		// counterclockwise indicates ids with increasing number

		// find coneId for each Node and sort it into HashMap cones
		for (SimpleNode n : pdtNeighbors) {
			if (n instanceof NewPhysicalGraphNode) {
				int key = calculateCone((NewPhysicalGraphNode) n, sourceNode);

				ArrayList<NewPhysicalGraphNode> list = cones.get(key);
				if (list != null) {
					list.add((NewPhysicalGraphNode) n); // save Typecast
				} else {
					list = new ArrayList<>();
					list.add((NewPhysicalGraphNode) n);
				}
				cones.put(key, list);

			} else {
				throw new RuntimeException("RMYS can use NewPhysicalGraphNodes only.");
			}
		}
		
		// find shortest edges for each cone
		HashMap<Integer, NewPhysicalGraphNode> shortest = new HashMap<>();
		for (int l = 0; l < RMYS.k; l++) {// for easier calculations initialize
											// hashmap with all possible coneIds
			shortest.put(l, null);
		}
		for (Integer t : cones.keySet()) {
			NewPhysicalGraphNode shortestNode = null; // variable to hold the
														// node
														// which is closest to
														// sourceNode in a cone
			double shortestvalue = Double.MAX_VALUE;
			for (NewPhysicalGraphNode p : cones.get(t)) {
				double distance = sourceNode.getPosition().distanceTo(p.getPosition());
				if (shortestvalue > distance) {
					shortestNode = p;
					shortestvalue = distance;
				}
			}
			if (shortestNode != null) {
				shortest.put(t, shortestNode);
				System.out.println("cone:"+ t +" shortest distance to node: " + shortestNode);
			}
		}

		HashSet<NewPhysicalGraphNode> calculatedNeighbors = new HashSet<>();
		// Since these shortest edges are selected anyway, they are added to the
		// topologyControl neighbors
		for (NewPhysicalGraphNode p : shortest.values()) {
			if (p != null) {
				calculatedNeighbors.add(p);
			}

		}

		// find maximal sequences of empty cones
		ArrayList<int[]> empty_cones_set = findMaximalSequences(cones);

		System.out.println("Empty cones: ");
		for(int[] values:empty_cones_set){
			System.out.print("[" + values[0] + ", " + values[1] + "], ");
		}
		System.out.println();
		
		/*
		// for each empty sequence do
		for (int[] interval : empty_cones_set) {
			if(pdtNeighbors.size()==calculatedNeighbors.size()){
				System.out.println("breaking loop since calculatedNeighbors contains all nodes out of pdtNeighbors");
				break;
			}
			// find orientation point
			double angleToZero = RMYS.cone_size * interval[0];
			double xpos = Math.cos(angleToZero); // rotate orientation point
													// (1,0) to empty cone
			double ypos = Math.sin(angleToZero);
			Position helppos = new Position(sourceNode.getPosition().xCoord + xpos,
					sourceNode.getPosition().yCoord + ypos, 0);

			if (interval[0] == interval[1]) { // just one empty cone; the
												// predecessor and the successor
												// cone are not empty

				// find nearest nodes clockwise and counterclockwise
				// the nearest nodes must reside in the cone before interval[0]
				// and after interval[1]
				NewPhysicalGraphNode clockwise = null;
				double smallestAngle = 0;
				Integer before = interval[0] - 1;
				if (before == -1) {// ensure loop
					before = RMYS.k - 1;
				}
				for (NewPhysicalGraphNode p : cones.get(before)) {
					double currentangle = calculateAngleForCone(p, helppos, sourceNode);
					if (currentangle > smallestAngle) {
						smallestAngle = currentangle;
						clockwise = p;
					}
				}
				if(clockwise==null){
					System.out.println("NULL");
				}
				System.out.println("for empty cone " + interval[0] + " " + clockwise.toString() + " is the next node clockwise" );
				// same for the nearest node counterclockwise
				NewPhysicalGraphNode counterclockwise = null;
				double greatestAngle = Double.MAX_VALUE;
				Integer after = interval[1] + 1;
				if (after == RMYS.k) { // ensure loop
					after = 0;
				}
				for (NewPhysicalGraphNode p : cones.get(after)) {
					double currentangle = calculateAngleForCone(p, helppos, sourceNode);
					if (currentangle < greatestAngle) {
						greatestAngle = currentangle;
						counterclockwise = p;
					}
				}
				System.out.println("for empty cone " + interval[0] + " " + counterclockwise.toString() + " is the next node counterclockwise" );
				if (counterclockwise != null && clockwise != null) {
					if (calculatedNeighbors.contains(counterclockwise)) {
						calculatedNeighbors.add(clockwise);
						System.out.println("adding clockwise: " + clockwise.toString());
					} else if (calculatedNeighbors.contains(clockwise)) {
						calculatedNeighbors.add(counterclockwise);
						System.out.println("adding counterclockwise: " + counterclockwise.toString());
					} else {
						double disclock = counterclockwise.getPosition().distanceTo(sourceNode.getPosition());
						double discounter = clockwise.getPosition().distanceTo(sourceNode.getPosition());
						if (disclock < discounter) {
							calculatedNeighbors.add(counterclockwise);
							System.out.println("adding counterclockwise " + counterclockwise.toString() +" with distance: " + disclock);
						} else if (discounter < disclock) {
							calculatedNeighbors.add(clockwise);
							System.out.println("adding clockwise " + clockwise.toString() +" with distance: " + discounter);
						} else {
							throw new RuntimeException("unspecified behavior: distance from " + sourceNode.toString()
									+ " to " + counterclockwise.toString() + " and " + clockwise.toString()
									+ " is equal.");
							// is not specified in Modified Yao Step

						}
					}
				}

			} else {

				// for the sake of simplicity all neighbors are sorted with
				// respect to the angle between
				// the empty cone sequence and themselves in sourceNode
				ArrayList<NewPhysicalGraphNode> sortedNeighbors = new ArrayList<>();
				final HashMap<NewPhysicalGraphNode, Double> anglemap = new HashMap<>();
				for (ArrayList<NewPhysicalGraphNode> list : cones.values()) {
					for (NewPhysicalGraphNode p : list) {
						anglemap.put(p, calculateAngleForCone(p, helppos, sourceNode));
						sortedNeighbors.add(p);
					}
				}
				

				Collections.sort(sortedNeighbors, new Comparator<NewPhysicalGraphNode>() {

					@Override
					public int compare(NewPhysicalGraphNode o1, NewPhysicalGraphNode o2) {
						if (anglemap.get(o1) < anglemap.get(o2)) {
							return -1;
						} else if (anglemap.get(o1) == anglemap.get(o2)) {
							return 0;
						} else {
							return 1;
						}
					}
				});

				// calculate sequence length
				int sequence_l = Math.abs(interval[1] - interval[0] + 1); // eg. 8-7=1,
																// but means 8
																// and 7 are
																// empty-> so
																// plus 1
				assert (sequence_l > 1): "sequence_length must be greater than 1 here!";

				int clockwiseNeighbors = (int) (sequence_l / 2.0);
				int counterclockwiseNeighbors = (int) ((sequence_l + 1) / 2.0);
				assert clockwiseNeighbors+ counterclockwiseNeighbors == sequence_l: "one neighbor too less!";
				// System.out.println();
				// System.out.println("clockwise count: " + clockwiseNeighbors);
				// System.out.println("counterclockwise count: " +
				// counterclockwiseNeighbors);
				// System.out.println("List of Node: " + sourceNode.toString());
				// for (PhysicalGraphNode p : sortedNeighbors) {
				// System.out.println("Node " + p.toString() + " with angle: " +
				// anglemap.get(p));
				// }

				// choose the first counterclockwiseNeighbors which are not
				// already selected
				int index = 0;
				System.out.println("sequence in the interval [" + interval[0] + ", " + interval[1]  + "] chooses: ");
				while (counterclockwiseNeighbors > 0 && index < sortedNeighbors.size()) {

					NewPhysicalGraphNode p = sortedNeighbors.get(index);
					if (calculatedNeighbors.contains(p)) {
						index++;
						continue;// take next node
					}
					calculatedNeighbors.add(p);
					System.out.print(p.toString() + " ");
					index++;
					counterclockwiseNeighbors -= 1;
				}
				
				System.out.println("counterclockwise");
				System.out.print("and: ");
				// choose the first clockwiseNeighbors which are not already
				// selected
				index = sortedNeighbors.size() - 1;
				while (clockwiseNeighbors > 0 && index >= 0) {
					NewPhysicalGraphNode p = sortedNeighbors.get(index);
					if (calculatedNeighbors.contains(p)) {
						index--;
						continue;// take next node
					}
					calculatedNeighbors.add(p);
					System.out.print(p.toString() + " ");
					index--;
					clockwiseNeighbors -= 1;
				}
				System.out.println("clockwise");
			}
		}*/

		return calculatedNeighbors;

	}

	private static ArrayList<int[]> findMaximalSequences(HashMap<Integer, ArrayList<NewPhysicalGraphNode>> cones) {
		ArrayList<int[]> empty_cones_set=new ArrayList<>();
		int endcone=RMYS.k;
		for (int i = 0; i < endcone; i++) {
			if (cones.get(i).size() == 0) {// start of a empty sequence found
				int truestart=i;
				if(i==0){ //check if there is another empty cone before 0 (e.g. 13, 12, 11,...)
					truestart=RMYS.k;
					while(truestart>0){
						truestart--;
						if(cones.get(truestart).size()!=0){
							break;
						}
					}
					if (truestart==0){
						//there is no node around! Cannot happen in connected graphs...
						break;
					}else{
						endcone=truestart;
					}
				}
				int j;
				for (j = i + 1; j < RMYS.k; j++) {
					if (cones.get(j).size() != 0) { // determines end of an
													// empty sequence
						j -= 1;
						break;
					}
				}
				if (j == RMYS.k) {// if last cone is empty, too
					j -= 1;
				}
				int[] empty_interval = { truestart, j }; // [0] indicates start, [1]
													// indicates end of empty
													// sequence of cones
				empty_cones_set.add(empty_interval);
				i = j; // prohibit duplicates
			}
		}
		return empty_cones_set;
	}

	public static void checkIfEdgeIsBidirectional(RMYSForwarderMessageHandler rmys) {

		RequestMessage request = new RequestMessage(rmys.tcID, rmys.node);
		for (PhysicalGraphNode pn : rmys.getKnownNeighbors()) {
			// create Messagehandler so the node knows what to do
			// this is needed, because of the use of the message handlers for
			// each node

			RMYSMessageHandler mh = new RMYSMessageHandler(rmys.tcID, pn, rmys.node);
			pn.messageHandlerMap.put(rmys.tcID, mh);

			request.candidates.add((NewPhysicalGraphNode) pn);// save typecast

		}
		System.out.println("Neighbors for node: " + rmys.node.toString());
		System.out.print("(");
		System.out.println(rmys.getKnownNeighbors());
		System.out.println(")");

		rmys.node.broadcast(request);
	}

	/**
	 * @param pos
	 * @return id of the cone in which node lies with respect to sourceNode
	 */
	private static int calculateCone(NewPhysicalGraphNode node, NewPhysicalGraphNode sourceNode) {

		double angle = calculateAngle(node, sourceNode);

		return (int) (angle / RMYS.cone_size);
	}

	/**
	 * @param pos
	 * @return the angle between the horizontal axis source node and node
	 *         (starting at 3 o'clock counterclockwise)
	 */
	private static double calculateAngle(NewPhysicalGraphNode node, NewPhysicalGraphNode sourceNode) {
		return calculateAngleForCone(node.getPosition(), sourceNode.getPosition());
	}

	/**
	 * @param pos
	 * @param sourceNodePos
	 * @return the angle between pos and x-axis in sourceNodePos between 0 and
	 *         2Pi
	 */
	public static double calculateAngleForCone(Position pos, Position sourceNodePos) {

		double angle = Math.atan2(pos.yCoord - sourceNodePos.yCoord, sourceNodePos.xCoord - pos.xCoord) + Math.PI;

		return angle;
	}

	private static double calculateAngleForCone(NewPhysicalGraphNode node, Position reference,
			NewPhysicalGraphNode sourceNode) {

		Position nodePos = node.getPosition();
		Position sourceNodePos = sourceNode.getPosition();

		double refangle = calculateAngleForCone(reference, sourceNodePos);
		double posangle = calculateAngleForCone(nodePos, sourceNodePos);
		double oriangle = posangle - refangle;

		return oriangle;

	}

	@Override
	protected void _start() {

	}

	@Override
	public void draw(Graphics g, PositionTransformation pt) {
		super.draw(g, pt);
		AbstractMessageHandler<?> amh = sourceNode.getMessageHandler(this.getTopologyControlID());
		if (amh != null) {
			amh.drawNode(g, pt);
		}
	}

}
