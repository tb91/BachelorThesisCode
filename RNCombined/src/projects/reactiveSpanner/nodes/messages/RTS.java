package projects.reactiveSpanner.nodes.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import projects.reactiveSpanner.Counter;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.reactivePDT.ReactivePDT;
import projects.reactiveSpanner.nodes.messageHandlers.BFP.BFP;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;

/**
 * @author Mavs
 *	'Request to Send' message which the forwarder has to send
 */
public class RTS extends BeaconlessMessage implements Request
{
	/**
	 * count all messages individual for every subgraph creation ID (for record)
	 */
	private static Map<UUID, Counter> counterMap = new HashMap<UUID, Counter>();
	
	private final double t_max;
		
	@Override
	public RTS clone() {
		return this;
	}
	
	/**
	 * Constructor for RTS message. Called by a node if it wants responds of its neighborhood according to the used subgraph strategy
	 * @param ID	unique ID of topology control process
	 * @param transmitter	source of this message
	 * @param t_max	maximum (relative) timeout for respond of neighbors
	 * @param strategy	the used beaconless subgraph strategy that neighbors should use for responding
	 */
	public RTS(final UUID ID, final PhysicalGraphNode transmitter, final EStrategy strategy)
	{
		super(ID, transmitter, strategy);
		switch(strategy)
		{
		case BFP:
			this.t_max = BFP.getMaximumTimeout();
			break;
		case REACTIVE_PDT:
			this.t_max = ReactivePDT.getMaximumTimeout();
			break;
		default:
			throw new RuntimeException("RTS message is currently only supported by BFP and reactive PDT algorithm.");
		
		}
		if(counterMap.containsKey(ID))
		{
			counterMap.get(ID).incValue();
		} else{
			Counter c = new Counter(1);
			counterMap.put(ID, c);
		}
	}

	/**
	 * Get the maximum timeout receiver nodes should set for their respond
	 * @return maximum timeout
	 */
	public final double getTimeout()
	{
		return t_max;
	}
	
	/**
	 * @return number of all sent messages of this type
	 */
	public static int numberOfSentMessages(final UUID tcID) {
		if(counterMap.containsKey(tcID))
			return counterMap.get(tcID).value();
		else throw new RuntimeException("UUID " + tcID + " does not exists in CTS counterMap pool");
	}
}
