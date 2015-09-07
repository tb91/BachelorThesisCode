package projects.reactiveSpanner.nodes.messageHandlers.simpleTopologyControls;

import java.awt.Graphics;
import java.util.Collection;
import java.util.Set;

import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.Utilities;
import projects.reactiveSpanner.nodes.messageHandlers.AbstractTopologyControl;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.tools.Tools;

/**
 * Local Gabrial Graph calculation used in combination with the subgraphStrategy framework
 * 
 * @author Mavs
 *
 */
public class LocalGG extends AbstractTopologyControl
{	
	Set<PhysicalGraphNode> localGabrielGraph;
	
	public LocalGG(final PhysicalGraphNode sourceNode) {
		super(EStrategy.GG, sourceNode);
		_init();
	}

	@Override
	public Set<PhysicalGraphNode> getSubgraphNodes() {
		return localGabrielGraph;
	}

	@Override
	protected void _init() {
		Collection<PhysicalGraphNode> neighborhood = Algorithms.getNeighborNodes(sourceNode, Utilities.getNodeCollectionByClass(PhysicalGraphNode.class));
		localGabrielGraph = Algorithms.buildGabrielGraph(neighborhood, sourceNode, Tools.getNodeList());
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
