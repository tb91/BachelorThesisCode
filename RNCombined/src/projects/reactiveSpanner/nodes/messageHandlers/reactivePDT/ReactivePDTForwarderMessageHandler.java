package projects.reactiveSpanner.nodes.messageHandlers.reactivePDT;

import java.awt.Color;
import java.awt.Graphics;

import projects.reactiveSpanner.CustomGlobal;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messages.CTS;
import projects.reactiveSpanner.nodes.messages.RTS;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.tools.Tools;

public class ReactivePDTForwarderMessageHandler extends	ReactivePDTMessageHandler
{
	private boolean hasTerminated;
	private BeaconlessTopologyControl subgraphControl;	//TODO If possible, change this
	
	public ReactivePDTForwarderMessageHandler(final BeaconlessTopologyControl subgraphControl, final PhysicalGraphNode sourceNode) {
		super(subgraphControl.getTopologyControlID(), sourceNode, sourceNode);
		this.hasTerminated = false;
		this.sourceNode.setColor(Color.RED);
		this.subgraphControl = subgraphControl;
		if(Tools.isSimulationInGuiMode())
		{
			Tools.getGUI().redrawGUI();	
		}	
	}

	@Override
	protected void receivedRTS(final RTS rts)
	{
		String warningMsg = "Warning: " + this.node.toString() + " received RTS as forwarder node. This case is not considered";
		// logger.logln(LogL.WARNING, warningMsg);
		throw new RuntimeException(warningMsg);
	}
	
	/* (non-Javadoc)
	 * @see projects.reactiveSpanner.nodes.nodeImplementations.ReactivePDTNode#receivedCTS(sinalgo.nodes.Node)
	 */
	@Override
	protected void receivedCTS(final CTS cts)
	{
		if(hasTerminated)
		{
			String msg = new String("ReactivePDTForwarderNode " + this.sourceNode.toString() + " received CTS of Node " + cts.getTransmitter().toString() + " but has already terminated.");
			// logger.logln(LogL.WARNING, msg);
			Tools.showMessageDialog(msg);
//			throw new RuntimeException(msg);
		}
		if(this.knownNeighbors == null)
			throw new RuntimeException("knownNeighbors Set of " + this.node.toString() + "and for topology control ID "+ tcID.toString() + " was not initialized!");
		
		// logger.logln(LogL.INFO, "ReactivePDTForwarderNode " + this.sourceNode.toString() + " received CTS of Node " + cts.getTransmitter().toString());
		knownNeighbors.add(cts.getTransmitter());
	}
	
	@Override
	public void timerTriggerEvent() {
		// logger.logln(LogL.INFO, this.sourceNode.toString() + " has terminated it's reactive calculation of it's PDT neighborhood");
		sourceNode.connect(knownNeighbors, Color.PINK);
		hasTerminated = true;
		subgraphControl.notifyTermination();
	}
	
	@Override
	public boolean hasTerminated()
	{
		return hasTerminated;
	}
	
	@Override
	public void drawNode(Graphics g, PositionTransformation pt) {
		this.sourceNode.setColor(Color.RED);	//source nodes are always red	//TODO
		//drawing circle for UDG radius
		g.setColor(Color.YELLOW);
		pt.translateToGUIPosition(this.sourceNode.getPosition());
		int r = (int) (CustomGlobal.R * pt.getZoomFactor());
		g.drawOval(pt.guiX - r, pt.guiY - r, r*2, r*2);
		
	}
}
