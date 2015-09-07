package projects.reactiveSpanner.routing;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.nodes.edges.DistEdge;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.NotYetImplementedException;
import sinalgo.nodes.Position;
import sinalgo.nodes.edges.Edge;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.storage.SortableVector;

/**
 * Class for Face Routing
 * <br>
 * According to Frey and Stojmenović there are different variants of Face Routing whether are continuative or volatile strategies.
 * The implemented variant here is the continuative <b>Greedy-Face-Greedy (GFG)</b> where the start position of the virtual edge between 
 * the start and the destination position (SD-line) is set to intersections with the SD-line that are closer to the destination than 
 * previously discovered intersections (and therefore  are closer to the destination as the start position of the SD-line). 
 * <br>
 * For the selection of the next hop also exists different variants whose are depending on crossing edges through the SD-Line.
 * The used variant here is <b>after crossing</b> of the SD-line.
 * 
 * @see On Delivery Guarantees and Worst-Case Forwarding Bounds of Elementary Face Routing Components in Ad Hoc and Sensor Networks
 * by Frey, Stojmenović (2010)
 * 
 * @author Matthias von Steimker
 * @param <T> type of nodes that are used for routing
 */
public class FaceRouting<T extends PhysicalGraphNode> extends RoutingProtocol<T> 
{	
	private class SDLine
	{					
		Position start;
		Position destination;
		
		private Position lastIntersectionFound;
		
		private SDLine(final Position startPos, final Position destinationPos)
		{	
			this.start = startPos;
			this.destination = destinationPos;
		}	
		
		/**
		 * Examine if the given <code>Position</code> is between the <code>start</code> and the <code>destination</code> of this <code>SDLine</code>
		 * @param toExamine position
		 * @return is between <code>start</code> and <code>destination</code>
		 */
		private boolean isPositionOnLine(final Position toExamine)
		{
			return Algorithms.isCollinear(start, destination, toExamine) && Algorithms.isWithin(start, toExamine, destination);
		}
		
		/**
		 * Examine if the edge represented by the two Position parameter is intersecting with this SDLine
		 * When the edge is intersecting, the intersection will be saved as last found intersection to use later.
		 * 
		 * @param edgePos0	start/end Position of the edge
		 * @param edgePos1	end/start Position of the edge
		 * @return is edge intersecting with the SDLine
		 */
		private boolean isIntersectingWith(final Position edgePos0, final Position edgePos1)
		{
			Position intersection = DistEdge.getIntersectionWith(start, destination, edgePos0, edgePos1);
			if(intersection != null && !start.equals(intersection))
			{
				lastIntersectionFound = intersection;
				return true;
			}
			else
			{
				return false;
			}
		}
		
		/**
		 * Get the last occurred intersection and set the token to null. So when you call this method two times in a row, the second 
		 * call will return <b>null</b>
		 * @return last found intersection or <b>null</b> if there did not occur any intersection
		 */
		private Position consumeLastIntersectionFound()
		{
			Position intersection = lastIntersectionFound;
			lastIntersectionFound = null;
			return intersection;
		}
	}
	
	/**
	 *	Node that has an greater angle(neighbor, current holder, last holder) is first
	 */
	private class RightHandedAngleComparator implements Comparator<T>
	{
		@Override
		public int compare(T o1, T o2) {
			final double o1Angle = Algorithms.getAngleBetween2D(o1.getPosition(), currentHolder.getPosition(), lastHolder.getPosition(), false);
			final double o2Angle = Algorithms.getAngleBetween2D(o2.getPosition(), currentHolder.getPosition(), lastHolder.getPosition(), false);
			return o1Angle > o2Angle? -1 : o1Angle == o2Angle ? 0 : 1;	//ordering in opposite direction to avoid reording of the last holding node to the end of the list
		}		
		@Override
		public String toString()
		{
			return "RIGHT_HANDED_RULE";
		}
	}
	
