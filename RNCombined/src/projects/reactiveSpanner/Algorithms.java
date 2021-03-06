package projects.reactiveSpanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.xml.transform.Source;

import projects.reactiveSpanner.FloydWarshall.AdjMatrixEdgeWeightedDigraph;
import projects.reactiveSpanner.FloydWarshall.DirectedEdge;
import projects.reactiveSpanner.FloydWarshall.FloydWarshall;
import projects.reactiveSpanner.nodes.messageHandlers.AbstractMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.Barriere.BarriereMessageHandler;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import projects.rmys.Algorithms_ext;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.nodes.NotYetImplementedException;
import sinalgo.nodes.Position;
import sinalgo.tools.Tools;
import sinalgo.tools.Triple;
import sinalgo.tools.Tuple;
		
public class Algorithms {

	static final double TOL = 0.0000001;
	static final double COLLINEARITY_TOL = 0.01;
	static final double POSITION_WITHING_TOL = 0.001;
	
	/*****************************************************************************
	 *****************************************************************************
	 *****************************************************************************
	 */
	
	/**
	 * Get the set of nodes that are in the disk with radius rMax around a given Position centralPos
	 * @param <T> type of Node
	 * @param centralPos middle point position of disk of radius rMax
	 * @param rMax max radius i. e. distance that a node could have to centralPos
	 * @return Set of all nodes that fulfills the conditions
	 */
	public static <T extends Node> Set<T> getNearNodes(final Position centralPos, final double rMax, final Iterable<T> workingSet)
	{
		Set<T> neighborNodes = new HashSet<T>();
		for(T v: workingSet)
		{
			if(centralPos.distanceTo(v.getPosition()) <= rMax) 
			{
				neighborNodes.add(v);
//				System.out.println("NodeID=" + central.ID + " NeighborID=" + dstNode.ID + "\nd(n,m)=" + dstNode.getPosition().distanceTo(central.getPosition()));
			}
		}
//		System.out.println("neighborsNodes: " + neighborNodes.toString());
		return neighborNodes;
	}
	
	/**
	 * Get the set of Positions that are in the disk with radius rMax around a given Position centralPos
	 * @param centralPos middle point position of disk of radius rMax
	 * @param rMax max radius i. e. distance that a node could have to centralPos
	 * @return Set of all Positions that fulfill the conditions
	 */
	public static Set<Position> getNearPositions(final Position centralPos, final double rMax, final Iterable<Position> workingSet)
	{
		Set<Position> neighborPositions = new HashSet<Position>();
		for(Position p : workingSet)
		{
			if(centralPos.distanceTo(p) <= rMax) 
			{
				neighborPositions.add(p);
//				System.out.println("NodeID=" + central.ID + " NeighborID=" + dstNode.ID + "\nd(n,m)=" + dstNode.getPosition().distanceTo(central.getPosition()));
			}
		}
//		System.out.println("neighborsNodes: " + neighborNodes.toString());
		return neighborPositions;
	}

	
		/**
		 * Return the set of nodes that are in the disk with in the config defined radius rMax around a given node n EXCLUDING node n itself
		 * @param <T> type of nodes
		 **/
		public static <T extends Node> Set<T> getNeighborNodes(final T srcNode, final Iterable<T> workingSet)
		{
			double r = -1.0;
			try {
				r = Configuration.getDoubleParameter("UDG/rMax");
			} catch (CorruptConfigurationEntryException e) {
				Tools.fatalError(e.getMessage());
			}
	//		System.out.println("_DEBUG:getNeighborNodes: r=" +r);
			Set<T> neighborNodes = Algorithms.getNearNodes(srcNode.getPosition(), r, workingSet);
			neighborNodes.remove(srcNode);
			return neighborNodes;
		}

		public static <T extends Node> Set<T> getOneHopNeighbors(final T srcNode, Set<T> workingSet)
		{
			Set<T> oneHopNodes = getNeighborNodes(srcNode, workingSet);
			
			return oneHopNodes;
		}

		/**
		 * Method to get the Position between Position p0 and Position p1
		 * @param p0 first Position
		 * @param p1 second Position
		 * @return Position between
		 */
		public static final Position getCentralPos(final Position p0, final Position p1)
		{
			final double xCoord = 0.5 *(p0.xCoord + p1.xCoord);
			final double yCoord = 0.5 *(p0.yCoord + p1.yCoord);
			final double zCoord = 0.5 *(p0.zCoord + p1.zCoord);
			return new Position(xCoord, yCoord, zCoord);
		}
		
		/**
		 * Method to get the Position between two Nodes n0 and n1
		 * @param <T> type of Node
		 * @param n0 first Node
		 * @param n1 second Node
		 * @return Position between
		 */
		public static final <T extends Node> Position getCentralPos(final T n0, final T n1)
		{
			return getCentralPos(n0.getPosition(), n1.getPosition());
		}

		public static Set<Node> getNodeListCopy()
		{
			Set<Node> nodes = new HashSet<Node>();
			for(Node v: Tools.getNodeList())
			{
				nodes.add(v);
			}
			return nodes;
		}
		
		
		
		/**
			 * Observe if there are other nodes in the disk with diameter (u, v)
			 * @param u first Node
			 * @param v second Node
			 * @return List of nodes that are in the disk with diameter (u, v) (excluding u and v)
			 */
			public static Disk2D disk(final Node u, final Node v, final Iterable<? extends Node> workingSet)
			{
				if(u == null || v == null)
				{
					throw new NullPointerException("One or both nodes for the selected disk operation does'nt exists");
				}
				
				//Observation will be done by getting position in center between u and v and then
				// by observing if there are other nodes in the circle around the center position
				Position diskMidPointPosition = getCentralPos(u.getPosition(), v.getPosition());
				final double r = 0.5 * u.getPosition().distanceTo(v.getPosition());
				Set<? extends Node> neighborsInDisk = getNearNodes(diskMidPointPosition, r, workingSet);
				
				//we are'nt interested in the border nodes u, v and w
				neighborsInDisk.remove(u);
				neighborsInDisk.remove(v);
				
				return new Disk2D(diskMidPointPosition, r, neighborsInDisk);
			}
			
