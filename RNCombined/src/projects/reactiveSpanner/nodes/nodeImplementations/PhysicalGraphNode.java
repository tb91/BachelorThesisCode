package projects.reactiveSpanner.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import projects.defaultProject.nodes.timers.MessageTimer;
import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.CustomGlobal;
import projects.reactiveSpanner.TopologyControlObserver;
import projects.reactiveSpanner.Utilities;
import projects.reactiveSpanner.exceptions.InvalidSubgraphStrategyException;
import projects.reactiveSpanner.nodes.messageHandlers.AbstractMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EState;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategyFactory;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.BCA.BCAMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.BFP.BFPMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.BarriereExt.BarriereExtMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.buildBackbone.CreateVirtuals;
import projects.reactiveSpanner.nodes.messageHandlers.buildBackbone.CreateVirtualsMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.reactivePDT.ReactivePDTMessageHandler;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.messages.Request;
import projects.reactiveSpanner.nodes.messages.RoutingMessage;
import projects.reactiveSpanner.routing.RoutingObserver;
import projects.reactiveSpanner.routing.RoutingProtocol;
import projects.reactiveSpanner.routing.RoutingProtocol.ERouting;
import projects.reactiveSpanner.routing.RoutingProtocol.ERoutingState;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.gui.helper.NodeSelectionHandler;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.nodes.Node.NodePopupMethod;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.messages.NackBox;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;

public class PhysicalGraphNode extends SimpleNode implements TopologyControlObserver, Serializable, RoutingObserver {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The last subgraph strategy that was started.
	 * 
	 * <b>Change to {@link SubgraphStrategyFactory}.getLastRequestedSubgraphStrategy() or use observer system for required subgraph strategies</b>
	 */
	@Deprecated
	private SubgraphStrategy lastSubgraphStrategy;
	public boolean drawRad = false;
	private Position _DEBUG_routingDestination = null;	//TODO remove me
	

	/**
	 * Map for topology control operations where this node is currently involved
	 */
	public final Map<UUID, AbstractMessageHandler<? extends SimpleNode>> messageHandlerMap = new HashMap<UUID, AbstractMessageHandler<? extends SimpleNode>>();
	/**
	 * Map for routing operations where this node is currently involved, for example to generate a subgraph for the next hop
	 */
	public final Map<UUID, RoutingMessage> routingMessageBuffer = new HashMap<UUID, RoutingMessage>();

	@Override
	public void init() {
		super.init();
		subgraphStrategyFactory = new SubgraphStrategyFactory(this);
	}

	/*
	 * Setting a destination for BFP routing algorithm
	 */
	@NodePopupMethod(menuText = "BFP")
	public SubgraphStrategy startBFP() {
		SubgraphStrategy lastSubgraphStrategy = null;
		try {
			lastSubgraphStrategy = this.subgraphStrategyFactory.request(EStrategy.BFP);
			lastSubgraphStrategy.addObserver(this);
			lastSubgraphStrategy.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lastSubgraphStrategy;
	}

	@NodePopupMethod(menuText = "Reactive PDT")
	public SubgraphStrategy startReactivePDT() {
		SubgraphStrategy lastSubgraphStrategy = null;
		try {
			lastSubgraphStrategy = this.subgraphStrategyFactory.request(EStrategy.REACTIVE_PDT);
			lastSubgraphStrategy.addObserver(this);
			lastSubgraphStrategy.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lastSubgraphStrategy;
	}

	@NodePopupMethod(menuText = "BuildBackbone")
	public SubgraphStrategy startBuildBackbone() {
		/*
		 * this.lastSubgraphStrategy = new BuildBackbone(this); return lastSubgraphStrategy;
		 */
			logger.logln(LogL.INFO, "Starting the BuildBackbone algorithm.");
			this.subgraphStrategyFactory.request(EStrategy.BUILD_BACKBONE).start();

		return lastSubgraphStrategy;
	}

	@NodePopupMethod(menuText = "BCA")
	public SubgraphStrategy startBCA() {
		SubgraphStrategy lastSubgraphStrategy = null;
		try {
			lastSubgraphStrategy = this.subgraphStrategyFactory.request(EStrategy.BCA);
			lastSubgraphStrategy.addObserver(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lastSubgraphStrategy;
	}


	
	@NodePopupMethod(menuText = "Greedy proceedings")
	public void startGreedyRouting() {
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			@Override
			public void handleNodeSelectedEvent(final Node v) {
				if (v == null) {
					return; // aborted
				}	
				sendRoutingMsg((PhysicalGraphNode) v, EStrategy.UDG, ERouting.GREEDY);
				_DEBUG_routingDestination = v.getPosition();
			}
		}, "Select the destination node to route the message");
	}
	
	@NodePopupMethod(menuText = "Face-Routing_GG")
	public void startFaceRoutingGG() {
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			@Override
			public void handleNodeSelectedEvent(final Node v) {
				if (v == null) {
					return; // aborted
				}
				sendRoutingMsg((PhysicalGraphNode) v, EStrategy.GG, ERouting.FACE_ROUTING);
				_DEBUG_routingDestination = v.getPosition();
			}
		}, "Select the destination node to route the message");
	}

