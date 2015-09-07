package projects.reactiveSpanner.nodes.messages.BCA;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.messages.Message;

public class SecondResponse extends AbstractMessage {

	public UUID sessionID;
	
	private PhysicalGraphNode source;
	
	public boolean cellLeaderElection;
	
	// Knoten, der node geantwortet hat
	public PhysicalGraphNode getSource() {
		return source;
	}
	
	// Knoten, der diese Nachricht abgeschickt hat
	public PhysicalGraphNode getNode() {
		return node;
	}

	public SecondResponse(UUID tcID, PhysicalGraphNode from,
			PhysicalGraphNode sourceNode, UUID sessionID, boolean cellLeaderElection) {
		super(tcID, from, BeaconlessTopologyControl.EStrategy.BCA);
		this.source = sourceNode;
		this.sessionID = sessionID;
		this.cellLeaderElection = cellLeaderElection;
	}
	
	@Override
	public Message clone() {
		return new SecondResponse(this.ID, this.node, this.source, this.sessionID, this.cellLeaderElection);
	}

}
