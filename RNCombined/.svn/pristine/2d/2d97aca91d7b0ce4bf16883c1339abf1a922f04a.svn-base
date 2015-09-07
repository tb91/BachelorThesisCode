package projects.reactiveSpanner.nodes.messageHandlers.buildBackbone;

import java.awt.Graphics;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.jdom.output.EscapeStrategy;

import com.sun.org.apache.bcel.internal.generic.GOTO;

import projects.reactiveSpanner.CustomGlobal;
import projects.reactiveSpanner.Utilities;
import projects.reactiveSpanner.nodes.messageHandlers.AbstractMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;
import sun.awt.SubRegionShowable;

/**
 * @author Tim
 * This topology control creates virtual nodes on each edge intersection of all
 * a) outgoing connections of each node if connectionTopology is null
 * b) knownNeighbors from the topology control with id connectionTopology
 * 
 * each (virtual and real) node receives routing to all its neighbors considering virtualNodes on this edge.
 * There is one exception, since there is another error in the paper. In some cases the controller of a virtual node
 * does not know all virtual nodes on the edge which crosses the edges of the controller due to a lack of information
 * from the 3-hop-beaconing (4-hop-beaconing would be sufficient). We found another solution which uses 3-hop-beaconing:
 * if the message is virtually routed (eg.: to reach a node over an edge with virtual nodes):
 * if the message would be routed over an edge with a virtual node, the sending node must check first, if it is the 
 * controller edge (the controller is either startnode or endnode of this edge) of this virtual node. if so, send to this virtual node.
 * if not: look whether you are aware of another virtual node behind the virtual node:
 * if so: send to this virtual node
 * if not: send to the first virtual node 
 * 
 */
public class CreateVirtuals extends BeaconTopologyControl {

	/**
	 * connectionTopology defines the topologyControl from which the knownNeighbors are taken if null then take outgoingConnections of each node
	 */
	UUID connectionTopology;

	public CreateVirtuals(PhysicalGraphNode sourceNode, UUID connectionTopology) {
		super(EStrategy.CREATE_VIRTUALS, sourceNode);
		this.connectionTopology = connectionTopology;

	}

	@Override
	protected void _start() {

		for (PhysicalGraphNode p : Utilities.getNodeCollectionByClass(PhysicalGraphNode.class)) {

			CreateVirtualsMessageHandler cvmh;

			if (connectionTopology == null) {//if there is no topologycontrol specified which this topologycontrol should use, take outgoing connections

				Set<PhysicalGraphNode> cons = new HashSet<PhysicalGraphNode>();
				for (Edge e : p.outgoingConnections) {
					cons.add((PhysicalGraphNode) e.endNode); 

				}
				cvmh = new CreateVirtualsMessageHandler(getTopologyControlID(), p, p, EStrategy.CREATE_VIRTUALS, cons);
			} else {
				//SubgraphStrategy strategy = p.requestSubgraph(EStrategy.BUILD_BACKBONE);  //does not work since the subgraph strategy only has one source node.
			    AbstractMessageHandler strategy=p.getMessageHandler(connectionTopology);	//but I want to have the subgraphnodes of each node.
				cvmh = new CreateVirtualsMessageHandler(getTopologyControlID(), p, p, EStrategy.CREATE_VIRTUALS, strategy.getKnownNeighbors());
			
			}
			if (!sourceNode.equals(p)) {//filter holder
				p.subgraphStrategyFactory.handOverSubgraphStrategy(this);
			}

			p.messageHandlerMap.put(getTopologyControlID(), cvmh);

			cvmh.start();
		}
	}

	@Override
	protected void _init() {
		// not needed
		
	}

	@Override
	public void draw(Graphics g, PositionTransformation pt) {
		
		for (PhysicalGraphNode p : Utilities.getNodeCollectionByClass(PhysicalGraphNode.class)) {
			p.getMessageHandler(this.getTopologyControlID()).drawNode(g, pt);

		}//draw method of each createVirtuals messagehandler
	}

}
