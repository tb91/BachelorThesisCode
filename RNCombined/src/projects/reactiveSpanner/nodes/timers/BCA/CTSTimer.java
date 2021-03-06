package projects.reactiveSpanner.nodes.timers.BCA;

import java.awt.Color;
import java.util.UUID;

import projects.reactiveSpanner.CustomGlobal;
import projects.reactiveSpanner.nodes.messageHandlers.BCA.BCAForwarderMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.BCA.BCAMessageHandler;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.messages.Message;

public class CTSTimer extends BCAMessageTimer {
	private BCAMessageHandler msgHandler;

	private boolean initialRequest;
	
	public UUID sessionID;
	
	private BCAMessageHandler RTSinitiator;
	
	public CTSTimer(Message msg, BCAMessageHandler msgHandler, boolean initialRequest,
			UUID sessionID, BCAMessageHandler RTSinitiator) {
		super(msg);
		this.msgHandler = msgHandler;
		this.initialRequest = initialRequest;
		this.sessionID = sessionID;
		this.RTSinitiator = RTSinitiator;
	}

	@Override
	public void fire() {
		super.fire();
		if (enabled) {
			msgHandler.isLeader = true;
			msgHandler.cellLeaderElected = true;
			this.node.setColor(Color.ORANGE);
//			CustomGlobal.cellLeader.add(this.node);
			
			if (initialRequest) {
				System.out.println("### Was initialRequest");
				msgHandler.switchToForwarderMsgHandler();
			} 
			// TODO else-Teil auskommentieren, um keine Kindersessions zu starten
			else {
				
				// Initialknoten herausfinden
				UUID motherNodeID = BCAMessageHandler.subSessionMap.get(sessionID);
				PhysicalGraphNode initialNode = BCAMessageHandler.motherNodeMap.get(motherNodeID);
				
				// F�r geschachtelte Anfragen (subSessions von subSessions)
				if (initialNode == null) {
					while (initialNode == null) {
						motherNodeID = BCAMessageHandler.subSessionMap.get(motherNodeID);
						initialNode = BCAMessageHandler.motherNodeMap.get(motherNodeID);
					}
				}
				
				if (msgHandler.isInIntersectionArea(initialNode)) {
					System.out.println("### " + msgHandler.node.toString()
							+ " is in IntersectionArea of "
							+ initialNode.toString() + "!");
					System.out.println("### " + msgHandler.node.toString()
							+ " Node will start new session!");
					msgHandler.getNewForwarderMsgHandler(sessionID);
				} else {
					System.out.println("### " + msgHandler.node.toString()
							+ " is not in IntersectionArea of " + initialNode.toString());
					RTSinitiator.broadcastFinishMessage(sessionID);
				}
			}
			
		}
	}

}