	/**
	 * Partial Delaunay Triangulation within the one-hop neighborhood of the current node
	 */
	@NodePopupMethod(menuText="Local PDT")
	public SubgraphStrategy localPDT() 
	{
		SubgraphStrategy lastSubgraphStrategy = null;
		try {
			lastSubgraphStrategy = this.subgraphStrategyFactory.request(EStrategy.PDT);
			for(PhysicalGraphNode n: lastSubgraphStrategy.getSubgraphNodes())
			{
				n.setColor(Color.MAGENTA);
			}
			this.setColor(Color.RED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lastSubgraphStrategy;
	}
	
	@NodePopupMethod(menuText = "Face-Routing_PDT")
	public void startFaceRoutingPDT() {
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			@Override
			public void handleNodeSelectedEvent(final Node v) {
				if (v == null) {
					return; // aborted
				}
				sendRoutingMsg((PhysicalGraphNode) v, EStrategy.PDT, ERouting.FACE_ROUTING);
				_DEBUG_routingDestination = v.getPosition();
			}
		}, "Select the destination node to route the message");
	}
	
	@NodePopupMethod(menuText = "Greedy-Face-Routing_GG")
	public void startGreedyFaceRoutingGG() {
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			@Override
			public void handleNodeSelectedEvent(final Node v) {
				if (v == null) {
					return; // aborted
				}
				sendRoutingMsg((PhysicalGraphNode) v, EStrategy.GG, ERouting.GREEDY_FACE);
				_DEBUG_routingDestination = v.getPosition();
			}
		}, "Select the destination node to route the message");
	}
	
	@NodePopupMethod(menuText = "Greedy-Face-Routing_PDT")
	public void startGreedyFaceRoutingPDT() {
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			@Override
			public void handleNodeSelectedEvent(final Node v) {
				if (v == null) {
					return; // aborted
				}
				sendRoutingMsg((PhysicalGraphNode) v, EStrategy.PDT, ERouting.GREEDY_FACE);
				_DEBUG_routingDestination = v.getPosition();
			}
		}, "Select the destination node to route the message");
	}
	
	/**
	 * just used for debugging and TODO: can be removed later (tim)
	 */
	@NodePopupMethod(menuText = "drawConnections")
	public void drawConnections() {
		/*
		 * this.lastSubgraphStrategy = new BuildBackbone(this); return lastSubgraphStrategy;
		 */
		try {
			if (this.getLastSubgraphStrategy() != null)
				if (this.getMessageHandler(lastSubgraphStrategy.getTopologyControlID()) instanceof CreateVirtualsMessageHandler) {
					CreateVirtualsMessageHandler ts = (CreateVirtualsMessageHandler) this.getMessageHandler(lastSubgraphStrategy.getTopologyControlID());
					ts.drawConnections = !ts.drawConnections;
				} else {
					logger.logln(LogL.ERROR_DETAIL, "The last used Strategy must be CreateVirtuals");
				}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * prints all connections of the specified (clicked) node into the console
	 */
	@NodePopupMethod(menuText = "printOutCons")
	public void printOutCons() {
		System.out.println("connections from " + this.toString());
		for (Edge e : outgoingConnections) {
			System.out.println(e.endNode);
		}
		System.out.println();

	}


	
	@NodePopupMethod(menuText = "Barriere")
	public SubgraphStrategy startBarriere() {
		SubgraphStrategy lastSubgraphStrategy = null;
		try {
			lastSubgraphStrategy = this.subgraphStrategyFactory.request(EStrategy.BARRIERE);
			lastSubgraphStrategy.addObserver(this);
			lastSubgraphStrategy.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lastSubgraphStrategy;
	}

	@NodePopupMethod(menuText = "BarriereExt")
	public SubgraphStrategy startBarriereExt() {
		SubgraphStrategy lastSubgraphStrategy = null;
		try {
			lastSubgraphStrategy = this.subgraphStrategyFactory.request(EStrategy.BARRIERE_EXT);
			lastSubgraphStrategy.addObserver(this);
			lastSubgraphStrategy.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lastSubgraphStrategy;
	}

	@NodePopupMethod(menuText = "BarriereExtTools")
	public void barriereTools() {
		Tools.clearOutput();
		AbstractMessageHandler amh = messageHandlerMap.get(lastSubgraphStrategy.getTopologyControlID());
		try {
			final BarriereExtMessageHandler bemh = (BarriereExtMessageHandler) amh;
			// print know nodes
			Tools.appendToOutput("=== Node " + this.ID + " has ===" + "\n");
			bemh.printKnownNodesToOutput();

			Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
				@Override
				public void handleNodeSelectedEvent(Node u) {
					if (u == null) {
						return; // the user aborted
					}
					PhysicalGraphNode p = (PhysicalGraphNode) u;
					// TODO
					bemh.printAngleMaxNode(p);
					bemh.printCriteria(p, true);
				}
			}, "Select a node");
		} catch (Exception e) {
			System.out.println("no barriere ext last");
		}
	}

	@NodePopupMethod(menuText = "draw rMin rMax")
	public void drawR() {
		this.drawRad = !drawRad;
	}

	/**
	 * Delete the specific message handler for topology control (TC). Usually done, when the created subgraph is not required anymore
	 * 
	 * @param topologyControlID
	 */
	public void deleteTCHandler(final UUID topologyControlID) {
		this.messageHandlerMap.remove(topologyControlID);
	}

	/*
	 * (non-Javadoc)
	 * @see sinalgo.nodes.Node#handleMessages(sinalgo.nodes.messages.Inbox)
	 */
	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message msg = inbox.next();

			if (msg instanceof RoutingMessage) {
				processRoutingMessage((RoutingMessage) msg);
			} else if (msg instanceof AbstractMessage) {
				AbstractMessage aMsg = (AbstractMessage) msg;
				//don't continue if msg source == this
				if (aMsg.getTransmitter().equals(this)) {
					return;
				}
				passingMessageToMessageHandler(aMsg);
			}
		}
	}

	/**
	 * Send a routing message that holds the specified routing algorithm on the specified subgraph strategy
	 * 
	 * @param destination where the message should be routed to
	 * @param subgraphStrategy subgraph strategy that is used to route the message
	 * @param routingAlg the specified routing algorithm
	 * @return routing algorithm to observe
	 */
	public RoutingProtocol<PhysicalGraphNode> sendRoutingMsg(final PhysicalGraphNode destination, final EStrategy subgraphStrategy, final ERouting routingAlg)
	{
		RoutingProtocol<PhysicalGraphNode> routing = RoutingProtocol.routing(this, destination, subgraphStrategy, routingAlg);
		RoutingMessage rMsg = new RoutingMessage(routing);
		routingMessageBuffer.put(rMsg.getID(), rMsg);
		rMsg.requestNextHop(this);
		return routing;
	}
	
	/**
	 * Process an incoming routing message. Inform the routing protocol hold by the message that it reached this node.
	 * @param msg that was received by this node
	 */
	private void processRoutingMessage(final RoutingMessage msg) {
		this.setColor(Color.RED);	//TODO No. Don't do it this way.
		//TODO unify/simplify the registration of the message in the message buffer, the arriving of a new holding node and registration of this node as observer of the routing event
		routingMessageBuffer.put(msg.getID(), msg);
		msg.arrivedNextHop(this);
		if (!msg.getCurrentRoutingState().equals(ERoutingState.FINISHED)) {
			logger.logln(LogL.INFO, this.toString() + " requests the next routing step for routing operation " + msg.getID());
			msg.requestNextHop(this);
		}
	}

	/**
	 * Received messages will be processed by message handlers to avoid interferences and separate different topology events from each other. This messages passes the message to the right topology control.
	 * 
	 * @param msg
	 *            that will be passed to the message handler
	 */
	private void passingMessageToMessageHandler(final AbstractMessage msg) {
		UUID ID = msg.getID();
		if (!messageHandlerMap.containsKey(ID)) {
			if (!(msg instanceof Request)) {
				logger.logln(LogL.INFO, this.toString() + " received message of unknown ID, but was no RTS");
				return;
			}

			switch (msg.getStrategy()) {
			case BFP:
				logger.logln(LogL.INFO, this.toString() + " received RTS and creates BFPMessageHandler with UUID " + ID.toString());
				messageHandlerMap.put(ID, new BFPMessageHandler(ID, this, msg.getTransmitter()));
				break;
			case REACTIVE_PDT:
				logger.logln(LogL.INFO, this.toString() + " received RTS and creates ReactivePDTMessageHandler with UUID " + ID.toString());
				messageHandlerMap.put(ID, new ReactivePDTMessageHandler(ID, this, msg.getTransmitter()));
				break;
			case BCA:
				logger.logln(LogL.INFO, this.toString() + " received RTS and creates BCAMessageHandler with UUID " + ID.toString());
				messageHandlerMap.put(ID, new BCAMessageHandler(ID, this, msg.getTransmitter()));
				break;
			default:
				String errorMsg = "Node " + this.ID + " has received a beaconless message from " + msg.getTransmitter() + ". The classified strategy for beaconless subgraph creation is unknown or not set.";
				logger.logln(LogL.ERROR_DETAIL, errorMsg);
				throw new InvalidSubgraphStrategyException(errorMsg);
			}
		}
		messageHandlerMap.get(ID).receivedMessage(msg);
	}

	/*
	 * (non-Javadoc)
	 * @see sinalgo.nodes.Node#handleNAckMessages(sinalgo.nodes.messages.NackBox)
	 */
	@Override
	public void handleNAckMessages(NackBox nackBox) {
		if (nackBox.hasNext()) {
			String warningMsg = "Warning: Node " + this.ID + " has dropped a message!";
			logger.logln(LogL.WARNING, warningMsg);
			Tools.warning(warningMsg);
			//currently we do not want dropped messages
			throw new RuntimeException(this.ID + " has dropped a message!");
		}
	}

	public AbstractMessageHandler<? extends SimpleNode> getMessageHandler(final UUID tcID) {
		if (messageHandlerMap.containsKey(tcID))
			return this.messageHandlerMap.get(tcID);

		System.out.println("messageHandlerMap of " + this.toString() + ": " + messageHandlerMap.toString());
		throw new RuntimeException("No message handler with UUID " + tcID + " exists.");
	}

	public void connect(Set<? extends SimpleNode> nodesToConnect, Color color) {
		logger.logln(LogL.INFO, this.toString() + " connecting to nodes " + nodesToConnect.toString());
		for (Node v : nodesToConnect) {
			this.addBidirectionalConnectionTo(v);
		}
		CustomGlobal.drawEdges(this, nodesToConnect, color);
	}

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		//TODO complete revision of the drawing part. Move drawables to the subgraphStrategies/routingProtocols and give options to show what should be rendered 
		
		this.setColor(nodeColor);
		this.drawingSizeInPixels = (int) (defaultDrawingSizeInPixels * pt.getZoomFactor()) / 12;
		this.drawAsDisk(g, pt, highlight, drawingSizeInPixels);

		if (subgraphStrategyFactory.getLastRequestedSubgraphStrategy() != null) {
			subgraphStrategyFactory.getLastRequestedSubgraphStrategy().draw(g, pt);
		}

		//		for(BeaconlessMessageHandler bmh: this.messageHandlerMap.values())
		//		{
		//			bmh.drawNode(g, pt);
		//		}

		if (super.diskToDraw != null) {
			double radius = diskToDraw.radius;
			pt.translateToGUIPosition(diskToDraw.center);
			int r = (int) (radius * pt.getZoomFactor());
			g.drawOval(pt.guiX - r, pt.guiY - r, r * 2, r * 2);
		}

		if (drawRad) {
			try {
				double rMin = Configuration.getDoubleParameter("QUDG/rMin");
				double rMax = Configuration.getDoubleParameter("QUDG/rMax");

				pt.translateToGUIPosition(this.getPosition());

				int size = (int) (pt.getZoomFactor() * rMin * 2);

				int x = pt.guiX - size / 2;
				int y = pt.guiY - size / 2;

				g.setColor(Color.red);
				g.drawOval(x, y, size, size);

				pt.translateToGUIPosition(this.getPosition());

				int sizeR = (int) (pt.getZoomFactor() * rMax * 2);

				int x1 = pt.guiX - sizeR / 2;
				int y1 = pt.guiY - sizeR / 2;

				g.drawOval(x1, y1, sizeR, sizeR);
			} catch (CorruptConfigurationEntryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(_DEBUG_routingDestination != null)
		{
			pt.drawDottedLine(g, this.getPosition(), _DEBUG_routingDestination);
		}
	} //end draw

	@Override
	public String toString() {
		String s = "Node(" + this.ID + ")";
		return s;
	}

	@Deprecated
	public SubgraphStrategy getLastSubgraphStrategy() {
		return lastSubgraphStrategy;
	}

	public void updateDrawing(SubgraphStrategy current) {
		lastSubgraphStrategy = current;
	}

	@Override
	public void onNotify(SubgraphStrategy topologyControl, EState event) {
		switch (event) {
		case PROCESSING:
			System.out.println("started");
			break;
		case TERMINATED:
			System.out.println("ended");
			break;
		default:
			break;

		}

	}

	@Override
	public void onNotify(UUID routingID, ERoutingState event) {
		switch (event) {
		case FINISHED:
			Tools.getTextOutputPrintStream().append("Routing Message reached destination " + this.toString());
			break;
		case DESTINATION_NODE_FOUND:
		case NEXT_HOP_FOUND:
			RoutingMessage rMsg = routingMessageBuffer.get(routingID).clone();
			if (rMsg == null) {
				throw new RuntimeException(this.toString() + " has been notified from the routing event with ID " + routingID.toString() + " but this ID is not known in the routing message buffer!");
			}
			MessageTimer sendRoutingMsgTimer = new MessageTimer(rMsg, rMsg.getNextHop());
			sendRoutingMsgTimer.startRelative(1, this); // Synchronized mode does not allow direct broadcast
			this.routingMessageBuffer.remove(routingID);
			rMsg.removeObserver(this);
			this.setColor(Color.DARK_GRAY);	//TODO No. Don't do it this way.
			break;
		case STUCKED:
			Tools.getTextOutputPrintStream().append("Routing Message stucked at node " + this.toString() + "\n");
			break;
		case WAITING_FOR_TOPOLOGY_CREATION:
			break;
		default:
			break;

		}
	}
}