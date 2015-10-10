package projects.rmys.nodes.messages;

import java.util.ArrayList;
import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messages.BeaconlessMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;
import sinalgo.nodes.messages.Message;

public class RequestMessage extends BeaconlessMessage {

	public ArrayList<NewPhysicalGraphNode> candidates;

	public RequestMessage(UUID tcID, PhysicalGraphNode transmitter) {
		super(tcID, transmitter, EStrategy.RMYS);
		candidates = new ArrayList<>();
	}

	@Override
	public Message clone() {
		return new RequestMessage(ID, getTransmitter());
	}

}
