package projects.rmys.nodes.messageHandler.beaconingMYS;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconTopologyControl;
import projects.reactiveSpanner.nodes.messages.RTS;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;

public class BeaconingMYS extends BeaconTopologyControl{

	public BeaconingMYS(EStrategy usedStrategy, PhysicalGraphNode sourceNode) {
		super(usedStrategy, sourceNode);
		_init();
	}

	@Override
	protected void _init() {
		RTS rts = new RTS(this.getTopologyControlID(), this.sourceNode, EStrategy.BEACONING_MYS);
		sourceNode.broadcast(rts);		
	}

	@Override
	protected void _start() {
		// TODO Auto-generated method stub
		
	}

}
