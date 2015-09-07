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
 * informs a node about a witness node. A witness is
 * a known node which is no neighbour. It is used for improved
 * extraction
 * @author cyron
 *
 */
public class NewWitness extends AbstractMessage{
	/**
	 * count all messages individual for every subgraph creation ID
	 */
	private static Map<UUID, Counter> counterMap = new HashMap<UUID, Counter>();
	private SimpleNode newWitness;

	/**
	 * 
	 * @param newWitness the witness node the target node has to
	 * be informed about.
	 */
	public NewWitness(UUID tcID, PhysicalGraphNode transmitter,
			EStrategy strategy, SimpleNode newWitness) {
		super(tcID, transmitter, strategy);

		this.newWitness=newWitness;
		
		if(counterMap.containsKey(ID))
		{
			counterMap.get(ID).incValue();
		} else{
			Counter c = new Counter(1);
			counterMap.put(ID, c);
		}
	}
	
	/**
	 * 
	 * @return the witness node
	 */
	public SimpleNode getNewWitness(){
		return this.newWitness;
	}

	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return new NewWitness(this.ID, this.node, this.strategy, this.newWitness);
	}
	
	/**
	 * @return number of all sent messages of this type
	 */
	public static int numberOfSentMessages(final UUID tcID) {
		if(counterMap.containsKey(tcID))
			return counterMap.get(tcID).value();
		else 
		{
			logger.logln(LogL.WARNING, "UUID " + tcID + " does not exists in NewWitness pool!");
			return 0;
		}
	}

}
