package projects.reactiveSpanner.routing;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import projects.reactiveSpanner.TopologyControlObserver;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EState;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.edges.Edge;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

/**
 * Super class of all implemented routing protocols. Unifies several processes, such as instantiating (works as factory),
 * status managing (changes status and informs observers) and also the routing process itself without dealing about getting
 * the next hop.
 * 
 * @author Matthias von Steimker
 * @param <T> type of nodes
 */
public abstract class RoutingProtocol<T extends PhysicalGraphNode> implements TopologyControlObserver
{
	protected static Logging logger = Logging.getLogger();
	
	/**
	 * Enum for supported Routing algorithms
	 */
	public static enum ERouting
	{
		GREEDY,
		FACE_ROUTING,
		GREEDY_FACE
	}
	
	/**
	 * Enum for the used routing protocol.
	 * These are all states, this protocol can receive
	 */
	public static enum ERoutingState
	{
		INITIALIZED,	//the routing has been initialized
		NEW_HOLDING_NODE,	//the holding node of this routingProtocol has changed (usually after arriving at the next hop)
		WAITING_FOR_TOPOLOGY_CREATION,	//the routing protocol waits for termination of the selected subgraph strategy
		NEXT_HOP_FOUND,	//a next hop has been found
		DESTINATION_NODE_FOUND, //the next hop that was found is the destination node
		FINISHED,	//the Routing Protocol has successfully calculated its path to the destination node
		STUCKED, //the Routing Protocol is stucking and cannot reach the destination node
		CANCELED //the processings has been canceled
	}
	public final UUID routingID;
	private T source;
	private T destination;
	/**
	 * The node that currently holds the routing message
	 */
	protected T currentHolder;
	/**
	 * The node that sent the routing message to the currentHolder
	 */
	protected T lastHolder;
	/**
	 * used Subgraph that is used for routing. Could be a beaconless or beaconing based processing
	 */
	protected EStrategy usedSubgrahStrategy;
	/**
	 * Subgraph interface of the currently holding node
	 */
	protected SubgraphStrategy subgraphInterface;
	/**
	 * The next hop that is found
	 */
	protected T nextHop;
	/**
	 * current state of this routing operation. Use this to determine if a next hop has been found, the holding node has changed, the routing is stucked or
	 * the routing operation is waiting for the termination of subgraph processing
	 */
	private ERoutingState currentState;
	/**
	 * Observers that could register to this routing event to gain notifications about changing states
	 */
	private Set<RoutingObserver> observers;
	/**
	 * number of hops this routing operation has done
	 */
	private int numHops;
	/**
	 * distance covered while sending this message
	 */
	private double traveledDistance;
	/**
	 * Routing algorithm type of this protocol
	 */
	private final ERouting routingAlgorithm;
	
	
	protected RoutingProtocol(final T sourceNode, final T destinationNode, T currentHolder, final EStrategy subgraphStrategy, final ERouting routingAlgorithm)
	{
		this.routingID = UUID.randomUUID();
		this.source = sourceNode;
		this.destination = destinationNode;
		this.currentHolder = currentHolder;
		this.routingAlgorithm = routingAlgorithm;
		this.lastHolder = null;
		this.usedSubgrahStrategy = subgraphStrategy;
		this.observers = new HashSet<RoutingObserver>();
		this.nextHop = null;
		this.numHops = 0;
		this.traveledDistance = 0.0;
		this.currentState = ERoutingState.INITIALIZED;
		logger.logln(LogL.INFO, "Routing message from " + sourceNode + " to " + destinationNode + " by using subgraph strategy " + subgraphStrategy);
		init();
		//debugDraw();
	}

	/**
	 * Get a routing protocol from this factory
	 * 
	 * @param from starting node of the routing process
	 * @param to destination for the routing process
	 * @param subraphStrategy subgraph strategy that is used to generate the underlying subgraph for this routing algorithm
	 * @param routingAlgorithm the used routing algorithm to progress the routing message
	 * @return the routing protocol to observe from an independent controller (this controller should not intervene in the routing process)
	 */
	public static final <U extends PhysicalGraphNode> RoutingProtocol<U> routing(final U from, final U to, final EStrategy subraphStrategy, final ERouting routingAlgorithm)
	{
		switch(routingAlgorithm)
		{
		case GREEDY:
			return new GreedyForwarding<U>(from, to, from, subraphStrategy);
		case FACE_ROUTING:
			return new FaceRouting<U>(from, to, from, subraphStrategy);
		case GREEDY_FACE:
			return new GreedyFace<U>(from, to, from, subraphStrategy);
		default:
			throw new RuntimeException("Requested unknown routing algorithm");	
		}	
	}
	
