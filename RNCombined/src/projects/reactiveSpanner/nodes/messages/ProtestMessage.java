package projects.reactiveSpanner.nodes.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import projects.reactiveSpanner.Counter;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class ProtestMessage extends BeaconlessMessage
{		
	/**
	 * count all messages individual for every subgraph creation ID
	 */
	private static Map<UUID, Counter> counterMap = new HashMap<UUID, Counter>();
	
	private Set<Node> violatingNodes;
	
	public ProtestMessage(final UUID ID, final PhysicalGraphNode transmitter, final Set<Node> violatingNodes)
	{
		super(ID, transmitter, BeaconlessTopologyControl.EStrategy.BFP);
		this.violatingNodes = violatingNodes;
		if(counterMap.containsKey(ID))
		{
			counterMap.get(ID).incValue();
		} else{
			Counter c = new Counter(1);
			counterMap.put(ID, c);
		}
	}
	
	@Override
	public Message clone() {
		return this;
	}
	
	public Set<Node> getViolatingNodes()
	{
		return this.violatingNodes;
	}
	
	/**
	 * @return number of all sent messages of this type
	 */
	public static int numberOfSentMessages(final UUID tcID) {
		if(counterMap.containsKey(tcID))
			return counterMap.get(tcID).value();
		else return 0;
	}
}
