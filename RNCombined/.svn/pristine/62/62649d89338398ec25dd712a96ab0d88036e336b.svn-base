package projects.reactiveSpanner.nodes.messages.BCA;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.messages.Message;

public class CTS extends AbstractMessage {

	public CTS(final UUID tcID, PhysicalGraphNode from) {
		super(tcID, from, BeaconlessTopologyControl.EStrategy.BCA);
	}

	@Override
	public Message clone() {
		return new CTS(this.ID, this.node);
	}
	
	public PhysicalGraphNode getSource() {
		return node;
	}

}
