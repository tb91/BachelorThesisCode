package projects.rmys.nodes.messageHandler;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;

public class RMYSForwarderMessageHandler extends RMYSMessageHandler {

	protected RMYSForwarderMessageHandler(UUID tcID, PhysicalGraphNode ownerNode, PhysicalGraphNode sourceNode,
			EStrategy strategy) {
		super(tcID, ownerNode, sourceNode, strategy);
		// TODO Auto-generated constructor stub
	}

}
