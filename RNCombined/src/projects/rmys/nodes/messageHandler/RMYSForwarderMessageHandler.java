package projects.rmys.nodes.messageHandler;

import java.awt.Color;
import java.awt.Graphics;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.timers.BeaconlessTimer;
import projects.rmys.Algorithms_ext;
import projects.rmys.nodes.messages.ProtestMessage;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Position;

public class RMYSForwarderMessageHandler extends RMYSMessageHandler {
	
	private static Logger logger = Algorithms_ext.getLogger();
	
	SubgraphStrategy pdt;
	boolean hasTerminated=false;
	BeaconlessTopologyControl subgraphControl;
	
	protected RMYSForwarderMessageHandler(BeaconlessTopologyControl subgraphControl,UUID tcID, PhysicalGraphNode sourceNode, SubgraphStrategy neighborhood) {
		super(tcID, sourceNode, sourceNode);
		this.pdt = neighborhood;
		this.node.setColor(Color.red);
		this.subgraphControl=subgraphControl;
		
		new BeaconlessTimer(this.tcID, 4012, sourceNode);
				
	}

	@Override
	public void timerTriggerEvent() {
		// logger.logln(LogL.INFO, this.sourceNode.toString() + " has terminated it's reactive calculation of it's PDT neighborhood");
		hasTerminated = true;
		subgraphControl.notifyTermination();
	}
	
	@Override
	public boolean hasTerminated(){
		return hasTerminated;
	}

	@Override
	public void drawNode(Graphics g, PositionTransformation pt) {

		for (int i = 0; i < RMYS.k; i++) {
			double angle = i * RMYS.cone_size;
			double xpos = RMYS.unit_radius * Math.cos(angle);
			double ypos = RMYS.unit_radius * Math.sin(angle);
			Position helppos = new Position(sourceNode.getPosition().xCoord + xpos,
					sourceNode.getPosition().yCoord + ypos, 0);
			pt.drawLine(g, sourceNode.getPosition(), helppos);
		}

	}
	

	@Override
	public void receivedMessage(AbstractMessage msg) {
		if (msg instanceof ProtestMessage) {

			// remove this node from the set, since the other node did not
			// accept this edge
			if (!this.knownNeighbors.remove(msg.getTransmitter())) {
				logger.log(Level.SEVERE, this.sourceNode
						+ " tried to remove a node which was not in it's knownNeighbors-set.\nThis should not occur!");
			}
			logger.log(Level.INFO,
					this.sourceNode + " removed " + msg.getTransmitter() + " from it's list of knownNeighbors");

		}
	}


}
