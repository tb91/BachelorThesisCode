package projects.rmys.nodes.messageHandler;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

import com.sun.javafx.geom.Vec2d;

import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import projects.rmys.nodes.messages.RequestMessage;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Position;

public class RMYSForwarderMessageHandler extends RMYSMessageHandler {
	SubgraphStrategy pdt;
	protected RMYSForwarderMessageHandler(UUID tcID, PhysicalGraphNode sourceNode, SubgraphStrategy neighborhood) {
		super(tcID, sourceNode, sourceNode);
		this.pdt = neighborhood;
	}

	public static void start(NewPhysicalGraphNode sourceNode, SubgraphStrategy pdt, RMYSMessageHandler rmys) {
		// for each PDT neighbour calculate its cone id

		// NodeId -> coneId
		// HashMap<Integer, Integer> nodeIds = new HashMap<>();
		// for (SimpleNode n : sourceNode.messageHandlerMap.get(pdt.getTopologyControlID()).getKnownNeighbors()) {
		// nodeIds.put(n.ID, calculateCone(n.getPosition()));
		// }

		// coneId -> NewPhysicalGraphNode
		HashMap<Integer, ArrayList<NewPhysicalGraphNode>> cones = new HashMap<>();
		for (int l = 0; l < RMYS.k; l++) {// for easier calculations initialize hashmap with all possible coneIds
			cones.put(l, new ArrayList<NewPhysicalGraphNode>());
		}
		// coneId 0 is the one starting at 3 o'clock running counterclockwise
		// counterclockwise indicates ids with increasing number

		// find coneId for each Node and sort it into HashMap cones
		for (SimpleNode n : sourceNode.messageHandlerMap.get(pdt.getTopologyControlID()).getKnownNeighbors()) {
			if (n instanceof NewPhysicalGraphNode) {
				int key = calculateCone(n.getPosition(), sourceNode);

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
		for (int l = 0; l < RMYS.k; l++) {// for easier calculations initialize hashmap with all possible coneIds
			shortest.put(l, null);
		}
		for (Integer t : cones.keySet()) {
			NewPhysicalGraphNode shortestNode = null; // variable to hold the node
														// which is closest to sourceNode in a cone
			double shortestvalue = Double.MAX_VALUE;
			for (NewPhysicalGraphNode p : cones.get(t)) {
				double distance = sourceNode.getPosition().distanceTo(p.getPosition());
				if (shortestvalue > distance) {
					shortestNode = p;
					shortestvalue = distance;
				}
			}
			shortest.put(t, shortestNode);
		}

		// Since these shortest edges are selected anyway, they are added to the topologyControl neighbors
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
					if (cones.get(j).size() != 0) { // determines end of an empty sequence
						j -= 1;
						break;
					}
				}
				if (j == RMYS.k) {// if last cone is empty, too
					j -= 1;
				}
				int[] empty_interval = { i, j }; // [0] indicates start, [1] indicates end of empty sequence of cones
				empty_cones_set.add(empty_interval);
				i = j; // prohibit duplicates
			}
		}

