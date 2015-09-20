package projects.rmys.nodes.messageHandler;

import java.awt.Color;
import java.awt.Graphics;
import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.rmys.nodes.messages.AcknowlegdementMessage;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Position;

public class RMYSForwarderMessageHandler extends RMYSMessageHandler {
	SubgraphStrategy pdt;
	protected RMYSForwarderMessageHandler(UUID tcID, PhysicalGraphNode sourceNode, SubgraphStrategy neighborhood) {
		super(tcID, sourceNode, sourceNode);
		this.pdt = neighborhood;
	}



	@Override
	public void drawNode(Graphics g, PositionTransformation pt) {

		for (int i = 0; i < RMYS.k; i++) {
			double angle = i * RMYS.cone_size;
			double xpos = RMYS.unit_radius * Math.cos(angle);
			double ypos = RMYS.unit_radius * Math.sin(angle);
			Position helppos = new Position(sourceNode.getPosition().xCoord + xpos,
					sourceNode.getPosition().yCoord + ypos, 0);
			g.setColor(Color.black);
			pt.drawLine(g, sourceNode.getPosition(), helppos);
		}

	}

	@Override
	public void receivedMessage(AbstractMessage msg) {
		if (msg instanceof AcknowlegdementMessage) {
			if (((AcknowlegdementMessage) msg).getAccepted()) {
				// no need to work; everything went fine
			} else {
				// remove this node from the set, since the other node did not accept this edge
				if (!this.knownNeighbors.remove(msg.getTransmitter())) {
					System.err.println(this.sourceNode
							+ " tried to remove a node which was not in it's knownNeighbors-set.\nThis should not occur!");
				}

				System.err.println(
						this.sourceNode + " removed " + msg.getTransmitter() + " from it's list of knownNeighbors");
			}
		}
	}


}
