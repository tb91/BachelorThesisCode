package projects.reactiveSpanner.nodes.timers;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconMessageHandler;
import sinalgo.nodes.timers.Timer;

/**
 * BeaconTimer that can be triggered for beaconing wait.
 * 
 * @author cyron
 *
 */
public class BeaconTimer extends Timer{
	
	BeaconMessageHandler bmh;
	
	public BeaconTimer(BeaconMessageHandler bmh, int startTime){
		
		this.bmh = bmh;
		this.startRelative(startTime, bmh.node);
	}
	
	@Override
	public void fire(){
		bmh.beaconTimerFire();
	}
}
