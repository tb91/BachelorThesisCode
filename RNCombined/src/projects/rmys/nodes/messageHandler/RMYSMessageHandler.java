package projects.rmys.nodes.messageHandler;

import java.awt.Graphics;
import java.util.HashMap;
import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.messages.CTS;
import projects.reactiveSpanner.nodes.messages.RTS;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.record.MessageRecord;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;
import sinalgo.gui.transformation.PositionTransformation;

public class RMYSMessageHandler extends BeaconlessMessageHandler {

	/**
	 * indicates that this was selected from another node
	 */
	HashMap<NewPhysicalGraphNode, Boolean> is_selected;

	protected RMYSMessageHandler(UUID tcID, PhysicalGraphNode ownerNode, PhysicalGraphNode sourceNode,
 EStrategy strategy) {
		super(tcID, ownerNode, sourceNode, strategy);
		is_selected = new HashMap<>();
		initializeKnownNeighborsSet();
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
