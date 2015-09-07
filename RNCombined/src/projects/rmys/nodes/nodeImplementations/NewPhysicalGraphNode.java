package projects.rmys.nodes.nodeImplementations;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EState;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.routing.RoutingProtocol.ERoutingState;

//need to use PhysicalGraphNode to be able to use functions from reactiveSpanner
//Creating a new Node to point out differences to ReactiveSpanner
public class NewPhysicalGraphNode extends PhysicalGraphNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NewPhysicalGraphNode() {
		super();

	}

	@Override
	public void onNotify(UUID routingID, ERoutingState event) {
		System.out.println("notifying node: " + event.name());

	}

	@Override
	public void onNotify(SubgraphStrategy topologyControl, EState event) {
		System.out.println("notifying node low: " + event.name());

	}

	/**
	 * Start RMYS on this node and display edges
	 */
	@NodePopupMethod(menuText = "RMYS")
	public void RMYS() {
		this.subgraphStrategyFactory.request(EStrategy.RMYS).start();
	}
}