			/**
			 * Observe if there are other nodes in the disk spanned by u, v and w. Works only in 2D at the moment.
			 * @param u first Node
			 * @param v second Node
			 * @param w third Node
			 * @return List of nodes that are in the disk spanned by u, v and w
			 * @throws IllegalArgumentException if the 3 nodes are on one line
			 */
			public static Disk2D disk(final Node u, final Node v, final Node w, final Iterable<? extends Node> workingSet)
			{
				//algorithm from http://stackoverflow.com/questions/4103405/what-is-the-algorithm-for-finding-the-center-of-a-circle-from-three-points
				
				
				final double offset = Math.pow(v.getPosition().xCoord,2) + Math.pow(v.getPosition().yCoord,2);
			    final double bc =   ( Math.pow(u.getPosition().xCoord,2) + Math.pow(u.getPosition().yCoord,2) - offset )/2.0;
			    final double cd =   (offset - Math.pow(w.getPosition().xCoord, 2) - Math.pow(w.getPosition().yCoord, 2))/2.0;
			    final double det =  (u.getPosition().xCoord - v.getPosition().xCoord) * (v.getPosition().yCoord - w.getPosition().yCoord) 
			    		- (v.getPosition().xCoord - w.getPosition().xCoord) * (u.getPosition().yCoord - v.getPosition().yCoord); 
				
			    if(Math.abs(det) < TOL){ throw new IllegalArgumentException("The 3 given nodes are collinear (on the same line). There is no circle."
			    		+ "\n" + u.toString() + ", " + v.toString() + ", " + w.toString()); }
			    
			    final double idet = 1/det;

			    final double centerx =  (bc * (v.getPosition().yCoord - w.getPosition().yCoord) 
			    		- cd * (u.getPosition().yCoord - v.getPosition().yCoord)) * idet;
			    final double centery =  (cd * (u.getPosition().xCoord - v.getPosition().xCoord) 
			    		- bc * (v.getPosition().xCoord - w.getPosition().xCoord)) * idet;
			    final double radius = 
			       Math.sqrt( Math.pow(v.getPosition().xCoord - centerx,2) + Math.pow(v.getPosition().yCoord-centery,2));
			    
				Position diskMidPointPosition = new Position(centerx, centery, 0);

				Set<? extends Node> neighborsInDisk = getNearNodes(diskMidPointPosition, radius, workingSet);
		
				//we are'nt interested in the border nodes u, v and w
				neighborsInDisk.remove(u);
				neighborsInDisk.remove(v);
				neighborsInDisk.remove(w);
				
				return new Disk2D(diskMidPointPosition,radius, neighborsInDisk);
			}
			
			/**
			 * Observe if there are other Positions in the disk spanned by u, v and w. Works only in 2D at the moment.
			 * @param u first Position
			 * @param v second Position
			 * @param w third Position
			 * @return List of Positions that are in the disk spanned by u, v and w
			 * @throws IllegalArgumentException if the three Positions are on one line
			 */
			public static Set<Position> positionDisk(final Position u, final Position v, final Position w, final Iterable<Position> workingSet)
			{
				//algorithm from http://stackoverflow.com/questions/4103405/what-is-the-algorithm-for-finding-the-center-of-a-circle-from-three-points
				
				
				final double offset = Math.pow(v.xCoord,2) + Math.pow(v.yCoord,2);
			    final double bc =   ( Math.pow(u.xCoord,2) + Math.pow(u.yCoord,2) - offset )/2.0;
			    final double cd =   (offset - Math.pow(w.xCoord, 2) - Math.pow(w.yCoord, 2))/2.0;
			    final double det =  (u.xCoord - v.xCoord) * (v.yCoord - w.yCoord) 
			    		- (v.xCoord - w.xCoord) * (u.yCoord - v.yCoord); 
				
			    if(Math.abs(det) < TOL){ throw new IllegalArgumentException("The 3 given nodes are collinear (on the same line). There is no circle."); }
			    
			    final double idet = 1/det;

			    final double centerx =  (bc * (v.yCoord - w.yCoord) 
			    		- cd * (u.yCoord - v.yCoord)) * idet;
			    final double centery =  (cd * (u.xCoord - v.xCoord) 
			    		- bc * (v.xCoord - w.xCoord)) * idet;
			    final double radius = 
			       Math.sqrt( Math.pow(v.xCoord - centerx,2) + Math.pow(v.yCoord-centery,2));
			    
				Position diskMidPointPosition = new Position(centerx, centery, 0);

				Set<Position> neighborsInDisk = getNearPositions(diskMidPointPosition, radius, workingSet);
		
				//we aren't interested in the border nodes u, v and w
				neighborsInDisk.remove(u);
				neighborsInDisk.remove(v);
				neighborsInDisk.remove(w);
				
				return neighborsInDisk;
			}
			
	/**
	 * Observe wether a Position p is contained in any circle defined by three
	 * non-collinear Positions of workingSet and calculates a new x-coordinate
	 * so that p is not contained in any circle.
	 * add declares in which direction the x-coordinate shall be shifted.
	 * add = true : increases x-coordinate
	 * 
	 * @param workingSet
	 * @param p
	 * @param add
	 * @return
	 */
	public static Position hasEnclosingCircle(Set<Position> workingSet,
			Position p, boolean add) {

		Position result = p;

		// get all permutations of three non-colinear nodes (=positions)
		ArrayList<PositionTriple> triples = PositionTriple
				.potenzmenge(workingSet);

		// get triple that has a circle enclosing p
		for (PositionTriple triple : triples) {
			Set<Position> positionsInCircle = positionDisk(triple.getX(),
					triple.getY(), triple.getZ(), workingSet);
			if (positionsInCircle.contains(p)) {
				do {
					if (add) {
						result.xCoord += 0.1;
					} else {
						result.xCoord -= 0.1;
					}
					positionsInCircle = positionDisk(triple.getX(),
							triple.getY(), triple.getZ(), workingSet);
				} while (positionsInCircle.contains(p));

			}
		}

		return result;
	}
	
