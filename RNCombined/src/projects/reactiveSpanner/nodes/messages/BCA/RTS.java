package projects.reactiveSpanner.nodes.messages.BCA;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.BCA.BCAMessageHandler;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.messages.Request;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.messages.Message;

public class RTS extends AbstractMessage implements Request {

	public boolean initialRequest;
	
	public PhysicalGraphNode initialNode;
	
	public UUID sessionID;
	
	public BCAMessageHandler msgHandler;
	
	public RTS(final UUID tcID, PhysicalGraphNode from, boolean initialRequest,
			UUID sessionID, BCAMessageHandler msgHandler) {
		super(tcID, from, BeaconlessTopologyControl.EStrategy.BCA);
		this.initialRequest = initialRequest;
		this.sessionID = sessionID;
		this.msgHandler = msgHandler;
	}

	@Override
	public Message clone() {
		return new RTS(this.ID, this.node, this.initialRequest, this.sessionID, this.msgHandler);
	}
	
	public PhysicalGraphNode getSource() {
		return node;
	}

}
