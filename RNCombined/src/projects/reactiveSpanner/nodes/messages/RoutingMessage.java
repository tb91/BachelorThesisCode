package projects.reactiveSpanner.nodes.messages;

import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.routing.RoutingObserver;
import projects.reactiveSpanner.routing.RoutingProtocol;
import projects.reactiveSpanner.routing.RoutingProtocol.ERoutingState;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;


/**
 * This message holds a routing protocol and is used to be routed from an starting node to a destination node.
 * <br>
 * This message wraps the main functions of the hold routing protocol
 * @author Mavs
 *
 */
public class RoutingMessage extends AbstractMessage
{
	private RoutingProtocol<PhysicalGraphNode> routingProtocol;

	public RoutingMessage(final RoutingProtocol<PhysicalGraphNode> routingProtocol)
	{
		super(routingProtocol.routingID, routingProtocol.getCurrentlyHoldingNode(), routingProtocol.getUsedTopologyStrategy());
		this.routingProtocol = routingProtocol;
	}
	
	public final PhysicalGraphNode getRoutingDestination()
	{
		return routingProtocol.getDestination();
	}
	
	public final PhysicalGraphNode getRoutingStart()
	{
		return node;
	}
	
	public final PhysicalGraphNode getNextHop()
	{
		return routingProtocol.getNextHop();
	}
	
	public final ERoutingState getCurrentRoutingState()
	{
		return routingProtocol.getCurrentState();
	}
	
	public void requestNextHop(RoutingObserver observer)
	{
		routingProtocol.requestNextRoutingStep(observer);
	}
	
	public void arrivedNextHop(final PhysicalGraphNode currentHolder)
	{
		String output = "RoutingMessage of " + this.routingProtocol.getRoutingType() + " event " + getID() + " reached " + currentHolder.toString();
		if(Tools.isSimulationInGuiMode())
		{
			Tools.getTextOutputPrintStream().append(output).append('\n');
		}
		logger.logln(LogL.INFO, output);
		this.routingProtocol.arrivedAtNewNode(currentHolder);
	}
	
	/**
	 * Add an observer to this routing event
	 * @param observer that should receive events from {@link ERoutingState}
	 */
	public void addObserver( final RoutingObserver observer )
	{
		this.routingProtocol.addObserver(observer);
	}
	/**
	 * Remove an observer from this routing event
	 * @param observer that should not receive events anymore
	 */
	public void removeObserver( final RoutingObserver observer )
	{
		this.routingProtocol.removeObserver(observer);
	}
	
	@Override
	public RoutingMessage clone() {
		return new RoutingMessage(this.routingProtocol);
	}
}
