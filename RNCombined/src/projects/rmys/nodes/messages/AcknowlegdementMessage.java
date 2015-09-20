package projects.rmys.nodes.messages;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messages.BeaconlessMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.messages.Message;

public class AcknowlegdementMessage extends BeaconlessMessage {
	private Boolean accepted;

	public AcknowlegdementMessage(UUID tcID, PhysicalGraphNode transmitter, Boolean accepted) {
		super(tcID, transmitter, EStrategy.RMYS);
		this.accepted = accepted;
	}

	@Override
	public Message clone() {
		return new AcknowlegdementMessage(ID, getTransmitter(), accepted);

	}

	public Boolean getAccepted() {
		return accepted;
	}

}
