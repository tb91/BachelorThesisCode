package projects.reactiveSpanner.nodes.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import projects.reactiveSpanner.Counter;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.buildBackbone.CreateVirtualsMessageHandler;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.LogL;

/**
 * Message that is a reply to a beaconing request. 
 * Contains a reply path for k-hop beaconing. 
 * 
 * @author cyron, Tim
 *
 */
public class NewBeaconReplyMessage extends BeaconMessage {
	/**
	 * count all messages individual for every subgraph creation ID
	 */
	private static Map<UUID, Counter> counterMap = new HashMap<UUID, Counter>();
	
	
	public HashMap<Integer, PhysicalGraphNode> nodes=new HashMap<Integer, PhysicalGraphNode>();
	
	/**
	 * saves the creator (=owner) of this message
	 */
//	private PhysicalGraphNode creator=new PhysicalGraphNode();
	
	public NewBeaconReplyMessage(UUID tcID, PhysicalGraphNode transmitter, EStrategy strategy
			, HashMap<Integer, PhysicalGraphNode> collectedNodes) {
		super(tcID, transmitter, strategy);
		createMessage(transmitter,collectedNodes);
		
	}
	
	private void createMessage(PhysicalGraphNode transmitter, HashMap<Integer, PhysicalGraphNode> nodes2) {
		
		
		if(counterMap.containsKey(ID))
		{
			counterMap.get(ID).incValue();
		} else{
			Counter c = new Counter(1);
			counterMap.put(ID, c);
		}
		
		for(Integer i:nodes2.keySet()){
			nodes.put(i, nodes2.get(i));
		}
		
	}

	public NewBeaconReplyMessage(UUID tcID, PhysicalGraphNode transmitter, PhysicalGraphNode creator, EStrategy strategy
			, HashMap<Integer, PhysicalGraphNode> nodes) {
		super(tcID, transmitter, strategy);
		createMessage(transmitter,nodes);
		
		
		
	}
	
		
	
	@Override
	public Message clone(){
		
		return directClone();
		//since clone does not work correctly, we should use this instead
		//if you need for some reason correct cloning, tell me (Tim) please
		//return new BeaconReplyMessage(ID, getTransmitter(),getCreator(), strategy, replyPath);
	}
	
	/**
	 * @return number of all sent messages of this type
	 */
	public static int numberOfSentMessages(final UUID tcID) {
		if(counterMap.containsKey(tcID))
			return counterMap.get(tcID).value();
		else 
		{
			logger.logln(LogL.WARNING, "UUID " + tcID + " does not exists in BeaconReply pool!");
			return 0;
		}
	}

	public NewBeaconReplyMessage directClone(){
		
		return new NewBeaconReplyMessage(this.ID, getTransmitter(), this.getStrategy(), nodes );
	}


}
