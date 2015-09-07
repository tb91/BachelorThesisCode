package projects.reactiveSpanner.nodes.timers.BCA;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.BCA.BCAMessageHandler;
import sinalgo.nodes.timers.Timer;

public class FirstRequestPhaseDoneTimer extends Timer {
	
	public UUID sessionID;
	
	BCAMessageHandler msgHandler;
	
	boolean enabled = true;
	
	public FirstRequestPhaseDoneTimer(BCAMessageHandler msgHandler, UUID sesionID){
		this.msgHandler = msgHandler;
		this.sessionID = sessionID;
	}

	@Override
	public void fire() {
		if (enabled) {
			msgHandler.firstRequestPhaseDone(this.sessionID);
		}
	}
	
	public void disable() {
		enabled = false;
	}
}