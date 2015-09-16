package projects.rmys.nodes.messageHandler;

import java.awt.Graphics;

import projects.reactiveSpanner.TopologyControlObserver;
import projects.reactiveSpanner.nodes.messageHandlers.AbstractMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.reactivePDT.ReactivePDT;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.gui.transformation.PositionTransformation;

/**
 * @author timmy
 *
 */
public class RMYS extends BeaconlessTopologyControl {

	public static int k = -1;
	public static double cone_size = -1;
	public static double unit_radius = -1;

	static {
		try {
			k = Configuration.getIntegerParameter("RMYS/k_value");
			unit_radius = Configuration.getIntegerParameter("UDG/rMax");
		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}

	ReactivePDT pdt;
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

					forwarderMh.start();
				}

			}
		});

		// adds a RMYSMessageHandler for compatibility reasons to the
		// ReactiveSpanner Framework
		forwarderMh = new RMYSForwarderMessageHandler(getTopologyControlID(), sourceNode, pdt);
		forwarderMh.initializeKnownNeighborsSet();
		sourceNode.messageHandlerMap.put(super.getTopologyControlID(), forwarderMh);

		pdt.start();
	}

	protected void calculate() {

	}

	@Override
	protected void _start() {

	}

	@Override
	public void draw(Graphics g, PositionTransformation pt) {
		super.draw(g, pt);
		AbstractMessageHandler<?> amh = sourceNode.getMessageHandler(this.getTopologyControlID());
		if (amh != null) {
			amh.drawNode(g, pt);
		}
	}


}
