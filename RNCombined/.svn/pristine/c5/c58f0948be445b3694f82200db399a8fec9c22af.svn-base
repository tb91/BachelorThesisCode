package projects.reactiveSpanner.nodes.messageHandlers.BFP;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import projects.defaultProject.nodes.timers.MessageTimer;
import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.BFP.BFP.Phase;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.messages.CTS;
import projects.reactiveSpanner.nodes.messages.ProtestMessage;
import projects.reactiveSpanner.nodes.messages.ProtestPhaseOverMessage;
import projects.reactiveSpanner.nodes.messages.RTS;
import projects.reactiveSpanner.nodes.messages.SelectionPhaseOverMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.timers.BeaconlessTimer;
import projects.reactiveSpanner.record.BFPMessageRecord;
import projects.reactiveSpanner.record.MessageRecord;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.tools.logging.LogL;

public class BFPMessageHandler extends BeaconlessMessageHandler
{
	protected BFP.Phase currentPhase;
	
	/**
	 * flag, that supposes that the owner node of this message handler received a CTS of another node that lies in the specific proximity region
	 */
	private boolean nodeIsHidden = false;
	/**
	 * flag, that supposes that this node has send a protest against an violating node
	 */
	private boolean nodeSentProtest = false;
	
	/**
	 * Set of nodes that are violating the proximity condition
	 */
	private Set<Node> violatingNodes = new HashSet<Node>();
	
	
	public BFPMessageHandler(final UUID routingID, final PhysicalGraphNode ownerNode, final PhysicalGraphNode sourceNode)
	{
		super(routingID, ownerNode, sourceNode, BeaconlessTopologyControl.EStrategy.BFP);
		currentPhase = Phase.SELECTION;
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
		else if (msg instanceof ProtestMessage)
		{
			overhearProtest((ProtestMessage) msg);
		}
		else if(msg instanceof SelectionPhaseOverMessage)
		{
			this.currentPhase = BFP.Phase.PROTEST;
			startListening();
		} 
		else if(msg instanceof ProtestPhaseOverMessage)
		{
			logger.logln(LogL.INFO, this.node.toString() + " received ProtestPhaseOverMessage of topology control ID "  + tcID.toString());
			nodeIsHidden = false;
			if(!violatingNodes.isEmpty())
			{
				logger.logln(LogL.ERROR_DETAIL, "Set of violating nodes of " + this.node.toString() + " is not empty after end of protest phase. " +
						"set of violating nodes still contains " + violatingNodes.toString());
				violatingNodes.clear();
			}
		}
		else throw new RuntimeException("Received message of a not supported type");
	}
	
	@Override
	protected void receivedRTS(final RTS rts)
	{
		logger.logln(LogL.INFO, this.node.toString() + " sets its contentionTimer to " + d + " ms for topology control ID " + this.tcID.toString());
		contentionTimer = new BeaconlessTimer(this.tcID, delay(), this.node);
	}

	@Override
	protected void receivedCTS(final CTS cts)
	{
		PhysicalGraphNode transmitter = cts.getTransmitter();
		logger.logln(LogL.INFO, this.node.toString() + " received CTS from " + transmitter.toString());	
		
		if(this.knownNeighbors == null)
			throw new RuntimeException("knownNeighbors Set of " + this.node.toString() + "and for topology control ID "+ tcID.toString() + " was not initialized!");
		
		knownNeighbors.add(transmitter);	
		//System.out.println("_DEBUG:receivedCTS: I'm node " + this.toString() + " and received CTS from " + node.toString());
		if(!nodeIsHidden)
		{
			//TODO when upgrade for CNG than do it here
			if(Algorithms.isInGabrielCircle(transmitter, this.sourceNode, this.node))
			{
				logger.logln(LogL.INFO, this.node.toString() + " has encountered that " + transmitter.toString() 
						+ " is in GG between forwarder and itself. " + this.node.toString() + " is hidden now.");
				nodeIsHidden = true;
				this.contentionTimer.cancel();
			}
		}
		else
		{
			if(Algorithms.isInGabrielCircle(this.node, this.sourceNode, transmitter))
			{
				logger.logln(LogL.INFO, this.node.toString() + " has encountered that itself is in GG between forwarder and " 
			+ transmitter.toString() + ". Adding " + transmitter.toString() + " to violating nodes");
				violatingNodes.add(transmitter);
			}
			else
			{
				logger.logln(LogL.INFO, this.node.toString() + " has encountered that itself is not in GG between forwarder and " 
						+ transmitter.toString() + ". All clear");
			}
		}
	}
	
