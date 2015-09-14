package projects.rmys.nodes.messageHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.sun.javafx.geom.Vec2d;

import projects.reactiveSpanner.TopologyControlObserver;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.reactivePDT.ReactivePDT;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Position;

/**
 * @author timmy
 *
 */
public class RMYS extends BeaconlessTopologyControl {

	private static int k = -1;
	private static double cone_size = -1;

	static {
		try {
			k = Configuration.getIntegerParameter("RMYS/k_value");
		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}

	ReactivePDT pdt;
	RMYSMessageHandler rmys;

	public RMYS(NewPhysicalGraphNode sourceNode) {
		super(EStrategy.RMYS, sourceNode);

		cone_size = 2 * Math.PI / k;

		_init();
	}

	@Override
	protected void _init() {
		pdt = new ReactivePDT(sourceNode);
		pdt.addObserver(new TopologyControlObserver() {

			@Override
			public void onNotify(SubgraphStrategy topologyControl, EState event) {
				if (pdt.hasTerminated()) { // as soon as RMYS gets notified
											// thats rPDT has terminated it
											// starts Modified Yao Step
					calculate();
				}

			}
		});

		// adds a RMYSMessageHandler for compatibility reasons to the
		// ReactiveSpanner Framework
		RMYSMessageHandler rmys = new RMYSMessageHandler(super.getTopologyControlID(), sourceNode, sourceNode, super.getStrategyType());
		rmys.initializeKnownNeighborsSet();
		sourceNode.messageHandlerMap.put(super.getTopologyControlID(), rmys);

		pdt.start();
	}

	protected void calculate() {
		// for each PDT neighbour calculate its cone id

		// NodeId -> coneId
		// HashMap<Integer, Integer> nodeIds = new HashMap<>();
		// for (SimpleNode n : sourceNode.messageHandlerMap.get(pdt.getTopologyControlID()).getKnownNeighbors()) {
		// nodeIds.put(n.ID, calculateCone(n.getPosition()));
		// }

		// coneId -> NewPhysicalGraphNode
		HashMap<Integer, ArrayList<NewPhysicalGraphNode>> cones = new HashMap<>();
		// coneId 0 is the one starting at 3 o'clock running counterclockwise
		// counterclockwise indicates ids with increasing number

		// find coneId for each Node and sort it into HashMap cones
		for (SimpleNode n : sourceNode.messageHandlerMap.get(pdt.getTopologyControlID()).getKnownNeighbors()) {
			if (n instanceof NewPhysicalGraphNode) {
				int key = calculateCone(n.getPosition());

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
		for (int l = 0; l < k; l++) {
			shortest.put(l, null); // for easier calculations initialize array with all possible coneIds
		}
		for (Integer t : cones.keySet()) {
			NewPhysicalGraphNode shortestNode = null; // variable to hold the node
														// which is closest to sourceNode in a cone
			double shortestvalue = Double.MAX_VALUE;
			for (NewPhysicalGraphNode p : cones.get(t)) {
				double distance = sourceNode.getPosition().distanceTo(p.getPosition());
				if (shortestvalue < distance) {
					shortestNode = p;
					shortestvalue = distance;
				}
			}
			shortest.put(t, shortestNode);
		}

		// Since these shortest edges are selected anyway, they are added to the topologyControl neighbors
		for (NewPhysicalGraphNode p : shortest.values()) {

			rmys.getKnownNeighbors().add(p);

		}

		// find maximal sequences of empty cones
		ArrayList<int[]> empty_cones_set = new ArrayList<>();

		for (int i = 0; i < cones.keySet().size(); i++) {
			if (cones.get(i).size() == 0) {// start of a empty sequence found
				int j;
				for (j = i + 1; j < cones.keySet().size(); j++) {
					if (cones.get(j).size() != 0) { // determines end of an empty sequence
						j -= 1;
						break;
					}
				}
				int[] empty_interval = { i, j }; // [0] indicates start, [1] indicates end of empty sequence of cones
				empty_cones_set.add(empty_interval);
				i = j + 1; // prohibit duplicates
			}
		}

		// for each empty sequence do
		for (int[] interval : empty_cones_set) {
			// find orientation point
			double angleToZero = cone_size * interval[0];
			double xpos = Math.cos(angleToZero);
			double ypos = Math.sin(angleToZero);
			Position helppos = new Position(sourceNode.getPosition().xCoord + xpos, sourceNode.getPosition().yCoord + ypos, 0);

			if (interval[0] == interval[1]) { // just one empty cone; the predecessor and the successor cone are not empty

				// find nearest nodes clockwise and counterclockwise
				// the nearest nodes must reside in the cone before interval[0] and after interval[1]
				NewPhysicalGraphNode counterclockwise = null;
				double smallestAngle = Double.MAX_VALUE;
				for (NewPhysicalGraphNode p : cones.get(interval[0] - 1)) {
					double currentangle = calculateAngleForCone(p.getPosition(), helppos);
					if (currentangle < smallestAngle) {
						smallestAngle = currentangle;
						counterclockwise = p;
					}
				}

				// same for the nearest node clockwise
				NewPhysicalGraphNode clockwise = null;
				double greatestAngle = 0;
				for (NewPhysicalGraphNode p : cones.get(interval[1] + 1)) {
					double currentangle = calculateAngleForCone(p.getPosition(), helppos);
					if (currentangle > greatestAngle) {
						greatestAngle = currentangle;
						clockwise = p;
					}
				}

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
						throw new RuntimeException("unspecified behaviour: distance from " + sourceNode.toString() + " to "
								+ clockwise.toString() + " and " + counterclockwise.toString() + " is equal."); // is not specified in Modified Yao
																												// Step

					}
				}

			} else {

				// for the sake of simplicity all neighbors are sorted with respect to the angle between
				// the empty cone sequence and themselves in sourceNode
				ArrayList<NewPhysicalGraphNode> sortedNeighbors = new ArrayList<>();
				final HashMap<NewPhysicalGraphNode, Double> anglemap = new HashMap<>();
				for (ArrayList<NewPhysicalGraphNode> list : cones.values()) {
					for (NewPhysicalGraphNode p : list) {
						anglemap.put(p, calculateAngleForCone(p.getPosition(), helppos));
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
				int sequence_l = interval[1] - interval[0] + 1; // eg. 8-7=1, but means 8 and 7 are empty-> so plus 1
				assert(sequence_l > 1);

				int clockwiseNeighbors = (int) (sequence_l / 2.0);
				int counterclockwiseNeighbors = (int) ((sequence_l + 1) / 2.0);

				System.out.println();
				System.out.println("List of Node: " + sourceNode.toString());
				for (PhysicalGraphNode p : sortedNeighbors) {
					System.out.println("Node " + p.toString() + " with angle: " + anglemap.get(p));
				}
			}
		}

	}

	@Override
	protected void _start() {

	}

	/**
	 * @param pos
	 * @return id of the cone in which pos lies with respect to sourceNode
	 */
	private int calculateCone(Position pos) {

		double angle = calculateAngle(pos);

		return (int) (angle / cone_size);
	}

	/**
	 * @param pos
	 * @return the angle between the horzontal axis source node and pos (starting at 3 o'clock counterclockwise)
	 */
	private double calculateAngle(Position pos) {
		return calculateAngleForCone(pos, new Position(sourceNode.getPosition().xCoord + 1, sourceNode.getPosition().yCoord, 0));
	}

	/**
	 * @param pos
	 * @param reference
	 * @return the angle between pos and reference in sourceNode
	 */
	private double calculateAngleForCone(Position pos, Position reference) {

		// create vectors
		Vec2d vecOr = new Vec2d((pos.xCoord - sourceNode.getPosition().xCoord), (pos.yCoord - sourceNode.getPosition().yCoord));
		Vec2d vechelp = new Vec2d(reference.xCoord - sourceNode.getPosition().xCoord, reference.yCoord - sourceNode.getPosition().yCoord);

		// normalize vectors
		double lengthOr = calculateLength(vecOr);
		vecOr.x /= lengthOr;
		vecOr.y /= lengthOr;

		double lengthhelp = calculateLength(vechelp);
		vechelp.x /= lengthhelp;
		vechelp.y /= lengthhelp;

		return Math.acos(vecOr.x * vechelp.x + vecOr.y * vechelp.y);
	}

	/**
	 * @param vec
	 * @return Euclidean length of vec
	 */
	private double calculateLength(Vec2d vec) {
		return Math.sqrt(vec.x * vec.x + vec.y * vec.y);
	}
}
