package projects.rmys.nodes.messageHandler;

import java.util.ArrayList;
import java.util.HashMap;

import com.sun.javafx.geom.Vec2d;

import projects.reactiveSpanner.TopologyControlObserver;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.reactivePDT.ReactivePDT;
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
	private static double cones_size = -1;

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

		cones_size = 2 * Math.PI / k;
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
					_start();
				}

			}
		});

		// adds a RMYSMessageHandler for compatibility reasons to the
		// ReactiveSpanner Framework
		RMYSMessageHandler rmys = new RMYSMessageHandler(super.getTopologyControlID(), sourceNode, sourceNode, super.getStrategyType());
		sourceNode.messageHandlerMap.put(super.getTopologyControlID(), rmys);

		pdt.start();
	}



	@Override
	protected void _start() {
		//for each PDT neighbour calculate its cone id
		
		//NodeId -> coneId
		// HashMap<Integer, Integer> nodeIds = new HashMap<>();
		// for (SimpleNode n : sourceNode.messageHandlerMap.get(pdt.getTopologyControlID()).getKnownNeighbors()) {
		//	nodeIds.put(n.ID, calculateCone(n.getPosition()));
		//}
		
		// coneId -> NewPhysicalGraphNode
		HashMap<Integer, ArrayList<NewPhysicalGraphNode>> cones = new HashMap<>();
		// coneId 0 is the one starting at 3 o'clock running counterclockwise
		// counterclockwise indicates ids with increasing number

		// find coneId for each Node and sort it into HashMap cones
		for (SimpleNode n : sourceNode.messageHandlerMap.get(pdt.getTopologyControlID()).getKnownNeighbors()) {
			if (n instanceof NewPhysicalGraphNode) {
				int key=calculateCone(n.getPosition());
				
				ArrayList<NewPhysicalGraphNode> list=cones.get(key);
				if(list!=null){
					list.add((NewPhysicalGraphNode)n); //save Typecast
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
			if (interval[0] == interval[1]) { // just one empty cone; the predecessor and the successor cone are not empty
				// find nearest nodes clockwise and counterclockwise
				// the nearest nodes must reside in the cone before interval[0] and after interval[1]
				NewPhysicalGraphNode counterclockwise = null;
				double smallestAngle = Double.MAX_VALUE;
				for (NewPhysicalGraphNode p : cones.get(interval[0] - 1)) {
					double currentangle = calculateAngle(p.getPosition());
					if (currentangle < smallestAngle) {
						smallestAngle = currentangle;
						counterclockwise = p;
					}
				}

				// same for the nearest node clockwise
				NewPhysicalGraphNode clockwise = null;
				double greatestAngle = 0;
				for (NewPhysicalGraphNode p : cones.get(interval[1] + 1)) {
					double currentangle = calculateAngle(p.getPosition());
					if (currentangle > greatestAngle) {
						greatestAngle = currentangle;
						clockwise = p;
					}
				}


			} else {

			}
		}

	}


	/**
	 * @param pos
	 * @return id of the cone in which pos lies with respect to sourceNode
	 */
	private int calculateCone(Position pos) {

		double angle = calculateAngle(pos);

		return (int) (angle / cones_size);
	}

	/**
	 * @param pos
	 * @return the angle between the horzontal axis source node and pos (starting at 3 o'clock counterclockwise)
	 */
	private double calculateAngle(Position pos) {
		// define point to define zero on horizontal axis
		Position help = new Position(sourceNode.getPosition().xCoord + 1, sourceNode.getPosition().yCoord, 0);

		// create vectors
		Vec2d vecOr = new Vec2d((pos.xCoord - sourceNode.getPosition().xCoord), (pos.yCoord - sourceNode.getPosition().yCoord));
		Vec2d vechelp = new Vec2d(help.xCoord - sourceNode.getPosition().xCoord, help.yCoord - sourceNode.getPosition().yCoord);

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
