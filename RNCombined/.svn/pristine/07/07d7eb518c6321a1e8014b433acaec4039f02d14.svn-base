package projects.reactiveSpanner.routing;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.tools.logging.LogL;

/**
 * Routing algorithm that combines Greedy forwarding and Face Routing. As long as no neighbor is closer to the destination as
 * the currently holding node, greedy processing will be used. When this is not the case, face routing is used until a
 * node has been found that is closer than the node at that one face routing has been started.
 * 
 * Internally this algorithm uses GreedyRouting and FaceRouting as sub-routing routines, so there are 3 different ID's that
 * are used for processing.
 * 
 * @author Matthias von Steimker
 * @param <T> type of nodes that are used for routing
 */
public class GreedyFace<T extends PhysicalGraphNode> extends RoutingProtocol<T> implements RoutingObserver
{
	private enum EGFG_STATE{FIRST_HOP_SEARCHING, GREEDY, FACE_RECOVERY};
	
	private RoutingProtocol<T> greedySubProtocol;
	private RoutingProtocol<T> faceRoutingSubProtocol;
	
	private boolean isStucked;
	private EGFG_STATE currentGFGState;
	private T closestNodeToDestination;
	
	protected GreedyFace(T sourceNode, T destinationNode,
			T currentHolder, EStrategy subgraphStrategy) {
		super(sourceNode, destinationNode, currentHolder, subgraphStrategy, ERouting.GREEDY_FACE);
	}

	@Override
	protected void init() {
		isStucked = false;
		currentGFGState = EGFG_STATE.FIRST_HOP_SEARCHING;
		if(greedySubProtocol == null)
		{
			greedySubProtocol = new GreedyForwarding<T>(super.getSource(), super.getDestination(), currentHolder, EStrategy.UDG);
			this.closestNodeToDestination = currentHolder;
		} else {
			greedySubProtocol.restartProcessing(super.getSource(), super.getDestination(), currentHolder, EStrategy.UDG);
		}
	}

	@Override
	protected void _requestedNextRoutingStep()
	{
		switch(currentGFGState)
		{
		case FACE_RECOVERY:
			synchronizeSubProcessing(faceRoutingSubProtocol);
			faceRoutingSubProtocol.requestNextRoutingStep(this);
			break;
		case GREEDY:
			synchronizeSubProcessing(greedySubProtocol);
			greedySubProtocol.requestNextRoutingStep(this);
			break;
		case FIRST_HOP_SEARCHING:
			currentGFGState = EGFG_STATE.GREEDY;
			greedySubProtocol.requestNextRoutingStep(this);
			break;
		default:
			break;
		}
	}

	@Override
	protected boolean _isStucked()
	{
		return isStucked;
	}

	@Override
	public void onNotify(UUID routingID, ERoutingState event)
	{
		//got notification by one of the sub-routings
		switch(currentGFGState)
		{
		case GREEDY:
			if(!greedySubProtocol.routingID.equals(routingID))
			{
				logger.logln(LogL.WARNING, "GFG Routing Operation was notified by the event " + event + "  that was not of the active greedy processings");
				return;
			}
			handleGreedyProcessing(event);
			break;	
		case FACE_RECOVERY:
			if(!faceRoutingSubProtocol.routingID.equals(routingID))
			{
				logger.logln(LogL.WARNING, "GFG Routing Operation was notified by the event " + event + " that was not of the active face recovery");
				return;
			}
			handleFaceRecovery(event);
			break;
		case FIRST_HOP_SEARCHING:
		default:
			break;	
		}
	}

	/**
	 * Synchronize the sub-routing routine with this GFG-Routing
	 * @param toSync
	 */
	private void synchronizeSubProcessing(RoutingProtocol<T> toSync)
	{
		toSync.arrivedAtNewNode(this.currentHolder);	//TODO throws warning when switched between sub routing protocols
		//the lastHolder as to be set especially, because the sub routing could be canceled and activated later and the lastHold is not 
		toSync.lastHolder = this.lastHolder;
	}
	
	private void handleGreedyProcessing(final ERoutingState event)
	{
		switch (event)
		{
		case NEXT_HOP_FOUND:
		case DESTINATION_NODE_FOUND:
			this.nextHop = greedySubProtocol.nextHop;
			this.closestNodeToDestination = this.nextHop;
			break;
		case STUCKED:
			logger.logln(LogL.INFO, "GFG operation " + super.routingID + " has stucked with greedy processings at " + currentHolder + ". Switching to Face recovery mode.");
			greedySubProtocol.cancelProcessing();
			startFaceRecovery();
		default:
			break;
		}
	}

	private void startFaceRecovery()
	{
		this.currentGFGState = EGFG_STATE.FACE_RECOVERY;
		if(faceRoutingSubProtocol == null)
		{
			faceRoutingSubProtocol = new FaceRouting<T>(currentHolder, super.getDestination(), currentHolder, super.getUsedTopologyStrategy());
		}
		else {
			faceRoutingSubProtocol.restartProcessing(currentHolder, super.getDestination(), currentHolder, super.getUsedTopologyStrategy());
		}
		faceRoutingSubProtocol.requestNextRoutingStep(this);
	}

	private void handleFaceRecovery(final ERoutingState event) {
		switch (event) {
		case NEXT_HOP_FOUND:
		case DESTINATION_NODE_FOUND:
			this.nextHop = faceRoutingSubProtocol.nextHop;
			if(this.nextHop.getPosition().squareDistanceTo(super.getDestination().getPosition()) 
					< this.closestNodeToDestination.getPosition().squareDistanceTo(super.getDestination().getPosition()))
			{
				logger.logln(LogL.INFO, "Routing operation " + super.routingID + " has found " + nextHop + " that is closer to destination than " + closestNodeToDestination 
						+ ". Switching to greedy processings");
				this.closestNodeToDestination = this.nextHop;
				faceRoutingSubProtocol.cancelProcessing();
				this.currentGFGState = EGFG_STATE.GREEDY;
				greedySubProtocol.restartProcessing(currentHolder, super.getDestination(), currentHolder, super.getUsedTopologyStrategy());
			}
			break;
		case STUCKED:
			isStucked = true;
			break;
		default:
			break;
		}
	}
}
