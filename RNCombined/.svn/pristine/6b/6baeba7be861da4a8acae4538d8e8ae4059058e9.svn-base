package projects.reactiveSpanner.nodes.timers;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconTopologyControl;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;

public class BeaconTopologyTimer extends Timer{
	
	BeaconTopologyControl btt;

	public BeaconTopologyTimer(BeaconTopologyControl btt, int startTime){
		super();
		this.btt = btt;
		this.startGlobalTimer(startTime);
	}
	
	@Override
	public void fire() {
		btt.topologyTimerFire();
	}

}
