package projects.reactiveSpanner.nodes.edges;

import java.awt.Color;
import java.awt.Graphics;

import projects.reactiveSpanner.Algorithms;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.gui.helper.Arrow;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Position;
import sinalgo.nodes.edges.BidirectionalEdge;
import sinalgo.nodes.edges.Edge;
import sinalgo.tools.logging.Logging;

public class DistEdge extends BidirectionalEdge implements Weighted<Double, DistEdge>
{
	private static Logging logger = Logging.getLogger();
	/**
	 * Value to determine when two positions are assumed as identically. So, that two edges that are ending
	 * in one node are not assumed as intersecting each other
	 */
	private static double TOL = -1;
	private static boolean drawOnlyEdgesWithMessages = false;
	
	static {
		try {
			TOL = Configuration.getDoubleParameter("Graph/PositionEqualityThreshold");
			drawOnlyEdgesWithMessages = Configuration.getBooleanParameter("Graph/DrawOnlyEdgesWithMessages");
		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}
	
	/**
	 * A minimum bounding sphere around the edge for fast observation if another 
	 * edge is close enough to intersect with this one.
	 */
	private class BoundingSphere
	{
		final public Position center;
		final public double squaredRadius;	//no need to root the radius
		
		public BoundingSphere(Edge e)
		{
			this.center = Algorithms.getCentralPos(e.startNode, e.endNode);
			this.squaredRadius = e.startNode.getPosition().squareDistanceTo(this.center);
		}
		
		public boolean isIntersectingWith(final BoundingSphere bs)
		{
			return (this.center.squareDistanceTo(bs.center) <= squaredRadius );
		}
	}
	
	/**
	 * weight of this edge which would be compared. Here, weight is corresponding to the length of the edge
	 */
	private double weight;
	
	/**
	 * the actual minimal boundingSphere
	 * Attention: Moving of nodes is not considered and therefore, edge lengths and corresponding boundingSpheres will
	 * not be updated 
	 */
	private BoundingSphere boundingSphere;
	
	
//	public DistEdge(final Node from, final Node to)
//	{
//		this.startNode = from;
//		this.endNode = to;
//	}
	
	@Override
	public void initializeEdge()
	{
		super.initializeEdge();
		assert(TOL > 0);
		defaultColor = Color.WHITE;		//<<may result in ugly white stripes through other elements of the screen, maybe add some if into the draw method (tim)
										//^^fixed=> see draw method
		boundingSphere = new BoundingSphere(this);
		weight = this.startNode.getPosition().distanceTo(this.endNode.getPosition());	
	}

	@Override
	public final Double weight()
	{
		return weight;
	}
	
	@Override
	public int compareTo(final DistEdge o) {
		if (this.weight < o.weight) {
			return -1;
		}
		if (this.weight > o.weight) {
			return 1;
		}
		return 0;
	}
	
	/**
	 * Get intersection of two edges defined by two positions each.
	 * @param e1p1 first edge, first position
	 * @param e1p2 first edge, second position
	 * @param e2p1 second edge, first position
	 * @param e2p2 second edge, second position
	 * @return intersection or <b>null</b> if there is no intersection
	 */
	public static final Position getIntersectionWith(final Position e1p1, final Position e1p2, final Position e2p1, final Position e2p2)
	{
		double denom = ((e2p2.yCoord - e2p1.yCoord) * (e1p2.xCoord - e1p1.xCoord)) - ((e2p2.xCoord - e2p1.xCoord) * (e1p2.yCoord - e1p1.yCoord));
		if (denom == 0)
		{
			throw new RuntimeException("Denominator of intersection between edges E[" + e1p1.toString() + ","+ e1p2.toString() + "] and E[" + e2p1.toString() + "," + e2p2.toString() + "] is zero! This case is not supported");
		}
			
		double ua = (((e2p2.xCoord - e2p1.xCoord) 
				* (e1p1.yCoord - e2p1.yCoord)) 
				- ((e2p2.yCoord - e2p1.yCoord) 
						* (e1p1.xCoord - e2p1.xCoord))) / denom;
		double ub = (((e1p2.xCoord - e1p1.xCoord) 
				* (e1p1.yCoord - e2p1.yCoord)) 
				- ((e1p2.yCoord - e1p1.yCoord) 
						* (e1p1.xCoord - e2p1.xCoord))) / denom;
		if ((ua < 0) || (ua > 1) || (ub < 0) || (ub > 1)) {
			return null;
		}
		Position result = add(e1p1, multiply(ua, (sub(e1p2, e1p1))));
		if (checkAlmostEqual(result, e1p1) || checkAlmostEqual(result, e1p2) || checkAlmostEqual(result, e2p1) || checkAlmostEqual(result, e2p2)) {
			return null; // if the lines cross in a point which is in the set of given positions we do not want to count this as "crossing"
		}

		return result;
	}
	
	/**
	 * Get intersection of {@link DistEdge} e with this edge. If there is no intersection or edges are (partly) overlaying, <b>null</b> will be returned.
	 * @param other other edge
	 * @return intersection or <b>null</b> if there is no intersection
	 */
	public final Position getIntersectionWith(final DistEdge other)
	{
		//fast observation if the bounding spheres of both edges are intersecting each other
		if( !boundingSphere.isIntersectingWith(other.boundingSphere) )
		{
			//logger.logln(LogL.INFO, other.toString() + " is not in bounding sphere with " +this.toString());
			return null;
		}
		return getIntersectionWith(this.startNode.getPosition(), this.endNode.getPosition(), other.startNode.getPosition(), other.endNode.getPosition());
	}
	
	private static boolean checkAlmostEqual(final Position p1, final Position p2) {
		if (Math.abs(p1.xCoord) < Math.abs(p2.xCoord - TOL) && Math.abs(p1.xCoord) > Math.abs(p2.xCoord + TOL)) {
			if (Math.abs(p1.yCoord) < Math.abs(p2.yCoord - TOL) && Math.abs(p1.yCoord) > Math.abs(p2.yCoord + TOL)) {
				return true;
			}
		}
		return false;
	}

	private static Position multiply(final double d, final Position p) {
		return new Position(d * p.xCoord, d * p.yCoord, d * p.zCoord);
	}

	private static Position sub(final Position p1, final Position p2) {
		return new Position(p1.xCoord - p2.xCoord, p1.yCoord - p2.yCoord, p1.zCoord - p2.zCoord);
	}

	private static Position add(final Position p1, final Position p2) {
		return new Position(p1.xCoord + p2.xCoord, p1.yCoord + p2.yCoord, p1.zCoord + p2.zCoord);
	}
	
	@Override
	public String toString() {
		return "E[(" + this.startNode.ID + ")(" + this.endNode.ID + ")]";
	}
	
//	@Override
//	public String toString() {
//		return "From: (" + this.endNode.getPosition().toString() + ") to: (" + this.startNode.getPosition().toString() + ")\n" +
//				"Length: " + this.endNode.getPosition().distanceTo(this.startNode.getPosition());
//		//return "Type: " + Global.toShortName(this.getClass().getName());
//	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt) {
		if(drawOnlyEdgesWithMessages && this.numberOfMessagesOnThisEdge==0){  //=> creates an immense speedup! Edges without messages on it are not being drawn white anymore, instead not being drawn at all.
			return;
		}
		Position p1 = startNode.getPosition();
		pt.translateToGUIPosition(p1);
		int fromX = pt.guiX, fromY = pt.guiY; // temporarily store
		Position p2 = endNode.getPosition();
		pt.translateToGUIPosition(p2);
		
		if((this.numberOfMessagesOnThisEdge == 0)&&
				(this.oppositeEdge != null)&&
				(this.oppositeEdge.numberOfMessagesOnThisEdge > 0)){
			// only draws the arrowHead (if drawArrows is true)
			Arrow.drawArrowHead(fromX, fromY, pt.guiX, pt.guiY, g, pt, getColor());
		} else {
			Arrow.drawArrow(fromX, fromY, pt.guiX, pt.guiY, g, pt, getColor());
		}
	}
}
