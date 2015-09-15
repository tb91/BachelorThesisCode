package projects.rmys.nodes.messageHandler;

import projects.reactiveSpanner.TopologyControlObserver;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.reactivePDT.ReactivePDT;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;

/**
 * @author timmy
 *
 */
public class RMYS extends BeaconlessTopologyControl {

	public static int k = -1;
	public static double cone_size = -1;

	static {
		try {
			k = Configuration.getIntegerParameter("RMYS/k_value");
		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}

	ReactivePDT pdt;
	RMYSMessageHandler rmys;
	RMYSForwarderMessageHandler forwarderMh;

	public RMYS(NewPhysicalGraphNode sourceNode) {
		super(EStrategy.RMYS, sourceNode);

		cone_size = 2 * Math.PI / k;

		_init();
	}

	@Override
	protected void _init() {
		pdt = new ReactivePDT(sourceNode);
		pdt.addObserver(new TopologyControlObserver() {

			@Override
			public void onNotify(SubgraphStrategy topologyControl, EState event) {
				if (pdt.hasTerminated()) { // as soon as RMYS gets notified
											// thats rPDT has terminated it
											// starts Modified Yao Step
					forwarderMh = new RMYSForwarderMessageHandler(getTopologyControlID(), sourceNode, pdt);
					forwarderMh.start();
				}

			}
		});

		// adds a RMYSMessageHandler for compatibility reasons to the
		// ReactiveSpanner Framework
		rmys = new RMYSMessageHandler(super.getTopologyControlID(), sourceNode, sourceNode, super.getStrategyType());
		rmys.initializeKnownNeighborsSet();
		sourceNode.messageHandlerMap.put(super.getTopologyControlID(), rmys);

		pdt.start();
	}

	protected void calculate() {

	}

	@Override
	protected void _start() {

	}


}
