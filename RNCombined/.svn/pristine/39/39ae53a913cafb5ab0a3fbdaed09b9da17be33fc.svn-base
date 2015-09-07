package projects.reactiveSpanner.nodes.messages;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.Logging;


public abstract class AbstractMessage extends Message {
	static Logging logger = Logging.getLogger();
	/**
	 * The specific topology control / routing ID this message is assigned to.
	 */
	protected final UUID ID;
	/**
	 * The node that sent the Message
	 */
	protected final PhysicalGraphNode node;
	/**
	 * The used Strategy for sending this message. Important to know for the message handler for reacting on this message 
	 */
	protected final EStrategy strategy;
	
	
	protected AbstractMessage(final UUID ID, final PhysicalGraphNode transmitter, final EStrategy strategy)
	{
		this.ID = ID;
		this.node = transmitter;
		this.strategy = strategy;
	}
	
	/**
	 * @return node that has sent this message
	 */
	public PhysicalGraphNode getTransmitter() {
		return this.node;
	}
	/**
	 * @return used strategy this message invokes
	 */
	public EStrategy getStrategy() {
		return this.strategy;
	}
	/**
	 * @return the topology control / routing operations ID this message belongs to
	 */
	public final UUID getID()
	{
		return this.ID;
	}  
	}
