package projects.reactiveSpanner.nodes.messageHandlers.BCA;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import projects.reactiveSpanner.CustomGlobal;
import projects.reactiveSpanner.nodes.timers.BCA.PauseTimer;
import projects.reactiveSpanner.nodes.timers.BCA.CTSTimer;
import projects.reactiveSpanner.nodes.messages.BCA.FinishMessage;
import projects.reactiveSpanner.nodes.messages.BCA.FirstRequest;
import projects.reactiveSpanner.nodes.messages.BCA.FirstResponse;
import projects.reactiveSpanner.nodes.messages.BCA.SecondRequest;
import projects.reactiveSpanner.nodes.messages.BCA.SecondResponse;
import projects.reactiveSpanner.nodes.messages.BCA.RTS;
import projects.reactiveSpanner.nodes.messages.BCA.CTS;
import projects.reactiveSpanner.nodes.messageHandlers.AbstractMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.timers.BCA.BCAMessageTimer;
import projects.reactiveSpanner.nodes.timers.BCA.FirstRequestPhaseDoneTimer;
import projects.reactiveSpanner.record.MessageRecord;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.models.ConnectivityModel;
import sinalgo.nodes.Position;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;

public class BCAMessageHandler extends AbstractMessageHandler<PhysicalGraphNode> {
	
	public static HashMap<UUID, PhysicalGraphNode> motherNodeMap = new HashMap<UUID, PhysicalGraphNode>();
	// subSessionMap<subSession, motherSession>
	public static HashMap<UUID, UUID> subSessionMap = new HashMap<UUID, UUID>();
	
	public CTSTimer CTSTimer;
	
	public HashMap<UUID, BCAMessageTimer> firstRequestTimerMap = new HashMap<UUID, BCAMessageTimer>();
	public HashMap<UUID, BCAMessageTimer> firstResponseTimerMap = new HashMap<UUID, BCAMessageTimer>();
	public HashMap<UUID, BCAMessageTimer> secondResponseTimerMap = new HashMap<UUID, BCAMessageTimer>();
	public HashMap<UUID, FirstRequestPhaseDoneTimer> firstRequestPhaseDoneTimerMap = new HashMap<UUID, FirstRequestPhaseDoneTimer>();
	public HashMap<UUID, PauseTimer> pauseTimerMap = new HashMap<UUID, PauseTimer>();
	

	//Knoten merkt sich, ob in seiner Zelle bereits ein Leader (Clusterhead) gew�hlt wurde
	public boolean cellLeaderElected = false;
	public boolean isLeader = false;
	
	//Knoten merkt sich, ob die eigene Zelle schon eine FinishMessage gesendet hat
//	public HashMap<UUID, Boolean> cellSentFinishMessage = new HashMap<UUID, Boolean>();
	public boolean cellSentFinishMessage = false;
	
	//Knoten merkt sich, ob er CellLeaderElection gestartet hat
	public boolean responsibleForLeaderElection = false;
	
	// Enth�lt die Kinderzellen dieser Zelle (als Br�ckenknoten), welche eine
	// Session gestartet haben.
	// Wird die Session der Kinderzelle beendet, wird sie aus dieser Liste gel�scht.
	public Set<PhysicalGraphNode> unfinishedChildrenCells = new HashSet<PhysicalGraphNode>();
	
	private boolean checkedConnectionModel = false;
	
	//Deaktiviert diesen MsgHandler.
	//Er wertet keine ankommenden Nachrichten mehr aus.
//	private boolean deactivated = false;
	public Set<UUID> deactivatedSession = new HashSet<UUID>();
	
	public double tmaxTime = -1;
	
	public Position[] activeCells;
	
	protected static double tmax;{
		try {
			tmax = Configuration.getIntegerParameter("Timer/BCAtMax");
		} catch(CorruptConfigurationEntryException e) {
			Tools.fatalError(e.getMessage());
		}
	}
	
	protected static int udr;{
		try {
			udr = Configuration.getIntegerParameter("UDG/rMax");
		} catch(CorruptConfigurationEntryException e) {
			Tools.fatalError(e.getMessage());
		}
	}
	
	protected int rMin;{
		try {
			rMin = Configuration.getIntegerParameter("QUDG/rMin");
		} catch(CorruptConfigurationEntryException e) {
			Tools.fatalError(e.getMessage());
		}
	}
	
	protected int rMax;{ 
		try {
			rMax = Configuration.getIntegerParameter("QUDG/rMax");
		} catch(CorruptConfigurationEntryException e) {
			Tools.fatalError(e.getMessage());
		}
	}
	
	protected double kacheldiagonale = rMin;
	protected double kachelbreite = Math.sqrt((rMin * rMin) / 2);
	
	public BCAMessageHandler(final UUID tcID, final PhysicalGraphNode ownerNode,
			final PhysicalGraphNode sourceNode) {
		super(tcID, ownerNode, sourceNode, BeaconlessTopologyControl.EStrategy.BCA);
	}

	@Override
	public void receivedMessage(AbstractMessage msg) {

//		if (!deactivated) {

		if (msg instanceof FirstRequest) {
			if (!deactivatedSession.contains(((FirstRequest) msg).sessionID)) {
				receivedFirstRequest((FirstRequest) msg);
			}
		}

		if (msg instanceof FirstResponse) {
			if (!deactivatedSession.contains(((FirstResponse) msg).sessionID)) {
				receivedFirstResponse((FirstResponse) msg);
			}
		}

		if (msg instanceof SecondRequest) {
			if (!deactivatedSession.contains(((SecondRequest) msg).sessionID)) {
				receivedSecondRequest((SecondRequest) msg);
			}
		}

		if (msg instanceof SecondResponse) {
			if (!deactivatedSession.contains(((SecondResponse) msg).sessionID)) {
				receivedSecondResponse((SecondResponse) msg);
			}
		}

		if (msg instanceof RTS) {
			receivedRTS((RTS) msg);
		}

		if (msg instanceof CTS) {
			receivedCTS((CTS) msg);
		}
		
		if (msg instanceof FinishMessage) {

			receivedFinishMessage((FinishMessage) msg);
			
		}

//		}
	}
	
