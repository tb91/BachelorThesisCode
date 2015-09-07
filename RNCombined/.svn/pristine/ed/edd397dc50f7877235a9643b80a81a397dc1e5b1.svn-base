package projects.reactiveSpanner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.tools.Tools;

public class GraphConnectivity {

		/**
		 * Examining the connectivity of the complete graph i. e. all nodes. if each node v has a 
		 * connection (directly or in more hop distance) to a other node u, then returning true otherwise false.
		 * @param <T> Node class
		 * @param workingSet set of nodes which we want to observe
		 * @return if the Graph is connected
		 */
		public static <T extends Node> boolean isGraphConnected(final Iterable<T> workingSet)
		{
			if(!workingSet.iterator().hasNext())
			{
				return false;
			}
			Tools.reevaluateConnections();
			HashMap<Node, Character> nodes2Color = GraphConnectivity.recursiveDFS(workingSet.iterator().next(), workingSet);
			for(Entry<Node, Character> entry: nodes2Color.entrySet())
			{
				if(entry.getValue().equals('w'))	
				{
					//if the character is 'w'(for white) it means, that it was'nt reachable by a other randomly chosen node;
//					CustomGlobal.logger.logln(LogL.INFO, "Graph is not connected");
					return false;
				}
			}
//			CustomGlobal.logger.logln(LogL.INFO, "Graph is connected");
			return true;
		}

		/**
		 * Get all nodes that were not reachable by the selected node
		 * @param selectedNode the connection of the node that will be observed
		 * @param workingSet all nodes that will be considered
		 * @return the set of nodes that are not connected to the selected node (this set is smaller or equals the workingSet)
		 */
		public static <T extends Node> Set<T> getUnconnectedNodes(final T selectedNode, final Iterable<T> workingSet)
		{
			Set<T> unconnectedNodes = new HashSet<T>();
			for(T node: workingSet)
			{
				unconnectedNodes.add(node);
			}
			Tools.reevaluateConnections();
			HashMap<Node, Character> nodes2Color = GraphConnectivity.recursiveDFS(selectedNode, workingSet);
			for(Entry<Node, Character> entry: nodes2Color.entrySet())
			{
//				System.out.println("_DEBUG:isGraphConnected: Node " + entry.getKey() + " has color " + entry.getValue());
				if(entry.getValue().equals('b'))	
				{
					//if the character is 'b'(for black) it means, that it was reachable by a other randomly chosen node, so we can remove
					//it from the list of unconnected nodes
					unconnectedNodes.remove(entry.getKey());
				}
			}
//			CustomGlobal.logger.logln(LogL.INFO, "Graph is connected");
			return unconnectedNodes;
		}
		
		/**
		 * Depth First Search algorithm similar to the DFS algorithm by Cormen et. al.
		 * Using the Character color to mark all nodes we have already inspected. Colors are defined like in the following:
		 * 	w (white): node not reached at the moment
		 *  g (gray): node which is currently in progress
		 *  b (black): node which is reachable
		 * 
		 * @param selectedNode the connection of the node that will be observed
		 * @param workingSet all nodes that will be considered
		 * @return HashMap with nodes maped to the Character color
		 */
		private static <T extends Node> HashMap<Node, Character> recursiveDFS(final T selectedNode, final Iterable<T> workingSet)
		{
			HashMap<Node, Character> nodes2Color = new HashMap<Node, Character>();	
			
			for(Node v: workingSet)
			{
				nodes2Color.put(v, 'w');
			}
			if(workingSet.iterator().hasNext())
			{
				recursiveDFSvisit(selectedNode, nodes2Color);
			}
			return nodes2Color;
		}
		
	/**
	 * Recursive part of the DFS algorithm similar to Cormen et. al.
	 * 
	 * @param u Node we are currently examining
	 * @param nodes2Color Map of all nodes maped to their current reachability
	 */
	private static void recursiveDFSvisit(final Node u, HashMap<Node, Character> nodes2Color)
	{
		nodes2Color.put(u, 'g');
		for(Edge e: u.outgoingConnections)
		{
			Node v = e.endNode;
			if(nodes2Color.get(v).equals('w'))
			{
				recursiveDFSvisit(v, nodes2Color);
			}
		}
		nodes2Color.put(u, 'b');
	}
}
