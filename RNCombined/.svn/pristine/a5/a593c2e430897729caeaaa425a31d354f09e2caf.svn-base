package projects.reactiveSpanner.nodes.timers.BCA;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.BCA.BCAForwarderMessageHandler;
import sinalgo.nodes.timers.Timer;

public class FinishTimer extends Timer {
	
	public UUID sessionID;
	
	private boolean enabled = true; // if not enabled, timer will not fire
	BCAForwarderMessageHandler msgHandler;
	
	public FinishTimer(BCAForwarderMessageHandler msgHandler, UUID sessionID){
		this.msgHandler = msgHandler;
		this.sessionID = sessionID;
	}
	
	@Override
	public void fire() {
		if (enabled) {
			msgHandler.done(this.sessionID);
		}
	}
		
	public void disable() {
		enabled = false;
	}
}
