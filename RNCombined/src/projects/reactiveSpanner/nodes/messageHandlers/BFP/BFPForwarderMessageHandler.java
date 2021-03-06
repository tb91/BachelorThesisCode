package projects.reactiveSpanner.nodes.messageHandlers.BFP;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Set;
import projects.defaultProject.nodes.timers.MessageTimer;
import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.CustomGlobal;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messages.CTS;
import projects.reactiveSpanner.nodes.messages.ProtestMessage;
import projects.reactiveSpanner.nodes.messages.ProtestPhaseOverMessage;
import projects.reactiveSpanner.nodes.messages.RTS;
import projects.reactiveSpanner.nodes.messages.SelectionPhaseOverMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.timers.BeaconlessTimer;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;

public class BFPForwarderMessageHandler extends BFPMessageHandler
{
	private boolean hasTerminatedSelectionPhase;
	private boolean hasTerminatedProtestPhase;
	
	private BeaconlessTopologyControl subgraphControl;	//TODO If possible, change this
	
	public BFPForwarderMessageHandler(final BeaconlessTopologyControl subgraphControl, final PhysicalGraphNode ownerNode) {
		super(subgraphControl.getTopologyControlID(), ownerNode, ownerNode);
		hasTerminatedSelectionPhase = false;
		hasTerminatedProtestPhase = false;
		this.subgraphControl = subgraphControl;
		
		this.sourceNode.setColor(Color.RED);
		if(Tools.isSimulationInGuiMode())
		{
			Tools.getGUI().redrawGUI();	
		}	
	}
	
	@Override
	protected void receivedRTS(final RTS rts)
	{
		String warningMsg = "Warning: " + this.node.toString() + " received RTS as forwarder node.";
		logger.logln(LogL.WARNING, warningMsg);
	}
	
	@Override
	protected void receivedCTS(final CTS cts)
	{
		if(this.knownNeighbors == null)
			throw new RuntimeException("knownNeighbors Set of " + this.node.toString() + "and for topology control ID "+ tcID.toString() + " was not initialized!");
		
		if(hasTerminatedSelectionPhase)
		{
			logger.logln(LogL.WARNING, "BFPForwarderNode " + this.sourceNode.toString() + " received CTS of " + cts.getTransmitter().toString() + " but has already terminated selection phase.");
			return;
		}
		knownNeighbors.add(cts.getTransmitter());
	}
	
	@Override
	public void timerTriggerEvent()
	{
		switch(currentPhase)
		{
		case SELECTION:
			endSelection();
			break;
		case PROTEST:
			endProtest();
			break;
		default:
			throw new RuntimeException("Another phase than 'Protest' or 'Selection' is selected. This case is not intended.");
		}
	}
	/**
	 * Will be called by BFP controller after the contentionTimer has expired. Afterwards the protest phase
	 * will be started by sending a SelectionPhaseOverMessage and the contention timer will be started once again.
	 */
	public void endSelection()
	{
		logger.logln(LogL.INFO, "Forwarder " + this.sourceNode.toString() + " terminates selection phase");
		this.currentPhase = BFP.Phase.PROTEST;
		hasTerminatedSelectionPhase = true;
		//direct broadcast is not nicely seen in synchronous mode, so we create first a timer and fire directly 
		//in the next round
		MessageTimer selectionPhaseOverTimer = new MessageTimer(new SelectionPhaseOverMessage(this.tcID, this.sourceNode));
		selectionPhaseOverTimer.startRelative(1, this.sourceNode);
		contentionTimer = new BeaconlessTimer(this.tcID, BFP.getMaximumTimeout(), this.node);
	}
	
	/**
	 * Have to be called by the BFP controller after the contentionTimer of the forwarder has expired.
	 * End of protest phase will be initiated. Sending of a message so signal the end of protest. 
	 * Connecting to all nodes that fulfilled the gabriel circle condition
	 */
	public void endProtest()
	{
		logger.logln(LogL.INFO, "Forwarder " + this.sourceNode.toString() + " broadcasts ProtestPhaseOverMessage");
		hasTerminatedProtestPhase = true;
		//direct broadcast is not nicely seen in synchronous mode, so we create a timer firstly and fire direct 
		//in the next round
		MessageTimer protestPhaseOverTimer = new MessageTimer(new ProtestPhaseOverMessage(this.tcID, this.sourceNode));
		protestPhaseOverTimer.startRelative(1, this.node);
		logger.logln(LogL.INFO, this.sourceNode.toString() + " has terminated with the BFP");
		sourceNode.connect(knownNeighbors, Color.RED);
		this.subgraphControl.notifyTermination();
	}
	
	/**
	 * This method is called when this forwarder received a protest message of a BFPNode. Then it removes all
	 * violating nodes of its set of connecting nodes
	 * @param transmitter Node which has sent the overheard protest
	 * @param violatingNodes all violating nodes that the sender has detected
	 */
	@Override
	protected void overhearProtest(final ProtestMessage protestMsg)
	{
		final Node transmitter = protestMsg.getTransmitter();
		final Set<Node> overheardViolatingNodes = protestMsg.getViolatingNodes();
		
		if(hasTerminatedProtestPhase)
		{
			String errorMsg = "BFPForwarderNode " + this.sourceNode.toString() + " received Protest msg of " + transmitter.toString() + " but has already terminated protest phase.";
			logger.logln(LogL.ERROR_DETAIL, errorMsg);
			throw new RuntimeException(errorMsg);
		}
		
		if(transmitter instanceof PhysicalGraphNode)
		{
			for(Node v: overheardViolatingNodes)
			{
				knownNeighbors.remove(v);
			}
		} else {
			throw new RuntimeException("Overhear protest message of a non-BFPNode.");
		}
	}
	
	@Override
	public void broadcastCTS(){throw new RuntimeException("The case that a BFP forwarder broadcasts a CTS is not provided!");}	//bad design
	@Override
	public void protest(){throw new RuntimeException("The case that a BFP forwarder protests is not provided!");}	//bad design
	
	@Override
	public boolean hasTerminated()
	{
		return hasTerminatedProtestPhase;
	}
	
	@Override
	public void drawNode(Graphics g, PositionTransformation pt) {
		this.sourceNode.setColor(Color.RED);	//source nodes are always red
		//drawing circle for UDG radius
		g.setColor(Color.YELLOW);
		pt.translateToGUIPosition(this.sourceNode.getPosition());
		int r = (int) (CustomGlobal.R * pt.getZoomFactor());
		g.drawOval(pt.guiX - r, pt.guiY - r, r*2, r*2);
		
		//drawing circles around the forwarder node and his connected nodes for visualizing that there
		//is no other node in this circle
		pt.translateToGUIPosition(this.sourceNode.getPosition());
		g.setColor(Color.LIGHT_GRAY);
		for(Node v: knownNeighbors)
		{
			double radius = 0.5 * this.sourceNode.getPosition().distanceTo(v.getPosition());
			pt.translateToGUIPosition(Algorithms.getCentralPos(this.sourceNode.getPosition(), v.getPosition()));
			r = (int) (radius * pt.getZoomFactor());
			g.drawOval(pt.guiX - r, pt.guiY - r, r*2, r*2);
		}
	}
}
