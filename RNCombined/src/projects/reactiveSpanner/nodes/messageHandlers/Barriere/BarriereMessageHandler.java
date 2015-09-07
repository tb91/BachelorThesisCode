package projects.reactiveSpanner.nodes.messageHandlers.Barriere;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import projects.defaultProject.nodes.timers.MessageTimer;
import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.Counter;
import projects.reactiveSpanner.Disk2D;
import projects.reactiveSpanner.nodes.messageHandlers.AbstractMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.Barriere.Barriere.Init;
import projects.reactiveSpanner.nodes.messageHandlers.Barriere.Barriere.Phase;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.messages.BeaconMessage;
import projects.reactiveSpanner.nodes.messages.BeaconReplyMessage;
import projects.reactiveSpanner.nodes.messages.BeaconRequestMessage;
import projects.reactiveSpanner.nodes.messages.CTS;
import projects.reactiveSpanner.nodes.messages.NewNeighbour;
import projects.reactiveSpanner.nodes.messages.ProtestMessage;
import projects.reactiveSpanner.nodes.messages.RTS;
import projects.reactiveSpanner.nodes.messages.VirtualMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import projects.reactiveSpanner.nodes.timers.BeaconTimer;
import projects.reactiveSpanner.record.BFPMessageRecord;
import projects.reactiveSpanner.record.BarriereMessageRecord;
import projects.reactiveSpanner.record.MessageRecord;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;

public class BarriereMessageHandler extends BeaconMessageHandler<SimpleNode>{
	
	// ==========================================================================
	// ==================== START SUPPORT STRUCTURES ============================
	// ==========================================================================
	
	/**
	 * mapping for virtual nodes
	 */
	protected HashMap<Integer, SimpleNode> virtualNodes;
	/**
	 * mapping from virtual node ID to physical
	 * hopnode. WARNING: Integer key is NOT ID of 
	 * the physical hop node but the virtual node. 
	 */
	protected HashMap<Integer, SimpleNode> physicalHopNodes;
	/**
	 * mapping for all unprocessed nodes.
	 */
	protected HashMap<Integer, SimpleNode> unprocessedNodes;
	/**
	 * nodes that are deleted due to extractionPhase
	 */
	protected HashMap<Integer, SimpleNode> extractedNodes;
	/**
	 * virtual neighbour nodes that are deleted due to extractionPhase
	 */
	protected HashMap<Integer, SimpleNode> extractedVirtualNodes;
	/**
	 * map the hop distances between virtual connected neighbours
	 * (Integer, Integer = nodeID, hops)
	 */
	protected HashMap<Integer, Integer> hopDistances; 
	
	protected boolean started = false;
	
	public static Phase p = Phase.STEADY;
	public static Init i = Init.EPIDEMIC;
	
	

	protected BarriereMessageHandler(UUID tcID, PhysicalGraphNode ownerNode,
			PhysicalGraphNode sourceNode, EStrategy strategy) {
		super(tcID, ownerNode, sourceNode, strategy);

		// mapping of virtuals and unprocessed
		this.initializeMapping();
	}
	
	public BarriereMessageHandler(final UUID routingID, final PhysicalGraphNode ownerNode, final PhysicalGraphNode forwarderNode){
		super(routingID, ownerNode, forwarderNode, BeaconlessTopologyControl.EStrategy.BARRIERE);

		// mapping of virtuals and unprocessed
		this.initializeMapping();
		
	}
	
	// ==========================================================================
	// ===================== END SUPPORT STRUCTURES =============================
	// ==========================================================================

	// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	
	// ==========================================================================
	// ===================== START RECEIVE FUNCTIONS ============================
	// ==========================================================================

	@Override
	public void receivedMessage(AbstractMessage msg) {
		if(msg instanceof BeaconRequestMessage)
		{
			receivedBeaconRequestMessage((BeaconRequestMessage)msg);
		}
		else if(msg instanceof BeaconReplyMessage)
		{
			receivedBeaconReplyMessage((BeaconReplyMessage)msg);
		}
		else if(msg instanceof VirtualMessage)
		{
			receivedVirtualMessage((VirtualMessage)msg);
		}
		else if(msg instanceof NewNeighbour){
			receivedNewNeighbourMessage((NewNeighbour)msg);
		}
		
	}

	/**
	 * handles NewNeighbour messages. Neighbours that are 
	 * not known will be added to virtualNodes
	 * @param nn NewNeighbour message containing the new 
	 * neighbour node
	 */
	public void receivedNewNeighbourMessage(NewNeighbour nn){
		// does not have neighbour yet
		if(!hasNeighbour(nn.getNewNeighbour())){
			addVirtualNeighbour(nn.getNewNeighbour(), nn.getTransmitter());
			
			// neighbour set changed, start completion & extraction again
			completionPhase();
		}
	}
	
