package projects.reactiveSpanner.nodes.messageHandlers.simpleTopologyControls;

import java.awt.Graphics;
import java.util.Set;

import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.Utilities;
import projects.reactiveSpanner.nodes.messageHandlers.AbstractTopologyControl;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.gui.transformation.PositionTransformation;

/**
 * Local unit disk neighborhood calculation used in combination with the subgraphStrategy framework
 * 
 * @author Mavs
 *
 */
public class LocalUDG extends AbstractTopologyControl
{	
	Set<PhysicalGraphNode> localUDG;
	
	public LocalUDG(final PhysicalGraphNode sourceNode) {
		super(EStrategy.UDG, sourceNode);
		_init();
	}

	@Override
	public Set<PhysicalGraphNode> getSubgraphNodes() {
		return localUDG;
	}

	@Override
	protected void _init() {
		localUDG = Algorithms.getNeighborNodes(sourceNode, Utilities.getNodeCollectionByClass(PhysicalGraphNode.class));
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
