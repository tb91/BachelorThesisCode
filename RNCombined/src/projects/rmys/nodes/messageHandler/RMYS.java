package projects.rmys.nodes.messageHandler;

import projects.reactiveSpanner.TopologyControlObserver;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.reactivePDT.ReactivePDT;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;

public class RMYS extends BeaconlessTopologyControl {

	ReactivePDT pdt;

	public RMYS(NewPhysicalGraphNode sourceNode) {
		super(EStrategy.RMYS, sourceNode);
		_init();
	}

	@Override
	protected void _init() {
		pdt = new ReactivePDT(sourceNode);
		pdt.addObserver(new TopologyControlObserver() {

			@Override
			public void onNotify(SubgraphStrategy topologyControl, EState event) {
				System.out.println("notifying!!");

			}
		});

	}

	@Override
	protected void _start() {
		// TODO Auto-generated method stub

	}


}