	/**
	 * This method is called directly after the end of the selection phase.
	 * If this node was set to hidden, it has to listen to other nodes until its timer has expired again
	 */
	private void startListening()
	{
		logger.logln(LogL.INFO, this.node.toString() + " received SelectionPhaseOverMessage of topology control ID "  + tcID.toString());
		if(nodeIsHidden && !violatingNodes.isEmpty() && !(this.sourceNode.equals(this.node)))
		{
			logger.logln(LogL.INFO, this.node.toString() + " is hidden and has to protest against unreported violating nodes " + violatingNodes.toString());
			contentionTimer = new BeaconlessTimer(this.tcID, delay(), this.node);
		}
	}
	
	/**
	 * This method is called if this node receives a protest message of another BFP node.
	 * In event of this, the set of violating nodes will be checked if a node w' lies in the proximity region between
	 * a node u and x. Here: if <b>transmitter</b> is in Gabriel Circle of <b>BFP.fowarder</b> and any node x of <b>BFP.getViolatingNodes()</b>
	 * @param transmitter Node which has sent the overheard protest
	 * @param overheardViolatingNodes is the set of overheard violating nodes of transmitter. <b>Only needed for forwarder!</b>
	 */
	protected void overhearProtest(final ProtestMessage protestMsg)
	{
		final Node transmitter = protestMsg.getTransmitter();
		
		logger.logln(LogL.INFO, this.node.toString() + " overheard protest from " + transmitter.toString());
		if(!nodeIsHidden)
			return;
		
		Set<Node> nodesToRemoveFromViolatingNodes = new HashSet<Node>();
		
		for(Node x: this.violatingNodes)
		{		
			//TODO when upgrade for CNG than do it here
			if(Algorithms.isInGabrielCircle(transmitter, this.sourceNode, x))
			{
				nodesToRemoveFromViolatingNodes.add(x);
			}
		}
//		System.out.println("_DEBUG:overhearProtest: removing " + nodesToRemoveFromViolatingNodes.toString() + " from violating nodes");
		this.violatingNodes.removeAll(nodesToRemoveFromViolatingNodes);	
	}

	protected void protest()
	{	
		if(!violatingNodes.isEmpty())
		{
			logger.logln(LogL.INFO, this.node.toString() + " protests against violating nodes " + violatingNodes.toString());
			broadcastTimer = new MessageTimer(new ProtestMessage(this.tcID, this.node, violatingNodes));
			broadcastTimer.startRelative(1, this.node);
			nodeSentProtest = true;
			this.node.setColor(Color.ORANGE);
		}
		else
		{
			logger.logln(LogL.INFO, this.node.toString() + " has no more nodes to protest against");
		}
	}

	@Override
	public boolean hasTerminated() {
		return ((BFPForwarderMessageHandler) sourceNode.getMessageHandler(tcID)).hasTerminated();	//XXX: need to find better solution
		 																						// Typecast is save!
	}

	@Override
	public void timerTriggerEvent() {
		switch(currentPhase)
		{
		case SELECTION:
			broadcastCTS();
			break;
		case PROTEST:
			protest();
			break;
		default:
			throw new RuntimeException("Other phase than 'Protest' or 'Selection' is selected. That cannot be.");
		}
	}
	
	private double delay()
	{
		return ((super.d) / BeaconlessTopologyControl.R) * BFP.getMaximumTimeout();
	}
	
	@Override
	public void drawNode(Graphics g, PositionTransformation pt) {
		if(nodeSentProtest)
		{
			this.node.setColor(Color.LIGHT_GRAY);
			return;
		}
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
		return new BFPMessageRecord(this.sourceNode, this.tcID, 
				RTS.numberOfSentMessages(tcID), 
				CTS.numberOfSentMessages(tcID), 
				ProtestMessage.numberOfSentMessages(tcID));
	}
}
