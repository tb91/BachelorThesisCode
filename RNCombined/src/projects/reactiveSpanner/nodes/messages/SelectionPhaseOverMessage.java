package projects.reactiveSpanner.nodes.messages;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.messages.Message;

public class SelectionPhaseOverMessage extends BeaconlessMessage
{
	public SelectionPhaseOverMessage(final UUID ID, final PhysicalGraphNode transmitter) {
		super(ID, transmitter, BeaconlessTopologyControl.EStrategy.BFP);
	}

	@Override
	public Message clone() {
		return this;
	}

}
