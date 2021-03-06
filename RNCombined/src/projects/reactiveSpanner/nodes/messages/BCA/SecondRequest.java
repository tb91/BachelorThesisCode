package projects.reactiveSpanner.nodes.messages.BCA;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.Position;
import sinalgo.nodes.messages.Message;

public class SecondRequest extends AbstractMessage {

	public UUID sessionID;
	
	public Position[] indirectCells;
	
	public Position[] getIndirectCells() {
		return indirectCells;
	}

	public SecondRequest(final UUID tcID, PhysicalGraphNode from, Position cells[], UUID sessionID){
		super(tcID, from, BeaconlessTopologyControl.EStrategy.BCA);
		this.indirectCells = cells;
		this.sessionID = sessionID;
	}
	
	public PhysicalGraphNode getSource() {
		return node;
	}

	@Override
	public Message clone() {
		return new SecondRequest(this.ID, this.node, this.indirectCells, this.sessionID);
	}

}
