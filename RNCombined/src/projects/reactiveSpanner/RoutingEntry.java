package projects.reactiveSpanner;

import sinalgo.nodes.Position;

/**
 * @author Tim
 *  a routingEntry in a routingTable containing a position of the node and it's id.
 *  NOTE: You cannot and shall not change the members of this class when it is created, 
 *     since this might lead to unexpected behavior in the routing process.
 */
public class RoutingEntry {

	/**
	 * the position of the node this entry represents
	 */
	private Position pos;
	
	/**
	 * the id of the node this entry represents
	 */
	private int id;
	
	public RoutingEntry(Position pos, int id){
		this.pos=pos;
		this.id=id;
	}

	
	@Override
	public String toString(){
		//return "E:(x="+Math.round((pos.xCoord*10))/10.0 +", y=" + Math.round((pos.yCoord*10))/10.0 +", id="+id+")";
		return "Node("+id+")";
	}
	
	public Position getPos() {
		return pos;
	}

	public int getId() {
		return id;
	}
	
}
