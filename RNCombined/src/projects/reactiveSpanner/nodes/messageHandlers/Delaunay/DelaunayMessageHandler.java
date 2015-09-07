package projects.reactiveSpanner.nodes.messageHandlers.Delaunay;

import java.awt.Graphics;
import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.AbstractMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.record.MessageRecord;
import sinalgo.gui.transformation.PositionTransformation;

public class DelaunayMessageHandler extends AbstractMessageHandler {

	protected DelaunayMessageHandler(UUID tcID, PhysicalGraphNode ownerNode,
			PhysicalGraphNode sourceNode, EStrategy strategy) {
		super(tcID, ownerNode, sourceNode, strategy);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void receivedMessage(AbstractMessage msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawNode(Graphics g, PositionTransformation pt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public MessageRecord getCurrentMessageRecord() {
		// TODO Auto-generated method stub
		return null;
	}

}
