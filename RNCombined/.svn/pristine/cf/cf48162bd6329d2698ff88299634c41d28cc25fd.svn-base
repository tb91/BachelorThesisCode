package projects.reactiveSpanner.nodes.timers.BCA;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.BCA.BCAMessageHandler;
import sinalgo.nodes.timers.Timer;

public class PauseTimer extends Timer {
	
	public UUID sessionID;
	
	public boolean enabled = true;
	public BCAMessageHandler msgHandler;
	public double pause;
	
	public PauseTimer(double pause, BCAMessageHandler msgHandler, UUID sessionID){
		this.msgHandler = msgHandler;
		this.pause = pause;
		this.sessionID = sessionID;
	}
	
	public void disable() {
		enabled = false;
	}
	

	@Override
	public void fire() {

		if (enabled) {
			msgHandler.broadcastFirstRequest(pause, this.sessionID);
		}
	}

}
