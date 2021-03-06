package projects.reactiveSpanner.nodes.messageHandlers.BarriereExt;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import projects.defaultProject.nodes.timers.MessageTimer;
import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.Disk2D;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.Barriere.BarriereMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.Barriere.Barriere.Phase;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.messages.BeaconReplyMessage;
import projects.reactiveSpanner.nodes.messages.BeaconRequestMessage;
import projects.reactiveSpanner.nodes.messages.NewNeighbour;
import projects.reactiveSpanner.nodes.messages.NewWitness;
import projects.reactiveSpanner.nodes.messages.VirtualMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import projects.reactiveSpanner.record.BarriereExtMessageRecord;
import projects.reactiveSpanner.record.MessageRecord;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

public class BarriereExtMessageHandler extends BarriereMessageHandler{

	/**
	 * witnesses, known nodes that are not neighbours,
	 * but in PDT range
	 */
	protected HashMap<Integer, SimpleNode> witnessNodes;
	
	protected HashMap<Integer, HashSet<SimpleNode>> maxAngleNodes;
	
	/**
	 * deleted ID, deletion criteria
	 */
	protected HashMap<Integer, String> criteria;
	
	protected BarriereExtMessageHandler(UUID tcID, PhysicalGraphNode ownerNode,
			PhysicalGraphNode sourceNode, EStrategy strategy) {
		
		// inits in super
		super(tcID, ownerNode, sourceNode, strategy);
		this.initializewitnesses();
	}
	