	public void receivedFinishMessage(FinishMessage msg) {
		//Kam die FinishMessage aus der eigenen Zelle, leiten wir sie weiter
		if (getKachel(this.node).equals(getKachel(msg.getTransmitter()))) {
			if (!cellSentFinishMessage) {
				cellSentFinishMessage = true;

				if (responsibleForLeaderElection) {
					BCAMessageTimer FinishMessageTimer = new BCAMessageTimer(
							new FinishMessage(msg.getID(), this.node, this.node));
					FinishMessageTimer.startRelative(1, this.node);

					System.out.println(this.node + " leitet FinishMessage von "
							+ msg.getTransmitter() + " weiter ");
				}
			}
		}
		
		if (unfinishedChildrenCells.contains(msg.source)) {
			BCAMessageTimer FinishMessageTimer = new BCAMessageTimer(
					msg.clone());
			FinishMessageTimer.startRelative(1, this.node);
			
			unfinishedChildrenCells.remove(msg.source);
			System.out.println("###### " + this.node.toString() + " entfernte "
					+ msg.source.toString()
					+ " aus den unfinishedChildrenCells");
		}
	}

	public void receivedFirstRequest(FirstRequest msg) {

		if (!checkedConnectionModel) {
			String cm = this.node.getConnectivityModel().getClass().getName();
			String UDG = ConnectivityModel.getConnectivityModelInstance("UDG")
					.getClass().getName();
			// String QUDG =
			// ConnectivityModel.getConnectivityModelInstance("StaticQUDG")
			// .getClass().getName();

			if (cm.equals(UDG)) {

				// Julians Variante
				rMin = udr;
				rMax = rMin;

				// Florentins Variante
				// udr = rMax;
				// R/r <= sqrt(2);
			}

			checkedConnectionModel = true;
		}
		
		UUID sessionID = msg.sessionID;

//		initialNodeMap.put(sessionID, msg.getSource());

		BCAMessageTimer	firstRequestTimer = firstRequestTimerMap.get(sessionID);
		
		// Falls ein Timer f�r FirstRequest schon l�uft, wird er um tmax
		// verl�ngert
		if (activeCells != null && firstRequestTimer != null
				&& firstRequestTimer.getFireTime() >= Global.currentTime) {

			double pause = firstRequestTimer.getFireTime()
					- Tools.getGlobalTime();
			
			firstRequestTimer.disable();
			firstRequestTimer = null;
			firstRequestTimerMap.put(sessionID, firstRequestTimer);
			
			PauseTimer pauseTimer = new PauseTimer(pause, this, sessionID);
			pauseTimer.startRelative(tmax + 4, this.node);
			pauseTimerMap.put(sessionID, pauseTimer);

			logger.logln(LogL.INFO, this.node.toString() + " pause for "
					+ (tmax + 4) + "+" + pause + " rounds.");
		} else {
			if (getKachel(this.node).equals(getKachel(msg.getSource()))) {
				// gleiche Kachel: Verfahren beenden (tue nichts)
			} else {
				// eigene Kachel addressiert? > Timer f�r FirstResponse
				// setzen
				if (isMessageForMe(msg.getDirectCells())) {
					// euklidische Distanz d berechnen: |Knoten <->
					// MittelPunkt Kachel|
					double d = this.getKachelMitte().distanceTo(
							this.node.getPosition());
					// Timerwert t berechnen
					double t = ((d) / (0.5 * kacheldiagonale)) * tmax;
					broadcastFirstResponse(t, sessionID);
					logger.logln(LogL.INFO, this.node.toString()
							+ " start FirstResponseTimer for " + msg.getSource() + " with t = " + t);
				}
			}
		}
	}
	
