package projects.reactiveSpanner.nodes.messageHandlers.reactivePDT;

import java.awt.Graphics;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.timers.BeaconlessTimer;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.tools.logging.LogL;

/**
 * Class for Reactive Partial Delaunay Partition calculation
 * 
 * @author Matthias von Steimker
 * @see Reactive planar spanner construction in wireless ad hoc and sensor networks by M. Benter, F. Neumann, and H. Frey (2013)
 */
public class ReactivePDT extends BeaconlessTopologyControl
{
	/**
	 * maximum delay of a process cycle respectively to the value in the configuration
	 */
	private static double t_max = -1;
	
	static
	{	
		try
		{
			t_max = Configuration.getDoubleParameter("ReactivePDT/MaximumTimeout");
		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}
	public ReactivePDT(PhysicalGraphNode sourceNode) {
		super(BeaconlessTopologyControl.EStrategy.REACTIVE_PDT, sourceNode);
		_init();
//		start();
	}
	
	/**
	 * @return maximum relative timeout (without consideration of already elapsed time)
	 */
	public static double getMaximumTimeout()
	{
		return t_max;
	}

	@Override
	protected void _init(){
		forwarderMsgHandler = new ReactivePDTForwarderMessageHandler(this, sourceNode);	
		if (sourceNode.messageHandlerMap.containsKey(this.getTopologyControlID())) {
			logger.logln(LogL.WARNING, "Putting another/ repeatedly a message handler with ID " + this.getTopologyControlID().toString() + " to the messageHandlerMap of " + this.toString());
		}
		sourceNode.messageHandlerMap.put(this.getTopologyControlID(), forwarderMsgHandler); //wringing of messageHandler <-> forwarder
		
		//test if the used connectivity model is UDG
		String usedCModel = forwarderMsgHandler.node.getConnectivityModel().getClass().getName();
		usedCModel = usedCModel.substring(usedCModel.lastIndexOf('.') + 1);
		if(!usedCModel.equals("UDG"))
		{
			throw new RuntimeException("Used Connectivity model is " + usedCModel + ", but Reactive PDT is only supported under the UDG model!");
		}
	}
	
	@Override
	protected void _start() {
		//we add 1 to timeout, so that the forwarder would receive a CTS from nodes that have the distance of exactly UDG_R 
		//to the forwarder right before terminating the calculation
		final int epsilon = 5;
		final double timeout = getMaximumTimeout() + epsilon;
		forwarderMsgHandler.broadcastRTS(new BeaconlessTimer(forwarderMsgHandler.tcID, timeout, forwarderMsgHandler.node));
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt) {
		// TODO Auto-generated method stub
		
	}
}