	/**
	 * handles virtual messages that are sent
	 * when a virtual neighbours has to be reached.
	 * the message is then received by the responsible 
	 * hop node.
	 * @param vm containing original Message (with destination
	 * == virtual node of transmitter)
	 */
	public void receivedVirtualMessage(VirtualMessage vm){
		
		MessageTimer original = vm.getOriginalMessageTimer();
		messageTimerQueue.add(original);
		this.executeTimerQueue();
	}
	
	@Override
	public void receivedBeaconRequestMessage(BeaconRequestMessage brm){
		if(!requestedNodes.contains(brm.getTransmitter())){
			
			super.receivedBeaconRequestMessage(brm);	
			BeaconReplyMessage brp = new BeaconReplyMessage(tcID,this.node,planarSubgraphCreationStrategy,brm.getBeaconHistory());
			MessageTimer mt = new MessageTimer(brp,brm.getTransmitter());
			
			requestedNodes.add(brm.getTransmitter());
			
			// very important, send to neighbours which are not yet known in neighbour lists
			this.enqueue(mt);
		}
		if(i==Init.EPIDEMIC){
			if(!started){
				initializeAdjacency();
				started=true;
				
			}
		}
		
		this.knownNeighbors.add(brm.getTransmitter());
		this.unprocessedNodes.put(brm.getTransmitter().ID, brm.getTransmitter());
		this.executeTimerQueue();
	}
	
	/**
	 * simple 1 hop beacon approach: adds
	 * the neighbour that sent the BeaconReplyMessage
	 * to knownNeighbours
	 */
	@Override
	public void receivedBeaconReplyMessage(BeaconReplyMessage brm){
		if(!hasNeighbour(brm.getTransmitter())){
			super.knownNeighbors.add(brm.getTransmitter());
			unprocessedNodes.put(brm.getTransmitter().ID, brm.getTransmitter());
		}
	}
	
	// ==========================================================================
	// ====================== END RECEIVE FUNCTIONS =============================
	// ==========================================================================
	
	// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	
	// ==========================================================================
	// ===================== START ROUTING FUNCTIONS ============================
	// ==========================================================================

	// TODO apply deleted nodes
	@Override
	public void executeTimerQueue(){
		//System.out.println("much send?");
		
		while(!messageTimerQueue.isEmpty()){
			MessageTimer mt = messageTimerQueue.poll();
			if(mt.getReceiver()!=null){
				if(hasNeighbour(mt.getReceiver())){
					// if target is virtual neighbour
					if(isVirtualNode(mt.getReceiver())){
						//System.out.println("much send?");
						VirtualMessage vm = new VirtualMessage(tcID, node, planarSubgraphCreationStrategy, mt);
						MessageTimer vmt = new MessageTimer(vm, getHopNode(mt.getReceiver()));
						messageTimerQueue.add(vmt);
						
					}else{
						//System.out.println("much send!");
						mt.startRelative(1, node);
					}
				}
			}else{
				// broadcast hmm
				//System.out.println("mucho sendo");
				mt.startRelative(1, node);
			}
			
		}
	}
	
	/**
	 * subgraph neighbourhood message execution
	 */
	public void executeSubgraphTimerQueue(){
		
		while(!messageTimerQueue.isEmpty()){
			MessageTimer mt = messageTimerQueue.poll();
			
			if(mt.getReceiver()!=null){
				if(hasSubgraphNeighbour(mt.getReceiver())){
					// if target is virtual neighbour
					if(isVirtualNode(mt.getReceiver())){
						VirtualMessage vm = new VirtualMessage(tcID, node, planarSubgraphCreationStrategy, mt);
						MessageTimer vmt = new MessageTimer(vm, getHopNode(mt.getTargetNode()));
						messageTimerQueue.add(vmt);
					}else{
						mt.startRelative(1, node);
					}
				}
			}else{
				// broadcast hmm
				mt.startRelative(1, node);
			}
		}
	}
	
	public void executeTimerQueueInit(){
		while(!messageTimerQueue.isEmpty()){
			MessageTimer mt = messageTimerQueue.poll();
			
			if(mt.getReceiver()!=null){
				if(isVirtualNode(mt.getReceiver())){
					VirtualMessage vm = new VirtualMessage(tcID, node, planarSubgraphCreationStrategy, mt);
					MessageTimer vmt = new MessageTimer(vm, getHopNode(mt.getTargetNode()));
					messageTimerQueue.add(vmt);
				}else{
					mt.startRelative(1, node);
				}
			}else{
				mt.startRelative(1, node);
			}
		}
	}
	
