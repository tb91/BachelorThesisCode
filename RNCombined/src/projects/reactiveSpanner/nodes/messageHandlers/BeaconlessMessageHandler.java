package projects.reactiveSpanner.nodes.messageHandlers;

import java.awt.Color;
import java.util.UUID;

import projects.defaultProject.nodes.timers.MessageTimer;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messages.CTS;
import projects.reactiveSpanner.nodes.messages.RTS;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.timers.AbstractBeaconlessTimer;
import sinalgo.tools.logging.LogL;

/**
 * Extended message handler for local beaconless topology controls based on RTS/CTS principle
 * 
 * @author Mavs 
 */
public abstract class BeaconlessMessageHandler extends AbstractMessageHandler<PhysicalGraphNode>
{
	protected BeaconlessMessageHandler(UUID tcID, PhysicalGraphNode ownerNode, PhysicalGraphNode sourceNode, EStrategy strategy) {
		super(tcID, ownerNode, sourceNode, strategy);

		this.contentionTimer = null;
		this.hasSentCTSMessage = false;
	}

	/**
	 * Starting the algorithm by creating and sending a immediately RTS broadcast.
	 * 
	 */
	public void broadcastRTS(final AbstractBeaconlessTimer contentionTimer) {
		
		logger.logln(LogL.INFO, "SourceNode " + this.sourceNode.toString() + " broadcasts RTS to topology control ID " + this.tcID.toString());
		broadcastTimer = new MessageTimer(new RTS(this.tcID, this.sourceNode, this.planarSubgraphCreationStrategy)); // Synchronized mode do
																													// not allow direct
																													// broadcast
		broadcastTimer.startRelative(1, sourceNode);
		this.contentionTimer = contentionTimer; // starting timer for the
												// duration of the phase itself
	}
	
	/**
	 * Broadcasting a CTS message to all neighbors in connection range
	 */
	public void broadcastCTS(){
		// logger.logln(LogL.INFO, this.node.toString() + " broadcasts CTS to topology control ID " + this.tcID.toString());
		MessageTimer broadcastCTSTimer = new MessageTimer(new CTS(this.tcID, node, this.planarSubgraphCreationStrategy));
		broadcastCTSTimer.startRelative(1, node); // Synchronized mode does not allow direct broadcast
		
		//this.node.setColor(Color.DARK_GRAY);
		hasSentCTSMessage = true;
	}
	
	/**
	 * flag, that supposes that this node has sent a CTS message in the selection phase and was not set
	 */
	protected boolean hasSentCTSMessage;

	/**
	 * contentionTimer owned by this node to specify, when it has to send a CTS message
	 */
	protected AbstractBeaconlessTimer contentionTimer;
	

	/**
	 * Reaction on firing of the timer holding node
	 */
	public abstract void timerTriggerEvent();

	/**
	 * msgTimer owned by this node to specify, when it has to send a broadcast message (in general used for direct broadcast)
	 */
	public MessageTimer broadcastTimer;
	
	/**
	 * @return true, if the calculation of the beaconless calculation has been finished, otherwise false
	 */
	public abstract boolean hasTerminated();
	
	/**
	 * This method is called whenever a RTS message is received
	 * 
	 * @param sender
	 *            of the RTS message
	 */
	protected abstract void receivedRTS(final RTS msg);

	/**
	 * This method is called when this node received a CTS message of another node,
	 * 
	 * @param sender
	 *            of the CTS message
	 */
	protected abstract void receivedCTS(final CTS msg);
}
