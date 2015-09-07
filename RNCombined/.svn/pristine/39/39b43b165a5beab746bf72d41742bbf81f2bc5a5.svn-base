package projects.reactiveSpanner.nodes.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import projects.defaultProject.nodes.timers.MessageTimer;
import projects.reactiveSpanner.Counter;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.LogL;

/**
 * This is a virtual message used for sending over virtual connections. 
 * A virtual Message contains the original message and will be forwarded
 * to the hop node. The hop node can then use the original message to 
 * forward it to the correct destination, not changing the original source.
 * 
 * There can be virtual messages inside virtual messages. (recursive virtual
 * connections).
 * @author cyron
 *
 */
public class VirtualMessage extends AbstractMessage{
	/**
	 * count all messages individual for every subgraph creation ID
	 */
	private static Map<UUID, Counter> counterMap = new HashMap<UUID, Counter>();
	/**
	 * original message
	 */
	private MessageTimer originalMsg;

	/**
	 * 
	 * @param originalMsg the original message to be sent on
	 * the virtual connection
	 */
	public VirtualMessage(UUID tcID, PhysicalGraphNode transmitter,
			EStrategy strategy, MessageTimer originalMsg) {
		super(tcID, transmitter, strategy);
		
		this.originalMsg=originalMsg;
		
		if(counterMap.containsKey(ID))
		{
			counterMap.get(ID).incValue();
		} else{
			Counter c = new Counter(1);
			counterMap.put(ID, c);
		}
	}
	
	/**
	 * getter for the hop node to get the original message
	 * @return the original message
	 */
	public MessageTimer getOriginalMessageTimer(){
		return this.originalMsg;
	}

	@Override
	public Message clone() {
		return new VirtualMessage(this.ID, this.node, this.strategy, this.originalMsg);
	}
	
	/**
	 * @return number of all sent messages of this type
	 */
	public static int numberOfSentMessages(final UUID tcID) {
		if(counterMap.containsKey(tcID))
			return counterMap.get(tcID).value();
		else 
		{
			logger.logln(LogL.WARNING, "UUID " + tcID + " does not exists in VirtualMessage pool!");
			return 0;
		}
	}

}
