package projects.reactiveSpanner.nodes.messages.BCA;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.messages.Request;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.Position;
import sinalgo.nodes.messages.Message;

public class FirstRequest extends AbstractMessage implements Request {
	
	public UUID sessionID;
	
	public Position[] directCells;
	
	//TODO Wofür?
//	public boolean leaderElected;

	public PhysicalGraphNode initialNode;
	
	public Position[] getDirectCells() {
		return directCells;
	}

	public FirstRequest(final UUID tcID, PhysicalGraphNode from,
			Position cells[], /*boolean leaderElected,*/ UUID sessionID, PhysicalGraphNode initialNode) {
		super(tcID, from, BeaconlessTopologyControl.EStrategy.BCA);
		this.directCells = cells;
//		this.leaderElected = leaderElected;
		this.sessionID = sessionID;
		this.initialNode = initialNode;
	}

	public PhysicalGraphNode getSource() {
		return node;
	}

	public Message clone() {
		return new FirstRequest(this.ID, this.node, this.directCells, /*this.leaderElected,*/ this.sessionID, this.initialNode);
	}

}