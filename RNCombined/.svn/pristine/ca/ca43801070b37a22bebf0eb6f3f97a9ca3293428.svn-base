package projects.reactiveSpanner.nodes.messageHandlers;

import java.util.UUID;

import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;



public abstract class BeaconTopologyControl extends AbstractTopologyControl
{
	/**
	 * Minimum transmission radius of network model QUDG
	 */
	public static double qr = -1;
	/**
	 * Maximum transmission radius of network model QUDG
	 */
	public static double qR = -1;
	
	static{
		try
		{
			qR = Configuration.getDoubleParameter("QUDG/rMax");
			qr = Configuration.getDoubleParameter("QUDG/rMin");
		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}
	
	
	public BeaconTopologyControl(EStrategy usedStrategy, PhysicalGraphNode sourceNode) {
		super(usedStrategy, sourceNode);
	}
	
	
	
	public void topologyTimerFire(){
		
	}

}
