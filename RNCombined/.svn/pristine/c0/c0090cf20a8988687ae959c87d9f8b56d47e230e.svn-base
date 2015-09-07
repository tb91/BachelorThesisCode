package projects.reactiveSpanner;

import java.util.UUID;

import javax.management.RuntimeErrorException;

import projects.reactiveSpanner.nodes.messageHandlers.AbstractMessageHandler;
import projects.reactiveSpanner.nodes.messageHandlers.buildBackbone.BuildBackboneMessageHandler;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.Node;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

public class BridgePair {
	static Logging logger=Logging.getLogger();
	
	public BuildBackboneMessageHandler first;
	public BuildBackboneMessageHandler second;

	public BridgePair(BuildBackboneMessageHandler first, BuildBackboneMessageHandler second) {
		super();
		this.first = first;
		this.second = second;
	}
	
	@Override
	public BridgePair clone(){
		return new BridgePair(first, second);
	}
	
	public BridgePair(Node first, Node second, UUID tcID){
		if(first instanceof PhysicalGraphNode && second instanceof PhysicalGraphNode){
			PhysicalGraphNode fir=(PhysicalGraphNode)first;
			PhysicalGraphNode sec=(PhysicalGraphNode)second;
			
			AbstractMessageHandler amhfir=fir.getMessageHandler(tcID);
			AbstractMessageHandler amhsec=sec.getMessageHandler(tcID);
			
			if(amhfir!=null && amhsec!=null && amhfir instanceof BuildBackboneMessageHandler && amhsec instanceof BuildBackboneMessageHandler){
				this.first=(BuildBackboneMessageHandler)amhfir;
				this.second=(BuildBackboneMessageHandler)amhsec;
			}else{
				String s="Tried to create a BridgePair from at least one non-BuildbackboneMessageHandler";
				logger.logln(LogL.ERROR_DETAIL, s);
				throw new RuntimeException(s);
			}
			
		}else{
			String s="Tried to create a BridgePair from at least one non-PhysicalGraphNode";
			logger.logln(LogL.ERROR_DETAIL, s);
			throw new RuntimeException(s);
		}

	}

	@Override
	public String toString(){
		return "(" + first.toString() + ", " + second.toString() + ") ";
	}
}
