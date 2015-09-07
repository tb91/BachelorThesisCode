package projects.reactiveSpanner.nodes.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.BridgePair;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.buildBackbone.BuildBackboneMessageHandler;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.messages.Message;

public class DeliverBridgeNodesMessage extends BeaconMessage {

	private HashMap<Integer, BridgePair> bridgeNodes;

	public DeliverBridgeNodesMessage(UUID tcID, PhysicalGraphNode transmitter, EStrategy strategy, HashMap<Integer, BridgePair> bridgeNodes) {
		super(tcID, transmitter, strategy);

	
		this.bridgeNodes =bridgeNodes ;

	}

	@Override
	public Message clone() {

		return new DeliverBridgeNodesMessage(this.ID, this.node, this.strategy, bridgeNodes);
	}

	public HashMap<Integer, BridgePair> getBridgeNodes() {
		return (HashMap<Integer, BridgePair>) bridgeNodes.clone();
	}

}