	/**
	 * Calculate Gabriel Graph Nodes for a given srcNode and its connectedNodes
	 * @param <T>
	 * @param neighborhood
	 * @param srcNode
	 */
	public static <T extends Node> Set<T> buildGabrielGraph(final Collection<T> neighborhood, final Node srcNode, final Iterable<? extends Node> workingSet)
	{
		//Set for all nodes that are connected to srcNode in the Gabriel Graph
		//We are putting the complete neighborhood into the set of gabriel Nodes
		//and sorting out nodes that have no gabriel edge to srcNode
		Set<T> gabrielNodes = new HashSet<T>(neighborhood);
		for(T u1: neighborhood)
		{
			//extract nodes that are in the disk with diameter (u1, srcNode)
			Set<? extends Node> nodesInDisk = disk(u1, srcNode, workingSet).nodesInDisk;
			for(Node u2: nodesInDisk)
			{
				if(!(u1.equals(u2)) && neighborhood.contains(u2))
				{
					gabrielNodes.remove(u1);
				}
			}
		}
		return gabrielNodes;
	}

	/**
	 * Check if the Node <b>toBeChecked</b> is in the circle between <b>circlePoint1</b> and <b>circlePoint2</b>.
	 * The circle has the diameter of circlePoint1 to circlePoint2.
	 * 
	 */
	public static boolean isInGabrielCircle(final Node toBeChecked, final Node circlePoint1, final Node circlePoint2)
	{
		//Observation will be done by getting position in center between u and v and then
		// by observing if distance from toBeChecked to center is smaller than the radius
		Position diskMidPointPosition = getCentralPos(circlePoint1.getPosition(), circlePoint2.getPosition());
		final double r = circlePoint1.getPosition().distanceTo(diskMidPointPosition);
		final double disToBeChecked = toBeChecked.getPosition().distanceTo(diskMidPointPosition);

		return disToBeChecked < r;
	}

	/**
	 * Get the node that is nearest to a given position
	 * @param <T>
	 * @param toThisPos the selected position
	 * @param workingSet Set of nodes, that has to be observed
	 * @return nearest node of set workingSet
	 * @throws RuntimeException if workingSet is empty
	 */
	public static <T extends Node> T getNearestNode(final Position toThisPos, final Iterable<T> workingSet)
	{
		if(!workingSet.iterator().hasNext())
		{
			throw new RuntimeException("Algorithms::getClostestNode: given parameter workingSet is empty!");
		}

		T closest = workingSet.iterator().next();
		for(T v: workingSet)
		{
			if(v.getPosition().squareDistanceTo(toThisPos) < closest.getPosition().squareDistanceTo(toThisPos))
			{
				closest = v;
			}
		}
		return closest;
	}

	/**
	 * Get the signed angle between Position <code>pos1</code>, Position <code>center</code> and Position <code>pos2</code>.
	 * 
	 * @param pos1 position of first opposite side
	 * @param apexPos applied position of angle contained by vector (pos1 - center) and vector (pos2 - center)
	 * @param pos2 position of second opposite side
	 * @param inRadians flag to indicate if angle is returned in degrees or radiant
	 * @return angle in radiant if <code>inRadians</code> is true and vice versa in degrees if <code>inRadians</code> is false
	 */
	public static double getSignedAngleBetween(final Position pos1, final Position apexPos, final Position pos2, final boolean inRadians)
	{	
		//first vector
		double v1x = pos1.xCoord - apexPos.xCoord;
		double v1y = pos1.yCoord - apexPos.yCoord;
		double v1z = pos1.zCoord - apexPos.zCoord;			
		double l1 = Math.sqrt(v1x * v1x + v1y * v1y + v1z * v1z);

		//second vector
		double v2x = pos2.xCoord - apexPos.xCoord;
		double v2y = pos2.yCoord - apexPos.yCoord;
		double v2z = pos2.zCoord - apexPos.zCoord;			
		double l2 = Math.sqrt(v2x * v2x + v2y * v2y + v2z * v2z);

		double dotPv1v2 = v1x * v2x + v1y * v2y + v2z * v2z;

		final double denominator = l1 * l2;
		if(denominator == 0){
			throw new RuntimeException("Divide by zero! pos1:" + pos1.toString() + ", apexPos: " + apexPos.toString() + ", pos2: " + pos2.toString());
		}
		final double rad = Math.acos(dotPv1v2 / denominator);

		if(inRadians)
			return rad;
		else
			return Math.toDegrees(rad);
	}

	/*
	 * Berechnet den Winkel zwischen zwei Positionen. Dabei muss pos2 gegen den Uhrzeigersinn weiter gelegen sein als pos1.
	 */
	public static double getAngleBetween2D(final Position pos1, final Position apexPos, final Position pos2, boolean inRadians)
	{		
		double angle1 = 0;
		double angle2 = 0;
		double angleResult = 0;

		//first vector
		double v1x = pos1.xCoord - apexPos.xCoord;
		double v1y = pos1.yCoord - apexPos.yCoord;

		//second vector
		double v2x = pos2.xCoord - apexPos.xCoord;
		double v2y = pos2.yCoord - apexPos.yCoord;

		angle1 = Math.toDegrees(Math.atan2(v1y, v1x));
		if(angle1 < 0) angle1 += 360;
//		angle1 = Math.round(angle1);

		angle2 = Math.toDegrees(Math.atan2(v2y, v2x));
		if(angle2 < 0) angle2 += 360;
//		angle2 = Math.round(angle2);

		angleResult = angle2 - angle1;

		if(angleResult < 0) angleResult += 360;

		//			if(angleResult >= 360) angleResult -= 360;

		//			angleResult = Math.round(angleResult);

		if(inRadians)
			return Math.toRadians(angleResult);
		else
			return angleResult;
	}

