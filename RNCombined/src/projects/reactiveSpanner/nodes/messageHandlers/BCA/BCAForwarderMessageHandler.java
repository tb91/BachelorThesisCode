package projects.reactiveSpanner.nodes.messageHandlers.BCA;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import projects.reactiveSpanner.CustomGlobal;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.messages.BCA.FinishMessage;
import projects.reactiveSpanner.nodes.messages.BCA.FirstRequest;
import projects.reactiveSpanner.nodes.messages.BCA.FirstResponse;
import projects.reactiveSpanner.nodes.messages.BCA.SecondRequest;
import projects.reactiveSpanner.nodes.messages.BCA.SecondResponse;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.timers.BCA.BCAMessageTimer;
import projects.reactiveSpanner.nodes.timers.BCA.FinishTimer;
import projects.reactiveSpanner.nodes.timers.BCA.FirstRequestPhaseDoneTimer;
import projects.reactiveSpanner.nodes.timers.BCA.SecondRequestTimer;
import projects.reactiveSpanner.record.MessageRecord;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.models.ConnectivityModel;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.nodes.timers.Timer;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;
	
public class BCAForwarderMessageHandler extends BCAMessageHandler {

	public Timer secondRequestTimer;
//	public FinishTimer finishTimer;
	
	public Set<UUID> sessionList = new HashSet<UUID>();
	
	public HashMap<UUID, FinishTimer> finishTimerMap = new HashMap<UUID, FinishTimer>();
	
	public boolean phase2 = false;
	
	private PhysicalGraphNode[] nArray = new PhysicalGraphNode[25];
	private HashMap<PhysicalGraphNode, Position> hm = new HashMap<PhysicalGraphNode, Position>();
	{
		hm.put(this.node, getKachel(node));
		putArray(this.node);
	}
	
	public BCAForwarderMessageHandler(final UUID tcID, PhysicalGraphNode sourceNode) {
		super(tcID, sourceNode, sourceNode);
	}
	
	// TODO Falls es keine newSession ist, muss ich an die alte MotherSession rankommen!
	public void broadcastFirstRequest(BCAMessageHandler oldMsgHandler, boolean newSession, UUID motherSessionID) {
		
		this.cellLeaderElected = true;
		
		logger.logln(LogL.INFO, "Round " + Global.currentTime + ": SourceNode "
				+ this.node.toString()
				+ " broadcasts FirstRequest to topology control ID "
				+ this.tcID.toString());
		
		UUID sessionID = UUID.randomUUID();
		
		if (newSession) {
			motherNodeMap.put(sessionID, this.node);
			subSessionMap.put(sessionID, sessionID);
		} else {
			subSessionMap.put(sessionID, motherSessionID);
		}
		
		sessionList.add(sessionID);
		
		BCAMessageTimer firstRequestTimer = new BCAMessageTimer(new FirstRequest(this.tcID,
				this.node, getDirectCells(), sessionID, this.node));
		firstRequestTimer.startRelative(1, node);
		firstRequestTimerMap.put(sessionID, firstRequestTimer);
		
		// Im Originalcode war von tmax+3 die Rede, doch ich sende den FirstRequest
		// erst in der nächsten Runde aus.
		broadcastSecondRequest(tmax+4, sessionID);
		this.node.setColor(Color.RED);
		
		FirstRequestPhaseDoneTimer firstRequestPhaseDoneTimer = new FirstRequestPhaseDoneTimer(this, sessionID);
		firstRequestPhaseDoneTimer.startRelative(firstRequestTimer.getFireTime() + tmax + 5, this.node);
		firstRequestPhaseDoneTimerMap.put(sessionID, firstRequestPhaseDoneTimer);
		
		startFinishTimer(sessionID);
		
		// UDG oder QUDG?
		String cm = this.node.getConnectivityModel().getClass().getName();
		String UDG = ConnectivityModel.getConnectivityModelInstance("UDG").getClass()
				.getName();
//		String QUDG = ConnectivityModel.getConnectivityModelInstance("StaticQUDG")
//				.getClass().getName();

		if (cm.equals(UDG)) {
			rMin = udr;
			rMax = rMin;
		}

		hm.put(this.node, getKachel(this.node));
		putArray(this.node);

		if (cm.equals(UDG)) {
			PhysicalGraphNode dummy = this.node;
			nArray[0] = dummy;
			nArray[4] = dummy;
			nArray[20] = dummy;
			nArray[24] = dummy;
		}
		
		if (oldMsgHandler != null) {
			oldMsgHandler.deactivatedSession.add(sessionID);
		}
	}
	
