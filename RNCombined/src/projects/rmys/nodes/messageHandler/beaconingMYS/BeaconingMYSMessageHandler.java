package projects.rmys.nodes.messageHandler.beaconingMYS;

import java.awt.Graphics;
import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.messages.CTS;
import projects.reactiveSpanner.nodes.messages.RTS;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.record.MessageRecord;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;
import sinalgo.gui.transformation.PositionTransformation;

public class BeaconingMYSMessageHandler extends BeaconMessageHandler<NewPhysicalGraphNode> {

	protected BeaconingMYSMessageHandler(UUID tcID, PhysicalGraphNode ownerNode, PhysicalGraphNode sourceNode,
			EStrategy strategy) {
		super(tcID, ownerNode, sourceNode, strategy);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void beaconTimerFire() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receivedMessage(AbstractMessage msg) {
		if (msg instanceof RTS){
			receivedRTS((RTS)msg);
		}else if (msg instanceof CTS){
			receivedCTS((CTS)msg);
		}else{
			System.err.println("ignored a message: " + msg.toString());
		}
		
	}
	
	public void receivedRTS(RTS rts){
		
	}
	public void receivedCTS(CTS cts){
		//sourceNode.messageHandlerMap.get(this.tcID).getKnownNeighbors().add(cts.)
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
