package projects.reactiveSpanner.nodes.messageHandlers.BarriereExt;

import java.awt.Graphics;

import projects.reactiveSpanner.Utilities;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.buildBackbone.BuildBackboneMessageHandler;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.timers.BeaconTopologyTimer;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;

public class BarriereExt extends BeaconTopologyControl{

	private BeaconTopologyTimer terminationTimer;
	
	public BarriereExt(PhysicalGraphNode sourceNode) {
		super(EStrategy.BARRIERE_EXT, sourceNode);
//		start();
	}
	
	@Override
	protected void _start() {
		epidemicInit();
		//globalInit();
		//localInit();
		terminationTimer = new BeaconTopologyTimer(this, 100);
	}
	
	/**
	 * init for epidemic progression of the algorithm,
	 * starting with a single target and every following
	 * receiver of beacons will then start as well
	 */
	private void epidemicInit(){
		
		logger.logln(LogL.INFO, "adding Barriere messageHandlers to all Nodes.");
		Node n = Tools.getRandomNode();
		
		for (PhysicalGraphNode p : Utilities.getNodeCollectionByClass(PhysicalGraphNode.class)) {
			// give every node a new messageHandler with same TopologyControlID

			BarriereExtMessageHandler bemh = new BarriereExtMessageHandler(super.getTopologyControlID(), p, p, super.getStrategyType());//TODO:maybe change to p, sourcenode
			// if (!p.equals(this.sourceNode)){

			// sourceNode.subgraphStrategyFactory.request(EStrategy.BUILD_BACKBONE,this.getTopologyControlID());

			// }
			if (!p.equals(sourceNode)) {
				p.subgraphStrategyFactory.handOverSubgraphStrategy(this);
			}

			p.messageHandlerMap.put(super.getTopologyControlID(), bemh);

			//bemh.epidemicInit();

		}
										
		if(n instanceof PhysicalGraphNode){
			// add message handler
			PhysicalGraphNode p=(PhysicalGraphNode)n;
				
			BarriereExtMessageHandler bemh = (BarriereExtMessageHandler)p.getMessageHandler(super.getTopologyControlID());
			
			bemh.epidemicInit();
		}else{
			logger.logln(LogL.WARNING,"Tried to create a BarriereMessageHandler on a non-PhysicalGraphNode");
		}
	}
	
	/**
	 * start globally. Every node starts beaconing at the same time.
	 */
	private void globalInit(){
		logger.logln(LogL.INFO, "adding Barriere messageHandlers to all Nodes.");
		for(Node n:Tools.getNodeList()){	
										
			if(n instanceof PhysicalGraphNode){
				// add message handler
				PhysicalGraphNode p=(PhysicalGraphNode)n;
				
				BarriereExtMessageHandler bmh= new BarriereExtMessageHandler(super.getTopologyControlID(), p, p,super.getStrategyType());
				// deprecated
				//p.putNewForwarderMessageHandler(super.getTopologyControlID(),bmh);
					
				p.subgraphStrategyFactory.handOverSubgraphStrategy(this);
				p.messageHandlerMap.put(super.getTopologyControlID(), bmh);
				
				bmh.localInit();
				
				
			}else{
				logger.logln(LogL.WARNING,"Tried to create a BarriereMessageHandler on a non-PhysicalGraphNode");
			}
			
			
		}
	}
	
	/**
	 * Starting the algorithm on only 1 node without
	 * progression in the network.
	 */
	private void localInit(){
		logger.logln(LogL.INFO, "adding Barriere messageHandlers to all Nodes.");
		Node n = Tools.getRandomNode();
										
		if(n instanceof PhysicalGraphNode){
			// add message handler
			PhysicalGraphNode p=(PhysicalGraphNode)n;
				
			BarriereExtMessageHandler bmh= new BarriereExtMessageHandler(super.getTopologyControlID(), p, p,super.getStrategyType());
			// deprecated
			//p.putNewForwarderMessageHandler(super.getTopologyControlID(),bmh);
				
			p.subgraphStrategyFactory.handOverSubgraphStrategy(this);
			p.messageHandlerMap.put(super.getTopologyControlID(), bmh);

			bmh.localInit();
		}else{
			logger.logln(LogL.WARNING,"Tried to create a BarriereMessageHandler on a non-PhysicalGraphNode");
		}
		
	}

	@Override
	protected void _init() {
		//epidemicInit();
		//localInit();
		//globalInit();
	}
	
	@Override
	public void topologyTimerFire(){
		super.currentState = EState.TERMINATED;
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt) {
		for (PhysicalGraphNode p : Utilities.getNodeCollectionByClass(PhysicalGraphNode.class)) {
			
			try{
				p.getMessageHandler(this.getTopologyControlID()).drawNode(g, pt);
			}catch(Exception npe){
				
			}

		}

	}
}
