package projects.reactiveSpanner.nodes.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import projects.reactiveSpanner.Counter;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.LogL;

/**
 * Informs a node about a new neighbour node. The
 * source of the message then becomes the hop node for
 * the virtual connection (if necessary).
 * @author cyron
 *
 */
public class NewNeighbour extends AbstractMessage{
	/**
	 * count all messages individual for every subgraph creation ID
	 */
	private static Map<UUID, Counter> counterMap = new HashMap<UUID, Counter>();
	private SimpleNode newNeighbour;

	/**
	 * 
	 * @param newNeighbour the new neighbour the destination of
	 * this message has to be informed about
	 */
	public NewNeighbour(UUID tcID, PhysicalGraphNode transmitter,
			EStrategy strategy, SimpleNode newNeighbour) {
		super(tcID, transmitter, strategy);
		this.newNeighbour=newNeighbour;
		
		if(counterMap.containsKey(ID))
		{
			counterMap.get(ID).incValue();
		} else{
			Counter c = new Counter(1);
			counterMap.put(ID, c);
		}
	}
	
	/**
	 * @return the new neighbour node
	 */
	public SimpleNode getNewNeighbour(){
		return this.newNeighbour;
	}

	@Override
	public Message clone() {
		return new NewNeighbour(this.ID, this.node, this.strategy, this.newNeighbour);
	}
	
	/**
	 * @return number of all sent messages of this type
	 */
	public static int numberOfSentMessages(final UUID tcID) {
		if(counterMap.containsKey(tcID))
			return counterMap.get(tcID).value();
		else 
		{
			logger.logln(LogL.WARNING, "UUID " + tcID + " does not exists in NewNeighbour pool!");
			return 0;
		}
	}

}
