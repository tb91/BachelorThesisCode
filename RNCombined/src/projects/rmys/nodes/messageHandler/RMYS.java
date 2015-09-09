package projects.rmys.nodes.messageHandler;

import java.util.HashMap;

import com.sun.javafx.geom.Vec2d;

import projects.reactiveSpanner.TopologyControlObserver;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.reactivePDT.ReactivePDT;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;
import sinalgo.nodes.Position;

/**
 * @author timmy
 *
 */
public class RMYS extends BeaconlessTopologyControl {

	ReactivePDT pdt;

	public RMYS(NewPhysicalGraphNode sourceNode) {
		super(EStrategy.RMYS, sourceNode);
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
		RMYSMessageHandler rmys = new RMYSMessageHandler(super.getTopologyControlID(), sourceNode, sourceNode,
				super.getStrategyType());
		sourceNode.messageHandlerMap.put(super.getTopologyControlID(), rmys);

		pdt.start();
	}



	@Override
	protected void _start() {
		//for each PDT neighbour calculate its cone id
		
		//NodeId -> coneId
		HashMap<Integer, Integer> nodeIds = new HashMap<>();
		for (SimpleNode n : sourceNode.messageHandlerMap.get(pdt.getTopologyControlID()).getKnownNeighbors()) {
			calculateCone(n.getPosition());
		}

	}


	/**
	 * @param pos
	 * @return id of the cone in which pos lies with respect to sourceNode
	 */
	private int calculateCone(Position pos) {
		// define point to define zero on horizontal axis
		Position help = new Position(pos.xCoord + 1, pos.yCoord, 0);

		// create vectors
		Vec2d vecOr = new Vec2d(pos.xCoord - sourceNode.getPosition().xCoord,
				pos.yCoord - sourceNode.getPosition().yCoord);
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
		System.out.println("Angle: " + angle);
		// FIXME:complete
		return 0;
	}

	private double calculateLength(Vec2d vec) {
		return Math.sqrt(vec.x * vec.x + vec.y * vec.y);
	}
}
