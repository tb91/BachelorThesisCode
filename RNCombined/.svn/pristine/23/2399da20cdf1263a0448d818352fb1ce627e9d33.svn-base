package projects.reactiveSpanner.nodes.messageHandlers.BCA;

import projects.reactiveSpanner.nodes.messageHandlers.BeaconlessTopologyControl;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;

public class BCA extends BeaconlessTopologyControl {

//	BCAForwarderMessageHandler forwarderMsgHandler;
	BCAMessageHandler msgHandler;
	
	public BCA(PhysicalGraphNode sourceNode) {
		super(BeaconlessTopologyControl.EStrategy.BCA, sourceNode);
		_init();
	}

	@Override
	protected void _start() {
		
	}

	@Override
	protected void terminate() {
		// Auto-generated method stub
	}

	@Override
	protected void _init() {
//		forwarderMsgHandler = new BCAForwarderMessageHandler(this.getTopologyControlID(), sourceNode);
//		sourceNode.messageHandlerMap.put(this.getTopologyControlID(), forwarderMsgHandler);
//		forwarderMsgHandler.broadcastFirstRequest();
		
		msgHandler = new BCAMessageHandler(this.getTopologyControlID(), sourceNode, sourceNode);
		sourceNode.messageHandlerMap.put(this.getTopologyControlID(), msgHandler);
		msgHandler.broadcastRTS(true, null);
	}
	
}

/*
@Override
protected void init(){
	forwarderMsgHandler = new ReactivePDTForwarderMessageHandler(this, sourceNode);	
	
	//test if the used connectivity model is UDG
	String usedCModel = forwarderMsgHandler.node.getConnectivityModel().getClass().getName();
	usedCModel = usedCModel.substring(usedCModel.lastIndexOf('.') + 1);
	if(!usedCModel.equals("UDG"))
	{
		throw new RuntimeException("Used Connectivity model is " + usedCModel + ", but Reactive PDT is only supported under the UDG model!");
	}
}

@Override
public void start() {
	//we add 1 to timeout, so that the forwarder would receive a CTS from nodes that have the distance of exactly UDG_R 
	//to the forwarder right before terminating the calculation
	final int epsilon = 5;
	final double timeout = getMaximumTimeout() + epsilon;
	forwarderMsgHandler.broadcastRTS(new BeaconlessTimer(forwarderMsgHandler, timeout));
	notify(EEvents.STARTED);
}
*/