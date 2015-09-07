package projects.reactiveSpanner.nodes.messageHandlers;

import java.util.ArrayList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import projects.defaultProject.nodes.timers.MessageTimer;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messages.BeaconReplyMessage;
import projects.reactiveSpanner.nodes.messages.BeaconRequestMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import projects.reactiveSpanner.nodes.timers.BeaconTimer;
import sinalgo.nodes.Node;

/**
 * @author Tim, edited by cyron
 * Used for BuildBackbone and other topology controls with beaconing
 *
 */
public abstract class BeaconMessageHandler<T extends SimpleNode> extends AbstractMessageHandler<T>{
	/**
	 * contains MessageTimer pending to be send
	 */
	protected Queue<MessageTimer> messageTimerQueue;
	
	protected BeaconTimer beaconTimer;

	/**
	 * contains Node IDs for which a replayMessage has already sent
	 */
	protected ArrayList<Node> requestedNodes;
	
	protected BeaconMessageHandler(UUID tcID, PhysicalGraphNode ownerNode, PhysicalGraphNode sourceNode, EStrategy strategy) {
		super(tcID, ownerNode, sourceNode, strategy);
		messageTimerQueue=new LinkedBlockingQueue<MessageTimer>();
	
		requestedNodes=new ArrayList<Node>();
	}

	/**
	 * @param brm the received Message
	 * overwrite this method to handle ReplyMessages
	 * 
	 */
	public void receivedBeaconReplyMessage(BeaconReplyMessage brm){
				
	}
	
	/**
	 * @param brm the received Message
	 * overwrite this method to handle RequestMessages
	 * use super.receivedBeaconRequestMessage!
	 */
	public void receivedBeaconRequestMessage(BeaconRequestMessage brm){
		brm.forwardStep(this.node);
		requestedNodes.add(brm.getTransmitter());
	}
	
	/**
	 * enqueues a MessageTimer into the MessageTimer
	 * Queue. 
	 * 
	 * @param msgt MessageTimer can be Broadcast 
	 * or single target
	 * @return true if enqueue was successful
	 */
	protected boolean enqueue(MessageTimer msgt){
		return messageTimerQueue.add(msgt);
	}
	
	/**
	 * starts all Timers in the MessageTimer Queue
	 * with Time 1. 
	 */
	protected void executeTimerQueue(){
		// the way queues are meant to use
		while(!messageTimerQueue.isEmpty()){
			MessageTimer mt = messageTimerQueue.poll();
			mt.startRelative(1, node);
		}
		/*for(MessageTimer msgt : messageTimerQueue){
			msgt.startRelative(1, sourceNode);
		}
		messageTimerQueue.clear();//do not REMOVE this! :-)
		*/
	}
	
	/**
	 * This function will be used by the BeaconTimer
	 * when it's fired. 
	 * Override it to give functionality to it
	 */
	public abstract void beaconTimerFire();
}
