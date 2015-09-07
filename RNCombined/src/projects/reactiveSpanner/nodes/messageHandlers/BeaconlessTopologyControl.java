package projects.reactiveSpanner.nodes.messageHandlers;

import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;

public abstract class BeaconlessTopologyControl extends AbstractTopologyControl
{			
	/**
	 * (Standard) transmission radius of network model
	 */
	public static double R = -1;

	static{
		try
		{
			R = Configuration.getDoubleParameter("UDG/rMax");
		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}

	protected BeaconlessMessageHandler forwarderMsgHandler;
	
	protected BeaconlessTopologyControl(EStrategy usedStrategy, PhysicalGraphNode sourceNode)
	{
		super(usedStrategy, sourceNode);
	}
	
	/**
	 * This method is to inform this strategy for termination of the forwarders message handler. 
	 * <b>This method has to be called only by the forwarders message handler!</b>
	 */
	//TODO Find better solution
	public void notifyTermination()
	{
		if(forwarderMsgHandler.hasTerminated())
			terminate();
		else
			throw new RuntimeException("Subgraph strategy controller of subgraph " + this.getTopologyControlID() + " was informed about termination of the forwarder, "
					+ "but forwarder has not terminated. This case should not occur. The forwarder node has to terminate before notifying"
					+ "its controller.");
	}
}
