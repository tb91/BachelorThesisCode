package projects.reactiveSpanner.nodes.messageHandlers.reactivePDT;

import java.awt.Color;
import java.awt.Graphics;
import java.util.UUID;

import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.messages.CTS;
import projects.reactiveSpanner.nodes.messages.RTS;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.timers.BeaconlessTimer;
import projects.reactiveSpanner.record.MessageRecord;
import projects.reactiveSpanner.record.ReactivePDTMessageRecord;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;

public class ReactivePDTMessageHandler extends BeaconlessMessageHandler
{		
	private double absoluteStartTime;
	private double absoluteTimeout;
	private double absoluteCycleTimeout;
	
	private double currentMaxAngle;
	
	private double t_max;	//relative timeout
	
	public ReactivePDTMessageHandler(final UUID routingID, final PhysicalGraphNode ownerNode, final PhysicalGraphNode forwarderNode) {
		super(routingID, ownerNode, forwarderNode, BeaconlessTopologyControl.EStrategy.REACTIVE_PDT);
	}

	@Override
	public void receivedMessage(final AbstractMessage msg)
	{
		if(msg instanceof RTS)
		{
			receivedRTS(((RTS) msg));
		} 
		else if (msg instanceof CTS)
		{
			receivedCTS(((CTS) msg));
		}
		else throw new RuntimeException("Received message of a not supported type");
	}	
	
	@Override
	protected void receivedRTS(RTS rts)
	{
		this.t_max = ReactivePDT.getMaximumTimeout();
		double timeout = delay();
		logger.logln(LogL.INFO, this.node.toString() + " sets its contentionTimer to " + timeout + " ms");
		this.node.setColor(Color.CYAN);
		currentMaxAngle = Math.PI * 0.5;
		contentionTimer = new BeaconlessTimer(this.tcID, timeout, this.node);
		absoluteStartTime = Tools.getGlobalTime();
		absoluteTimeout = Tools.getGlobalTime() + timeout;
		absoluteCycleTimeout = Tools.getGlobalTime() + t_max;
	}

	@Override
	protected void receivedCTS(final CTS cts) {
		logger.logln(LogL.INFO, this.node.toString() + " received CTS from " + cts.getTransmitter().toString());
		if(this.knownNeighbors == null)
			throw new RuntimeException("knownNeighbors Set of " + this.node.toString() + "and for topology control ID "+ tcID.toString() + " was not initialized!");
		if(hasSentCTSMessage)
			return;
		
		knownNeighbors.add(cts.getTransmitter());
		if(Algorithms.isViolatingGPDTCriteria(this.sourceNode, this.node, knownNeighbors))
		{
			contentionTimer.cancel();
		}
		else
		{
			for(Node z: knownNeighbors)
			{
				double angle = Algorithms.getSignedAngleBetween(this.sourceNode.getPosition(), z.getPosition(), this.node.getPosition(), true);
				if(angle > currentMaxAngle)
				{
					currentMaxAngle = angle;
					
					double oldTimeout = contentionTimer.getFireTime() - Tools.getGlobalTime();
					System.out.println("oldTimeout: " + oldTimeout + " d: " + d + " angle: " + Math.toDegrees(angle) + " UDGr: " + BeaconlessTopologyControl.R + " tmax: " + t_max);
					
					final double timeProportionalToDiameterUVW = (d / (Math.sin(angle) * BeaconlessTopologyControl.R)) * t_max;
					final double alreadyDelayedTime = Tools.getGlobalTime() - this.absoluteStartTime;	//relative
					double newRelativeTimeout = timeProportionalToDiameterUVW - alreadyDelayedTime;
					
					if(alreadyDelayedTime < 0)
						throw new RuntimeException("alreadyDelayedTime=" + alreadyDelayedTime + " cannot be negative!");
					if(newRelativeTimeout < 0)
						throw new RuntimeException("newRelativeTimeout=" + newRelativeTimeout +" cannot be negative!");
					
					logger.logln(LogL.INFO, this.node.toString() + " set contention timer from " + oldTimeout + " to " + newRelativeTimeout);
					contentionTimer.cancel();
					contentionTimer = new BeaconlessTimer(this.tcID, newRelativeTimeout, this.node);					
				}
			}
		}
	}
	
	@Override
	public void timerTriggerEvent() {
		this.broadcastCTS();
	}
	
	@Override
	public void drawNode(Graphics g, PositionTransformation pt) {				
		if(hasSentCTSMessage)
		{
			this.node.setColor(Color.DARK_GRAY);
			return;
		}
		this.node.setColor(Color.CYAN);	//is in range of the forwarder
	}
	
	@Override
	public MessageRecord getCurrentMessageRecord()
	{
		return new ReactivePDTMessageRecord(this.sourceNode, this.tcID, 
				RTS.numberOfSentMessages(tcID), 
				CTS.numberOfSentMessages(tcID));
	}
	
	@Override
	public boolean hasTerminated() {
		return ((BeaconlessMessageHandler)sourceNode.getMessageHandler(tcID)).hasTerminated();  //XXX: need to find better solution
																								// Typecast is save!
	}
	
	private double delay()
	{
		return ((super.d) / BeaconlessTopologyControl.R) * ReactivePDT.getMaximumTimeout();
	}
}
