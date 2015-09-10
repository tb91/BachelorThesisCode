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

		// Since these shortest edges are selected anyway, they are added to the topologyControl neighbours
		for (NewPhysicalGraphNode p : shortest.values()) {

			rmys.getKnownNeighbors().add(p);

		}

		// find maximal sequences of empty cones



	}


	/**
	 * @param pos
	 * @return id of the cone in which pos lies with respect to sourceNode
	 */
	private int calculateCone(Position pos) {
		// define point to define zero on horizontal axis
		Position help = new Position(sourceNode.getPosition().xCoord + 1, sourceNode.getPosition().yCoord, 0);

		// create vectors
		Vec2d vecOr = new Vec2d((pos.xCoord - sourceNode.getPosition().xCoord),
				(pos.yCoord - sourceNode.getPosition().yCoord));
		Vec2d vechelp = new Vec2d(help.xCoord - sourceNode.getPosition().xCoord,
				help.yCoord - sourceNode.getPosition().yCoord);

		// normalize vectors
		double lengthOr = calculateLength(vecOr);
		vecOr.x /= lengthOr;
		vecOr.y /= lengthOr;

		double lengthhelp = calculateLength(vechelp);
		vechelp.x /= lengthhelp;
		vechelp.y /= lengthhelp;

		double angle = Math.acos(vecOr.x * vechelp.x + vecOr.y * vechelp.y);

		// System.out.println(
		// "Angle in Node " + sourceNode.ID + " to Node at (" + pos.xCoord + ",
		// " + pos.yCoord + ") = " + angle);


		return (int) (angle / cones_size);
	}

	/**
	 * @param vec
	 * @return Euclidean length of vec
	 */
	private double calculateLength(Vec2d vec) {
		return Math.sqrt(vec.x * vec.x + vec.y * vec.y);
	}
}
