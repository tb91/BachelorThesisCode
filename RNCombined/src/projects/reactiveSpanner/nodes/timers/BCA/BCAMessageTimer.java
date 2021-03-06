package projects.reactiveSpanner.nodes.timers.BCA;

import java.util.UUID;

import projects.reactiveSpanner.CustomGlobal;
import projects.reactiveSpanner.nodes.messageHandlers.BCA.BCAMessageHandler;
import projects.reactiveSpanner.nodes.messages.AbstractMessage;
import projects.reactiveSpanner.nodes.messages.BCA.FinishMessage;
import projects.reactiveSpanner.nodes.messages.BCA.FirstRequest;
import projects.reactiveSpanner.nodes.messages.BCA.FirstResponse;
import projects.reactiveSpanner.nodes.messages.BCA.SecondRequest;
import projects.reactiveSpanner.nodes.messages.BCA.SecondResponse;
import projects.reactiveSpanner.nodes.messages.BCA.RTS;
import projects.reactiveSpanner.nodes.messages.BCA.CTS;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.messages.Message;

public class BCAMessageTimer extends projects.defaultProject.nodes.timers.MessageTimer {
	protected boolean enabled = true; // if not enabled, timer will not send
	
	private int msgType = -1;
	private BCAMessageHandler msgHandler;
	
	
	// zu Testzwecken
	private AbstractMessage msgCopy;
	
	// F�r den Fall, dass wir ein RTS senden m�ssen
	private UUID sessionID;
	
	public BCAMessageTimer(Message msg) {
		super(msg);

		// zu Testzwecken
		msgCopy = (AbstractMessage) msg;
		
		// Logging
		if (msg instanceof FirstRequest) {
			msgType = 0;
		}
		if (msg instanceof FirstResponse) {
			msgType = 1;
			this.msgHandler = ((FirstResponse) msg).msgHandler;
			this.sessionID = ((FirstResponse) msg).sessionID;
		}
		if (msg instanceof SecondRequest) {
			msgType = 2;
		}
		if (msg instanceof SecondResponse) {
			msgType = 3;
		}
		if (msg instanceof RTS) {
			msgType = 4;
		}
		if (msg instanceof CTS) {
			msgType = 5;
		}
		if (msg instanceof FinishMessage){
			msgType = 6;
		}
	}

	@Override
	public void fire() {
		if (enabled) {
			super.fire();
			
			// Logging
			if (msgType != -1)
//				CustomGlobal.totalMessages++;
			
			switch (msgType) {
			case 0:
//				CustomGlobal.firstRequests++;
//				CustomGlobal.firstRequestNodes.add(this.node);
				System.out.println(this.node.toString() + " fired FirstRequestTimer!");
				break;
			case 1:
//				CustomGlobal.firstResponses++;
				String str1 = this.node.toString()
						+ " fired FirstResponseTimer";
//				CustomGlobal.firstResponseNodes.add(this.node);
				if (((FirstResponse) msgCopy).cellLeaderElection) {
					msgHandler.broadcastRTS(false, this.sessionID);
					str1 += " mit cellLeaderElection!";
				} else {
					str1 += "!";
				}
				System.out.println(str1);
				break;
			case 2:
//				CustomGlobal.secondRequests++;
				System.out.println(this.node.toString() + " fired SecondRequestTimer!");
				break;
			case 3:
//				CustomGlobal.secondResponses++;
//				CustomGlobal.secondResponseNodes.add(this.node);
				String str2 = this.node.toString() + " fired SecondResponseTimer";
				if (((SecondResponse) msgCopy).cellLeaderElection) {
					str2 += " mit cellLeaderElection f�r!";
				} else {
					str2 += "!";
				}
				System.out.println(str2);
				break;
			case 4:
//				CustomGlobal.RTS++;
//				CustomGlobal.RTSNodes.add(this.node);
				System.out.println(this.node.toString() + " broadcasts RTS!");
				break;
			case 5:
//				CustomGlobal.CTS++;
//				CustomGlobal.CTSNodes.add(this.node);
				System.out.println(this.node.toString() + " fired CTSTimer!");
				break;
			case 6:
				System.out.println(this.node.toString() + " fired FinishMessageTimer!");
				break;
			}
		}
	}
	
	public void disable() {
		enabled = false;
	}
	
}