	/**
	 * @return the current state of this routing operation
	 */
	public final ERoutingState getCurrentState()
	{
		return currentState;
	}
	/**
	 * @return the used topology strategy that is used to route the message
	 */
	public final EStrategy getUsedTopologyStrategy()
	{
		return usedSubgrahStrategy;
	}
	/**
	 * @return the currently holding node of the routing operation
	 */
	public final T getCurrentlyHoldingNode()
	{
		return currentHolder;
	}
	/**
	 * @return the <b>next hop</b> or <b>null</b> if the current state does not equals <code>DESTINATION_NODE_FOUND</code> or <code>NEXT_HOP_FOUND</code>
	 */
	public T getNextHop()
	{
		switch(currentState)
		{
		case DESTINATION_NODE_FOUND:
		case NEXT_HOP_FOUND:
			return nextHop;
		case FINISHED:
			logger.logln(LogL.WARNING, "Attention: Called next hop of routing operation " + this.routingID + ", but routing has been finished already. Returning null.");
			return null;
		case STUCKED:
			logger.logln(LogL.WARNING, "Attention: Called next hop of routing operation " + this.routingID + ", but routing has been stucked. Returning null.");
			return null;
		default:
			logger.logln(LogL.WARNING, "Attention: Called next hop of routing operation " + this.routingID + " before this next hop was calculated yet. Returning null.");
			return null;
		}
	}
	
	/**
	 * Request the next routing step to procedure this routing operation. This is generally done, when the holding node has been changed.
	 * <br>
	 * The request follows the pipelining principle:<br>
	 * 1. Has the routing algorithm been canceled?<br>
	 * 2. Has the routing algorithm already finished (current holding node equals destination)?<br>
	 * 3. Generate the subgraph and when it has not terminated yet, return and wait for notification.<br>
	 * 4. Request the actual next hop.<br>
	 * 5. Prove if the algorithm has been stucked or there is no next hop.<br>
	 * 6. Inform observers about the next hop (and perhaps even about found destination node).<br>
	 * 
	 * @param observer that want to be informed about changed states of this routing operation
	 */
	public void requestNextRoutingStep(RoutingObserver observer)
	{
		if(observer != null)
		{
			this.observers.add(observer);
		}
		if(currentState.equals(ERoutingState.CANCELED))
		{
			logger.logln(LogL.WARNING, "Routing operation with ID " + this.routingID + " was requested for the next routing step, but "
					+ "processing has been canceled before. Routing has to be restarted to request the next routing step.");
			return;
		}
		
		if(currentHolder.equals(destination))
		{
			logger.logln(LogL.WARNING, "Routing operation with ID " + this.routingID + " was requested for the next routing step, but "
					+ "the current holding node equals already the destination node.");
			this.currentState = ERoutingState.FINISHED;
			return;
		}
		this.subgraphInterface = currentHolder.requestSubgraph(usedSubgrahStrategy);	
		if(!isSubgraphCalculated())
		{
			logger.logln(LogL.INFO, "Routing operation with ID " + this.routingID + " is waiting for completion of topology control strategy "
					+ this.usedSubgrahStrategy + " by currently holding node " + this.currentHolder.toString());
			this.currentState = ERoutingState.WAITING_FOR_TOPOLOGY_CREATION;
			return;
		}
		
		_requestedNextRoutingStep();
		
		if(_isStucked() || nextHop == null)	//if isStucked() 
		{
			logger.logln(LogL.WARNING, "Routing operation with ID " + this.routingID + " has been stucked at " + this.currentHolder.toString());
			this.currentState = ERoutingState.STUCKED;
		}
		else
		{
			if(nextHop.equals(destination))
			{
				logger.logln(LogL.INFO, "Routing operation with ID " + this.routingID + " has found a path to the destination " 
			+ this.destination.toString());
				this.currentState = ERoutingState.DESTINATION_NODE_FOUND;
			}
			else
			{
				logger.logln(LogL.INFO, "Routing operation with ID " + this.routingID + " has decided for " 
						+ this.nextHop.toString() + " as next hop");
				this.currentState = ERoutingState.NEXT_HOP_FOUND;
			}	
		}
		notifyObservers();
	}
	