	// ==========================================================================
	// ====================== END ROUTING FUNCTIONS =============================
	// ==========================================================================
	
	// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	
	// ==========================================================================
	// ==================== START ALGORITHM FUNCTIONS ===========================
	// ==========================================================================
	
	/**
	 * processes the completion phase of barriere
	 * algorithm for every unprocessed neighbour.
	 * 
	 * note that hasNeighbour is relevant instead of
	 * hasSubgraphNeighbour.
	 */
	public void completionPhase(){
		// phase control
		p=Phase.COMPLETION;
		
		// process every node once
		HashMap<Integer, SimpleNode> unprocessedNodesCopy = (HashMap<Integer, SimpleNode>) unprocessedNodes.clone();
		Collection<SimpleNode> unprocessed = unprocessedNodesCopy.values();
		
		for(SimpleNode v : unprocessed){
			for(SimpleNode w : getAllKnownNeighbourNodes()){
				// no self processing
				if(w.ID==v.ID)
					continue;
				
				Set<Node> wSet = new HashSet<Node>();
				wSet.add(w);
				Disk2D disc = Algorithms.disk(node, v, wSet);
				
				if(!disc.nodesInDisk.isEmpty())// node w in disc u--v
				{
					try {
						// already initialized?
						double rMin = Configuration.getDoubleParameter("QUDG/rMin");
						double rMax = Configuration.getDoubleParameter("QUDG/rMax");
						
						double distance = v.getPosition().distanceTo(w.getPosition());
						if(distance>rMin && distance<=rMax)
						{
							// send NewNeighbour message
							NewNeighbour nn1 = new NewNeighbour(tcID, node, planarSubgraphCreationStrategy, w);
							NewNeighbour nn2 = new NewNeighbour(tcID, node, planarSubgraphCreationStrategy, v);
							
							// messages to timer queue
							this.enqueue(new MessageTimer(nn1, v));
							this.enqueue(new MessageTimer(nn2, w));
							
							
						}
					} catch (CorruptConfigurationEntryException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			// iterating over clone of hashmap,
			// deleting from original
			unprocessedNodes.remove(v.ID);
		}
		
		// send all messages
		this.executeTimerQueue();
		
		
		// phase control
		p=Phase.STEADY;
		Tools.repaintGUI();
		extractionPhase();
	}
	
	/**
	 * extracts a subgraph that is planar by
	 * using GG. Every deleted note gets
	 * put into extractedNodes (hasSubgraphNeighbour
	 * ignores extracted Nodes) for working on the
	 * subgraph
	 */
	public void extractionPhase(){
		// phase control
		p=Phase.EXTRACTION;
		
		for(SimpleNode v : getAllKnownSubgraphNeighbourNodes()){
			Set<SimpleNode> aknn = (Set<SimpleNode>)getAllKnownNeighbourNodes().clone();
			aknn.remove(v);
			Disk2D disc = Algorithms.disk(node, v,aknn);
			
			if(!disc.nodesInDisk.isEmpty()){
				if(isVirtualNode(v)){
					virtualNodes.remove(v.ID);
				}
				extractNeighbour(v);
			}
		}
		
		// should work, equals is implemented
		// keep eye on this
		// removes all extracted physical nodes from
		// knownNeighbors.
		knownNeighbors.removeAll(extractedNodes.values());
		
		// phase control
		p=Phase.STEADY;
		Tools.repaintGUI();
	}
	
	// ==========================================================================
	// ===================== END ALGORITHM FUNCTIONS ============================
	// ==========================================================================
	
	// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	
	// ==========================================================================
	// ======================= START MISC FUNCTIONS =============================
	// ==========================================================================

	@Override
	public void drawNode(Graphics g, PositionTransformation pt) {
		// TODO
		Color backupColor = g.getColor();
		
		// black if unbusy, green if completionPhase,
		// blue if extractionPhase
		switch(p){
		
		case STEADY:
			node.setColor(Color.black);
		case COMPLETION:
			node.setColor(Color.green);
		case EXTRACTION:
			node.setColor(Color.blue);
		}
		
		
		// green line for virtual connections
		g.setColor(Color.green);
		for(Node n : virtualNodes.values()){
			pt.translateToGUIPosition(this.node.getPosition());
			int x1 = pt.guiX;
			int y1 = pt.guiY;
			pt.translateToGUIPosition(n.getPosition());
			int x2 = pt.guiX;
			int y2 = pt.guiY;
			
			g.drawLine(x1, y1, x2, y2);
		}
		
		// black line for physical connections
		g.setColor(Color.blue);
		for(Node n : knownNeighbors){
			pt.translateToGUIPosition(this.node.getPosition());
			int x1 = pt.guiX;
			int y1 = pt.guiY;
			pt.translateToGUIPosition(n.getPosition());
			int x2 = pt.guiX;
			int y2 = pt.guiY;
			
			g.drawLine(x1, y1, x2, y2);
		}
		
		g.setColor(backupColor);
	}
	
	
	// ==========================================================================
	// ======================== END MISC FUNCTIONS ==============================
	// ==========================================================================
	
	// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	
	// ==========================================================================
	// ======================= START HELP FUNCTIONS =============================
	// ==========================================================================
	
	@Override
	public MessageRecord getCurrentMessageRecord() {

		return new BarriereMessageRecord(tcID,
				NewNeighbour.numberOfSentMessages(tcID), 
				BeaconRequestMessage.numberOfSentMessages(tcID), 
				BeaconReplyMessage.numberOfSentMessages(tcID), 
				VirtualMessage.numberOfSentMessages(tcID));
	}
	
	@Override
	public Set<SimpleNode> getKnownNeighbors(){
		return getAllKnownSubgraphNeighbourNodes();
	}
	
	/**
	 * gets the hopnode to a target node
	 * which can be virtual. if its not virtual
	 * it will return targetNode
	 * 
	 * @param targetNode the possible virtual
	 * @return hopnode of targetNode or targetNode itself
	 */
	public Node getHopNode(Node targetNode){
		if(isVirtualNode(targetNode))
			return physicalHopNodes.get(targetNode.ID);
		return targetNode;
	}
	
	/**
	 * get all known neighbours including virtuals,
	 * extracted and normal neighbours
	 * @return collection of all neighbour nodes
	 */
	public HashSet<SimpleNode> getAllKnownNeighbourNodes(){
		
		HashSet<SimpleNode> nodes = getAllKnownSubgraphNeighbourNodes();
		nodes.addAll(extractedNodes.values());
		nodes.addAll(extractedVirtualNodes.values());
		
		return nodes;
	}
	
	/**
	 * get all known neighbours including virtuals
	 * and normal neighbours
	 * @param <T>
	 * 
	 * @return collection of all subgraph neighbour nodes
	 */
	public HashSet<SimpleNode> getAllKnownSubgraphNeighbourNodes(){
		HashSet<SimpleNode> nodes = new HashSet<SimpleNode>();
		
		// add all clones ;)
		nodes.addAll(knownNeighbors);
		nodes.addAll(virtualNodes.values());
		
		return nodes;
	}
	
	/**
	 * returns a HashSet of all ever known physical
	 * neighbour nodes. Use this if the original graph
	 * is needed. 
	 * @return all ever known physical nodes
	 */
	public HashSet<Node> getAllKnownPhysicalNeighbourNodes(){
		HashSet<Node> nodes = new HashSet<Node>();
		
		nodes.addAll(knownNeighbors);
		nodes.addAll(extractedNodes.values());
		
		return nodes;
	}
	
	/**
	 * adds a new virtual neighbour in a correct manner
	 * 
	 * @param targetNode the new virtual neighbour
	 * @param hopNode the physical hop neighbour
	 * to reach the virtual neighbour
	 * 
	 * addon: count physical hops?
	 */
	protected void addVirtualNeighbour(SimpleNode targetNode, SimpleNode hopNode){
		
		
		this.virtualNodes.put(targetNode.ID, targetNode);
		this.physicalHopNodes.put(targetNode.ID, hopNode);
		this.unprocessedNodes.put(targetNode.ID, targetNode);
	}
	
	/**
	 * puts a neighbour node into extractedNodes
	 * map.
	 * @param n node to extract
	 */
	protected void extractNeighbour(SimpleNode n){
		if(isVirtualNode(n)){
			extractedVirtualNodes.put(n.ID, n);
		}else{
			extractedNodes.put(n.ID, n);
		}
	}
	
	@Override
	public void beaconTimerFire(){
		// TODO start completion,
		// start extraction
		
		
		this.completionPhase();
	}
	
	public int getMaxHop(){
		int maxHop = 0;
		
		for(Node n : virtualNodes.values()){
			int currentHop = getHopsTo(n);
			if(currentHop>maxHop)
				maxHop=currentHop;
		}
		
		for(Node n : extractedVirtualNodes.values()){
			int currentHop = getHopsTo(n);
			if(currentHop>maxHop)
				maxHop=currentHop;
		}
		
		return maxHop;
	}
	
	/**
	 * local approach defining hops to virtual neighbours
	 * 
	 * some casting was done...
	 * 
	 * @param n the node to which the hop distance will be calculated
	 * @return hop distance to n
	 */
	public int getHopsTo(Node n){
		if(isVirtualNode(n)){
			//return getHopNode(n).getHopsTo
			BarriereMessageHandler bmh = (BarriereMessageHandler) ((PhysicalGraphNode)getHopNode(n)).getMessageHandler(this.tcID);
			return this.getHopsTo(getHopNode(n)) + bmh.getHopsTo(n);
		}else{
			return 1;
		}
	}
	
	// ==========================================================================
	// ======================== END HELP FUNCTIONS ==============================
	// ==========================================================================
	
	// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	
	// ==========================================================================
	// ====================== START CHECK FUNCTIONS =============================
	// ==========================================================================
	/**
	 * checks whether a node is virtual or not
	 * 
	 * @param targetNode node to be checked
	 * @return true if targetNode is virtual
	 */
	public boolean isVirtualNode(Node targetNode){
		/*if(physicalHopNodes.containsKey(targetNode.ID))
			return true;
		return false;*/
		/*if(virtualNodes==null)
			return false;
		if(targetNode==null)
			return false;
		boolean ret = virtualNodes.containsKey(targetNode.ID);
		ret = ret || extractedVirtualNodes.containsKey(targetNode.ID);*/
		
		return (virtualNodes.containsKey(targetNode.ID) || extractedVirtualNodes.containsKey(targetNode.ID));
	}
	
	/**
	 * checks whether a node is a neighbour
	 * of this message handlers node in the
	 * algorithm created subgraph or not
	 * 
	 * @return true if n is a subgraph neighbour
	 */
	public boolean hasSubgraphNeighbour(Node n){
		if(virtualNodes.containsKey(n.ID))
			return true;
		for(Node kn : knownNeighbors){
			if(kn.ID==n.ID)
				return true;
		}
		return false;
	}
	
	/**
	 * checks whether a node is a neighbour of
	 * this message handlers node or not
	 * 
	 * @param n node to be checked
	 * @return true if n is a neighbour
	 */
	public boolean hasNeighbour(Node n){
		return hasSubgraphNeighbour(n) || extractedNodes.containsKey(n.ID) || physicalHopNodes.containsKey(n.ID);
	}
	
	// ==========================================================================
	// ======================= END CHECK FUNCTIONS ==============================
	// ==========================================================================
	
	// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	
	// ==========================================================================
	// ====================== START INIT FUNCTIONS ==============================
	// ==========================================================================
	
	/**
	 * initializes all mapping structures
	 */
	protected void initializeMapping(){
		virtualNodes = new HashMap<Integer,SimpleNode>();
		physicalHopNodes = new HashMap<Integer,SimpleNode>();
		unprocessedNodes = new HashMap<Integer,SimpleNode>();
		extractedNodes = new HashMap<Integer,SimpleNode>();
		extractedVirtualNodes = new HashMap<Integer,SimpleNode>();
		hopDistances = new HashMap<Integer,Integer>();
	}
	
	/**
	 * broadcasts a beacon request to initialize
	 * neighbour adjacency list
	 */
	protected void initializeAdjacency(){
		//start Timer for init here
		// TODO
		
		started=true;
		BeaconRequestMessage brm = new BeaconRequestMessage(tcID, node, planarSubgraphCreationStrategy, 1);
		MessageTimer broadcastTimer = new MessageTimer(brm);
		this.enqueue(broadcastTimer);
		
		//special init
		this.executeTimerQueueInit();
		// time to stop receiving neighbour messages
		beaconTimer = new BeaconTimer(this, 5);
		
		
	}
	
	/**
	 * start for local init
	 * for init on a single node
	 */
	public void localInit(){
		//initializeAdjacency();
		i=Init.LOCAL;
		initializeAdjacency();
	}
	/**
	 * init for epidemic progression
	 * of the algorithm in the network
	 * (startAlgorithm broadcast)
	 */
	public void epidemicInit(){
		i=Init.EPIDEMIC;
		initializeAdjacency();
	}
	
	// ==========================================================================
	// ======================== END INIT FUNCTIONS ==============================
	// ==========================================================================
	
}
