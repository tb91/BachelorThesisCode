package projects.reactiveSpanner.nodes.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import projects.reactiveSpanner.Counter;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.tools.logging.LogL;

/**
 * @author Mavs
 * Clear to Send message
 */
public class CTS extends BeaconlessMessage
{
	/**
	 * count all messages individual for every subgraph creation ID
	 */
	private static Map<UUID, Counter> counterMap = new HashMap<UUID, Counter>();
	
	@Override
	public CTS clone() {
		return this;
	}
	public CTS(final UUID ID, final PhysicalGraphNode transmitter, final BeaconlessTopologyControl.EStrategy strategy)
	{
		super(ID, transmitter, strategy);
		if(counterMap.containsKey(ID))
		{
			counterMap.get(ID).incValue();
		} else{
			Counter c = new Counter(1);
			counterMap.put(ID, c);
		}	
	}
	
	/**
	 * @return number of all sent messages of this type
	 */
	public static int numberOfSentMessages(final UUID tcID) {
		if(counterMap.containsKey(tcID))
			return counterMap.get(tcID).value();
		else 
		{
			logger.logln(LogL.WARNING, "UUID " + tcID + " does not exists in CTS counterMap pool and therefor no CTS message was sent. Is this right?");
			return 0;
		}
	}
}