	public void startFinishTimer(UUID sessionID) {
		FinishTimer finishTimer = new FinishTimer(this, sessionID);
		finishTimer.startRelative(3 * tmax + 8, this.node);
		finishTimerMap.put(sessionID, finishTimer);
		System.out.println(this.node.toString() + " engaged FinishTimer!");
	}
	
	public void broadcastSecondRequest(double timer, UUID sessionID) {
		
		logger.logln(LogL.INFO, "Round " + Global.currentTime + ": SourceNode " + this.node.toString()
				+ " broadcasts SecondRequest to topology control ID "
				+ this.tcID.toString() + " in " + timer + " rounds");
		
		secondRequestTimer = new SecondRequestTimer(this, sessionID);
		secondRequestTimer.startRelative(timer - 1, this.node);
	}
	
	public void broadcastSecondRequestNow(UUID sessionID){
		boolean openCells = false;
		
		Position[] indirectCells = new Position[nArray.length];
		for (int i = 0; i < nArray.length; i++) {
			if (nArray[i] == null && indirectCells[i] == null) {
				indirectCells[i] = getKachelX(i);
				openCells = true;
			}
		}
		
		if (openCells) {
			secondRequestTimer = new BCAMessageTimer(new SecondRequest(this.tcID,
					this.node, indirectCells, sessionID));
			secondRequestTimer.startRelative(1, this.node);
		}
	}
	
	@Override
	public void receivedMessage(AbstractMessage msg) {
		if (msg instanceof FirstResponse) {
			receivedFirstResponse((FirstResponse) msg);
		}
		
		if (msg instanceof SecondResponse) {
			receivedSecondResponse((SecondResponse) msg);
		}
		
		if (msg instanceof FirstRequest) {
			UUID sessionID = ((FirstRequest) msg).sessionID;
			
			FinishTimer finishTimer = finishTimerMap.get(sessionID);
			
			if (finishTimer != null) {
				finishTimer.disable();
				finishTimer = new FinishTimer(this, sessionID);
				finishTimer.startRelative(2 * tmax + 4, this.node);
				finishTimerMap.put(sessionID, finishTimer);
			}
		}
		
		if (msg instanceof FinishMessage) {
			
			// TODO zu Testzwecken
//			System.out.println();
//			System.out.println(this.node.toString()
//					+ " received FinishMessage from Node "
//					+ msg.getTransmitter());
//			System.out.println("UnfinishedChildrenCells: "
//					+ unfinishedChildrenCells.toString());
//			System.out.println("Cell of answering node:"
//					+ ((FinishMessage) msg).source.toString());
//			System.out
//					.println("Does unfinishedChildrenCells contain that cell? "
//							+ unfinishedChildrenCells.contains(((FinishMessage) msg).source));
//			System.out.println();
			// zu Testzwecken
			
			if (unfinishedChildrenCells.contains(((FinishMessage) msg).source)) {
				unfinishedChildrenCells.remove(((FinishMessage) msg).source);
				System.out.println("###### " + this.node.toString() + " entfernte "
						+ ((FinishMessage) msg).source.toString()
						+ " aus den Kindersessions");
			}
		}
	}
	
	@Override
	public void receivedFirstResponse(FirstResponse msg) {
		
		if (sessionList.contains(msg.sessionID)) {
			
			logger.logln(LogL.INFO, "Round " + Global.currentTime
					+ ": SourceNode " + this.node.toString()
					+ " received FirstResponse from Node "
					+ msg.getSource().toString());

			logger.logln(LogL.INFO, "Cell leader election started? "
					+ msg.cellLeaderElection);

			if (msg.cellLeaderElection
					&& !unfinishedChildrenCells.contains(msg.getSource())
					&& !(getKachel(msg.getSource())
							.equals(getKachel(this.node)))) {
				//TODO Auskommentieren, wenn BCA ohne Kindersessions ausgeführt werden soll
				unfinishedChildrenCells.add(msg.getSource());
				System.out.println("###### " + this.node.toString() + " fügte "
						+ msg.getSource().toString()
						+ " als Kindersession hinzu");
			}

			PhysicalGraphNode n = msg.getSource();
			Position cell = getKachel(n);
			if (!hm.containsValue(cell)) {
				hm.put(n, cell);
				putArray(n);
				this.knownNeighbors.add(n);

				// TODO Kante von diesem Knoten zu msg.getSource()
				n.setColor(Color.RED);
			}
		}
	}
	
