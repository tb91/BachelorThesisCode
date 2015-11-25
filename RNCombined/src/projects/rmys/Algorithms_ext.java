package projects.rmys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.management.RuntimeErrorException;

import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.FloydWarshall.AdjMatrixEdgeWeightedDigraph;
import projects.reactiveSpanner.FloydWarshall.DirectedEdge;
import projects.reactiveSpanner.FloydWarshall.FloydWarshall;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import projects.rmys.nodes.messageHandler.RMYS;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;
import sinalgo.nodes.Node;
import sinalgo.runtime.nodeCollection.NodeCollectionInterface;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

public class Algorithms_ext {

	private static Logging logger = Logging.getLogger();

	public static double rmysSpan(boolean hopdistance) {
		return rmysSpan(createMYSNeighborhood(), hopdistance);
	}
	
	public static double rmysSpan(HashMap<Node, Set<NewPhysicalGraphNode>> graph, boolean hopdistance){
		AdjMatrixEdgeWeightedDigraph UDGMatrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size());
		AdjMatrixEdgeWeightedDigraph RMYSMatrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size());

		// needed to guarantee bidirectional edges
		HashMap<Node, Set<NewPhysicalGraphNode>> completeRMYSGraph = createMYSNeighborhood();

		for (Node n : Tools.getNodeList()) {
			Collection<Node> neighborhood = Algorithms.getNeighborNodes(n, Tools.getNodeList());

			for (Node v : neighborhood) {
				DirectedEdge de = null;
				// add edge UDG
				// ID starts with 1, matrix with index 0
				if (hopdistance) {
					de = new DirectedEdge(n.ID - 1, v.ID - 1, 1);
				} else {
					de = new DirectedEdge(n.ID - 1, v.ID - 1, n.getPosition().distanceTo(v.getPosition()));
				}

				UDGMatrix.addEdge(de);
			}

			for (Node v : completeRMYSGraph.get(n)) {
				DirectedEdge de = null;
				// add edge GG
				// ID starts with 1, matrix with index 0
				if (hopdistance) {
					de = new DirectedEdge(n.ID - 1, v.ID - 1, 1);
				} else {
					de = new DirectedEdge(n.ID - 1, v.ID - 1, n.getPosition().distanceTo(v.getPosition()));
				}
				RMYSMatrix.addEdge(de);

			}

		}

		int V = UDGMatrix.V();

		FloydWarshall UDGfw = new FloydWarshall(UDGMatrix);
		FloydWarshall RMYSfw = new FloydWarshall(RMYSMatrix);

		double ratio = Algorithms.spanningRatio(UDGfw, RMYSfw, V);
		return ratio;
	}

	private static Set<NewPhysicalGraphNode> buildRMYS(Set<NewPhysicalGraphNode> neighborhood, NewPhysicalGraphNode n) {
		return RMYS.calculateMYS((NewPhysicalGraphNode) n, neighborhood);

	}

	public static HashMap<Node, Set<NewPhysicalGraphNode>> createMYSNeighborhood() {
		HashMap<Node, Set<NewPhysicalGraphNode>> completeRMYSGraph = new HashMap<>();

		for (Node n : Tools.getNodeList()) {
			Collection<Node> neighborhood = Algorithms.getNeighborNodes(n, Tools.getNodeList());
			Set<Node> PDTNodes = Algorithms.buildPartialDelaunayTriangulation(neighborhood, n);
			Set<NewPhysicalGraphNode> castedPDTNodes = new HashSet<>();

			for (Node p : PDTNodes) {
				if (p instanceof NewPhysicalGraphNode) {
					castedPDTNodes.add((NewPhysicalGraphNode) p);
				}
			}

			Set<NewPhysicalGraphNode> RMYSNodes = buildRMYS(castedPDTNodes, (NewPhysicalGraphNode) n);
			completeRMYSGraph.put(n, RMYSNodes);
		}

		// ensure bidrectional edges
		for (Node n : completeRMYSGraph.keySet()) {
			for (Node p : completeRMYSGraph.get(n)) {
				if (!completeRMYSGraph.get(p).contains(n)) {
					logger.log(LogL.ERROR_DETAIL,
							n.toString() + " has a unidirectional edge to " + p.toString() + " " + n.toString() + ": "
									+ completeRMYSGraph.get(n) + " " + p.toString() + ": " + completeRMYSGraph.get(p)
									+ '\n' + "Values may be inaccurate!");
				}
			}
		}
		return completeRMYSGraph;
	}
	
	
	//====================PDT CREATION===========
	
	public static double PDTSpan(HashMap<Node, Set<Node>> graph, boolean hopdistance){
		AdjMatrixEdgeWeightedDigraph UDGMatrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size()+1);
		AdjMatrixEdgeWeightedDigraph PDTMatrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size()+1);
		
		HashMap<Node, Set<Node>> completePDTGraph = createPDTNeighborhood();
		
		for(Node n : Tools.getNodeList()){
			Collection<Node> neighborhood = Algorithms.getNeighborNodes(n, Tools.getNodeList());
			
			for(Node v: neighborhood)
			{
				//add edge UDG
				DirectedEdge de;
				if(hopdistance){
					de = new DirectedEdge(n.ID, v.ID, 1);
				}else{
					de = new DirectedEdge(n.ID, v.ID, n.getPosition().distanceTo(v.getPosition()));
				}
				UDGMatrix.addEdge(de);
			}
			for(Node v: completePDTGraph.get(n)){
				//add edge GG
				DirectedEdge de;
				if(hopdistance){
					de = new DirectedEdge(n.ID, v.ID, 1);
				}else{
					de = new DirectedEdge(n.ID, v.ID, n.getPosition().distanceTo(v.getPosition()));
				}
				PDTMatrix.addEdge(de);
			}
		}
		
		int V = UDGMatrix.V();
		
		FloydWarshall UDGfw = new FloydWarshall(UDGMatrix);
		FloydWarshall PDTfw = new FloydWarshall(PDTMatrix);
		
		return Algorithms.spanningRatio(UDGfw, PDTfw, V);
		
	}
	
	public static HashMap<Node, Set<Node>> createPDTNeighborhood(){
	
		HashMap<Node, Set<Node>> pdtneighbors=new HashMap<>();
		
		for(Node n : Tools.getNodeList()){
			Collection<Node> neighborhood = Algorithms.getNeighborNodes(n, Tools.getNodeList());
			Set<Node> pdtNodes = Algorithms.buildPartialDelaunayTriangulation(neighborhood, n);
			pdtneighbors.put(n, pdtNodes);
		}
		return pdtneighbors;
	}
	
}
