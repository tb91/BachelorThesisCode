package projects.reactiveSpanner.nodes.messageHandlers.simpleTopologyControls;

import java.awt.Graphics;
import java.util.Collection;
import java.util.Set;

import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.Utilities;
import projects.reactiveSpanner.nodes.messageHandlers.AbstractTopologyControl;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.gui.transformation.PositionTransformation;

/**
 * Local partial Delaunay Triangulation calculation used in combination with the subgraphStrategy framework
 * 
 * @author Mavs
 *
 */
public class LocalPDT extends AbstractTopologyControl
{	
	Set<PhysicalGraphNode> localPDT;
	
	public LocalPDT(final PhysicalGraphNode sourceNode) {
		super(EStrategy.PDT, sourceNode);
		_init();
	}

	@Override
	public Set<PhysicalGraphNode> getSubgraphNodes() {
		return localPDT;
	}

	@Override
	protected void _init() {
		Collection<PhysicalGraphNode> neighborhood = Algorithms.getNeighborNodes(sourceNode, Utilities.getNodeCollectionByClass(PhysicalGraphNode.class));
		localPDT = Algorithms.buildPartialDelaunayTriangulation(neighborhood, sourceNode);
		terminate();
	}

	@Override
	protected void _start() {
		_init();
	}
	@Override
	public void draw(Graphics g, PositionTransformation pt) {
	}
}
