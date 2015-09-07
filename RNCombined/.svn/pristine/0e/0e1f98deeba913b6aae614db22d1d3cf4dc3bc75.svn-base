package projects.reactiveSpanner.nodes.messageHandlers.buildBackbone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import projects.reactiveSpanner.RoutingEntry;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;

import sinalgo.nodes.Node;

public interface BuildbackboneTopology {

	/**
	 * @return the routing Table of the node or virtual node
	 */
	public HashMap<BuildbackboneTopology, ArrayList<RoutingEntry>> getRoutingTable();
	
	/**
	 * @return the node or the controller of the virtual node
	 */
	public PhysicalGraphNode getNode();
	
	/**
	 * @return the nodes which were collected from the 3-hop-beaconing
	 */
	public HashMap<Integer, PhysicalGraphNode> getCollectedNodes();
}
