package projects.reactiveSpanner.nodes.messageHandlers;

import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import projects.reactiveSpanner.record.MessageRecord;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

/**
 * Abstract super class for all message handler. A message handler is used as part of an topology control algorithm (subgraph strategy)
 * and its task is to manage events that occur in a specific topology control event. It takes over the complete responsibilities of the
 * holding node for this task. A message handler will be hold by a single node and will be generated by runtime when specific 
 * events occur, for example, an RTS message was received by the holding node, then this node will generate a message handler for the 
 * unique ID of this event and this message handler will control the behavior of the node in respect to this event.
 *
 * @param <T> The node type of known neighbors of the holding node
 * 
 * @author Mavs
 */
public abstract class AbstractMessageHandler<T extends SimpleNode> {

	protected static Logging logger = Logging.getLogger();
	/**
	 * ID of topology control this message handler is assigned to
	 */
	public final UUID tcID;
	/**
	 * node that holds this message handler
	 */
	public final PhysicalGraphNode node;
	/**
	 * node that is forwarder (executor of the local event) for this node
	 */
	protected final PhysicalGraphNode sourceNode;
	/**
	 * all known neighbors of this node respectively to the used topology control (excluding the forwarder node)
	 */
	protected Set<T> knownNeighbors = null;
	/**
	 * Used strategy for creating subgraph
	 */
	public final EStrategy planarSubgraphCreationStrategy;
	
	/**
	 * distance to forwarder
	 */
	protected final double d;

	/**
	 * The constructor of the handler initializes all currently available informations. Generally it will be called if a node is selected as forwarder, or if a neighbor of a node receives an RTS message of a forwarder. If this message handler is called by a forwarder the <code>ownerNode == forwarderNode</code>
	 * 
	 * @param tcID
	 *            the ID this handler is assigned to
	 * @param ownerNode
	 *            node that holds this handler
	 * @param forwarderNode
	 *            source node that want to gain its neighborhood
	 * @param strategy
	 *            used strategy to gain the neighborhood (e.g. rPDT, BFP)
	 */
	protected AbstractMessageHandler(final UUID tcID, final PhysicalGraphNode ownerNode, final PhysicalGraphNode sourceNode, final EStrategy strategy)
	{	
		this.tcID = tcID;
		this.node = ownerNode;
		this.sourceNode = sourceNode; // setting forwarder and set d to a value
		this.d = this.node.getPosition().distanceTo(sourceNode.getPosition());
		
		this.planarSubgraphCreationStrategy = strategy;
		initializeKnownNeighborsSet();
	}

	public boolean isForwardersMessageHandler() {
		return this.node.equals(this.sourceNode);
	}

	
	public Set<T> getKnownNeighbors() {
		return knownNeighbors;
	}

	public void initializeKnownNeighborsSet() {
		if (knownNeighbors != null) {
			logger.logln(LogL.WARNING, this.node.toString() + " has already initialized its set of knownNeighbors for topology control ID " + this.tcID.toString() + ". Clearing set");
			knownNeighbors.clear();
		} else {
			// logger.logln(LogL.INFO, this.toString() + " created knownNeighbors Set");
			knownNeighbors = new HashSet<T>();
		}

	}

	/**
	 * ##################### Abstract methods ######################### ################################################################
	 */
	/**
	 * General method for received messages. Here should be determined of which type this message is for further actions
	 * 
	 * @param msg received message
	 */
	public abstract void receivedMessage(final AbstractMessage msg);

	/**
	 * Draw something special
	 */
	public abstract void drawNode(Graphics g, PositionTransformation pt);

	/**
	 * Returning a record of all currently sent messages of UUID <code>tcID</code>
	 */
	public abstract MessageRecord getCurrentMessageRecord();

}