	/**
	 *	Node that has an greater angle(last holder, current holder, neighbor) is first
	 */
	private class LeftHandedAngleComparator implements Comparator<T>
	{
		@Override
		public int compare(T o1, T o2) {
			final double o1Angle = Algorithms.getAngleBetween2D(lastHolder.getPosition(), currentHolder.getPosition(), o1.getPosition(), false);
			final double o2Angle = Algorithms.getAngleBetween2D(lastHolder.getPosition(), currentHolder.getPosition(), o2.getPosition(), false);
			return o1Angle > o2Angle? -1 : o1Angle == o2Angle ? 0 : 1; //ordering in opposite direction to avoid reording of the last holding node to the end of the list
		}
		@Override
		public String toString()
		{
			return "LEFT_HANDED_RULE";
		}
	}
	
	private enum EFaceRoutingState{ON_SD_LINE, FACE_TRAVERSING, SWITCH_FACE};
	
	private final Comparator<T> leftHandedRule = new LeftHandedAngleComparator();
	private final Comparator<T> rightHandedRule = new RightHandedAngleComparator();
	
	/**
	 * The comparator that is used for the face traversing, either left or right handed
	 */
    private Comparator<T> activeTraversalRule;
    /**
     * Flag to determine which rule is currently in use
     */
    private boolean isLeftHanded;
	/**
	 * The current state of the face routing event. Mainly used to determine if a left or right handed rule is used
	 */
	private EFaceRoutingState currentFaceRoutingState;
	/**
	 * The virtual edge between the starting node and end destination node of the routing algorithm
	 */
	private SDLine virtualSDline;
	/**
	 * The first selected edge by this routing event. This edge will be used to determine if the destination is reachable
	 */
	private Edge firstSelectedEdge;
	/**
	 * The hop number when the last time the firstSelectedEdge was changed
	 */
	private int lastHopChangedFirstSelectedEdge;
	
	protected FaceRouting(T sourceNode, T destinationNode,	T currentHolder, EStrategy subgraphStrategy)
	{
		super(sourceNode, destinationNode, currentHolder, subgraphStrategy, ERouting.FACE_ROUTING);
	}

	@Override
	protected void init() {
		firstSelectedEdge = null;
		lastHopChangedFirstSelectedEdge = -1;	//there was no hop done when this algorithm has been initialized
		lastHolder = null;	//TODO move to a init of RoutingProtocol
		activeTraversalRule = null;
		isLeftHanded = false;
		
		//observe  whether the currently holding node is settled on the SD-Line or not
		if(currentHolder.equals(super.getSource()))
		{
			currentFaceRoutingState = EFaceRoutingState.ON_SD_LINE;
			virtualSDline = new SDLine(currentHolder.getPosition(), super.getDestination().getPosition());
		}
		else
		{
//			//TODO currently not working:the algorithm is initialized with currentholder != source node. there is not first edge nor the virtualSDline setted yet
//			final double angleInDegrees = Algorithms.getAngleBetween2D(this.currentHolder.getPosition(), super.getSource().getPosition(), super.getDestination().getPosition(), false);
//			
//			//decide whether to use left or right handed rule
//			if(angleInDegrees < 180.0)
//			{
//				traversalRuleHandlingComparator = rightHandedRule;
//				isLeftHanded = false;
//				logger.logln(LogL.INFO, "Using RIGHT_HANDED_RULE to traverse faces for routing event " + super.routingID);
//			}
//			else
//			{	
//				traversalRuleHandlingComparator = leftHandedRule;
//				isLeftHanded = true;
//				logger.logln(LogL.INFO, "Using LEFT_HANDED_RULE to traverse faces for routing event " + super.routingID);
//			}
//			currentFaceRoutingState = EFaceRoutingState.FACE_TRAVERSING;
			throw new NotYetImplementedException("Face-Routing currently does not support the initiale case of inequality of source node and currently holding node");
		}
	}
	
	@Override
	protected void _requestedNextRoutingStep()
	{
		nextHop = calculateNextHop();
	}