	/**
	 * Observe the current state of the used subgraph model
	 * @return true if the current states equals <code>EState.TERMINATED</code>
	 */
	private boolean isSubgraphCalculated()
	{
		switch(subgraphInterface.getCurrentState())
		{
		case INITIALIZED:
			subgraphInterface.addObserver(this);
			subgraphInterface.start();
			return false;
		case PROCESSING:
			subgraphInterface.addObserver(this);
			return false;
		case TERMINATED:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * This function <b>has to be called</b> when the routing message holding this routing protocol arrived at a new node (currently holding node has changed)
	 * @param newHoldingNode the node that currently holds the routing message with this protocol
	 */
	public void arrivedAtNewNode(final T newHoldingNode)
	{
		if(this.currentState.equals(ERoutingState.CANCELED))
		{
			return;
		}
		
		this.currentState = ERoutingState.NEW_HOLDING_NODE;
		if(this.nextHop != null && !this.nextHop.equals(newHoldingNode))
		{
			logger.logln(LogL.WARNING, "Warning: The RoutingProtocol with ID " + this.routingID.toString() + " arrived at the new holder " +
		newHoldingNode.toString() + ", but this node was not the suggested next hop " + this.nextHop.toString());
		}
		this.lastHolder = this.currentHolder;
		this.currentHolder = newHoldingNode;
		this.nextHop = null;
		this.subgraphInterface = null;
		this.numHops++;
		this.traveledDistance += this.lastHolder.getPosition().distanceTo(this.currentHolder.getPosition());
		
		if(currentHolder.equals(destination))
		{
			logger.logln(LogL.INFO, "The " + this.getRoutingType() + " operation with ID " + this.routingID.toString() + " arrived successfully after " + this.numHops + " hops at the destination " + this.destination.toString());
			this.currentState = ERoutingState.FINISHED;
		}
		notifyObservers();
		debugEdgeDraw();
	}
	
	/**
	 * specific pre-calculations that are needed by the executing routing protocol
	 */
	protected abstract void init();
	/**
	 * The actual implementation of the routing algorithm. This routine is called when the routing processing has <b>not</b> been <b>canceled</b>,
	 * the <b>currently holding node</b> does <b>not equals</b> the <b>destination</b> and the <b>subgraph has been calculated</b>.
	 * <br>
	 * When possible, the next hop should have been found by returning of this method, otherwise it will be assumed that the routing is stucked.
	 * @param observer
	 */
	protected abstract void _requestedNextRoutingStep();
	/**
	 * Observation for possible progress of this routing operation
	 * 
	 * @return is this algorithm not able to find a path to the destination
	 */
	protected abstract boolean _isStucked();
	/**
	 * @return the number of hops this routing event has already accomplished
	 */
	public int getNumberOfHops()
	{
		return numHops;
	}
	/**
	 * @return euclidean distance covered between all currently traveled nodes
	 */
	public double getTraveledDistance()
	{
		return traveledDistance;
	}
	/**
	 * @return the start node of this routing event
	 */
	public final T getSource()
	{
		return source;
	}
	/**
	 * @return the destination node of this routing event
	 */
	public final T getDestination()
	{
		return destination;
	}
	/**
	 * @return type of this routing protocol
	 */
	public final ERouting getRoutingType()
	{
		return routingAlgorithm;
	}
	
	/**
	 * Add an observer to this routing event
	 * @param observer that should receive events from {@link EState}
	 */
	public void addObserver( final RoutingObserver observer )
	{
		this.observers.add(observer);
	}
	/**
	 * Remove an observer from this routing event
	 * @param observer that should not receive events anymore
	 */
	public void removeObserver( final RoutingObserver observer )
	{
		if(observers.contains(observer))
		{
			this.observers.remove(observer);
		}
		else
		{
			logger.logln(LogL.WARNING, "Warning: Requested remove of " + observer.toString() + " from observers of routing event " 
					+ this.routingID.toString() + ", but was no observer.");
		}
	}

	/* (non-Javadoc)
	 * @see projects.reactiveSpanner.TopologyControlObserver#onNotify(projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy, projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EEvents)
	 */
	@Override
	public void onNotify(SubgraphStrategy topologyControl, final EState event) {
		if(!topologyControl.equals(subgraphInterface))
		{
			logger.logln(LogL.WARNING, "Routing Event with ID " + this.routingID + " has been notified about another topology control event than the subscribed one. This case seems unusual.");
		}
		switch(event)
		{
		case TERMINATED:
			requestNextRoutingStep(null);
			subgraphInterface.removeObserver(this);
			break;
		default:
			break;
		}	
	}
	
	private void notifyObservers()
	{
		for(RoutingObserver observer: observers)
		{
			observer.onNotify(this.routingID, this.currentState);
		}
	}
	
	private void debugDraw() {
		this.source.setColor(Color.GREEN);
		this.destination.setColor(Color.ORANGE);
	}
	private void debugEdgeDraw() {
		
		Edge e = lastHolder.getEdgeTo(currentHolder);
		e.defaultColor = Color.GRAY;
	}

	public void restartProcessing(final T sourceNode, final T destinationNode, T currentHolder, final EStrategy subgraphStrategy)
	{
		this.source = sourceNode;
		this.destination = destinationNode;
		this.currentHolder = currentHolder;
		this.usedSubgrahStrategy = subgraphStrategy;
		this.lastHolder = null;
		this.nextHop = null;
		this.numHops = 0;
		this.traveledDistance = 0.0;
		this.currentState = ERoutingState.INITIALIZED;
		logger.logln(LogL.INFO, "Restarting routing processing "+ this.routingID + ". Routing message from " + sourceNode + " to " + destinationNode + " by using subgraph strategy " + subgraphStrategy);
		notifyObservers();
		init();
//		debugDraw();
	}
	
	public void cancelProcessing()
	{
		this.source = null;
		this.destination = null;
		this.currentHolder = null;
		this.usedSubgrahStrategy = null;
		this.lastHolder = null;
		this.nextHop = null;
		this.currentState = ERoutingState.CANCELED;
		logger.logln(LogL.INFO, "Canceled routing processing "+ this.routingID);
		notifyObservers();
	}
}