	/**
	 * Build the partial delaunay triangulation (PDT) for the given node <code>srcNode</code>. PDT is defined as
	 * all gabriel nodes plus the node which maximizing the angle among all nodes within the Gabriel circles around
	 * <code>srcNode</code> and the given nodes in the neighborhood
	 * @param <T> type of nodes
	 * @see On the Spanning Ratio of Partial Delaunay Triangulation MASS (2012), 
	 * @param neighborhood
	 * @param srcNode
	 * @return
	 */
		public static <T extends Node> Set<T> buildPartialDelaunayTriangulation(final Collection<T> neighborhood, final Node srcNode)
		{
			int r = -1;
			//Get unit edge length
			try {
				r = Configuration.getIntegerParameter("UDG/rMax");

			} catch (CorruptConfigurationEntryException e) {
				Tools.fatalError(e.getMessage());
			}
			Set<T> PDTNodes = new HashSet<T>();
			

			for(T v: neighborhood)
			{
				final double distanceSrcNodeToV = srcNode.getPosition().distanceTo(v.getPosition());
				
				if(distanceSrcNodeToV > r){
					continue;
				}
				
				Set<? extends Node> nodesInGabrielCircle = Algorithms.disk(srcNode, v, Tools.getNodeList()).nodesInDisk;
				//if v is already a gabriel node, we can continue with the next node of the neighborhood
				if(nodesInGabrielCircle.isEmpty())
				{
					PDTNodes.add(v);
					continue;
				}
				
				Tuple<Node, java.lang.Double> nodeAndAngle = new Tuple<Node, java.lang.Double>();	//tpl for maximizing angle and it's corresponding node
				double maxAngle = -1;
				for(Node w: nodesInGabrielCircle)
				{
					if(w.equals(v) || w.equals(srcNode)){
						continue;
					}
					Disk2D uniqueCircle = disk(srcNode, v, w, Tools.getNodeList());
					
					Set<? extends Node> nodesInUniqueCircle = uniqueCircle.nodesInDisk;
					//intersection of all nodes in unique cirlce and one-hop neighborhood of srcNode
					nodesInUniqueCircle.retainAll(neighborhood); 
					//we don't continue if the unique circle contains one-hop neighbors of srcNode
					if(nodesInUniqueCircle.isEmpty())
					{
						double angle = Algorithms.getSignedAngleBetween(srcNode.getPosition(), w.getPosition(), v.getPosition(), true);
						if(angle > maxAngle){
							maxAngle = angle;
							nodeAndAngle.first = w;
							nodeAndAngle.second = maxAngle;
						}
					}
				}
				if(nodeAndAngle.first != null){
					//the last constraint that has to be satisfied is that sin(alpha) >= distance(srcNode, v) / r
					if(Math.sin(nodeAndAngle.second) >= distanceSrcNodeToV / r){
						PDTNodes.add(v);
					}
				}
			}
//			System.out.println(srcNode.toString() + " has following pdtNeighbors: " + PDTNodes.toString());
		return PDTNodes;
	}
		
	/**
	 * Dividing the given workingSet of nodes into three new Sets (included in the returned Triple). The given two line Positions
	 * are defining the line between the two halves. Nodes that are on the line are in the third Set of nodes.
	 * @param <T> type of Node
	 * @param linePosFrom first Position to define line
	 * @param linePosTo second Position to define line
	 * @param workingSet nodes we are working on
	 * @return Triple of nodes. first == "left" of the line, second == "right" of the line and third == on the line
	 */
	public static <T extends Node> Triple<Set<T>, Set<T>, Set<T>> divideNodesInHalfPlanes(final Position linePosFrom, final Position linePosTo,final Iterable<T> workingSet)
	{
		Set<T> leftNodes = new HashSet<T>();
		Set<T> rightNodes = new HashSet<T>();
		Set<T> nodesOnLine = new HashSet<T>();
		
		//first prove if we have a strict vertical line
		if(linePosFrom.xCoord == linePosTo.xCoord)
		{
			for(T v: workingSet)
			{
//				if(v.getPosition().equals(linePos1) || v.getPosition().equals(linePos2)){
//					continue;
//				}
				if(v.getPosition().xCoord < linePosFrom.xCoord)
				{
					leftNodes.add(v);
				}
				else if(v.getPosition().xCoord > linePosFrom.xCoord)
				{
					rightNodes.add(v);
				}
				else
				{
					nodesOnLine.add(v);
				}
			}
		}
		else //we have no strict vertical line
		{
//			Object X;
//			Object Y;
			final double gradient = (linePosTo.yCoord - linePosFrom.yCoord) / (linePosTo.xCoord - linePosFrom.xCoord);
			final double n = linePosFrom.yCoord - gradient * linePosFrom.xCoord;
			
			for(T v: workingSet)
			{
//				if(v.getPosition().equals(linePos1) || v.getPosition().equals(linePos2)){
//					continue;
//				}
				
				final double yCoordOfLineAtNodePos = gradient * v.getPosition().xCoord + n;
				if(v.getPosition().yCoord > yCoordOfLineAtNodePos)
				{
					leftNodes.add(v);
				}
				else if(v.getPosition().yCoord < yCoordOfLineAtNodePos)
				{
					rightNodes.add(v);
				}
				else {
					nodesOnLine.add(v);
				}
			}
		}
		return new Triple<Set<T>, Set<T>, Set<T>>(leftNodes, rightNodes, nodesOnLine);
	}
	
	/**
	 * Returning the Set to which the decision Node belongs to (or not). 
	 * If flag sameSide is true, the Set containing decisionNode will be returned and vice versa the other Set of nodes.
	 * If the decisionNode is in the third Set, the Node is on the line between the two open half planes.
	 * If the decisionNode is neither in one of the halves, nor on the line it was not included in the working set
	 * @param <T> type of Node
	 * @param dividedNodeSets with  first == "left" of the line, second == "right" of the line and third == on the line
	 * @param decisionNode
	 * @param sameSide if true, the open half plane containing decisionNode will be returned and if false the other open half plane
	 */
	public static <T extends Node> Set<T> getNodesOfOpenHalfPlane(final Triple<Set<T>, Set<T>, Set<T>> dividedNodeSets, final Node decisionNode, final boolean sameSide)
	{
		if(dividedNodeSets.first.contains(decisionNode))
		{
			if(sameSide)
				return dividedNodeSets.first;
			else return dividedNodeSets.second;
		}
		else if (dividedNodeSets.second.contains(decisionNode))
		{
			if(sameSide)
				return dividedNodeSets.second;
			else return dividedNodeSets.first;
		}
		else if (dividedNodeSets.third.contains(decisionNode))
		{
			String errorMsg = "Decision node " +decisionNode.toString()
					+ " is colinear to two other Nodes. This case is not supported. Clear out all three colinear nodes before starting algorithm";
			CustomGlobal.logger.logln(sinalgo.tools.logging.LogL.HINTS, errorMsg);
//			throw new RuntimeException(errorMsg);
			return dividedNodeSets.third;
		}
		else
		{
			String errorMsg = "Node " + decisionNode + " is not in the left half plane, nor right half plane and neither on the line";
			CustomGlobal.logger.logln(sinalgo.tools.logging.LogL.HINTS, errorMsg);
//			throw new RuntimeException(errorMsg);
			return new HashSet<T>();
		}
	}
	
