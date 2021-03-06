package projects.reactiveSpanner.nodes.messageHandlers;

import java.awt.Graphics;
import java.util.Set;
import java.util.UUID;

import projects.reactiveSpanner.TopologyControlObserver;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.gui.transformation.PositionTransformation;

/**
 * This Interface is the elementary representation of all topology controls. Furthermore, it holds
 * the enumeration of all available subgraphs {@link EStrategy} and the enumeration of 
 * processing states {@link EState} for topology control observer. 
 * 
 * @author Mavs
 *
 */
public interface SubgraphStrategy
{
	/**
	 * Enum for the used topology controls.
	 * New algorithms have to be added here
	 */
	public static enum EStrategy
	{
		UDG		//Unit disk graph
		,GG		//Gabriel Graph
		,PDT	//Partial Delaunay Triangulation
		,BFP	//reactive calculation of the Gabriel Graph
		,REACTIVE_PDT	//reactive calculation of the Partial Delaunay Triangulation
		,BUILD_BACKBONE	//magic	//TODO
		, RMYS // ADDED: Reactive Modified Yao Step construction
		, BEACONING_MYS // ADDED: non-local "RMYS". 
		,BARRIERE	//more magic	//TODO
		,BARRIERE_EXT	//extended more magic	//TODO
		,CREATE_VIRTUALS //this strategy planarizes a graph due to adding virtual nodes on each edge-edge intersection
		,BCA	//reactive something	//TODO
		; //Add more strategies here
	}

	/**
	 * Enum for the current state of this subgraph strategy.
	 * All subscribed observers will be notified when one of the 
	 * events occur
	 */
	public static enum EState
	{
		INITIALIZED,
		PROCESSING,
		TERMINATED
	}
	
	/**
	 * Start execution of subgraph creation
	 */
	public void start();	
	/**
	 * Checks if the calculation of the subgraph has been finished. The state of the SubgraphStrategy would be <code>TERMINATED</code> as well.
	 */
	public boolean hasTerminated();
	/**
	 * Get the current state of the subgraph strategy
	 */
	public EState getCurrentState();
	/**
	 * Get the unique ID of this subgraph creation
	 */
	public UUID getTopologyControlID();
	/**
	 * Returns the nodes of the calculated subgraph
	 * @param <T> type of nodes, extended from SimpleNode
	 */
	public <T extends PhysicalGraphNode> Set<T> getSubgraphNodes();
	/**
	 * Returns the type of strategy
	 */
	public EStrategy getStrategyType();
	/**
	 * Add an observer to this subgraph strategy
	 * @param observer that should receive events from {@link EState}
	 */
	public void addObserver( final TopologyControlObserver observer );	
	/**
	 * Remove an observer from this subgraph strategy
	 * @param observer that should not receive events anymore
	 */
	public void removeObserver( final TopologyControlObserver observer );
	
	/**
	 * @param g
	 * @param pt
	 *   this method is used to invoke messagehandler.draw(g,pt)
	 *   otherwise you see nothing..
	 */
	public void draw(Graphics g, PositionTransformation pt);
}
