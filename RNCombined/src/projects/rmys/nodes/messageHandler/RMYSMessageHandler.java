package projects.rmys.nodes.messageHandler;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.UUID;

import projects.reactiveSpanner.TopologyControlObserver;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EState;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.reactivePDT.ReactivePDT;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.messages.CTS;
import projects.reactiveSpanner.nodes.messages.RTS;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.record.MessageRecord;
import projects.rmys.Algorithms_ext;
import projects.rmys.nodes.messages.ProtestMessage;
import projects.rmys.nodes.messages.RequestMessage;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;
import sinalgo.gui.transformation.PositionTransformation;

public class RMYSMessageHandler extends BeaconlessMessageHandler {

	/**
	 * indicates that this was selected from another node
	 */
	HashMap<NewPhysicalGraphNode, Boolean> is_selected;


	public RMYSMessageHandler(UUID tcID, PhysicalGraphNode ownerNode, PhysicalGraphNode sourceNode) {
		super(tcID, ownerNode, sourceNode, EStrategy.RMYS);
		is_selected = new HashMap<>();
		
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
		if (msg instanceof RequestMessage) {
			if (((RequestMessage) msg).candidates.contains(this.node)) {
				this.node.setColor(Color.CYAN);
				final ReactivePDT pdt = new ReactivePDT(this.node);

				pdt.addObserver(new TopologyControlObserver() {

					@Override
					public void onNotify(SubgraphStrategy topologyControl, EState event) {
						if (pdt.hasTerminated()) {
							runRMYS(pdt);
						}

					}
				});

				pdt.start();
			}
		} else {
			System.err.println(this.node.toString() + " got an unkown message: " + msg.toString());
		}


	}

	private void runRMYS(ReactivePDT pdt) {
		this.getKnownNeighbors().addAll(RMYS.calculateMYS((NewPhysicalGraphNode) this.node, 
				this.node.getMessageHandler(pdt.getTopologyControlID()).getKnownNeighbors())); // this part can be optimized
		if (this.getKnownNeighbors().contains(sourceNode)) {
			this.node.setColor(Color.green);
		} else {
			this.node.setColor(Color.red);
			ProtestMessage protest = new ProtestMessage(this.tcID, this.node);
			this.node.send(protest, sourceNode); // send answer to forwarder
			Algorithms_ext.incMessageNumber(EStrategy.RMYS);
		}
		

	}

	@Override
	public MessageRecord getCurrentMessageRecord() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void drawNode(Graphics g, PositionTransformation pt) {


	}



}
