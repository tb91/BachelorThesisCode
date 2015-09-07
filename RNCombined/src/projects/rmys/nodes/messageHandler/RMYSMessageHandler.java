package projects.rmys.nodes.messageHandler;

import java.awt.Graphics;
import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.messages.CTS;
import projects.reactiveSpanner.nodes.messages.RTS;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.record.MessageRecord;
import sinalgo.gui.transformation.PositionTransformation;

public class RMYSMessageHandler extends BeaconlessMessageHandler {

	protected RMYSMessageHandler(UUID tcID, PhysicalGraphNode ownerNode, PhysicalGraphNode sourceNode,
			EStrategy strategy) {
		super(tcID, ownerNode, sourceNode, strategy);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void timerTriggerEvent() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasTerminated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void receivedRTS(RTS msg) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void receivedCTS(CTS msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receivedMessage(AbstractMessage msg) {
		System.out.println("got message: " + msg.toString());

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