	public static <T extends Node> Set<T> getNodesOfOpenHalfPlane(final Position linePos1, final Position linePos2, final Node decisionNode, final boolean sameSide, final Iterable<T> workingSet)
	{
		Triple<Set<T>, Set<T>, Set<T>> nodeSets = divideNodesInHalfPlanes(linePos1, linePos2, workingSet);
		return getNodesOfOpenHalfPlane(nodeSets, decisionNode, sameSide);
	}
	
	public static <T extends Node> boolean isViolatingGPDTCriteria(final Node u, final Node v, final Set<T> workingSet)
	{
		// CustomGlobal.logger.logln(sinalgo.tools.logging.LogL.INFO, "proving of GPDT Criteria between " + u.toString() + " and " + v.toString() + " on working
		// set " + workingSet.toString());
		Set<? extends Node> nodesInDisk = Algorithms.disk(u, v, workingSet).nodesInDisk;
		
		//needed in the inner loop
		Triple<Set<T>, Set<T>, Set<T>> nodesInHalfPlanes = Algorithms.divideNodesInHalfPlanes(u.getPosition(), v.getPosition(), workingSet);
		final double normalizedDistUV = (u.getPosition().distanceTo(v.getPosition())) / CustomGlobal.R;
		
		for(Node w: nodesInDisk)
		{
			Set<? extends Node> nodesInUniqueCircle = Algorithms.disk(u, v, w, workingSet).nodesInDisk;
			// CustomGlobal.logger.logln(sinalgo.tools.logging.LogL.INFO, "nodes in unique circle of " + u.toString() + ", " + v.toString() + ", " +
			// w.toString() + ": " + nodesInUniqueCircle.toString());
			
			//we use those nodes that are not in the open half plane to that w belongs 
			Set<T> nodesOfOtherHalfPlane = getNodesOfOpenHalfPlane(nodesInHalfPlanes, w, false);
			
			// CustomGlobal.logger.logln(sinalgo.tools.logging.LogL.INFO, "nodes nodesOfOtherHalfPlane: " + nodesOfOtherHalfPlane.toString());
			nodesInUniqueCircle.retainAll(nodesOfOtherHalfPlane);
			
			// CustomGlobal.logger.logln(sinalgo.tools.logging.LogL.INFO, "nodes in unique circle and in other half plane: " + nodesInUniqueCircle.toString());
			//if there are no nodes in the intersection of the circumcircle and the open half plane, 
			//we have to prove if the sinus of the angle (uwv) is not smaller than the normalized distance (uv)
			if(nodesInUniqueCircle.isEmpty())
			{
				final double angle = Algorithms.getSignedAngleBetween(u.getPosition(), w.getPosition(), v.getPosition(), true);
				final double sinAngle = Math.sin(angle);
				
				if(sinAngle < normalizedDistUV)
				{
					// CustomGlobal.logger.logln(sinalgo.tools.logging.LogL.INFO, "Sinus of angle between " + u.toString() + ", " + w.toString()
					// + " and " + v.toString() + "=" + sinAngle + " is smaller then normalized distance of "
					// + v.toString() + " to " + u.toString() + "=" + normalizedDistUV + " => Violation of GPDT Criteria");
					return true;
				}
			} else { //we do not continue if the unique circle contains nodes
				// CustomGlobal.logger.logln(sinalgo.tools.logging.LogL.INFO, "There are nodes in unique circle and other half plane => Violation of GPDT
				// Criteria");
				return true;
			}
		}
		// CustomGlobal.logger.logln(sinalgo.tools.logging.LogL.INFO, "No violation of GPDT Criteria");
		return false;
	}
	
	/**
	 * @return true iff p0, p1, and p2 all lie on the same line
	 */
	public static boolean isCollinear(final Position p0, final Position p1, final Position p2)
	{
		return (p1.xCoord - p0.xCoord) * (p2.yCoord - p0.yCoord) <= (p2.xCoord - p0.xCoord) * (p1.yCoord - p0.yCoord) + COLLINEARITY_TOL &&
				(p1.xCoord - p0.xCoord) * (p2.yCoord - p0.yCoord) >= (p2.xCoord - p0.xCoord) * (p1.yCoord - p0.yCoord) - COLLINEARITY_TOL;
	}
	/**
	 * @param p0 first position
	 * @param toCheck position to check
	 * @param p1 second position
	 * @return true iff toCheck is between p0 and p1
	 */
	public static boolean isWithin(final Position p0, final Position toCheck, final Position p1)
	{
		//because of numerical inaccuracy we do not make the effort to distinct between inclusiveness and exclusiveness.
		return ((toCheck.xCoord >= p0.xCoord - POSITION_WITHING_TOL && toCheck.xCoord <= p1.xCoord + POSITION_WITHING_TOL) 
				&& (toCheck.yCoord >= p0.yCoord - POSITION_WITHING_TOL && toCheck.yCoord <= p1.yCoord + POSITION_WITHING_TOL)) ||
				((toCheck.xCoord >= p1.xCoord - POSITION_WITHING_TOL && toCheck.xCoord <= p0.xCoord + POSITION_WITHING_TOL) 
						&& (toCheck.yCoord >= p1.yCoord - POSITION_WITHING_TOL && toCheck.yCoord <= p0.yCoord + POSITION_WITHING_TOL));
	}
	