	public void receivedFirstResponse(FirstResponse msg) {
		
		UUID sessionID = msg.sessionID;
		
		BCAMessageTimer firstResponseTimer = firstResponseTimerMap.get(sessionID);
		
		BCAMessageTimer firstRequestTimer =  firstRequestTimerMap.get(sessionID);
		
		// firstResponseTimer not finished yet?
		if (firstResponseTimer != null  && firstResponseTimer.getFireTime() > Global.currentTime) {
			// falls FirstResponse aus der eigenen Kachel kam > Timer abbrechen
			if (getKachel(this.node).equals(getKachel(msg.getSource()))) {
				firstResponseTimer.disable();
				firstResponseTimerMap.put(sessionID, firstResponseTimer);
				this.node.setColor(Color.BLUE);
				logger.logln(LogL.INFO, "	" + this.node.toString() + " cancels timer");
				
				//TODO del
				this.node.setColor(Color.BLUE);
			}
		} else {
			//firstResponseTimer wurde bereits gefeuert
			//Antwort eines Knotens aus aktiver Kachel an Forwarder weiterleiten
			if (activeCells != null && firstRequestTimer != null
					&& firstRequestTimer.getFireTime() < Tools.getGlobalTime()
					&& isInAktiveKacheln(getKachel(msg.getSource()))) {

				broadcastSecondResponse(1, msg.getSource(), sessionID, msg.cellLeaderElection);
				this.knownNeighbors.add(msg.getSource());
				
				if (msg.cellLeaderElection) {
					unfinishedChildrenCells.add(msg.getSource());
					System.out.println("###### " + this.node.toString() + " f�gte "
							+ msg.getSource().toString()
							+ " als unfinishedChildrenCell hinzu");
				}

				for (int i = 0; i < activeCells.length; i++) {
					if (activeCells[i] != null
							&& activeCells[i]
									.equals(getKachel(msg.getSource()))) {
						activeCells[i] = null;
					}
				}
				
				if (isAktiveKachelnLeer()) {
					activeCells = null;
					firstRequestTimer.disable();
					firstRequestTimer = null;
					firstRequestTimerMap.put(sessionID, firstRequestTimer);
				}

			} else {
				//die Nachricht kommt von einem Knoten aus einer aktiven Kachel,
				//f�r die wir noch keine Anfrage geschickt haben > Knoten aus aktiven Kacheln l�schen
				
				if (activeCells != null && pauseTimerMap.get(sessionID) != null) {
					for (int i = 0; i < activeCells.length; i++) {
						if (activeCells[i] != null
								&& activeCells[i].equals(getKachel(msg
										.getSource()))) {
							activeCells[i] = null;
						}
					}
					logger.logln(LogL.INFO, "	" + this.node.toString() + " deletes some active cells");
					
					if (isAktiveKachelnLeer()) {
						activeCells = null;
						//Bei QUDG brauche ich anscheinend die folgende Abfrage. Warum?
//						if (firstRequestTimer != null) {
							firstRequestTimer.disable();
							firstRequestTimer = null;
							firstRequestTimerMap.put(sessionID, firstRequestTimer);
//						}
					}
				}
			}
		}
	}
	
	public void receivedSecondRequest(SecondRequest msg) {
		
		UUID sessionID = msg.sessionID;
		
		if (getKachel(this.node).equals(getKachel(msg.getSource()))) {
			
			PauseTimer pauseTimer = null;
			pauseTimerMap.put(sessionID, pauseTimer);
			
			activeCells = msg.getIndirectCells().clone();
			
//			System.out.println("          KONTROLLE          ");
//			String tempStr = "";
//			for (Position p : activeCells) {
//				if (p == null) continue;
//				tempStr += "[" + p.xCoord + " | " + p.yCoord + " | " + p.zCoord + "]" + "   ";
//			}
//			System.out.println("          " + this.node.toString() + " ; " + tempStr);
			
			double[] t = new double[activeCells.length];
			
			// Jetzt muss die Entfernung zur jeweiligen Kachel berechnet
			// werden, womit der Timer bestimmt werden kann (d*tmax)
			for (int i = 0; i < activeCells.length; i++) {
				t[i] = isKachelInRadius(activeCells[i]);
			}
			
			double tmin = tmax + 1;
			
			for (int i = 0; i < t.length; i++) {
				if (t[i] > 0 && t[i] < tmin)
					tmin = t[i];
			}
			
			if (tmin < tmax + 1 & tmin != 0) {
				broadcastFirstRequest(tmin, msg.sessionID);
				logger.logln(LogL.INFO, this.node.toString() + " start FirstRequestTimer with t = " + tmin);
				this.node.setColor(Color.CYAN);
			}
		}
	}
	
