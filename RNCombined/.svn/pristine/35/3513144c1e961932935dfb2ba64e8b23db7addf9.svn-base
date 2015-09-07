package projects.reactiveSpanner.nodes.messages;

import java.util.HashMap;
import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.buildBackbone.BuildBackboneMessageHandler;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.messages.Message;

public class DeliverSetMessage extends BeaconMessage{

	//TODO Data container instead of BuildBackboneMessageHandler?
	private HashMap<BuildBackboneMessageHandler,BuildBackboneMessageHandler> distinctIDs;
	
	public DeliverSetMessage(UUID tcID, PhysicalGraphNode transmitter, EStrategy strategy, HashMap<BuildBackboneMessageHandler,BuildBackboneMessageHandler> distinctIDs) {
		super(tcID, transmitter, strategy);
		this.distinctIDs=distinctIDs;
	}

	public HashMap<BuildBackboneMessageHandler,BuildBackboneMessageHandler> getDistinctIDs() {
		return distinctIDs;
	}

	@Override
	public Message clone() {
		return new DeliverSetMessage(this.ID, this.node, this.strategy, this.distinctIDs);
	}
}
