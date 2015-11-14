package projects.rmys;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.FloydWarshall.AdjMatrixEdgeWeightedDigraph;
import projects.reactiveSpanner.FloydWarshall.DirectedEdge;
import projects.reactiveSpanner.FloydWarshall.FloydWarshall;
import sinalgo.nodes.Node;
import sinalgo.runtime.nodeCollection.NodeCollectionInterface;
import sinalgo.tools.Tools;

public class Algorithms_ext {

	
	public static double rmysSpan(){
		AdjMatrixEdgeWeightedDigraph UDGMatrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size());
		AdjMatrixEdgeWeightedDigraph RMYSMatrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size());
		
		for(Node n : Tools.getNodeList()){
			Collection<Node> neighborhood = Algorithms.getNeighborNodes(n, Tools.getNodeList());
			Set<Node> PDTNodes = Algorithms.buildPartialDelaunayTriangulation(neighborhood,n);
			Set<Node> RMYSNodes = buildRMYS(PDTNodes, n, Tools.getNodeList());
			for(Node v: neighborhood)
			{
				DirectedEdge de = null;
				//add edge UDG
				// ID starts with 1, matrix with index 0
				de = new DirectedEdge(n.ID-1, v.ID-1, n.getPosition().distanceTo(v.getPosition()));
				UDGMatrix.addEdge(de);
			}
			for(Node v: RMYSNodes){
				DirectedEdge de = null;
				//add edge GG
				// ID starts with 1, matrix with index 0
				de = new DirectedEdge(n.ID-1, v.ID-1, n.getPosition().distanceTo(v.getPosition()));
				RMYSMatrix.addEdge(de);

			}
		}
		
		int V = UDGMatrix.V();
		
		FloydWarshall UDGfw = new FloydWarshall(UDGMatrix);
		FloydWarshall RMYSfw = new FloydWarshall(RMYSMatrix);
		
		double ratio= Algorithms.spanningRatio(UDGfw, RMYSfw, V);
		return ratio;
	}

	private static <T extends Node> Set<T> buildRMYS(Collection<T> neighborhood, T n, NodeCollectionInterface nodeList) {
		Set<T> RMYSNodes = new HashSet<T>();
		for(T p: neighborhood){
			
		}
		return null;
	}
}