	@Override
	protected boolean _isStucked()
	{
		if(firstSelectedEdge == null)
		{
			return false;	// the first selected edge is not set, so this algorithm has just started or switched a face and is not stucking
		}
		else if(subgraphInterface.getSubgraphNodes().size() == 0)
		{
			return true;  // the holding node has no neighbors and cannot progress
		}
		else if(currentHolder.equals(firstSelectedEdge.startNode) && nextHop.equals(firstSelectedEdge.endNode) 
				&& super.getNumberOfHops() != lastHopChangedFirstSelectedEdge)
		{
			return true; // the selected edge equals the first edge this algorithm has processed (since face switching), this could only happen when no path to the destination exists
		}
		else
		{
			return false; // routing is still progressing	
		}
	}
	
	/**
	 * @return the next hop or <b>null</b> if a next hop have not been found
	 */
	private T calculateNextHop()
	{
		Set<T> subgraphNodes = subgraphInterface.getSubgraphNodes();
		//TODO taking only nodes in UDG distance to the holding node, make also other connectivity models available
		subgraphNodes = Algorithms.getOneHopNeighbors(currentHolder, subgraphNodes);
		
		// is the destination already a neighbor of the current holder?
		if(subgraphNodes.contains(super.getDestination()))
		{
			return super.getDestination();
		}
		
		switch(currentFaceRoutingState)
		{
		case ON_SD_LINE:
			T nextHopAfterFirstEdgeFinding = handleOnLine(subgraphNodes);
			firstSelectedEdge = currentHolder.getEdgeTo(nextHopAfterFirstEdgeFinding);
			lastHopChangedFirstSelectedEdge = super.getNumberOfHops();
			logger.logln(LogL.INFO, "First selected edge for Face-Routing operation " + super.routingID + " is " + firstSelectedEdge);
			logger.logln(LogL.INFO, "Using "+ activeTraversalRule + " to traverse faces for routing event " + super.routingID);
			if(Tools.isSimulationInGuiMode())
			{
				Tools.appendToOutput("First selected edge is " + firstSelectedEdge + "\n");
				Tools.appendToOutput("Using " + activeTraversalRule + "\n");
			}
			return nextHopAfterFirstEdgeFinding;
		case FACE_TRAVERSING:
			return handleFaceTraversal(subgraphNodes);
		case SWITCH_FACE:
			T nextHopAfterSwitchFace = handleFaceTraversal(subgraphNodes);
			firstSelectedEdge = currentHolder.getEdgeTo(nextHopAfterSwitchFace);
			lastHopChangedFirstSelectedEdge = super.getNumberOfHops();
			logger.logln(LogL.INFO, "First selected edge changed to " + firstSelectedEdge + " and resume with "+ currentFaceRoutingState + " for Face-Routing operation " + super.routingID);
			if(Tools.isSimulationInGuiMode())
			{
				Tools.appendToOutput("First selected edge is " + firstSelectedEdge + "\n");
			}
			return nextHopAfterSwitchFace;
		default:
			throw new RuntimeException("Invalid FaceRouting state occured by Routing Event " + super.routingID);	
		}
	}

	/**
	 * Handle processing when the currently holding node lies between source and destination (on the SDLine)
	 * 
	 * @param subgraphNodes	neighbors of the node
	 * @return the next hop
	 */
	private T handleOnLine(final Set<T> subgraphNodes)
	{
		if(subgraphNodes.isEmpty())
			return null;
			
		T angleMinimizingNode = getAngleMinimizingNode(subgraphNodes);
		//if the position of the first neighbor is on the line, we would reset the start of SDLine to the position of new node and return the new node immediately
		if(virtualSDline.isPositionOnLine(angleMinimizingNode.getPosition()) && !angleMinimizingNode.getPosition().equals(virtualSDline.start))
		{
			currentFaceRoutingState = EFaceRoutingState.ON_SD_LINE;
			virtualSDline.start = angleMinimizingNode.getPosition();
			logger.logln(LogL.INFO, "Found next hop " + angleMinimizingNode + " lays on SDLine for Face Routing operation " + super.routingID + ". Changing state to " + currentFaceRoutingState);
			if(Tools.isSimulationInGuiMode())
			{
				Tools.appendToOutput("Node on line\n");
			}
			return angleMinimizingNode;
		}	
		decideForRule(angleMinimizingNode.getPosition(), currentHolder.getPosition(), virtualSDline.destination, false);
		T nextHop = null;
		nextHop = angleMinimizingNode;
		currentFaceRoutingState = EFaceRoutingState.SWITCH_FACE;
		logger.logln(LogL.INFO, "Using "+ activeTraversalRule + " to traverse faces for routing event " + super.routingID);
		if(Tools.isSimulationInGuiMode())
		{
			Tools.appendToOutput("Using " + activeTraversalRule + "\n");
		}
		return nextHop;
	}

