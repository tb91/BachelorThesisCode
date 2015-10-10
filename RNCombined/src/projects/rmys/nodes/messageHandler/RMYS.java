package projects.rmys.nodes.messageHandler;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

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
import sinalgo.nodes.Position;

/**
 * @author timmy
 *
 *         
 */
public class RMYS extends BeaconlessTopologyControl {
	/*
	 * also korrektheitsbeweis MY(PDT(v))<=>RMYS(v)... erledigt
	 * 
	 *         Beispiel aufschreiben 
	 *         rpdt im grundlagen 
	 *         rmys im kasten (abstrakt)  erledigt
	 *         rmys als beispiel
	 * 
	 *         korrektheitsbeweis X erledigt
	 * 
	 *         eigenschaften: 
	 *         planar 
	 *         zusammenhang des graphen 
	 *         kosntant node degree 
	 *         spanner vermutung aufschreiben mit gründen 
	 *         anzahl der nachrichten (nachrichtenkomplexität) (Nachrichtengröße) erledigt 
	 *         local rmys pdt mit 1hop beaconing(mit mys)
	 *         
	 *         worst case szenario mit punkten immer weiter entfernt
	 */
	public static int k = -1;
	public static double cone_size = -1;
	public static double unit_radius = -1;

	static {
		try {
			k = Configuration.getIntegerParameter("RMYS/k_value");
			unit_radius = Configuration.getIntegerParameter("UDG/rMax");
		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}

	ReactivePDT pdt;
	RMYSForwarderMessageHandler forwarderMh;

	public RMYS(NewPhysicalGraphNode sourceNode) {
		super(EStrategy.RMYS, sourceNode);

		cone_size = 2 * Math.PI / k;

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

						calculateMYS((NewPhysicalGraphNode) sourceNode, pdt, forwarderMh);
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

	public static void calculateMYS(NewPhysicalGraphNode sourceNode, SubgraphStrategy pdt, RMYSMessageHandler rmys) {
		// for each PDT neighbour calculate its cone id

		// NodeId -> coneId
		// HashMap<Integer, Integer> nodeIds = new HashMap<>();
		// for (SimpleNode n :
		// sourceNode.messageHandlerMap.get(pdt.getTopologyControlID()).getKnownNeighbors())
		// {
		// nodeIds.put(n.ID, calculateCone(n.getPosition()));
		// }

		// coneId -> NewPhysicalGraphNode
		HashMap<Integer, ArrayList<NewPhysicalGraphNode>> cones = new HashMap<>();
		for (int l = 0; l < RMYS.k; l++) {// for easier calculations initialize
											// hashmap with all possible coneIds
			cones.put(l, new ArrayList<NewPhysicalGraphNode>());
		}
		// coneId 0 is the one starting at 3 o'clock running counterclockwise
		// counterclockwise indicates ids with increasing number

		// find coneId for each Node and sort it into HashMap cones
		for (SimpleNode n : sourceNode.messageHandlerMap.get(pdt.getTopologyControlID()).getKnownNeighbors()) {
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
			if (shortest != null) {
			shortest.put(t, shortestNode);
			}
		}

		// Since these shortest edges are selected anyway, they are added to the
		// topologyControl neighbors
		for (NewPhysicalGraphNode p : shortest.values()) {
			if (p != null) {
				rmys.getKnownNeighbors().add(p);
			}

		}

		// find maximal sequences of empty cones
		ArrayList<int[]> empty_cones_set = new ArrayList<>();

		for (int i = 0; i < RMYS.k; i++) {
			if (cones.get(i).size() == 0) {// start of a empty sequence found
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
				int[] empty_interval = { i, j }; // [0] indicates start, [1]
													// indicates end of empty
													// sequence of cones
				empty_cones_set.add(empty_interval);
				i = j; // prohibit duplicates
			}
		}

		// for each empty sequence do
		for (int[] interval : empty_cones_set) {
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
				NewPhysicalGraphNode counterclockwise = null;
				double smallestAngle = Double.MAX_VALUE;
				Integer before = interval[0] - 1;
				if (before == -1) {// ensure loop
					before = RMYS.k - 1;
				}
				for (NewPhysicalGraphNode p : cones.get(before)) {
					double currentangle = calculateAngleForCone(p, helppos, sourceNode);
					if (currentangle < smallestAngle) {
						smallestAngle = currentangle;
						counterclockwise = p;
					}
				}

				// same for the nearest node clockwise
				NewPhysicalGraphNode clockwise = null;
				double greatestAngle = 0;
				Integer after = interval[1] + 1;
				if (after == RMYS.k) { // ensure loop
					after = 0;
				}
				for (NewPhysicalGraphNode p : cones.get(after)) {
					double currentangle = calculateAngleForCone(p, helppos, sourceNode);
					if (currentangle > greatestAngle) {
						greatestAngle = currentangle;
						clockwise = p;
					}
				}
				if (clockwise != null && counterclockwise != null) {
					if (rmys.getKnownNeighbors().contains(clockwise)) {
						rmys.getKnownNeighbors().add(counterclockwise);
					} else if (rmys.getKnownNeighbors().contains(counterclockwise)) {
						rmys.getKnownNeighbors().add(clockwise);
					} else {
						double disclock = clockwise.getPosition().distanceTo(sourceNode.getPosition());
						double discounter = counterclockwise.getPosition().distanceTo(sourceNode.getPosition());
						if (disclock < discounter) {
							rmys.getKnownNeighbors().add(clockwise);
						} else if (discounter < disclock) {
							rmys.getKnownNeighbors().add(counterclockwise);
						} else {
							throw new RuntimeException("unspecified behavior: distance from " + sourceNode.toString()
									+ " to " + clockwise.toString() + " and " + counterclockwise.toString()
									+ " is equal.");
							// is not specified in Modified Yao Step

						}
					}
				} else {

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
				int sequence_l = interval[1] - interval[0] + 1; // eg. 8-7=1,
																// but means 8
																// and 7 are
																// empty-> so
																// plus 1
				assert(sequence_l > 1);

				int clockwiseNeighbors = (int) (sequence_l / 2.0);
				int counterclockwiseNeighbors = (int) ((sequence_l + 1) / 2.0);

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
				while (counterclockwiseNeighbors > 0 && index < sortedNeighbors.size()) {

					NewPhysicalGraphNode p = sortedNeighbors.get(index);
					if (rmys.getKnownNeighbors().contains(p)) {
						index++;
						continue;// take next node
					}
					rmys.getKnownNeighbors().add(p);
					index++;
					counterclockwiseNeighbors -= 1;
				}

				// choose the first clockwiseNeighbors which are not already
				// selected
				index = sortedNeighbors.size() - 1;
				while (clockwiseNeighbors > 0 && index >= 0) {
					NewPhysicalGraphNode p = sortedNeighbors.get(index);
					if (rmys.getKnownNeighbors().contains(p)) {
						index--;
						continue;// take next node
					}
					rmys.getKnownNeighbors().add(p);
					index--;
					clockwiseNeighbors -= 1;
				}

			}
		}

		if (rmys instanceof RMYSForwarderMessageHandler) {
			for (PhysicalGraphNode pn : rmys.getKnownNeighbors()) {
				// create Messagehandler so the node knows what to do

				RMYSMessageHandler mh = new RMYSMessageHandler(rmys.tcID, pn, sourceNode);
				pn.messageHandlerMap.put(rmys.tcID, mh);


				// perform actual communication
				RequestMessage request = new RequestMessage(rmys.tcID, sourceNode);

				// FIXME: broadcast
				sourceNode.send(request, pn);// ask each neighbour for
												// acknowledgement of this edge
			}
		} else {

		}

		System.out.println("Neighbors for node: " + sourceNode.toString());
		System.out.print("(");
		for (PhysicalGraphNode pn : rmys.getKnownNeighbors()) {
			System.out.print(pn.toString() + ", ");
		}
		System.out.println(")");

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
	 * @return the angle between the horizontal axis source node and node (starting at 3 o'clock counterclockwise)
	 */
	private static double calculateAngle(NewPhysicalGraphNode node, NewPhysicalGraphNode sourceNode) {
		return calculateAngleForCone(node.getPosition(), sourceNode.getPosition());
	}

	/**
	 * @param pos
	 * @param sourceNodePos
	 * @return the angle between pos and x-axis in sourceNodePos between 0 and 2Pi
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