		// for each empty sequence do
		for (int[] interval : empty_cones_set) {
			// find orientation point
			double angleToZero = RMYS.cone_size * interval[0];
			double xpos = Math.cos(angleToZero); // rotate orientation point (1,0) to empty cone
			double ypos = Math.sin(angleToZero);
			Position helppos = new Position(sourceNode.getPosition().xCoord + xpos, sourceNode.getPosition().yCoord + ypos, 0);

			if (interval[0] == interval[1]) { // just one empty cone; the predecessor and the successor cone are not empty

				// find nearest nodes clockwise and counterclockwise
				// the nearest nodes must reside in the cone before interval[0] and after interval[1]
				NewPhysicalGraphNode counterclockwise = null;
				double smallestAngle = Double.MAX_VALUE;
				Integer before = interval[0] - 1;
				if (before == -1) {// ensure loop
					before = RMYS.k - 1;
				}
				for (NewPhysicalGraphNode p : cones.get(before)) {
					double currentangle = calculateAngleForCone(p.getPosition(), helppos, sourceNode);
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
					double currentangle = calculateAngleForCone(p.getPosition(), helppos, sourceNode);
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
						anglemap.put(p, calculateAngleForCone(p.getPosition(), helppos, sourceNode));
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

				// System.out.println();
				// System.out.println("clockwise count: " + clockwiseNeighbors);
				// System.out.println("counterclockwise count: " +
				// counterclockwiseNeighbors);
				// System.out.println("List of Node: " + sourceNode.toString());
				// for (PhysicalGraphNode p : sortedNeighbors) {
				// System.out.println("Node " + p.toString() + " with angle: " +
				// anglemap.get(p));
				// }

				// choose the first counterclockwiseNeighbors which are not already selected
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

				// choose the first clockwiseNeighbors which are not already selected
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

		System.out.println("Neighbors for node: " + sourceNode.toString());
		System.out.print("(");
		for (PhysicalGraphNode pn : rmys.getKnownNeighbors()) {
			System.out.print(pn.toString() + ", ");

			RequestMessage request = new RequestMessage(rmys.tcID, sourceNode);

			sourceNode.send(request, pn);// ask each neighbour for
												// acknowledgement
		}
		System.out.println(")");
	}

	@Override
	public void drawNode(Graphics g, PositionTransformation pt) {

		for (int i = 0; i < RMYS.k; i++) {
			double angle = i * RMYS.cone_size;
			double xpos = RMYS.unit_radius * Math.cos(angle);
			double ypos = RMYS.unit_radius * Math.sin(angle);
			Position helppos = new Position(sourceNode.getPosition().xCoord + xpos,
					sourceNode.getPosition().yCoord + ypos, 0);
			g.setColor(Color.black);
			pt.drawLine(g, sourceNode.getPosition(), helppos);
		}

	}

	/**
	 * @param pos
	 * @return id of the cone in which pos lies with respect to sourceNode
	 */
	private static int calculateCone(Position pos, NewPhysicalGraphNode sourceNode) {

		double angle = calculateAngle(pos, sourceNode);

		return (int) (angle / RMYS.cone_size);
	}

	/**
	 * @param pos
	 * @return the angle between the horzontal axis source node and pos (starting at 3 o'clock counterclockwise)
	 */
	private static double calculateAngle(Position pos, NewPhysicalGraphNode sourceNode) {
		return calculateAngleForCone(pos,
				new Position(sourceNode.getPosition().xCoord + 1, sourceNode.getPosition().yCoord, 0), sourceNode);
	}

	/**
	 * @param pos
	 * @param reference
	 * @return the angle between pos and reference in sourceNode
	 */
	private static double calculateAngleForCone(Position pos, Position reference, NewPhysicalGraphNode sourceNode) {

		// create vectors
		Vec2d vecOr = new Vec2d((pos.xCoord - sourceNode.getPosition().xCoord), (pos.yCoord - sourceNode.getPosition().yCoord));
		Vec2d vechelp = new Vec2d(reference.xCoord - sourceNode.getPosition().xCoord, reference.yCoord - sourceNode.getPosition().yCoord);
		Vec2d poshelp = new Vec2d(reference.xCoord - pos.xCoord, reference.yCoord - pos.yCoord);

		double vecOr_l=calculateLength(vecOr);
		double vechelp_l=calculateLength(vechelp);
		double poshelp_l=calculateLength(poshelp);
		
		double angle = Math
				.acos((vechelp_l * vechelp_l + vecOr_l * vecOr_l - poshelp_l * poshelp_l) / (2 * vechelp_l * vecOr_l));
		return angle;
	}

	/**
	 * @param vec
	 * @return Euclidean length of vec
	 */
	private static double calculateLength(Vec2d vec) {
		return Math.sqrt(vec.x * vec.x + vec.y * vec.y);
	}

}
