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
public class BeaconReplyMessage extends BeaconMessage {
	/**
	 * count all messages individual for every subgraph creation ID
	 */
	private static Map<UUID, Counter> counterMap = new HashMap<UUID, Counter>();
	/**
	 * reply path (routing history)
	 */
	private Stack<Node> replyPath;
	/**
	 * next node of the replyPath
	 */
	private Node next;
	
	public HashMap<Integer, CreateVirtualsMessageHandler> nodes=new HashMap<Integer, CreateVirtualsMessageHandler>();
	
	/**
	 * saves the creator (=owner) of this message
	 */
//	private PhysicalGraphNode creator=new PhysicalGraphNode();
	
	public BeaconReplyMessage(UUID tcID, PhysicalGraphNode transmitter, EStrategy strategy
			, Stack<Node> replyPath) {
		super(tcID, transmitter, strategy);
		createMessage(transmitter, replyPath);
		
	}
	
	private void createMessage(PhysicalGraphNode transmitter, Stack<Node> replyPath) {
		if(replyPath!=null){
			this.replyPath=(Stack<Node>) replyPath.clone();  //TODO: unnecessary
			if(!this.replyPath.isEmpty()){
				next=this.replyPath.peek(); //replyPath should not be empty here
							
			}	
		}else{
			next=null; //indicating broadcast
		}
		
		if(counterMap.containsKey(ID))
		{
			counterMap.get(ID).incValue();
		} else{
			Counter c = new Counter(1);
			counterMap.put(ID, c);
		}
//		System.out.println("NACHRICHT von: " + transmitter);
//		for(Node n:replyPath){
//			System.out.print(n + "   ");
//		}
//		System.out.println();
		
	}

	public BeaconReplyMessage(UUID tcID, PhysicalGraphNode transmitter, PhysicalGraphNode creator, EStrategy strategy
			, Stack<Node> replyPath) {
		super(tcID, transmitter, strategy);
		createMessage(transmitter, replyPath);
		
	}
	
	/**
	 * @return the node which should receive the message
	 */
	public Node getNextDestination(){
		return next;
	}
	
	/**
	 * @return 	true if message must be forwarded
	 * 			false else
	 * since messages do not have any behavior we need to simulate it
	 * do not use this method arbitrarily
	 */
	public boolean forwardStep(){
		if(replyPath==null || replyPath.size()<=1){
			next=null;
			return false;
		}else{
			replyPath.pop();	
			next=replyPath.peek();	//for cloning its required to save the current destination
			return true;
		}
		
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

	@SuppressWarnings("unchecked")
	public BeaconReplyMessage directClone(){
		if(replyPath==null){
			replyPath=new Stack<Node>();
		}
		return new BeaconReplyMessage(this.ID, getTransmitter(), this.getStrategy(), (Stack<Node>) replyPath.clone() );
	}


}
