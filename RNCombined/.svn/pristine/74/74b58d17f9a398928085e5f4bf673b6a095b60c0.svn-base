package projects.reactiveSpanner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import com.sun.istack.internal.logging.Logger;

import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.nodes.edges.Edge;
import sinalgo.tools.logging.Logging;
import sun.util.logging.resources.logging;

public class Dijkstra {
	protected static Logging logger = Logging.getLogger();
	
	
	public static <node extends Node> ArrayList<RoutingEntry> dijkstra(HashMap<?, node> t, node src, node dest){
		Set<Node> nodes=new HashSet<Node>();
		nodes.addAll(t.values());
		return dijkstra(nodes, src, dest);
	}
	
	
	/**
	 * @param nodes the set of nodes on which dijkstra shall be computed
	 * @param src the node from which the path starts
	 * @param dest the destination
	 * @return the path which leads from src to dest, if any.
	 */
	public static ArrayList<RoutingEntry> dijkstra(Set<Node> nodes, Node src, Node dest){
		ArrayList<RoutingEntry> path=new ArrayList<RoutingEntry>();
		
		if(src.equals(dest)){
			RoutingEntry entry=new RoutingEntry(dest.getPosition(), dest.ID);
			path.add(entry);
			return path;
		}
		
		HashMap<Node, Node> previous=new HashMap<Node, Node>();
		final HashMap<Node, Double> dist=new HashMap<Node, Double>();
		HashMap<Node, Boolean> visited=new HashMap<Node, Boolean>();
		
		Comparator<Node> c=new Comparator<Node>() {  	//currently just counting the hop distance, 
														//maybe change later to achieve shortest path with weighted edges
			@Override
			public int compare(Node o1, Node o2) {
				if(dist.get(o1)<dist.get(o2)){
					return -1;
				}else if(dist.get(o1)>dist.get(o2)){
					return +1;
				}
				return 0;
			}
		};
		
		PriorityQueue<Node> q=new PriorityQueue<Node>(5,c);
		initialize(nodes, src, previous,dist,visited,q);
		
		boolean found=false;
		while(!q.isEmpty() && !found ){
			PhysicalGraphNode p=null;
			try {
				p=(PhysicalGraphNode)q.poll();
			} catch (ClassCastException e) {
				logger.logln(LogL.ERROR_DETAIL, "Please, use PhysicalGraphNodes only!");
			}
			
			
			for(Edge uv:p.outgoingConnections){ //FIXME: I need the neighbors of the topologycontrol here
				Node v=uv.endNode;
				
				if(visited.get(v)==null){//TODO:WORKAROUND: the loop walks over all nodes which are connected, 
					                     //but it is sufficient to walk over all nodes from the topologycontrol 
					/*src.highlight(true); 
					dest.highlight(true);
					System.out.println(nodes);
					System.out.println(visited);
					System.out.println(v);
					System.out.println("NULLLLLL");*/
					continue;
				}
				if(!visited.get(v)){	//if a node has not already been added, add it to the q
					visited.put(v, true);
					q.add(v);
				}
				if(q.contains(v)){
					dist_Update(p, v, previous, dist);	//if node is still in the q, check for new distance
					if(v.equals(dest)){ //found dest -> cancel computation
						found=true; //needed to leave the outer (while) loop
						break;
					}
				}
			}
		}
		
		
		path=createShortestPath(previous,src, dest);
		return path;
	}

	private static void initialize(Set<Node> nodes, Node src, HashMap<Node, Node> previous, HashMap<Node, Double> dist, HashMap<Node, Boolean> visited, PriorityQueue<Node> q) {
		for(Node p:nodes){
			dist.put(p,  Double.POSITIVE_INFINITY);  //set distance of all nodes to infinity
			visited.put(p, false);					//no node has been visited yet
		}
		dist.put(src, 0d);							//src node's distance is 0
		visited.put(src, true); 					//and has been visited
		q.add(src);									//because we add him here
		
				
	}
	
	private static void dist_Update(Node p1, Node p2, HashMap<Node, Node> previous, HashMap<Node, Double> dist ){
		double next=dist.get(p1)	+ 1;  //<-change to weight from p1 to p2 if you do not want to create hopdistance
		if(next<dist.get(p2)){		//if next distance is shorter than actual saved distance->replace
			dist.put(p2, next);
			previous.put(p2, p1);
		}
	}
	
	private static ArrayList<RoutingEntry> createShortestPath(HashMap<Node, Node> previous,Node src, Node dest){
		//create actual path from previous map
		ArrayList<RoutingEntry> path=new ArrayList<RoutingEntry>();
		RoutingEntry entry=new RoutingEntry(dest.getPosition(), dest.ID);
		path.add(entry);
		
		Node iterate=dest;
		while(previous.get(iterate)!=null){	
			iterate=previous.get(iterate);
			RoutingEntry entries=new RoutingEntry(iterate.getPosition(), iterate.ID);
			path.add(0,entries);
		}
		
		if(src.ID!=path.get(0).getId()){
			//throw new RuntimeException("Dijkstra found no path between Nodes: " + src +" and "+ dest);
			System.out.println("Dijkstra found no path between Nodes: " + src +" and "+ dest);
			
		}
		return path;
	}
	

}