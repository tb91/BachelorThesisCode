package projects.reactiveSpanner.routing;

import java.util.Set;

import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;

/**
 * Simple greedy forwarding algorithm. Following that neighbor that is closest to the destination node.
 * When there is no neighbor closer than the currently holding node, greedy forwarding got stucked.
 * 
 * @author Matthias von Steimker
 * @param <T> type of nodes that are used for routing
 */
public class GreedyForwarding<T extends PhysicalGraphNode> extends
		RoutingProtocol<T> {
	private boolean stucking;

	protected GreedyForwarding(final T sourceNode, final T destinationNode,
			final T currentHolder, final EStrategy subgraphStrategy) {
		super(sourceNode, destinationNode, currentHolder, subgraphStrategy,
				ERouting.GREEDY);
	}

	@Override
	protected void init() {
		stucking = false;
	}

	@Override
	protected void _requestedNextRoutingStep() {
		nextHop = calculateNextHop();
	}

	/**
	 * @return the next hop or <b>null</b> if a next hop could not been found
	 */
	private T calculateNextHop() {
		Set<T> subgraphNodes = subgraphInterface.getSubgraphNodes();

		// TODO taking only nodes in UDG distance to the holding node, consider also
		// other connectivity models
		subgraphNodes = Algorithms.getOneHopNeighbors(currentHolder,
				subgraphNodes);

		T mostGainedNode = this.currentHolder;
		for (T neighbor : subgraphNodes) {
			if (neighbor.getPosition().squareDistanceTo(
					super.getDestination().getPosition()) < mostGainedNode
					.getPosition().squareDistanceTo(
							super.getDestination().getPosition())) {
				mostGainedNode = neighbor;
			}
		}
		if (mostGainedNode.equals(this.currentHolder)) {
			this.stucking = true;
			return null;
		} else {
			this.stucking = false;
			return mostGainedNode;
		}
	}

	@Override
	protected boolean _isStucked() {
		return stucking;
	}
}
