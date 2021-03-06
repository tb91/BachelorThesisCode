package projects.reactiveSpanner.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.CustomGlobal;
import projects.reactiveSpanner.Disk2D;
import projects.reactiveSpanner.nodes.edges.DistEdge;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategyFactory;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.helper.NodeSelectionHandler;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.tools.Tools;
import sinalgo.tools.Triple;
import sinalgo.tools.logging.Logging;

public class SimpleNode extends Node{
	
	protected static Logging logger = Logging.getLogger();
//	private static String usedEdgeType = Configuration.getEdgeTypeShortName();
//	private Set<DistEdge> connections;
	
	/**
	 * Controller of all by this node started subgraph strategies
	 */
	public SubgraphStrategyFactory subgraphStrategyFactory;  //FIXME: cannot be protected.  need it in buildbackbone
	
	Disk2D diskToDraw = null;
	
	@Override
	public void handleMessages(Inbox inbox) {

	}

	@Override
	public void preStep()
	{

	}

	@Override
	public void init() {
		nodeColor = Color.BLUE;
	}

	@Override
	public void neighborhoodChange()
	{
		
	}

	@Override
	public void postStep()
	{

	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {

	}
	
	public SubgraphStrategy requestSubgraph(EStrategy subgraphType)
	{
		return subgraphStrategyFactory.request(subgraphType);
	}
	
	
	public Set<Node> getConnectedNodes()
	{
		Set<Node> neighbors = new HashSet<Node>();
		for(Edge e: this.outgoingConnections)
		{
			neighbors.add(e.endNode);
		}
		return neighbors;
	}
	
	/**
	 * Returns the edge to the node neighbor if it exists
	 * <br>
	 * <b>Attention:</b> inefficient implementation
	 * @param neighbor other node
	 * @return edge to neighbor if connection exists, else returning <b>null</b>
	 */
	public <T extends Node> Edge getEdgeTo(T neighbor)
	{
		super.updateConnections();
		for(Edge edge: this.outgoingConnections)
		{
			if(edge.endNode.equals(neighbor))
				return edge;
		}
		return null;
	}
	
//	/**
//	 * Getting the outgoing connections as actual type <code>T</code> derived from <code>Edge</code>.<br>
//	 * 
//	 * <b>Warning: Do not modify the set of outgoing connections. Adding/Removing of edges will have no 
//	 * influence on actual outgoing connections </b>
//	 * 
//	 * @return Set of all outgoing connections of this node
//	 */
//	@SuppressWarnings("unchecked")
//	public <T extends Edge> Set<T> getOutgoingConnectionsView()
//	{
////		//test if the used connectivity model is UDG
////		String usedCModel = forwarderMsgHandler.node.getConnectivityModel().getClass().getName();
////		usedCModel = usedCModel.substring(usedCModel.lastIndexOf('.') + 1);
////		if(!usedCModel.equals("UDG"))
////		{
////			throw new RuntimeException("Used Connectivity model is " + usedCModel + ", but Reactive PDT is only supported under the UDG model!");
////		}
//		Class<?> edgeClass = null;
//		try {
//			edgeClass = Utilities.getUsedEdgeType();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		
//		Set<T> edges = new HashSet<T>();
//		for(Edge e: this.outgoingConnections)
//		{
//			if(edgeClass.isInstance(e.getClass()))
//			{
//				edges.add((T) e); //is dynamically checked
//			} else
//			{
//				throw new RuntimeException("Edge type " + edgeClass.toString() + " of edge " + e.toString() + " does not match the used edge type defined by the configuration");
//			}
//		}
//		return edges;
//	}
	
	/**
	 * Deleting edges
	 * 
	 * does NOT work properly. Do not use this! This is only an example how not to do it.
	 */
	@Deprecated
	public void resetConnectionToThisNode()
	{
		List<Node> endNodes = new ArrayList<Node>();
		for(Edge e: this.outgoingConnections)
		{
			endNodes.add(e.endNode);
			if(e.endNode.outgoingConnections.contains(e.endNode,  this))
			{
				e.endNode.outgoingConnections.remove(e.endNode, this);
			}
		}
		this.outgoingConnections.removeAndFreeAllEdges();
	}
	
	/**
	 * Standard UDG neighborhood of this node
	 */
	// @NodePopupMethod(menuText="Neighbor UDG")
	public Set<Node> neighborUDG() {
		Collection<Node> neighborhood = Algorithms.getNeighborNodes(this, Tools.getNodeList());
		for(Node v: neighborhood)
		{
			this.addBidirectionalConnectionTo(v);
		}
		CustomGlobal.drawEdges(this, neighborhood, Color.BLACK);
		return new HashSet<Node>(neighborhood);
	}
	
	/**
	 * Gabriel Graph within the one-hop neighborhood of the current node
	 */
	// @NodePopupMethod(menuText="Neighbor GG")
	public Set<Node> neighborGG() {
		Collection<Node> neighborhood = Algorithms.getNeighborNodes(this, Tools.getNodeList());
		for(Node v: neighborhood)
		{
			this.addBidirectionalConnectionTo(v);
		}
		Set<Node> gabrielNodes = Algorithms.buildGabrielGraph(neighborhood, this, Tools.getNodeList());
		CustomGlobal.drawEdges(this, gabrielNodes, Color.green);
		return gabrielNodes;
	}
		
	/**
	 * Partial Delaunay Triangulation within the one-hop neighborhood of the current node
	 */
	// @NodePopupMethod(menuText="Neighbor PDT")
	public Set<Node> neighborPDT() {
		Collection<Node> neighborhood = Algorithms.getNeighborNodes(this, Tools.getNodeList());
		for(Node v: neighborhood)
		{
			this.addBidirectionalConnectionTo(v);
		}
		Set<Node> PDTNodes = Algorithms.buildPartialDelaunayTriangulation(neighborhood, this);
		CustomGlobal.drawEdges(this, PDTNodes, Color.MAGENTA);
		return PDTNodes;
	}
	
	/**
	 * Partial Delaunay Triangulation Diff Gabriel Graph within the one-hop neighborhood of the current node
	 */
	// @NodePopupMethod(menuText="Neighbor PDT/GG")
	public void neighborDiffPDTandGG() {
		Set<Node> neighborhood = Algorithms.getNeighborNodes(this, Tools.getNodeList());
		for(Node v: neighborhood)
		{
			this.addBidirectionalConnectionTo(v);
		}
		Set<Node> gabrielNodes = Algorithms.buildGabrielGraph(neighborhood, this, Tools.getNodeList());
		Set<Node> PDTNodes = Algorithms.buildPartialDelaunayTriangulation(neighborhood, this);
		PDTNodes.removeAll(gabrielNodes);
		CustomGlobal.drawEdges(this, PDTNodes, Color.BLUE);
	}
	
	// @NodePopupMethod(menuText = "Circumscribed Circle (around two nodes)")
	public void drawCircumcircleAroundTwoPoints() {
		final Node u = this;
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			@Override
			public void handleNodeSelectedEvent(final Node v) {
				if(v == null) {
					return; // aborted
				}
				diskToDraw = Algorithms.disk(u, v, Tools.getNodeList());	
			}
		}, "Select the second node for the circumscribed circle");
	}
	
	// @NodePopupMethod(menuText = "Circumscribed Circle (around three nodes)")
	public void drawCircumcircleAroundThreePoints() {
		final Node u = this;
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			@Override
			public void handleNodeSelectedEvent(final Node n) {
				if(n == null) {
					return; // aborted
				}
				Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
					@Override
					public void handleNodeSelectedEvent(final Node v) {
						if(v == null || v.equals(n)) {
							return; // aborted
						}
						
						diskToDraw = Algorithms.disk(u, n, v, Tools.getNodeList());						
					}
				}, "Select the third node for the circumscribed circle");
			}
		}, "Select the second node for the circumscribed circle");
	}
	
	// @NodePopupMethod(menuText = "Angle between three points (with angle apex
	// at this point)")
	public void angleBetweenThreePoints() {
		final Node v = this;
		v.setColor(Color.BLACK);
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			@Override
			public void handleNodeSelectedEvent(final Node u) {
				if(u == null) {
					return; // aborted
				}
				u.setColor(Color.MAGENTA);
				Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
					@Override
					public void handleNodeSelectedEvent(final Node w) {
						if(w == null) {
							return; // aborted
						}
						w.setColor(Color.MAGENTA);
						final double angle = Algorithms.getAngleBetween2D(u.getPosition(), v.getPosition(), w.getPosition(), false);
						Tools.showMessageDialog("Angle between " 
								+ u.toString() + ", " 
								+ v.toString() + ", " 
								+ w.toString() + " is " + angle + " degrees.");
					}
				}, "Select the third node for the angle");
			}
		}, "Select the second node for the angle");
	}
	
	// @NodePopupMethod(menuText = "Signed Angle between three points (with
	// angle apex at this point)")
	public void signedAngleBetweenThreePoints() {
		final Node v = this;
		v.setColor(Color.BLACK);
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			@Override
			public void handleNodeSelectedEvent(final Node u) {
				if(u == null) {
					return; // aborted
				}
				u.setColor(Color.MAGENTA);
				Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
					@Override
					public void handleNodeSelectedEvent(final Node w) {
						if(w == null || w.equals(u)) {
							return; // aborted
						}
						w.setColor(Color.MAGENTA);
						final double angle = Algorithms.getSignedAngleBetween(u.getPosition(), v.getPosition(), w.getPosition(), false);
						Tools.showMessageDialog("Angle between " 
								+ u.toString() + ", " 
								+ v.toString() + ", " 
								+ w.toString() + " is " + angle + " degrees.");
					}
				}, "Select the third node for the angle");
			}
		}, "Select the second node for the angle");
	}
	
	// @NodePopupMethod(menuText = "Colorize Open Half Planes between two
	// nodes")
	public void colorizeOpenHalfPlanesBetweenTwoPoints() {
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			@Override
			public void handleNodeSelectedEvent(Node n) {
				if(n == null) {
					return; // aborted
				}
				
				Triple<Set<Node>,Set<Node>,Set<Node>> nodeSets = Algorithms.divideNodesInHalfPlanes(getPosition(), n.getPosition(), Tools.getNodeList());
				for(Node v: nodeSets.first)
				{
					v.setColor(Color.ORANGE);
					
				}
				for(Node v: nodeSets.second)
				{
					v.setColor(Color.GREEN);
					
				}
				for(Node v: nodeSets.third)
				{
					v.setColor(Color.BLUE);					
				}
			}
		}, "Select a node to define the line");
	}
		
	/**
	 * Test for edge intersection routine
	 */
	// @NodePopupMethod(menuText="Test edge intersection")
	public void testEdgeIntersection()
	{
		try {
			Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
				@Override
				public void handleNodeSelectedEvent(final Node v) {
					if(v == null) {
						return; // aborted
					}
					for(Edge e0: outgoingConnections)
					{
						for(Edge e1: v.outgoingConnections)
						{
							if(e0.equals(e1) || e0.equals(e1.oppositeEdge))
								continue;

							Position intersect = ((DistEdge) e0).getIntersectionWith((DistEdge) e1);
							if(intersect == null)
							{
								Tools.getTextOutputPrintStream().println( e0.toString() + "is not intersecting with " + e1.toString() );
							} else
							{
								Tools.getTextOutputPrintStream().println( e0.toString() + " intersects with " + e1.toString() + " in "
										+ intersect.toString());
							}
						}
					}
				}
			}, "Select the second node for edges of another node.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		this.drawingSizeInPixels = (int) (defaultDrawingSizeInPixels * pt.getZoomFactor()) / 12;
		this.drawAsDisk(g, pt, highlight, drawingSizeInPixels);		
	} //end draw

	@Override
	public String toString() {
		String s = "Node(" + this.ID + ") [";
		Iterator<Edge> edgeIter = this.outgoingConnections.iterator();
		while(edgeIter.hasNext()){
			Edge e = edgeIter.next();
			Node n = e.endNode;
			s+=n.ID+" ";
		}
		return s + "]";
	}
	
	  // just omitted null checks
	  @Override
	  public int hashCode() {
	    final int hash = 3;
	    //hopefully ID will not change, but if it does, an immutable ID has to be introduced
	    return 7 * hash + this.ID;
	  }
}
