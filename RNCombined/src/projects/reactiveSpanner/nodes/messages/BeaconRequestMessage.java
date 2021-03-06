package projects.reactiveSpanner.nodes.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import projects.reactiveSpanner.Counter;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.LogL;

/**
 * Message that requests a beaconing to get
 * neighbourhood information. Contains a 
 * beaconHopRange and hops to do as well it is
 * saving the routing path it took for a reply.
 * 
 * @author cyron, Tim
 *
 */
public class BeaconRequestMessage extends BeaconMessage{
	
	/**
	 * Number of hops beaconing is getting information from
	 */
	private final int beaconHopRange;
	
	/**
	 * hops the beacon message has left to do
	 */
	private int beaconHopsToDo;
	
	/**
	 * The history of nodes visited by the beacon message
	 */
	private Stack<Node> beaconHistory;
	
	public boolean forward;
	
	private static Map<UUID, Counter> counterMap = new HashMap<UUID, Counter>();
	
	/**
	 * saves the creator (=owner) of this message
	 */
	//private PhysicalGraphNode creator=new PhysicalGraphNode();

	
	
		
	public BeaconRequestMessage(UUID tcID, PhysicalGraphNode transmitter,
			EStrategy strategy, int beaconHopRange) {
		super(tcID, transmitter, strategy);
		this.beaconHopRange=beaconHopRange;
		
		createMessage(transmitter);
	}
	
	private void createMessage(PhysicalGraphNode transmitter){
		this.beaconHistory = new Stack<Node>();
		
		beaconHopsToDo=beaconHopRange;
		if(beaconHopRange>0){
			forward=true;	
		}else{
			forward=false;
		}
		
		
		if(this.beaconHistory.isEmpty())
			addToHistory(transmitter);
		
		if(counterMap.containsKey(ID))
		{
			counterMap.get(ID).incValue();
		} else{
			Counter c = new Counter(1);
			counterMap.put(ID, c);
		}
		
		
	}
	
	
	private BeaconRequestMessage(UUID tcID, PhysicalGraphNode transmitter, PhysicalGraphNode creator,
			EStrategy strategy, int beaconHopRange, Stack<Node> history) {
		super(tcID, transmitter, strategy);
		this.beaconHopRange=beaconHopRange;
		createMessage(transmitter);
		this.beaconHistory=history;
		
	}
	
	public static int numberOfSentMessages(final UUID tcID) {
		if(counterMap.containsKey(tcID))
			return counterMap.get(tcID).value();
		else 
		{
			logger.logln(LogL.WARNING, "UUID " + tcID + " does not exists in BeaconRequestMessage counterMap pool and therefor no CTS message was sent. Is this right?");
			return 0;
		}
	}
	
	
	/**
	 * performs a forward step of the message,
	 * meaning decreasing the hops it has to do for its beacon and
	 * adding the last visited node to history
	 * @param n the node that has to be added to history
	 * @return true if message has to be forwarded
	 */
	public boolean forwardStep(Node n){
		boolean fw = decreaseHopsToDo();
		forward=fw;
		if(forward){
			addToHistory(n);
			
		}
		return fw;
		
		
	}
	
	private void addToHistory(Node n){
		beaconHistory.add(n);
	
	}
	
	private boolean decreaseHopsToDo(){	
		
		beaconHopsToDo -=1;
		if(beaconHopsToDo<=0){
			forward=false;
			return false;
		}

		forward=true;
		return true;
	}
	
	public Stack<Node> getBeaconHistory(){
		return (Stack<Node>) beaconHistory.clone();  //ITS URGENT TO CLONE THE HISTORY
	}

	@Override
	public Message clone() {
		return directClone();  
		//since clone does not work correctly, we should use this instead
		//if you need for some reason correct cloning, tell me (Tim) please
		//return new BeaconRequestMessage(ID, getTransmitter(),getCreator(), strategy, beaconHopRange);
	}

	public BeaconRequestMessage directClone(){
		
					
		return new BeaconRequestMessage(this.ID, getTransmitter(),getTransmitter(), this.getStrategy(), beaconHopsToDo, this.getBeaconHistory());
	}

	
	
}