	public BarriereExtMessageHandler(UUID routingID,
			PhysicalGraphNode ownerNode, PhysicalGraphNode forwarderNode) {
		
		// inits in super
		super(routingID, ownerNode, forwarderNode);
		this.initializewitnesses();
	}
	
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
		else if(msg instanceof NewWitness){
			receivedNewWitnessMessage((NewWitness)msg);
		}
	}
	
	@Override
	public void receivedNewNeighbourMessage(NewNeighbour nn){
		// does not have neighbour yet
		if(!hasNeighbour(nn.getNewNeighbour())){
			if(hasWitness(nn.getNewNeighbour())){
				// ID
				witnessNodes.remove(nn.getNewNeighbour().ID);
				// maybe its unprocessed
				unprocessedNodes.remove(nn.getNewNeighbour().ID);
			}
			addVirtualNeighbour(nn.getNewNeighbour(), nn.getTransmitter());
			// neighbour set changed, start completion & extraction again
			completionPhase();
		}
	}
	
	public void receivedNewWitnessMessage(NewWitness msg){
		// ERR
		Node witness = msg.getNewWitness();
		
		if(!hasNeighbour(witness) && !hasWitness(witness)){
			addWitness(msg.getNewWitness());
		}
		
		this.completionPhase();
	}
	
	// ==========================================================================
	// =================== START ALGORITHM FUNCTIONS ============================
	// ==========================================================================
	
	@Override
	public void completionPhase(){
		// phase control
		p=Phase.COMPLETION;
		
		// process every node once
		Set<SimpleNode> unprocessed = new HashSet<SimpleNode>();
		unprocessed.addAll(unprocessedNodes.values());
		for(SimpleNode v : unprocessed){
			
			// handle unprocessed witnesses
			if(hasWitness(v)){
				
				for(Node w : getAllKnownNeighbourNodes()){
					
					try {
						
						double rMin = Configuration.getDoubleParameter("QUDG/rMin");
						double rMax = Configuration.getDoubleParameter("QUDG/rMax");
						
						
						if(v.getPosition().distanceTo(w.getPosition())>rMin && v.getPosition().distanceTo(w.getPosition())<=rMax){
							
							NewWitness nw = new NewWitness(tcID, node, planarSubgraphCreationStrategy, v);
							
							this.enqueue(new MessageTimer(nw, w));
						}
						
					} catch (CorruptConfigurationEntryException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}else{
				
				try {
					double rMin = Configuration.getDoubleParameter("QUDG/rMin");
					double rMax = Configuration.getDoubleParameter("QUDG/rMax");
			
					for(SimpleNode w : getAllKnownNeighbourNodes()){
						if(w.ID==v.ID)
							continue;
				
							if(v.getPosition().distanceTo(w.getPosition())>rMin && v.getPosition().distanceTo(w.getPosition())<=rMax){
						
								HashSet<Node> wNode = new HashSet<Node>();
								wNode.add(w);
								Disk2D disc = Algorithms.disk(node, v, wNode);
								if(!disc.nodesInDisk.isEmpty()){
								
									NewNeighbour nn1 = new NewNeighbour(tcID, node, planarSubgraphCreationStrategy, w);
									NewNeighbour nn2 = new NewNeighbour(tcID, node , planarSubgraphCreationStrategy, v);
							
									// messages to timer queue
									this.enqueue(new MessageTimer(nn1, v));
									this.enqueue(new MessageTimer(nn2, w));
							
								}else{
									//extended
									NewWitness nw1 = new NewWitness(tcID, node, planarSubgraphCreationStrategy, w);
									NewWitness nw2 = new NewWitness(tcID, node, planarSubgraphCreationStrategy, v);
							
									this.enqueue(new MessageTimer(nw1, v));
									this.enqueue(new MessageTimer(nw2, w));
								}
							}

					}
			
				} catch (CorruptConfigurationEntryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
			
			// v is processed now
			unprocessedNodes.remove(v.ID);
		}
		
		// while ex. unprocessed v E W(u)
		
		// send all messages
		this.executeTimerQueue();
		
		// phase control
		p=Phase.STEADY;
		extractionPhase();
	}
	
	@Override
	public void extractionPhase(){
		// phase control
		p=Phase.EXTRACTION;
		
		// fix 2.0
		virtualNodes.putAll(extractedVirtualNodes);
		knownNeighbors.addAll(extractedNodes.values());
		extractedVirtualNodes.clear();
		extractedNodes.clear();
		
		//TODO
		// extraction
		for(SimpleNode v : getAllKnownSubgraphNeighbourNodes()){
			
			// select angle max
			Node w = node;
			double maxAngle = 0.0;
			// before: getAllKnowNeighbourNodes
			// now: getAllKnowNodes
			// consider witnesses as maxangle as well?!
			for(SimpleNode ws : getAllKnownNeighbourNodes()){
				if(ws.ID==v.ID){															// u   w     v
					continue;
				}
				double currentAngle = Algorithms.getSignedAngleBetween(node.getPosition(), ws.getPosition(), v.getPosition(), true);
				if(currentAngle>=maxAngle){
					// update angle max node
					w = ws;
					maxAngle = currentAngle;
				}
			}
			if(w.ID==node.ID){
				// oops, no angle max found
				continue;
			}
			/*if(maxAngleNodes.containsKey(v.ID)){
				maxAngleNodes.get(v.ID).add(w);
			}else{
				HashSet<Node> s = new HashSet<Node>();
				s.add(w);
				maxAngleNodes.put(v.ID, s);	
			}*/
			
			// w in disc u - v ?
			HashSet<Node> wSet = new HashSet<Node>();
			wSet.add(w);
			Disk2D discUV = Algorithms.disk(node, v, wSet);
			// w in disc, further checking needed (pdt)
			if(!discUV.nodesInDisk.isEmpty()){
				
				// not sure if optimum
				/*HashSet<Node> allOtherNodes = new HashSet<Node>();
				for(Node n : getAllKnownNodes()){
					if(n.ID==v.ID || n.ID==w.ID || n.ID==node.ID)
						continue;
					allOtherNodes.add(n);
				}*/
				HashSet<SimpleNode> allOtherNodes = getAllKnownNodes();
				allOtherNodes.remove(node);
				allOtherNodes.remove(v);
				allOtherNodes.remove(w);

				Disk2D discUVW = Algorithms.disk(node, v, w, allOtherNodes);
				// a node in disc UVW or circle is out of bound
				try {
					double rMax = Configuration.getDoubleParameter("QUDG/rMax");
					
					if(!discUVW.nodesInDisk.isEmpty() || Math.sin(maxAngle)<((node.getPosition().distanceTo(v.getPosition()) / rMax))){
						try{
							String discNodes="";
							for(Node x : discUVW.nodesInDisk){
								discNodes+=x.ID;
								if(hasWitness(x)){
									discNodes+="w";
								}
								if(isVirtualNode(x)){
									discNodes+="v";
								}
								discNodes+=" ,";
							}
							//String s = "";
							//s+="discUVWempty="+discUVW.nodesInDisk.isEmpty();
							/*criteria.put(v.ID, "discUVWempty="+discUVW.nodesInDisk.isEmpty()+"\n"
							+"angleTooBig="+(Math.sin(maxAngle)<((node.getPosition().distanceTo(v.getPosition()) / rMax)))+"\n"
							+"maxAngleNode="+w.ID+"\n"
							+"nodesInDisc="+discNodes);*/
							}catch(Exception e){
								e.printStackTrace();
								return;
							}
						// remove connection
						extractNeighbour(v);
						
						if(isVirtualNode(v)){
							virtualNodes.remove(v.ID);
						}
					}
				} catch (CorruptConfigurationEntryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		knownNeighbors.removeAll(extractedNodes.values());
		
		p=Phase.STEADY;
	}
	
	// ==========================================================================
	// ==================== END ALGORITHM FUNCTIONS =============================
	// ==========================================================================
	
	// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	
	// ==========================================================================
	// ======================= START HELP FUNCTIONS =============================
	// ==========================================================================
	@Override
	public MessageRecord getCurrentMessageRecord() {
		// TODO BarriereExtMessageRecord
		return new BarriereExtMessageRecord(tcID,
				NewNeighbour.numberOfSentMessages(tcID), 
				BeaconRequestMessage.numberOfSentMessages(tcID), 
				BeaconReplyMessage.numberOfSentMessages(tcID), 
				VirtualMessage.numberOfSentMessages(tcID),
				NewWitness.numberOfSentMessages(tcID));
	}
	
	/**
	 * add witness node
	 * @param n node to be added
	 */
	protected void addWitness(SimpleNode n){
		this.witnessNodes.put(n.ID, n);
		this.unprocessedNodes.put(n.ID, n);
	}
	
	// ==========================================================================
	// ======================== END HELP FUNCTIONS ==============================
	// ==========================================================================
	
	// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	
	// ==========================================================================
	// ======================= START TOOL FUNCTIONS =============================
	// ==========================================================================
	
	public void printKnownNodesToOutput(){
		Tools.appendToOutput("=== known physical"+" ===\n");
		Tools.appendToOutput("=== actual "+"===\n");
		for(Node n : knownNeighbors){
			Tools.appendToOutput("ID="+n.ID+"\n");
		}
		Tools.appendToOutput("=== deleted "+"===\n");
		for(Node n : extractedNodes.values()){
			Tools.appendToOutput("ID="+n.ID+"\n");
		}
		
		Tools.appendToOutput("=== known virtual "+"===\n");
		Tools.appendToOutput("=== actual "+"===\n");
		for(Node n : virtualNodes.values()){
			Tools.appendToOutput("ID="+n.ID+"\n");
		}
		Tools.appendToOutput("=== deleted "+"===\n");
		for(Node n : extractedVirtualNodes.values()){
			if(virtualNodes.containsKey(n.ID))
				continue;
			Tools.appendToOutput("ID="+n.ID+"\n");
		}
		Tools.appendToOutput("=== witness "+"===\n");
		for(Node n : witnessNodes.values()){
			Tools.appendToOutput("ID="+n.ID+"\n");
		}
	}
	
	public void printAngleMaxNode(PhysicalGraphNode p){
		Node w = node;
		double maxAngle = 0.0;
		for(Node ws : getAllKnownNeighbourNodes()){
			if(ws.ID==p.ID){															// u   w     v
				continue;
			}
			double currentAngle = Algorithms.getSignedAngleBetween(node.getPosition(), ws.getPosition(), p.getPosition(), true);
			if(currentAngle>=maxAngle){
				// update angle max node
				w = ws;
				maxAngle = currentAngle;
			}
		}
		if(w.ID==p.ID){
			Tools.appendToOutput("no angle max found!\n");
		}else{
			Tools.appendToOutput("angleMax ID="+w.ID+"\n");
			Tools.appendToOutput("angle="+maxAngle+"\n");
			Tools.appendToOutput("=== angleMaxHistory ==="+"\n");
			for(Node n : maxAngleNodes.get(p.ID)){
				Tools.appendToOutput("ID="+n.ID+" angle="+Algorithms.getSignedAngleBetween(node.getPosition(), n.getPosition(), p.getPosition(), true)+"\n");
			}
			Tools.appendToOutput("=== close history ==="+"\n");
		}
		
		if(criteria.containsKey(p.ID)){
			Tools.appendToOutput("=== deletion history ==="+"\n");
			Tools.appendToOutput(criteria.get(p.ID));
		}
	}
	
	public void printCriteria(PhysicalGraphNode p, boolean showDetail){
		
		boolean deleted = false;
		// select angle max
					Node w = node;
					double maxAngle = 0.0;
					for(Node ws : getAllKnownNeighbourNodes()){
						if(ws.ID==p.ID){															// u   w     v
							continue;
						}
						double currentAngle = Algorithms.getSignedAngleBetween(node.getPosition(), ws.getPosition(),p.getPosition(), true);
						if(currentAngle>=maxAngle){
							// update angle max node
							w = ws;
							maxAngle = currentAngle;
						}
					}
					if(w.ID==node.ID){
						// oops, no angle max found
						return;
					}
					
					// w in disc u - v ?
					HashSet<Node> wSet = new HashSet<Node>();
					wSet.add(w);
					Disk2D discUV = Algorithms.disk(node, p, wSet);
					// w in disc, further checking needed (pdt)
					if(!discUV.nodesInDisk.isEmpty()){
						if(showDetail){
							Tools.appendToOutput("Disc uv not empty"+"\n");
						}
						// not sure if optimum
						HashSet<SimpleNode> allOtherNodes = getAllKnownNodes();
						allOtherNodes.remove(node);
						allOtherNodes.remove(p);
						allOtherNodes.remove(w);

						Disk2D discUVW = Algorithms.disk(node, p, w, allOtherNodes);
						// a node in disc UVW or circle is out of bound
						try {
							double rMax = Configuration.getDoubleParameter("QUDG/rMax");
							if(!discUVW.nodesInDisk.isEmpty() && showDetail){
								Tools.appendToOutput("Disc uvw not empty"+"\n");
							}
							if(Math.sin(maxAngle)<((node.getPosition().distanceTo(p.getPosition()) / rMax)) && showDetail){
								Tools.appendToOutput("Disc uvw too big!");
							}
							if(!discUVW.nodesInDisk.isEmpty() || Math.sin(maxAngle)<((node.getPosition().distanceTo(p.getPosition()) / rMax))){
								// remove connection
								if(isVirtualNode(p)){
									// delete virtual
									Tools.appendToOutput("=== v connection deleted ===");
									return;
								}else{
									Tools.appendToOutput("=== p connection deleted ===");
									return;
								}
							}
						} catch (CorruptConfigurationEntryException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						if(showDetail)
							Tools.appendToOutput("Disc uv empty"+"\n");
					}
					Tools.appendToOutput("=== keeping connection ===");
	}
	
	// ==========================================================================
	// ======================== END TOOL FUNCTIONS ==============================
	// ==========================================================================
	
	// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	
	// ==========================================================================
	// ====================== START CHECK FUNCTIONS =============================
	// ==========================================================================
	public HashSet<SimpleNode> getAllKnownNodes(){
		HashSet<SimpleNode> nodes = getAllKnownNeighbourNodes();
		nodes.addAll(witnessNodes.values());
		
		return nodes;
		
	}
	/**
	 * checks if a node is the witness
	 * (non neighbour in PDT range) of
	 * this message handlers node.
	 * 
	 * @param n node to be checked
	 * @return true if node n is a witness
	 */
	protected boolean hasWitness(Node n){
		if(witnessNodes.containsKey(n.ID))
			return true;
		return false;
	}
	
	// ==========================================================================
	// ======================= END CHECK FUNCTIONS ==============================
	// ==========================================================================
	
	// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	
	// ==========================================================================
	// ======================= START INIT FUNCTIONS =============================
	// ==========================================================================
	
	/**
	 * initializes the witnesses HashMap.
	 */
	protected void initializewitnesses(){
		this.witnessNodes = new HashMap<Integer,SimpleNode>();
		
		// addon
		this.maxAngleNodes = new HashMap<Integer, HashSet<SimpleNode>>();
		this.criteria = new HashMap<Integer, String>();
	}

	// ==========================================================================
	// ======================== END INIT FUNCTIONS ==============================
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
		g.setColor(Color.red);
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
		g.setColor(Color.black);
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
}
