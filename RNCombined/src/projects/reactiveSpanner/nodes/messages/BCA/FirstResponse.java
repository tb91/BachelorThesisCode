package projects.reactiveSpanner.nodes.messages.BCA;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.BCA.BCAMessageHandler;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.messages.Message;

public class FirstResponse extends AbstractMessage {

	public UUID sessionID;
	
	public BCAMessageHandler msgHandler;
	public boolean cellLeaderElection;
	
	public FirstResponse(final UUID tcID, PhysicalGraphNode from,
			BCAMessageHandler msgHandler, boolean cellLeaderElection,
			UUID sessionID) {
		super(tcID, from, BeaconlessTopologyControl.EStrategy.BCA);
		this.msgHandler = msgHandler;
		this.sessionID = sessionID;
		this.cellLeaderElection = cellLeaderElection;
	}

	public PhysicalGraphNode getSource() {
		return node;
	}

	public Message clone() {
		return new FirstResponse(this.ID, this.node, this.msgHandler,
				this.cellLeaderElection, this.sessionID);
	}

}
