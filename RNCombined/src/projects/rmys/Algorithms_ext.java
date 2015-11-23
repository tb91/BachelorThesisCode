package projects.rmys;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.management.RuntimeErrorException;

import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.FloydWarshall.AdjMatrixEdgeWeightedDigraph;
import projects.reactiveSpanner.FloydWarshall.DirectedEdge;
import projects.reactiveSpanner.FloydWarshall.FloydWarshall;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import projects.rmys.nodes.messageHandler.RMYS;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;
import sinalgo.nodes.Node;
import sinalgo.runtime.nodeCollection.NodeCollectionInterface;
import sinalgo.tools.Tools;

public class Algorithms_ext {

	
	public static double rmysSpan(boolean topologieSpan){
		AdjMatrixEdgeWeightedDigraph UDGMatrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size());
		AdjMatrixEdgeWeightedDigraph RMYSMatrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size());
		
		//needed to guarantee bidirectional edges
		HashMap<Node, Set<NewPhysicalGraphNode>> completeRMYSGraph=new HashMap<>();
		
		
		for(Node n : Tools.getNodeList()){
			Collection<Node> neighborhood = Algorithms.getNeighborNodes(n, Tools.getNodeList());
			Set<Node> PDTNodes = Algorithms.buildPartialDelaunayTriangulation(neighborhood,n);
			Set<NewPhysicalGraphNode> castedPDTNodes=new HashSet<>();
			
			for(Node p: PDTNodes){
				if(p instanceof NewPhysicalGraphNode){
					castedPDTNodes.add((NewPhysicalGraphNode) p);
				}
			}
			
			Set<NewPhysicalGraphNode> RMYSNodes = buildRMYS(castedPDTNodes, (NewPhysicalGraphNode)n);
			completeRMYSGraph.put(n, RMYSNodes);
			for(Node v: neighborhood)
			{
				DirectedEdge de = null;
				//add edge UDG
				// ID starts with 1, matrix with index 0
				if(topologieSpan){
					de = new DirectedEdge(n.ID-1, v.ID-1, 1);
				}else{
					de = new DirectedEdge(n.ID-1, v.ID-1, n.getPosition().distanceTo(v.getPosition()));	
				}
				
				UDGMatrix.addEdge(de);
			}
			
			for(Node v: RMYSNodes){
				DirectedEdge de = null;
				//add edge GG
				// ID starts with 1, matrix with index 0
				if(topologieSpan){
					de = new DirectedEdge(n.ID-1, v.ID-1, 1);
				}else{
					de = new DirectedEdge(n.ID-1, v.ID-1, n.getPosition().distanceTo(v.getPosition()));	
				}
				RMYSMatrix.addEdge(de);

			}
			
		}
		
		//ensure bidrectional edges
		for(Node n: completeRMYSGraph.keySet()){
			for(Node p:completeRMYSGraph.get(n)){
				if(!completeRMYSGraph.get(p).contains(n)){
					throw new RuntimeException(n.toString() + " has a unidirectional edge to " + p.toString() + '\n'
							+ n.toString() + ": "+ completeRMYSGraph.get(n) + " " + p.toString() + ": " + completeRMYSGraph.get(p));
				}
			}
		}
		
		
		int V = UDGMatrix.V();
		
		FloydWarshall UDGfw = new FloydWarshall(UDGMatrix);
		FloydWarshall RMYSfw = new FloydWarshall(RMYSMatrix);
		
		double ratio= Algorithms.spanningRatio(UDGfw, RMYSfw, V);
		return ratio;
	}
	

	private static Set<NewPhysicalGraphNode> buildRMYS(Set<NewPhysicalGraphNode> neighborhood, NewPhysicalGraphNode n) {
		return RMYS.calculateMYS((NewPhysicalGraphNode) n, neighborhood);
		
	}
}
