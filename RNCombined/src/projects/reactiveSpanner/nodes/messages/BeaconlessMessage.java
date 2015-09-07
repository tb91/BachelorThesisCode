package projects.reactiveSpanner.nodes.messages;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;

/**
 * Abstract class for all messages with a known node as origin of this message.
 */
public abstract class BeaconlessMessage extends AbstractMessage
{

	protected BeaconlessMessage(UUID tcID, PhysicalGraphNode transmitter, EStrategy strategy) {
		super(tcID, transmitter, strategy);
		
	}
	
}