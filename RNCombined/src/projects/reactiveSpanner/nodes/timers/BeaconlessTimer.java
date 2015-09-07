package projects.reactiveSpanner.nodes.timers;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessMessageHandler;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.Node;

public class BeaconlessTimer extends AbstractBeaconlessTimer
{
	/**
	 * Constructor for a neighbor of the executing node of the topology control. Time is in general relatively setted
	 * to the distance to the source node. The timer starts directly
	 * @param tcID the routing ID this timer is assigned to
	 * @param relativeTimeout The timer will go off after a certain time relative to the current time
	 * @param n The node that started the timer and the one the timer will be fired.
	 */
	public BeaconlessTimer(final UUID tcID, final double relativeTimeout, final Node n)
	{
		super(tcID);
		this.startRelative(relativeTimeout, n);
	}
	
	@Override
	public void fire()
	{
		if(isQuiet)
			return;
		
		if(this.node instanceof PhysicalGraphNode)
		{
			PhysicalGraphNode bNode = (PhysicalGraphNode) node;
			
			
			if(bNode.getMessageHandler(tcID) instanceof BeaconlessMessageHandler)
			{
				BeaconlessMessageHandler msgH = (BeaconlessMessageHandler) bNode.getMessageHandler(tcID);
				msgH.timerTriggerEvent();
			}
		}
		else throw new RuntimeException("Timer cannot fire on non BeaconlessNode!");
	}
}
