package projects.reactiveSpanner.nodes.messageHandlers;

import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import projects.reactiveSpanner.TopologyControlObserver;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import projects.reactiveSpanner.record.MessageRecord;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.tools.logging.Logging;

/**
 * Abstract super layer of all topology control events. This could be seen as brain of the event and will be used for exterior control
 * of the complete events without interchanging with the message handlers of each node. This abstract super layer implements the
 * {@link SubgraphStrategy} interface and configures first necessary methods.
 * 
 * @author Mavs changed by Tim & cyron
 */
public abstract class AbstractTopologyControl implements SubgraphStrategy
{
	protected static Logging logger = Logging.getLogger();
	/**
	 * Unique ID of this topology control event
	 */
	private final UUID topologyControlID;
	/**
	 * Used topology control strategy
	 */
	private final EStrategy topologyControlStrategy;
	/**
	 * node holding this topology control
	 */
	protected final PhysicalGraphNode sourceNode;
	/**
	 * The current state of this topology control strategy
	 */
	protected EState currentState;
	/**
	 * Observers of this topology control that want to be informed about events
	 */
	private final HashSet<TopologyControlObserver> observers;
	
	protected AbstractTopologyControl(final EStrategy usedStrategy, final PhysicalGraphNode sourceNode){
		this.topologyControlStrategy=usedStrategy;
		this.sourceNode = sourceNode;
		topologyControlID = UUID.randomUUID();
		observers = new HashSet<TopologyControlObserver>();
		this.currentState = EState.INITIALIZED;	//TODO this could lead to problems, because things in sub classes would be initialized after this call
		notifyObservers();
	}	
	
	public final MessageRecord getMessageRecord()
	{
		return sourceNode.getMessageHandler(this.topologyControlID).getCurrentMessageRecord();
	}
	
	/**
	 * Get the current state of the subgraph strategy
	 */
	@Override
	public final EState getCurrentState()
	{
		return currentState;
	}
	@Override
	public boolean hasTerminated()
	{
		return currentState.equals(EState.TERMINATED);
	}
	
	@Override
	public final UUID getTopologyControlID() {
		return topologyControlID;
	}
	
	@Override
	//TODO change to a typsafe return. This is a design problem and probably requires greater changes
	public Set<? extends SimpleNode> getSubgraphNodes()
	{
		Set<? extends SimpleNode> subgraph = sourceNode.getMessageHandler(topologyControlID).getKnownNeighbors();
		return subgraph;
	}
	
	/**
	 * @return node that executes this local reactive topology control
	 */
	public PhysicalGraphNode getExecutingNode()
	{
		return sourceNode;
	}
		
	/**
	 * @return current usedStrategy
	 */
	@Override
	public final EStrategy getStrategyType()
	{
		return topologyControlStrategy;
	}
	
	/**
	 * initializes used algorithm
	 */
	protected abstract void _init();
	/**
	 * Actual implementation routine for starting the algorithm. Will be called by start()
	 */
	protected abstract void _start();
	
	@Override
	public void start()
	{
		_start();
		this.currentState = EState.PROCESSING;
		notifyObservers();
	}
	
	/**
	 * Signals the termination of the calculation of the topology to all observers
	 */
	protected void terminate()
	{
		this.currentState = EState.TERMINATED;
		notifyObservers();
	}

	
	@Override
	public void addObserver( final TopologyControlObserver observer )
	{
		observers.add(observer);
	}
	
	@Override
	public void removeObserver( final TopologyControlObserver observer )
	{
		observers.remove(observer);
	}
	
	protected void notifyObservers()
	{
		for(TopologyControlObserver observer: observers)
		{
			observer.onNotify(this, this.currentState);
		}
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt) {
		if (sourceNode.getMessageHandler(this.getTopologyControlID()) != null) {
			sourceNode.getMessageHandler(this.getTopologyControlID()).drawNode(g, pt);
		}
	}
}