	/**
	 * find the one node of given subgraph nodes that minimizes the unsigned angle(node, currentHolder, destination)
	 * @param subgraphNodes neighbors of the current holding node
	 * @return angle minimizing node
	 */
	private T getAngleMinimizingNode(Set<T> subgraphNodes)
	{
		//pre calculations for the first entry of the subgraphNodes
		T angleMinimizingNode = subgraphNodes.iterator().next();
		double minimizingAngle = Algorithms.getSignedAngleBetween(angleMinimizingNode.getPosition(), currentHolder.getPosition(), virtualSDline.destination, false);	//in degrees
		
		//search for the node that minimize the angle between start of SDLine, the neighbor node and the end of SDLine 
		for(T node: subgraphNodes)
		{			
			final double newAngle = Algorithms.getSignedAngleBetween(node.getPosition(), currentHolder.getPosition(), virtualSDline.destination, false);
			if(newAngle < minimizingAngle)
			{
				angleMinimizingNode = node;
				minimizingAngle = newAngle;
			}
		}
		return angleMinimizingNode;
	}
	
	/**
	 * Corresponding to the previously selected rule (right handed or left handed) the next hop is the one that is a node of the current face.
	 * A face is defined by a number of nodes with edges those do not cross each other or the SDLine
	 * @param subgraphNodes	neighbors of the currently holding node
	 * @return the next hop neighbor
	 */
	private T handleFaceTraversal(final Set<T> subgraphNodes)
	{	
		if(currentFaceRoutingState.equals(EFaceRoutingState.SWITCH_FACE))
		{
			decideForRule(virtualSDline.destination, currentHolder.getPosition(), lastHolder.getPosition(), false);
		}
		
		List<T> sortedList = getSortedNeighborsAccordingToRule(subgraphNodes);
		//the last holder should be at the end of the list
		assert(sortedList.indexOf(super.lastHolder) == sortedList.size() -1);
		
		T crossingEdgeEndNode = scanForCrossingEdgesOrNodesOnLine(sortedList);
		if(crossingEdgeEndNode == null)
		{
			currentFaceRoutingState = EFaceRoutingState.FACE_TRAVERSING;
			logger.logln(LogL.INFO, "Using "+ activeTraversalRule + " to traverse faces for routing event " + super.routingID);
			if(Tools.isSimulationInGuiMode())
			{
				Tools.appendToOutput("Using " + activeTraversalRule + "\n");
			}
			return sortedList.get(0);
		}
		switchRule(true);
		return crossingEdgeEndNode; //end node of last crossing edge in list
	}

	/**
	 * Decide between right handed or left handed rule. If the angle(leftPos, apexPos, rightPos) is greater than 180 degrees, the right handed rule will be used.
	 * @param leftPos left position according to apex position
	 * @param apexPos peak position of the angle
	 * @param rightPos right position according to apex position
	 * @param loggingOutput write in logger which rule has been chosen
	 */
	private void decideForRule(final Position leftPos, final Position apexPos, final Position rightPos, final boolean loggingOutput) {
		//use the unsigned angle to estimate if the chosen angle is left or right of the SDLine and the set the rule accordingly
		if(Algorithms.getAngleBetween2D(leftPos, apexPos, rightPos, false) > 180.0)
		{
			activeTraversalRule = rightHandedRule;
			isLeftHanded = false;
		}
		else
		{
			activeTraversalRule = leftHandedRule;
			isLeftHanded = true;
		}
		if(loggingOutput)
		{
			logger.logln(LogL.INFO, "Using "+ activeTraversalRule + " to traverse faces for routing event " + super.routingID);
			if(Tools.isSimulationInGuiMode())
			{
				Tools.appendToOutput("Using " + activeTraversalRule + "\n");
			}
		}
	}
	
