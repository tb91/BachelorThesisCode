package projects.reactiveSpanner.nodes.messageHandlers.BFP;

import java.awt.Graphics;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.timers.BeaconlessTimer;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.tools.logging.LogL;

/**
 * Class for Beaconless Forwarder Planarization algorithm
 * 
 * @author Matthias von Steimker
 * @see Message-Efficient Beaconless Georouting With Guarenteed Delivery in Wireless Sensor,
 * Ad hoc, and Actuator Networks by S. R�hrup, I. Stojmenovic (2009)
 */
public class BFP extends BeaconlessTopologyControl
{
	public static enum Phase{SELECTION, PROTEST};
	
	/**
	 * maximum delay of a process cycle respectively to the value in the configuration
	 */
	private static double t_max = -1;
	
	static
	{	
		try
		{
			t_max = Configuration.getDoubleParameter("BFP/MaximumTimeout");
		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}
	
//	// all relevant bfp nodes
//	public static Set<BeaconlessNode> relevantBFPNodes = new HashSet<BeaconlessNode>();
	
	
//	// all nodes that are within the proximity region of the current forwarderNode (currently not used)
//	private static Set<BFPNode> eligibleCandidates = new HashSet<BFPNode>();

	public BFP(PhysicalGraphNode sourceNode) {
		super(BeaconlessTopologyControl.EStrategy.BFP, sourceNode);
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
	protected void _init()
	{
		forwarderMsgHandler = new BFPForwarderMessageHandler(this, sourceNode);
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
	protected void _start()
	{
		//we add epsilon to timeout, so that the forwarder would receive a CTS from nodes that have the distance of exactly UDG_R 
		//to the forwarder right before terminating the calculation
		final int epsilon = 5;
		final double timeout = getMaximumTimeout() +epsilon;
		forwarderMsgHandler.broadcastRTS(new BeaconlessTimer(forwarderMsgHandler.tcID, timeout, forwarderMsgHandler.node));
	}
	
//	public BFP.Phase getCurrentPhase()
//	{
//		return forwarderMsgHandler.currentPhase;
//	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt) {
		// TODO Auto-generated method stub
		
	}
}