	/**
	 * Recognize all nodes that are on one line with two other nodes.
	 * @return Set of nodes that are collinear
	 * @throws CorruptConfigurationEntryException when configuration value "Graph/NodeCollinearityThreshold" is invalid 
	 */
	public static Set<Node> checkGraphOfCollinearNodes() throws CorruptConfigurationEntryException
	{
		final double epsilon = Configuration.getDoubleParameter("Graph/NodeCollinearityThreshold");
		Set<Node> violatingNodes = new HashSet<Node>();
		
		for(Node u: Tools.getNodeList())
		{
			if(violatingNodes.contains(u))
			{
				continue;
			}
			for(Node v: Tools.getNodeList())
			{
				if(u.equals(v))
				{
					continue;
				}
				if((u.getPosition().xCoord >= v.getPosition().xCoord - epsilon)
						&& (u.getPosition().xCoord <= v.getPosition().xCoord + epsilon))
				{
					//testing for points with same x coordinate
					for(Node w: Tools.getNodeList())
					{
						if(u.equals(w) || v.equals(w))
						{
							continue;
						}
						if((w.getPosition().xCoord >= u.getPosition().xCoord - epsilon)
							&& (w.getPosition().xCoord <= u.getPosition().xCoord + epsilon))
						{
							violatingNodes.add(w);
							violatingNodes.add(v);
							violatingNodes.add(u);
							CustomGlobal.logger.logln(sinalgo.tools.logging.LogL.INFO, u.toString() + 
									", " + v.toString() + ", " + w.toString() + " are collinear");
							break;
						}
					}
				}
				else
				{
					final double gradient = (v.getPosition().yCoord - u.getPosition().yCoord) / (v.getPosition().xCoord - u.getPosition().xCoord);
					final double intersectionYAxis = u.getPosition().yCoord - gradient * u.getPosition().xCoord;
					
					//testing for all nodes with different x coordinate
					for(Node w: Tools.getNodeList())
					{
						if(u.equals(w) || v.equals(w))
						{
							continue;
						}
							
						if((w.getPosition().yCoord >= (gradient * w.getPosition().xCoord + intersectionYAxis) - epsilon)
								&& (w.getPosition().yCoord <= (gradient * w.getPosition().xCoord + intersectionYAxis) + epsilon))
						{
							violatingNodes.add(w);
							violatingNodes.add(v);
							violatingNodes.add(u);
							CustomGlobal.logger.logln(sinalgo.tools.logging.LogL.INFO, u.toString() + 
									", " + v.toString() + ", " + w.toString() + " are collinear");
							break;
						}
					}
				}
			}
		}
		return violatingNodes;
	}
	
	/**
	 * Clear Graph out of nodes that are on a line with two other nodes.
	 */
	public static void clearGraphOfCollinearNodes()
	{
		try {
			for(Node toDelete: checkGraphOfCollinearNodes())
			{
				Tools.removeNode(toDelete);
			}
		} catch (CorruptConfigurationEntryException e) {
			e.printStackTrace();
		}
	}
	
	public static <T extends Node> T getNodeAtPosition(final Position p, final Iterable<T> nodeSet) {
		
		for (T n : nodeSet) {
			if (PositionTriple.compareDouble(n.getPosition().xCoord, p.xCoord)
					& PositionTriple.compareDouble(n.getPosition().yCoord,
							p.yCoord)) { return n; }
		}
		
		throw new RuntimeException("No node at this position");
		
	}
	
	/**
	 * makes the node IDs continuous, so that no number in between is missing
	 * this is useful for DirectedEdges in an adjacency matrix. The last node
	 * ID will then be equal to the Tools.getNodeList().size()
	 */
	public static void makeContinuousIDS(){
		
		boolean fixed = true;
		while(fixed){
			fixed=false;
			
			int idCount = 1;
			for(Node n : Tools.getNodeList()){
				if(n.ID>idCount){
					n.ID=n.ID-1;
					fixed=true;
				}
				idCount++;
			}
		}
	}
	
