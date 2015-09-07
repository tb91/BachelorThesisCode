package projects.reactiveSpanner.nodes.messages.BCA;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.Position;
import sinalgo.nodes.messages.Message;

public class FinishMessage extends AbstractMessage {

	public PhysicalGraphNode source;
	
	public FinishMessage(UUID tcID, PhysicalGraphNode from, PhysicalGraphNode source) {
		super(tcID, from, BeaconlessTopologyControl.EStrategy.BCA);
		this.source = source;
	}

	@Override
	public Message clone() {
		return new FinishMessage(this.ID, this.node, this.source);
	}

}