	/**
	 * Sort the set of neighbors according to their angle and save the result in a sorted list.
	 * <br>For <i>right handed rule</i> the greatest <b>angle(neighbor, current holder, last holder)</b> is first (clockwise direction from edge [current holder, last holder]).
	 * <br>For <i>left handed rule</i> the greatest <b>angle(last holder, current holder, neighbor)</b> is first (counterclockwise direction from edge [current holder, last holder]).
	 * @param subgraphNodes neighbors of the current holder
	 * @return the sorted List
	 */
	private final List<T> getSortedNeighborsAccordingToRule(final Collection<T> subgraphNodes)
	{
		SortableVector<T> sortedArray = new SortableVector<T>(subgraphNodes.size());
		sortedArray.addAll(subgraphNodes);
		sortedArray.sort( activeTraversalRule);
		return sortedArray;
	}
	
	/**
	 * Iterate through the sorted list of neighbor and return the end node of the last outgoing edge that intersects the SD-line (if any outgoing 
	 * edge intersects it). Also the start position will be replaced with the closest (to the destination) found intersection. 
	 * @param sortedList of neighbors of the currentHolder
	 * @return the end node of the last outgoing edge that crosses the SD-line. Returns <b>null</b> if there is no crossing edge.
	 */
	private T scanForCrossingEdgesOrNodesOnLine(List<T> sortedList)
	{
		T crossingEdgeEndNode = null;
		boolean hasIntersectingEdgeDetected = false;
		for(T v: sortedList)
		{
			if(v.equals(lastHolder) || v.getPosition().equals(virtualSDline.start))
				continue;
			
			if(virtualSDline.isPositionOnLine(v.getPosition()))
			{
				this.currentFaceRoutingState = EFaceRoutingState.ON_SD_LINE;
				assert(!virtualSDline.isIntersectingWith(currentHolder.getPosition(), v.getPosition()));
				logger.logln(LogL.INFO, "Found next hop " + v + " lays on SDLine for Face Routing operation " + super.routingID);
				if(Tools.isSimulationInGuiMode())
				{
					Tools.appendToOutput("Node on line\n");
				}
				virtualSDline.start = v.getPosition();
				return v; //neighbor lays on the SD-line
			}
			else if(virtualSDline.isIntersectingWith(currentHolder.getPosition(), v.getPosition()))
			{
				if(!hasIntersectingEdgeDetected)
				{
					hasIntersectingEdgeDetected = true;
					this.currentFaceRoutingState = EFaceRoutingState.SWITCH_FACE;
					logger.logln(LogL.INFO, "Face-Routing operation " + super.routingID + " found end of face.");
					if(Tools.isSimulationInGuiMode())
					{
						Tools.appendToOutput("Switch face\n");
					}
				}
				virtualSDline.start = virtualSDline.consumeLastIntersectionFound();
				crossingEdgeEndNode = v; //last found neighbor that lays "behind" the SD-line
			}
		}
		return crossingEdgeEndNode;
	}
	
	/**
	 * Switching the currently used rule (from left handed rule to right handed rule or vice versa from right handed rule to left handed rule)
	 * @param loggingOutput write in logger which rule has been chosen
	 */
	private final void switchRule(final boolean loggingOutput)
	{
		if(isLeftHanded)
		{
			activeTraversalRule = rightHandedRule;
		} else
		{
			activeTraversalRule = leftHandedRule;
		}
		isLeftHanded = !isLeftHanded;
		if(loggingOutput)
		{
			logger.logln(LogL.INFO, "Using "+ activeTraversalRule + " to traverse faces for routing event " + super.routingID);
			if(Tools.isSimulationInGuiMode())
			{
				Tools.appendToOutput("Using " + activeTraversalRule + "\n");
			}
		}
	}
}