	@Override
	public void receivedSecondResponse(SecondResponse msg) {
		
		if (sessionList.contains(msg.sessionID)) {

			if (msg.cellLeaderElection
					&& !unfinishedChildrenCells.contains(msg.getSource())
					&& !(getKachel(msg.getSource())
							.equals(getKachel(this.node)))) {
				//TODO Auskommentieren, wenn BCA ohne Kindersessions ausgeführt werden soll
				unfinishedChildrenCells.add(msg.getSource());
				System.out.println("###### " + this.node.toString() + " fügte "
						+ msg.getSource().toString()
						+ " als Kindersession hinzu");
			}
			
			logger.logln(LogL.INFO, "Round " + Global.currentTime
					+ ": SourceNode " + this.node.toString()
					+ " received SecondResponse from Node "
					+ msg.getNode().toString());

			PhysicalGraphNode n = msg.getSource();
			Position cell = getKachel(n);
			if (!hm.containsValue(cell)) {
				hm.put(n, cell);
				putArray(msg.getSource());
				this.knownNeighbors.add(msg.getSource());

				n.setColor(Color.RED);

				// TODO Kante von diesem Knoten zu msg.getNode() zeichnen
				// TODO Kante von msg.getSource() zu msg.getNode() zeichnen
				msg.getNode().setColor(Color.RED);
			}
		}
	}
	
	public void done(UUID sessionID) {

		if (unfinishedChildrenCells.size() == 0) {

			this.node.setColor(Color.RED);
			
			// TODO Muss ich das machen?
			sessionList.remove(sessionID);

			String str =  "Runde " + Global.currentTime + ": Session " + sessionID + ": Knoten " + this.node.ID
					+ ": FERTIG! Folgende Kacheln sind nicht erreichbar: ";
			for (int i = 0; i < nArray.length; i++) {
				if (nArray[i] == null)
					str = str + i + ",";
			}
			logger.logln(str);
			// Tools.stopSimulation();
			
			BCAMessageTimer FinishMessageTimer = new BCAMessageTimer(
					new FinishMessage(this.tcID, this.node,
							this.node));
			FinishMessageTimer.startRelative(1, this.node);

			boolean b = true;
			for (Node n : nArray) {
				if (n == null)
					b = false;
			}

			if (b) {
				logger.logln("Knoten " + this.node.ID
						+ ": FERTIG! Es sind keine offenen Kacheln Ã¼brig.");
				Tools.stopSimulation();
			}

//			System.out.println("	Gefundene Knoten: "
//					+ this.knownNeighbors.toString());
//			System.out.println();
//			System.out
//					.println("  Nachrichten: 		" + CustomGlobal.totalMessages);
//			System.out
//					.println("  FirstRequests:	" + CustomGlobal.firstRequests);
//			System.out.println("  FirstResponses:	"
//					+ CustomGlobal.firstResponses);
//			System.out.println("  SecondRequests:	"
//					+ CustomGlobal.secondRequests);
//			System.out.println("  SecondResponses:	"
//					+ CustomGlobal.secondResponses);
//			System.out.println("  RTS:				" + CustomGlobal.RTS);
//			System.out.println("  CTS:				" + CustomGlobal.CTS);
//			System.out.println();
//			System.out.println("FirstRequestNodes:   "
//					+ CustomGlobal.firstRequestNodes.toString());
//			System.out.println("FirstResponseNodes:  "
//					+ CustomGlobal.firstResponseNodes.toString());
//			System.out.println("SecondResponseNodes: "
//					+ CustomGlobal.secondResponseNodes.toString());
//			System.out.println("RTSNodes:			 "
//					+ CustomGlobal.RTSNodes.toString());
//			System.out.println("CTSNodes:			 "
//					+ CustomGlobal.CTSNodes.toString());
//			System.out.println();
//			System.out.println("Cell leader:         "
//					+ CustomGlobal.cellLeader.toString());
		} else {
			// Kinderzellen weitere 10 Runden geben, um fertig zu sein
			
			System.out.println("### ### ### ROUND " + Global.currentTime
					+ " ### ### ### " + this.node.toString() + " Done!");
			System.out.println("Aber unfinishedChildrenCells nicht leer: "
					+ unfinishedChildrenCells.toString());

//			if (this.node.getColor() != Color.WHITE) {
//				this.node.setColor(Color.WHITE);
//			} else {
				this.node.setColor(Color.BLACK);
//			}
			
			FinishTimer finishTimer = finishTimerMap.get(sessionID);
			finishTimer.disable();
			finishTimer = new FinishTimer(this, sessionID);
			finishTimer.startRelative(100, this.node);
			finishTimerMap.put(sessionID, finishTimer);
		}

	}

	@Override
	public void drawNode(Graphics g, PositionTransformation pt) {
		this.node.setColor(Color.RED);
	}

	@Override
	public MessageRecord getCurrentMessageRecord() {
		// Auto-generated method stub
		return null;
	}
	
	public void putArray(PhysicalGraphNode n) {
		for (int i=0;i<=24;i++){
			if(getKachel(n).equals(getKachelX(i))) nArray[i]=n;
		}
	}
	
}
