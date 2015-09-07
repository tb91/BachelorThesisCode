package projects.reactiveSpanner.nodes.timers.BCA;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.BCA.BCAForwarderMessageHandler;
import sinalgo.nodes.timers.Timer;

public class SecondRequestTimer extends Timer {
	
	public UUID sessionID;
	
	boolean enabled = true;
	public BCAForwarderMessageHandler msgHandler;

	public SecondRequestTimer(BCAForwarderMessageHandler msgHandler, UUID sessionID) {
		super();
		this.msgHandler = msgHandler;
		this.sessionID = sessionID;
	}
	
	@Override
	public void fire() {
		if (enabled) {
			msgHandler.broadcastSecondRequestNow(this.sessionID);
		}
	}
	
	public void disable() {
		enabled = false;
	}

}