	public void receivedSecondResponse(SecondResponse msg) {

		UUID sessionID = msg.sessionID;
		
		try {
			if (pauseTimerMap.get(sessionID) != null && isInAktiveKacheln(getKachel(msg.getSource()))) {
			
				// Kachel merken

				for (int i = 0; i < activeCells.length; i++) {
					if (activeCells[i] != null
							&& activeCells[i]
									.equals(getKachel(msg.getSource()))) {
						activeCells[i] = null;
					}
				}

				// Offene Kacheln �brig?
				boolean empty = true;
				// Gibt es noch offene Kacheln?
				if (activeCells != null) {
					for (int i = 0; i < activeCells.length; i++) {
						if (activeCells[i] != null)
							empty = false;
					}
				}
				
				boolean inReach = false;
				//Sind offene Kacheln erreichbar?
				if (activeCells != null){
					for (Position p : activeCells){
						if (isKachelInRadius(p) !=0) inReach = true;
					}
				}
				

				if (empty || !inReach) {
					// nein > Timer beenden
					activeCells = null;
					PauseTimer pauseTimer = pauseTimerMap.get(sessionID);
					pauseTimer.disable(); // zur Sicherheit
					pauseTimer = null;
					pauseTimerMap.put(sessionID, pauseTimer);
					logger.logln(LogL.INFO, "	" + this.node.toString()
							+ " cancels FirstRequestTimer.");
					logger.logln(LogL.INFO, "	" + this.node.toString()
							+ " has no active nor reachable cells anymore.");

					this.node.setColor(Color.RED);

				} else {
					// ja > nichts tun
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void receivedRTS(RTS msg) {
		if (getKachel(this.node).equals(getKachel(msg.getSource()))) {
			
//			System.out.println("#" + this.node + " received RTS from " + msg.getSource());
			
			if (!checkedConnectionModel) {
				String cm = this.node.getConnectivityModel().getClass().getName();
				String UDG = ConnectivityModel.getConnectivityModelInstance("UDG").getClass()
						.getName();
//				String QUDG = ConnectivityModel.getConnectivityModelInstance("StaticQUDG")
//						.getClass().getName();

				if (cm.equals(UDG)) {
					rMin = udr;
					rMax = rMin;
				}
				
				checkedConnectionModel = true;
			}
			
			// euklidische Distanz d berechnen: |Knoten <-> MittelPunkt Kachel|
			double d = this.getKachelMitte().distanceTo(
					this.node.getPosition());
			// Timerwert t berechnen
			double t = ((d) / (0.5 * kacheldiagonale)) * tmax;
			
			// Falls dieser Knoten das RTS versandte, geht der Timer erst in der n�chsten Runde los
			if (msg.getSource() == this.node) t++;
			
			if (CTSTimer == null) {
				CTSTimer = new CTSTimer(new CTS(this.tcID, this.node), this,
						msg.initialRequest, msg.sessionID, msg.msgHandler);
				//TODO Bei initialRequest ist msg.initialNode null!!
				CTSTimer.startRelative(t, this.node);
			
				System.out.println("   " + this.node.toString() + " CTSTimer: " + t);
			}
		}
	}
	
	public void receivedCTS(CTS msg) {
		if (getKachel(this.node).equals(getKachel(msg.getSource()))) {
			if (CTSTimer != null && CTSTimer.getFireTime() > Global.currentTime) {
				CTSTimer.disable();
				CTSTimer = null;
				
				System.out.println("#" + this.node + " heard CTS from " + msg.getSource() + " and cancels timer");
			}

			//Ein anderer Knoten hat zur selben Zeit geantwortet:
			//Knoten mit kleinerer ID wird Leader
			if (CTSTimer != null
					&& CTSTimer.getFireTime() <= Global.currentTime + 2
					&& CTSTimer.getFireTime() >= Global.currentTime - 2 ) {

				if (msg.getSource().ID < this.node.ID) {
					this.isLeader = false;
					this.node.setColor(Color.DARK_GRAY);
//					CustomGlobal.cellLeader.remove(this.node);
//					CustomGlobal.CTS--;
				}
				
			}
		}
	}

	public void broadcastRTS(boolean initialRequest, UUID sessionID) {
		responsibleForLeaderElection = true;
		
		BCAMessageTimer RTSTimer = new BCAMessageTimer(new RTS(tcID, this.node, initialRequest, sessionID, this));
		RTSTimer.startRelative(1, this.node);
		this.receivedRTS(new RTS(tcID, this.node, initialRequest, sessionID, this));
	}
	
	public void broadcastFirstRequest(double timer, UUID sessionID) {
		BCAMessageTimer	firstRequestTimer = firstRequestTimerMap.get(sessionID);
		
		firstRequestTimer = new BCAMessageTimer(new FirstRequest(this.tcID,
				this.node, activeCells, sessionID, BCAMessageHandler.motherNodeMap.get(BCAMessageHandler.subSessionMap.get(sessionID))));
		firstRequestTimer.startRelative(timer, node);
		firstRequestTimerMap.put(sessionID, firstRequestTimer);
		
		FirstRequestPhaseDoneTimer firstRequestPhaseDoneTimer = firstRequestPhaseDoneTimerMap.get(sessionID);
		
		if (firstRequestPhaseDoneTimer != null) {
			firstRequestPhaseDoneTimer.disable();
		}
		
		firstRequestPhaseDoneTimer = new FirstRequestPhaseDoneTimer(this, sessionID);
		firstRequestPhaseDoneTimer.startRelative(firstRequestTimer.getFireTime() + tmax + 5, this.node);
		firstRequestPhaseDoneTimerMap.put(sessionID, firstRequestPhaseDoneTimer);
	}
	
	public void broadcastFirstResponse(double timer, UUID sessionID){
		
		BCAMessageTimer firstResponseTimer = firstResponseTimerMap.get(sessionID);
		
		if (firstResponseTimer != null) {
			firstResponseTimer.disable();
		}
		
		firstResponseTimer = new BCAMessageTimer(new FirstResponse(
				this.tcID, this.node, this, !(this.cellLeaderElected), sessionID));
		firstResponseTimer.startRelative(timer, this.node);
		firstResponseTimerMap.put(sessionID, firstResponseTimer);
		
		cellLeaderElected = true;
	}

	public void broadcastSecondResponse(double timer, PhysicalGraphNode sourceNode, UUID sessionID, boolean cellLeaderElection){
		BCAMessageTimer secondResponseTimer = new BCAMessageTimer(
				new SecondResponse(this.tcID, this.node, sourceNode, sessionID, cellLeaderElection));
		secondResponseTimer.startRelative(timer, this.node);
		secondResponseTimerMap.put(sessionID, secondResponseTimer);
	}
	
	public void broadcastFinishMessage(UUID sessionID){
		BCAMessageTimer FinishMessageTimer = new BCAMessageTimer(
				new FinishMessage(this.tcID, this.node,
						this.node));
		FinishMessageTimer.startRelative(1, this.node);
		
		System.out
				.println("###### "
						+ this.node
						+ " broadcastet FinishMessage auf Befehl eines Zellengenossen!");
	}
	
	public void firstRequestPhaseDone(UUID sessionID) {
		
		BCAMessageTimer firstRequestTimer = firstRequestTimerMap.get(sessionID);
		
		if (firstRequestTimer != null) {
			firstRequestTimer.disable();
			firstRequestTimer = null;
			firstRequestTimerMap.put(sessionID, firstRequestTimer);
			
			logger.logln("Round " + Global.currentTime + ": "
					+ this.node.ID
					+ ": beendet Wartezeit auf Antwort von erreichbaren Knoten.");
			
			if (!this.node.getColor().equals(Color.RED))
				this.node.setColor(Color.BLACK);
		}
	}

	public void switchToForwarderMsgHandler() {
		BCAForwarderMessageHandler forwarderMsgHandler = new BCAForwarderMessageHandler(tcID, this.node);
		this.node.messageHandlerMap.put(tcID, forwarderMsgHandler);
		this.node.messageHandlerMap.remove(this);
		forwarderMsgHandler.broadcastFirstRequest(this, true, null);
		forwarderMsgHandler.cellLeaderElected = true;
	}
	
	public void getNewForwarderMsgHandler(UUID motherSessionID) {
		BCAForwarderMessageHandler forwarderMsgHandler = new BCAForwarderMessageHandler(tcID, this.node);
		this.node.messageHandlerMap.put(tcID, forwarderMsgHandler);
		forwarderMsgHandler.broadcastFirstRequest(this, false, motherSessionID);
		forwarderMsgHandler.cellLeaderElected = true;
	}
	
//	public void startNewBCASession(){
//		BCAForwarderMessageHandler forwarderMsgHandler = new BCAForwarderMessageHandler(tcID, this.node);
//		this.node.messageHandlerMap.put(tcID, forwarderMsgHandler);
//		forwarderMsgHandler.broadcastFirstRequest(null, false);
//		forwarderMsgHandler.cellLeaderElected = true;
//	}
	
	@Override
	public void drawNode(Graphics g, PositionTransformation pt) {
	}

	@Override
	public MessageRecord getCurrentMessageRecord() {
		// Auto-generated method stub
		return null;
	}
	
	
//-------------------------------------------------------------------------
//	Kachel-Funktionen
//-------------------------------------------------------------------------	
	
	public Position[] getDirectCells() {

		Position[] directCells = new Position[24];
		for (int i = 0; i < 24; i++) {
			if (i < 12)
				directCells[i] = getKachelX(i);
			if (i >= 12)
				directCells[i] = getKachelX(i + 1);
		}

		return directCells;
	}

	/**
	 * Gibt zur ID einer Kachel die fortlaufenden Nummerierung zur�ck. F�r
	 * Kachel 0 gibt die Methode (1,0,0) zur�ck.
	 */
	public Position getKachelX(int i) {
		Position myKachel = this.getKachel(this.node);
		double x = myKachel.xCoord;
		double y = myKachel.yCoord;
		Position[] tmp = new Position[25];
		tmp[0] = new Position(x - 2, y - 2, 0);
		tmp[1] = new Position(x - 1, y - 2, 0);
		tmp[2] = new Position(x + 0, y - 2, 0);
		tmp[3] = new Position(x + 1, y - 2, 0);
		tmp[4] = new Position(x + 2, y - 2, 0);
		tmp[5] = new Position(x - 2, y - 1, 0);
		tmp[6] = new Position(x - 1, y - 1, 0);
		tmp[7] = new Position(x + 0, y - 1, 0);
		tmp[8] = new Position(x + 1, y - 1, 0);
		tmp[9] = new Position(x + 2, y - 1, 0);
		tmp[10] = new Position(x - 2, y + 0, 0);
		tmp[11] = new Position(x - 1, y + 0, 0);
		tmp[12] = new Position(x + 0, y + 0, 0);
		tmp[13] = new Position(x + 1, y + 0, 0);
		tmp[14] = new Position(x + 2, y + 0, 0);
		tmp[15] = new Position(x - 2, y + 1, 0);
		tmp[16] = new Position(x - 1, y + 1, 0);
		tmp[17] = new Position(x - 0, y + 1, 0);
		tmp[18] = new Position(x + 1, y + 1, 0);
		tmp[19] = new Position(x + 2, y + 1, 0);
		tmp[20] = new Position(x - 2, y + 2, 0);
		tmp[21] = new Position(x - 1, y + 2, 0);
		tmp[22] = new Position(x + 0, y + 2, 0);
		tmp[23] = new Position(x + 1, y + 2, 0);
		tmp[24] = new Position(x + 2, y + 2, 0);
		return tmp[i];
	}

	/**
	 * Gibt an, in welcher Kachel der Knoten liegt. Dabei werden nicht die
	 * Koordinaten des Kachelursprungs zur�ckgegeben, sondern die fortlaufende
	 * Bezeichnung. Die zweite Kachel von oben links w�rde beispielsweise
	 * (1,0,0) zur�ckgeben.
	 */
	public Position getKachel(PhysicalGraphNode node) {
		double x = getKachelPosition(node).xCoord;
		double y = getKachelPosition(node).yCoord;
		double cx = x / kachelbreite;
		double cy = y / kachelbreite;
		return new Position(cx, cy, 0);
	}

	/**
	 * Gibt die Koordinaten der linke oberen Ecke der Kachel, in der der Knoten
	 * liegt, zur�ck.
	 */
	public Position getKachelPosition(PhysicalGraphNode node) {
		double x = node.getPosition().xCoord;
		double y = node.getPosition().yCoord;
		double cx = x - x % kachelbreite;
		double cy = y - y % kachelbreite;
		return new Position(cx, cy, 0);
	}
	
	/**
	 * @param msg
	 *            Nachricht vom Typ FirstRequest
	 * @return true, wenn das Array "msg.getDestKachel[]" den Eintrag
	 *         this.getKachel() enth�lt.
	 */
	public boolean isMessageForMe(Position[] cells) {
		Position[] array = cells;
		for (int i = 0; i < array.length; i++) {
			if (array[i] != null && array[i].equals(getKachel(this.node)))
				return true;
		}
		return false;
	}
	
	/**
	 * Gibt die Koordinaten des Mittelpunkts der Kachel, in der der Knoten
	 * liegt, zurück.
	 */
	public Position getKachelMitte() {
		Position kachelMitte = getKachelPosition(this.node);
		kachelMitte.xCoord = kachelMitte.xCoord
				+ (kacheldiagonale / Math.sqrt(8));
		kachelMitte.yCoord = kachelMitte.yCoord
				+ (kacheldiagonale / Math.sqrt(8));
		return kachelMitte;
	}
	
	public Position getKachelMitte(PhysicalGraphNode node) {
		Position kachelMitte = getKachelPosition(node);
		kachelMitte.xCoord = kachelMitte.xCoord
				+ (kacheldiagonale / Math.sqrt(8));
		kachelMitte.yCoord = kachelMitte.yCoord
				+ (kacheldiagonale / Math.sqrt(8));
		return kachelMitte;
	}

	public Position kachelToKachelMitte(Position p) {
		double x = p.xCoord * kachelbreite;
		double y = p.yCoord * kachelbreite;
		x = x + 0.5 * kachelbreite;
		y = y + 0.5 * kachelbreite;
		return new Position(x, y, 0);
	}

	public Position kachelToKachelEckeObenRechts(Position p) {
		double x = p.xCoord * kachelbreite;
		double y = p.yCoord * kachelbreite;
		x = x + kachelbreite;
		return new Position(x, y, 0);
	}

	public Position kachelToKachelEckeUntenRechts(Position p) {
		double x = p.xCoord * kachelbreite;
		double y = p.yCoord * kachelbreite;
		x = x + kachelbreite;
		y = y + kachelbreite;
		return new Position(x, y, 0);
	}

	public Position kachelToKachelEckeUntenLinks(Position p) {
		double x = p.xCoord * kachelbreite;
		double y = p.yCoord * kachelbreite;
		y = y + kachelbreite;
		return new Position(x, y, 0);
	}

	public Position kachelToKachelEckeObenLinks(Position p) {
		double x = p.xCoord * kachelbreite;
		double y = p.yCoord * kachelbreite;
		return new Position(x, y, 0);
	}

	public Position kachelToKachelMitteUnten(Position p) {
		double x = p.xCoord * kachelbreite + 0.5 * kachelbreite;
		double y = p.yCoord * kachelbreite + 1.0 * kachelbreite;
		return new Position(x, y, 0);
	}

	public Position kachelToKachelMitteOben(Position p) {
		double x = p.xCoord * kachelbreite + 0.5 * kachelbreite;
		double y = p.yCoord * kachelbreite;
		return new Position(x, y, 0);
	}

	public Position kachelToKachelMitteLinks(Position p) {
		double x = p.xCoord * kachelbreite;
		double y = p.yCoord * kachelbreite + 0.5 * kachelbreite;
		return new Position(x, y, 0);
	}

	public Position kachelToKachelMitteRechts(Position p) {
		double x = p.xCoord * kachelbreite + 1.0 * kachelbreite;
		double y = p.yCoord * kachelbreite + 0.5 * kachelbreite;
		return new Position(x, y, 0);
	}

	
	/**
	 * @return double 0<t<=tmax, wenn die Kachel im Senderadius liegt, oder t=0,
	 *         wenn die Kachel nicht im Senderadius liegt.
	 */
	public double isKachelInRadius(Position kachel) {
		double t = 0;
		if (kachel == null)
			return 0;
		if (kachel.equals(getKachelX(0))) {
			// untere rechte Ecke der Kachel0 für Distanzberechnung
			// minus Kachelbreite, damit größere Unterscheidung im timer-Wert
			Position p = kachelToKachelEckeUntenRechts(getKachelX(0));
			double d = this.node.getPosition().distanceTo(p);
			if (d <= rMax)
				t = ((d - kacheldiagonale) / (rMax - kacheldiagonale)) * tmax;
		}
		if (kachel.equals(getKachelX(1))) {
			// untere rechte Ecke der Kachel1 für Distanzberechnung
			// minus Kachelbreite, damit größere Unterscheidung im timer-Wert
			Position p = kachelToKachelEckeUntenRechts(getKachelX(1));
			double d = this.node.getPosition().distanceTo(p);
			if (d <= rMax)
				t = ((d - kachelbreite) / (rMax - kachelbreite)) * tmax;
		}
		// Bedingung für kachel2
		if (kachel.equals(getKachelX(2))) {
			// alle Knoten, die in dem oberen Bereich der Kachel liegen (~oberes
			// 1/3)
			Position p = this.kachelToKachelMitteOben(getKachel(this.node));
			double d1 = this.node.getPosition().yCoord
					- getKachelPosition(this.node).yCoord;
			double d2 = this.node.getPosition().distanceTo(p);
			if (d1 <= rMax - kachelbreite) {
				t = (d2 / Math.sqrt((Math.pow(rMax - kachelbreite, 2) + Math
						.pow(kachelbreite / 2, 2)))) * tmax;
				// log.logln(""+this.ID+": "+t);
			}
		}
		// Bedingung für kachel3
		if (kachel.equals(getKachelX(3))) {
			// untere linke Ecke der Kachel3 für Distanzberechnung
			// minus Kachelbreite, damit größere Unterscheidung im timer-Wert
			Position p = kachelToKachelEckeUntenLinks(getKachelX(3));
			double d = this.node.getPosition().distanceTo(p);
			if (d <= rMax)
				t = ((d - kachelbreite) / (rMax - kachelbreite)) * tmax;
		}
		// Bedingung für kachel4
		if (kachel.equals(getKachelX(4))) {
			// untere linke Ecke der Kachel4 für Distanzberechnung
			// minus Kachelbreite, damit größere Unterscheidung im timer-Wert
			Position p = kachelToKachelEckeUntenLinks(getKachelX(4));
			double d = this.node.getPosition().distanceTo(p);
			if (d <= rMax)
				t = ((d - kacheldiagonale) / (rMax - kacheldiagonale)) * tmax;
		}
		// Bedingung für kachel5
		if (kachel.equals(getKachelX(5))) {
			// untere rechte Ecke der Kachel5 für Distanzberechnung
			Position p = kachelToKachelEckeUntenRechts(getKachelX(5));
			double d = this.node.getPosition().distanceTo(p);
			if (d <= rMax)
				t = ((d - kachelbreite) / (rMax - kachelbreite)) * tmax;
		}
		// Bedingung für kachel6
		if (kachel.equals(getKachelX(6))) {
			// untere rechte Ecke der Kachel6 für Distanzberechnung
			Position p = kachelToKachelEckeUntenRechts(getKachelX(6));
			double d = this.node.getPosition().distanceTo(p);
			if (d <= rMax)
				t = (d / kacheldiagonale) * tmax;
		}
		// Bedingung für kachel7
		// alle Knoten der Kachel
		if (kachel.equals(getKachelX(7))) {
			Position p = this.kachelToKachelMitteOben(getKachel(this.node));
			double d = this.node.getPosition().distanceTo(p);
			t = (d / Math.sqrt((Math.pow(kachelbreite, 2) + Math.pow(
					kachelbreite / 2, 2)))) * tmax;
			// log.logln(""+this.ID+": "+t);
		}
		// Bedingung für kachel8
		if (kachel.equals(getKachelX(8))) {
			// untere linke Ecke der Kachel8 für Distanzberechnung
			Position p = kachelToKachelEckeUntenLinks(getKachelX(8));
			double d = this.node.getPosition().distanceTo(p);
			if (d <= rMax)
				t = (d / kacheldiagonale) * tmax;
		}
		// Bedingung für kachel9
		if (kachel.equals(getKachelX(9))) {
			// untere rechte Ecke der Kachel9 für Distanzberechnung
			// minus Kachelbreite, damit größere Unterscheidung im timer-Wert
			Position p = kachelToKachelEckeUntenLinks(getKachelX(9));
			double d = this.node.getPosition().distanceTo(p);
			if (d <= rMax)
				t = ((d - kachelbreite) / (kacheldiagonale - kachelbreite))
						* tmax;
		}
		// Bedingung für kachel10
		if (kachel.equals(getKachelX(10))) {
			// alle Knoten, die in dem linken Bereich der Kachel liegen (~linkes
			// 1/3)
			Position p = this.kachelToKachelMitteLinks(getKachel(this.node));
			double d1 = this.node.getPosition().xCoord - p.xCoord;
			double d2 = this.node.getPosition().distanceTo(p);
			if (d1 <= rMax - kachelbreite) {
				t = (d2 / Math.sqrt((Math.pow(rMax - kachelbreite, 2) + Math
						.pow(kachelbreite / 2, 2)))) * tmax;
				// log.logln(""+this.ID+": Kachel 8 "+t);
			}
		}

		// Bedingung für kachel11
		if (kachel.equals(getKachelX(11))) {
			// alle Knoten der Kachel
			Position p = this.kachelToKachelMitteLinks(getKachel(this.node));
			double d = this.node.getPosition().distanceTo(p);
			t = (d / Math.sqrt((Math.pow(kachelbreite, 2) + Math.pow(
					kachelbreite / 2, 2)))) * tmax;
			// log.logln(""+this.ID+": Kachel 9 "+t);
		}

		// Bedingung für kachel12 fällt weg, eigene Kachel

		// Bedingung für Kachel13
		if (kachel.equals(getKachelX(13))) {
			// alle Knoten
			Position p = this.kachelToKachelMitteRechts(getKachel(this.node));
			double d = this.node.getPosition().distanceTo(p);
			t = (d / Math.sqrt((Math.pow(kachelbreite, 2) + Math.pow(
					kachelbreite / 2, 2)))) * tmax;
			// log.logln(""+this.ID+": Kachel 11 "+t);
		}

		// Bedingung für Kachel14
		if (kachel.equals(getKachelX(14))) {
			// alle Knoten, die in dem rechten Bereich der Kachel liegen
			// (~linkes 1/3)
			Position p = this.kachelToKachelMitteRechts(getKachel(this.node));
			double d1 = p.xCoord - this.node.getPosition().xCoord;
			double d2 = this.node.getPosition().distanceTo(p);
			if (d1 <= rMax - kachelbreite) {
				t = (d2 / Math.sqrt((Math.pow(rMax - kachelbreite, 2) + Math
						.pow(kachelbreite / 2, 2)))) * tmax;
				// log.logln(""+this.ID+": Kachel 12 "+t);
			}
		}
		// Bedingung für kachel15
		if (kachel.equals(getKachelX(15))) {
			// obere rechte Ecke der Kachel15 für Distanzberechnung
			// minus Kachelbreite, damit größere Unterscheidung im timer-Wert
			Position p = kachelToKachelEckeObenRechts(getKachelX(15));
			double d = this.node.getPosition().distanceTo(p);
			if (d <= rMax)
				t = ((d - kachelbreite) / (kacheldiagonale - kachelbreite))
						* tmax;
		}
		// Bedingung für kachel16
		if (kachel.equals(getKachelX(16))) {
			// obere rechte Ecke der Kachel16 für Distanzberechnung
			Position p = kachelToKachelEckeObenRechts(getKachelX(16));
			double d = this.node.getPosition().distanceTo(p);
			if (d <= rMax)
				t = (d / (kachelbreite)) * tmax;
		}
		// Bedingung für kachel17
		if (kachel.equals(getKachelX(17))) {
			Position p = this.kachelToKachelMitteUnten(getKachel(this.node));
			double d = this.node.getPosition().distanceTo(p);
			t = (d / Math.sqrt((Math.pow(kachelbreite, 2) + Math.pow(
					kachelbreite / 2, 2)))) * tmax;
			// log.logln(""+this.ID+": "+t);
		}
		// Bedingung für kachel18
		if (kachel.equals(getKachelX(18))) {
			// obere linke Ecke der Kachel18 für Distanzberechnung
			Position p = kachelToKachelEckeObenLinks(getKachelX(18));
			double d = this.node.getPosition().distanceTo(p);
			if (d <= rMax)
				t = (d / (kachelbreite)) * tmax;
		}
		// Bedingung für kachel19
		if (kachel.equals(getKachelX(19))) {
			// obere linke Ecke der Kachel19 für Distanzberechnung
			// minus Kachelbreite, damit größere Unterscheidung im timer-Wert
			Position p = kachelToKachelEckeObenLinks(getKachelX(19));
			double d = this.node.getPosition().distanceTo(p);
			if (d <= rMax)
				t = ((d - kachelbreite) / (kacheldiagonale - kachelbreite))
						* tmax;
		}
		// Bedingung für kachel20
		if (kachel.equals(getKachelX(20))) {
			// obere rechte Ecke der Kachel20 für Distanzberechnung
			Position p = kachelToKachelEckeObenRechts(getKachelX(20));
			double d = this.node.getPosition().distanceTo(p);
			if (d <= rMax)
				t = ((d - kacheldiagonale) / (rMax - kacheldiagonale)) * tmax;
		}
		// Bedingung für kachel21
		if (kachel.equals(getKachelX(21))) {
			// obere rechte Ecke der Kachel21 für Distanzberechnung
			// minus Kachelbreite, damit größere Unterscheidung im timer-Wert
			Position p = kachelToKachelEckeObenRechts(getKachelX(21));
			double d = this.node.getPosition().distanceTo(p);
			if (d <= rMax)
				t = ((d - kachelbreite) / (kacheldiagonale - kachelbreite))
						* tmax;
		}
		// Bedingung für kachel22
		if (kachel.equals(getKachelX(22))) {
			// alle Knoten, die in dem unteren Bereich der Kachel liegen
			// (~oberes 1/3)
			Position p = this.kachelToKachelMitteUnten(getKachel(this.node));
			double d1 = p.yCoord - this.node.getPosition().yCoord;
			double d2 = this.node.getPosition().distanceTo(p);
			if (d1 <= rMax - kachelbreite) {
				t = (d2 / Math.sqrt((Math.pow(rMax - kachelbreite, 2) + Math
						.pow(kachelbreite / 2, 2)))) * tmax;
				// log.logln(""+this.ID+": "+t);
			}
		}
		// Bedingung für kachel23
		if (kachel.equals(getKachelX(23))) {
			// obere linke Ecke der Kachel23 für Distanzberechnung
			// minus Kachelbreite, damit größere Unterscheidung im timer-Wert
			Position p = kachelToKachelEckeObenLinks(getKachelX(23));
			double d = this.node.getPosition().distanceTo(p);
			if (d <= rMax)
				t = ((d - kachelbreite) / (kacheldiagonale - kachelbreite))
						* tmax;
		}
		// Bedingung für kachel24
		if (kachel.equals(getKachelX(24))) {
			// obere linke Ecke der Kachel24 für Distanzberechnung
			// minus Kachelbreite, damit größere Unterscheidung im timer-Wert
			Position p = kachelToKachelEckeObenLinks(getKachelX(24));
			double d = this.node.getPosition().distanceTo(p);
			if (d <= rMax)
				t = ((d - kacheldiagonale) / (rMax - kacheldiagonale)) * tmax;
		}
		return t;
	}
	
	/**
	 * @param kachel eine Kachel z.B. (1,1,0)
	 * @return true, die Kachel im Array aktiveKacheln enthalten ist.
	 */
	public boolean isInAktiveKacheln(Position kachel) {
		if (kachel != null && activeCells != null) {
			for (int i = 0; i < activeCells .length; i++) {
				if (activeCells [i] != null && activeCells [i].equals(kachel))
					return true;
			}
		}
		return false;
	}

	/**
	 * @return true, wenn das Array aktiveKacheln leer sein sollte.
	 */
	public boolean isAktiveKachelnLeer() {
		if (activeCells != null) {
			for (int i = 0; i < activeCells.length; i++) {
				if (activeCells[i] != null)
					return false;
			}
		}
		return true;
	}
	
	/**
	 * @res true, falls die Kachel des Knotens dieses MessageHandlers in der
	 *      IntersectionArea des Knotens sourceNode liegt.
	 * @param initialKachel
	 * @return
	 */
	public boolean isInIntersectionArea(PhysicalGraphNode sourceNode){
		
		Position initialKachelMitte = getKachelMitte(sourceNode);
		Position dieseKachelMitte = this.getKachelMitte();

		// Pr�fen, ob eigene Kachel vertikal oder horizontal au�erhalb der
		// IntersectionArea liegt
		Double maxAbstand = 3 * kachelbreite;
		Double xAbstand = Math.abs(initialKachelMitte.xCoord - dieseKachelMitte.xCoord);
		Double yAbstand = Math.abs(initialKachelMitte.yCoord - dieseKachelMitte.yCoord);
		
		if (xAbstand > maxAbstand || yAbstand > maxAbstand) {
			return false;
		}
		
		// Pr�fen, ob eigene Kachel eine Eckkachel ist
		maxAbstand = 2.5 * kacheldiagonale;
		Double abstand = initialKachelMitte.distanceTo(dieseKachelMitte);
		
		if (abstand > maxAbstand) {
			return false;
		}
		
		return true;
	}
}