	/**
	 * Calculated on a Unit Disc Graph connectivity model with a Gabriel Graph Algorithm
	 * 
	 * @return spanning ratio between the normal UDG Graph and its GG Subgraph
	 */
	public static double GGSpan(){
		AdjMatrixEdgeWeightedDigraph UDGMatrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size());
		AdjMatrixEdgeWeightedDigraph GGMatrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size());
		
		for(Node n : Tools.getNodeList()){
			Collection<Node> neighborhood = Algorithms.getNeighborNodes(n, Tools.getNodeList());
			Set<Node> gabrielNodes = Algorithms.buildGabrielGraph(neighborhood, n, Tools.getNodeList());
			for(Node v: neighborhood)
			{
				DirectedEdge de = null;
				//add edge UDG
				// ID starts with 1, matrix with index 0
				de = new DirectedEdge(n.ID-1, v.ID-1, n.getPosition().distanceTo(v.getPosition()));
				UDGMatrix.addEdge(de);
			}
			for(Node v: gabrielNodes){
				DirectedEdge de = null;
				//add edge GG
				// ID starts with 1, matrix with index 0
				de = new DirectedEdge(n.ID-1, v.ID-1, n.getPosition().distanceTo(v.getPosition()));
				GGMatrix.addEdge(de);

			}
		}
		
		int V = UDGMatrix.V();
		
		FloydWarshall UDGfw = new FloydWarshall(UDGMatrix);
		FloydWarshall GGfw = new FloydWarshall(GGMatrix);
		
		return Algorithms.spanningRatio(UDGfw, GGfw, V);
	}
	
	/**
	 * Calculated on a Unit Disc Graph connectivity model with a Partial Delaunay Triangulation Algorithm
	 * 
	 * @return spanning ratio between the normal UDG Graph and its PDT Subgraph
	 */
	public static double PDTSpan(boolean hopdistance){
		//moved implementation to Algorithms_ext
		return Algorithms_ext.PDTSpan(Algorithms_ext.createPDTNeighborhood(), false);
	}
	
	/**
	 * returns a directed edge matrix which is created
	 * by using getKnownNeighbors of the message handlers.
	 * It will use the last subgraph strategy that was used
	 * to get the message handlers.
	 * 
	 * @return directed edge matrix for further processing
	 * in FloydWarshall created from the handlers subgraph
	 */
	public static AdjMatrixEdgeWeightedDigraph getAdjMatrixEdgeWeightedDigraph(){
		PhysicalGraphNode pgn = Utilities.getRandomNode(PhysicalGraphNode.class, Tools.getNodeList());
		
		
		// DEPRECATED
		return getAdjMatrixEdgeWeightedDigraph(pgn.getLastSubgraphStrategy().getTopologyControlID());
	}
	
	
	/**
	 * Get a directed edge matrix to use with floyd warshall
	 * @param subgraphType requested type of subgraph
	 * @param hopDistance use <b>hop distance</b> as weight, else use <b>euclidean distance</b>
	 * @return directed edge matrix
	 */
	public static AdjMatrixEdgeWeightedDigraph getAdjMatrixEdgeWeightedDigraph(final EStrategy subgraphType, final boolean hopDistance){
		AdjMatrixEdgeWeightedDigraph matrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size());
		
		for(PhysicalGraphNode n: Utilities.getNodeCollectionByClass(PhysicalGraphNode.class))
		{
			SubgraphStrategy sub = n.requestSubgraph(subgraphType);
			if(!sub.hasTerminated())
			{
				sub.start();
			}
			if(!sub.hasTerminated())
				throw new NotYetImplementedException("getAdjMatrixEdgeWeightedDigraph: Only instant subgraph strategies are supported yet. Workaround: generate the requested subgraphStrategy for all nodes firstly and recall this function.");
			
			for(PhysicalGraphNode neighbor: sub.getSubgraphNodes())
			{
				DirectedEdge directedEdge;
				if(hopDistance)
				{
					directedEdge = new DirectedEdge(n.ID -1, neighbor.ID -1, 1);
				} else {
					directedEdge = new DirectedEdge(n.ID -1, neighbor.ID -1, n.getPosition().distanceTo(neighbor.getPosition()));
				}
				try{
				matrix.addEdge(directedEdge);
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					System.out.println("\t nodeslist size: " + Tools.getNodeList().size());
					System.out.println("\t n.ID: " + n.ID);
					System.out.println("\t neighbor.ID: " + neighbor.ID);
					System.out.println("\t matrix: " + matrix);
					System.out.println("\t directedEdge: " + directedEdge);
					e.printStackTrace();
					Tools.showMessageDialog("EXCEPTION!");
				}
			}
		}
		return matrix;
	}
	
	public static final FloydWarshall generateFloydWarshall(final AdjMatrixEdgeWeightedDigraph matrix)
	{
		return new FloydWarshall(matrix);
	}
	
	
	/**
	 * returns a directed edge matrix which is created by 
	 * using getKnownNeighbors of the message handlers.
	 * weight == distance between nodes
	 * 
	 * @param handlerID ID of the message handler that illustrates
	 * a certain Subgraph
	 * @return directed edge matrix for further processing
	 * in FloydWarshall created from the handlers subgraph
	 */
	public static AdjMatrixEdgeWeightedDigraph getAdjMatrixEdgeWeightedDigraph(UUID handlerID){
		AdjMatrixEdgeWeightedDigraph matrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size()+1);
		
		for(Node u : Tools.getNodeList()){
			PhysicalGraphNode pu = (PhysicalGraphNode)u;
			AbstractMessageHandler<? extends Node> amh = pu.messageHandlerMap.get(handlerID);
			
			// whole neighbourset plz =)
			for(Node v : amh.getKnownNeighbors()){
				DirectedEdge de = new DirectedEdge(u.ID,v.ID, u.getPosition().distanceTo(v.getPosition()));
				matrix.addEdge(de);
			}
		}
		
		return matrix;
	}
	

	/**
	 * only to use with barriere message handlers. Uses the 
	 * original Graph that was created only by beaoning.
	 * 
	 * @param handlerID ID of the message handler that illustrates
	 * a certain Subgraph
	 * @param hopDistance use <b>hop distance</b> as weight, else use <b>euclidean distance</b>
	 * 
	 * @return directed edge matrix for further processing
	 * in FloydWarshall created from the handlers subgraph
	 */
	public static AdjMatrixEdgeWeightedDigraph getAdjMatrixEdgeWeightedDigraphBarrierePhysicalGraph(UUID handlerID, final boolean hopDistance){
		AdjMatrixEdgeWeightedDigraph matrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size()+1);
		
		for(Node u : Tools.getNodeList()){
			PhysicalGraphNode pu = (PhysicalGraphNode)u;
			AbstractMessageHandler<? extends SimpleNode> amh = pu.messageHandlerMap.get(handlerID);
			
			if(amh instanceof BarriereMessageHandler){
				BarriereMessageHandler bmh = (BarriereMessageHandler)amh;
				
				for(Node v : bmh.getAllKnownPhysicalNeighbourNodes()){
					DirectedEdge de;
					if(hopDistance){
						de = new DirectedEdge(u.ID, v.ID, 1.0);
					}else{
						de = new DirectedEdge(u.ID, v.ID, u.getPosition().distanceTo(v.getPosition()));
					}
					matrix.addEdge(de);
				}
				
			}else{
				System.err.println("No barriere message handler as last strategy");
				return null;
			}
		}
		
		return matrix;
	}
	
	/**
	 * only use with barriere message handlers. Uses the "subgraph"
	 * with regard to the actual physical routing paths between
	 * virtual neighbours.
	 * 
	 * @param handlerID ID of the message handler that illustrates
	 * a certain Subgraph
	 * @param hopDistance use <b>hop distance</b> as weight, else use <b>euclidean distance</b>
	 * 
	 * @return directed edge matrix for further processing
	 * in FloydWarshall created from the handlers subgraph
	 */
	public static AdjMatrixEdgeWeightedDigraph getAdjMatrixEdgeWeightedDigraphBarriereSubgraphActualVirtual(UUID handlerID, final boolean hopDistance){
		try{
		
		AdjMatrixEdgeWeightedDigraph matrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size()+1);
		
		for(Node u : Tools.getNodeList()){
			PhysicalGraphNode pu = (PhysicalGraphNode)u;
			// null?
			AbstractMessageHandler<? extends SimpleNode> amh = pu.messageHandlerMap.get(handlerID);
			
			if(amh instanceof BarriereMessageHandler){
				BarriereMessageHandler bmh = (BarriereMessageHandler)amh;
				
				for(Node v : bmh.getKnownNeighbors()){
					if(hopDistance){
						DirectedEdge de = new DirectedEdge(u.ID, v.ID, bmh.getHopsTo(v));
						matrix.addEdge(de);
					}else{
						if(bmh.isVirtualNode(v)){
						
							double distance = getBarriereVirtualPathLength(bmh, v);
						
							/*System.out.println("Vd: "+bmh.node.getPosition().distanceTo(v.getPosition()));
							System.out.println("Ad: "+distance);
							System.out.println("----------------");*/
						
							DirectedEdge de = new DirectedEdge(u.ID, v.ID, distance);
							matrix.addEdge(de);
						
						}else{
							DirectedEdge de = new DirectedEdge(u.ID, v.ID, u.getPosition().distanceTo(v.getPosition()));
							matrix.addEdge(de);
						}
					}
				}
				
			}else{
				System.err.println("No barriere message handler as last strategy");
				return null;
			}
		}
		
		return matrix;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * recursive function to calculate the distance between
	 * virtual neighbours using the actual physical hops
	 * 
	 * @param bmh message handler starting point
	 * @param targetNode destination (virtual neighbour) of starting point
	 * @return physical hop distance between 2 neighbours that a virtual to
	 * each other
	 */
	public static double getBarriereVirtualPathLength(BarriereMessageHandler bmh, Node targetNode){
		UUID handlerID = bmh.tcID;
		PhysicalGraphNode hopNode = (PhysicalGraphNode)bmh.getHopNode(targetNode);
		BarriereMessageHandler bmhHop = (BarriereMessageHandler)hopNode.messageHandlerMap.get(handlerID);
		
		double firstHopDistance = bmh.node.getPosition().distanceTo(hopNode.getPosition());
		
		if(bmhHop.isVirtualNode(targetNode)){
			return firstHopDistance + getBarriereVirtualPathLength(bmhHop, targetNode);
		}else{
			return firstHopDistance + hopNode.getPosition().distanceTo(targetNode.getPosition());
		}
	}
	
	/**
	 * only use with barriere message handlers. Uses the "subgraph"
	 * with regard to the shortest physical path between virtual
	 * neighbours.
	 * 
	 * @param handlerID ID of the message handler that illustrates
	 * a certain Subgraph
	 * @param pGraph FloydWarshall of the original physical graph (for getting real shortest path
	 * of virtual connections)
	 * @param hopDistance use <b>hop distance</b> as weight, else use <b>euclidean distance</b>
	 * 
	 * @return directed edge matrix for further processing
	 * in FloydWarshall created from the handlers subgraph
	 */
	public static AdjMatrixEdgeWeightedDigraph getAdjMatrixEdgeWeightedDigraphBarriereSubgraphShortestVirtual(UUID handlerID, FloydWarshall pGraph, final boolean hopDistance){
		AdjMatrixEdgeWeightedDigraph matrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size()+1);
		
		for(Node u : Tools.getNodeList()){
			PhysicalGraphNode pu = (PhysicalGraphNode)u;
			AbstractMessageHandler<? extends SimpleNode> amh = pu.messageHandlerMap.get(handlerID);
			
			if(amh instanceof BarriereMessageHandler){
				BarriereMessageHandler bmh = (BarriereMessageHandler)amh;
				
				for(Node v : bmh.getKnownNeighbors()){
					if(bmh.isVirtualNode(v)){
						
						// actual shortest path between the 2 nodes.
						DirectedEdge de = new DirectedEdge(u.ID, v.ID, pGraph.dist(bmh.node.ID, v.ID));
						matrix.addEdge(de);
						
					}else{
						DirectedEdge de;
						if(hopDistance)
							de = new DirectedEdge(u.ID, v.ID, 1.0);
						else
							de = new DirectedEdge(u.ID, v.ID, u.getPosition().distanceTo(v.getPosition()));
						matrix.addEdge(de);
					}
				}
				
			}else{
				System.err.println("No barriere message handler as last strategy");
				return null;
			}
		}
		
		return matrix;
	}
	
	/**
	 * calculates the spanning ratio between two floyd warshall graphs
	 * with same nodes. (G original graph, H extracted graph)
	 * 
	 * @param graph floyd warshall physical graph
	 * @param subgraph floyd warshall new generated graph
	 * @param V number of nodes in floydwarshall
	 * @return spanning ratio ( max u,v E G { dH{u,v}/dG{u,v} })
	 */
	public static double spanningRatio(FloydWarshall graph, FloydWarshall subgraph, int V){
		//for(int x = 0; x<graph.)
		
		double graphLongest = 0.0;
		double subgraphLongest = 0.0;
		double maxRatio = 0.0d;
		for(int x = 0; x<V; x++){
			for(int y = x+1; y<V; y++){
				if(graphLongest<graph.dist(x, y) && graph.dist(x, y)!=Double.POSITIVE_INFINITY){
					graphLongest = graph.dist(x, y);
				}
				
				if(subgraphLongest<subgraph.dist(x, y) && subgraph.dist(x, y)!=Double.POSITIVE_INFINITY){
					subgraphLongest = subgraph.dist(x, y);
				}
				
				if(subgraph.dist(x, y)!=Double.POSITIVE_INFINITY && graph.dist(x, y)!= Double.POSITIVE_INFINITY){
					double currentRatio = subgraph.dist(x,y)/graph.dist(x,y);
					if(maxRatio<currentRatio){
						maxRatio = currentRatio;
					}
				
				}
			}
		}
		
		//System.out.println(subgraphLongest/graphLongest+"");
		//System.out.println("sg longest: "+subgraphLongest);
		//System.out.println("g longest: "+graphLongest);
		return maxRatio;
	}
	
	
	/**
	 * @param list which should be copied
	 * @return a copy from a list. No items are being cloned.
	 */  /*
	public static <E> ArrayList<E> copyList(List<E> list){
		ArrayList<E> copiedList=new ArrayList<E>();
		for(Integer i=0;i<list.size();i++){
			copiedList.add(list.get(i));
		}
		return copiedList;
	}
	*/
}
