package projects.reactiveSpanner.nodes.messageHandlers.buildBackbone;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import projects.reactiveSpanner.RoutingEntry;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.nodes.messages.Message;

public class VirtualNode implements BuildbackboneTopology {

	private Position position;
	private PhysicalGraphNode controller;
	private int id;
	
	
	private PhysicalGraphNode startnode1;
	private PhysicalGraphNode endnode1;
	private PhysicalGraphNode startnode2;
	private PhysicalGraphNode endnode2;
	
	
	
	
	private HashMap<BuildbackboneTopology, ArrayList<RoutingEntry>> routingTable=new HashMap<BuildbackboneTopology, ArrayList<RoutingEntry>>();
	
	
	public VirtualNode(Position position, PhysicalGraphNode controller, PhysicalGraphNode startnode1, PhysicalGraphNode endnode1,PhysicalGraphNode startnode2, PhysicalGraphNode endnode2) {
		this.position = position;
		this.controller = controller;
		//create seed using the x and y coord of a virtual node (
		String seed=Math.round(position.xCoord*1000000)+""+Math.round(position.yCoord*1000000);
		//abuse random number generator to generate equal ids for equal virtual nodes
		id=new Random(Long.parseLong(seed)).nextInt();
//		System.out.println("got id:"+ id + " with coordinates: " + position.xCoord+", " + position.yCoord);
		
		//we need to save on which edge-edge-intersection the virtual node is
		this.startnode1=startnode1;
		this.endnode1=endnode1;
		this.startnode2=startnode2;
		this.endnode2=endnode2;
		
	}
	
	public PhysicalGraphNode getStartnode1() {
		return startnode1;
	}

	public PhysicalGraphNode getEndnode1() {
		return endnode1;
	}

	public PhysicalGraphNode getStartnode2() {
		return startnode2;
	}

	public PhysicalGraphNode getEndnode2() {
		return endnode2;
	}
	

	
	@Override
	public HashMap<BuildbackboneTopology, ArrayList<RoutingEntry>> getRoutingTable() {
		return routingTable;
	}
	
	@Override
	public HashMap<Integer, PhysicalGraphNode> getCollectedNodes() {
		throw new RuntimeException("This method should not be called from VirtualNode");
		
	}

	
	@Override
	public String toString(){
		return "vNode("+ this.id + ")"; 
	}

	
	
	
	/*
	 * ========Getter und Setter===========
	 */
	
	public Position getPosition() {
		return position;
	}

	public PhysicalGraphNode getController() {
		return controller;
	}

	

	public int getId() {
		return id;
	}
	

	protected void setRoutingTable(HashMap<BuildbackboneTopology, ArrayList<RoutingEntry>> routingTable) {
		this.routingTable = routingTable;
	}
	
	protected void addRoutingEntry(BuildbackboneTopology key, ArrayList<RoutingEntry> arrayList){
		routingTable.put(key, arrayList);
	}

	@Override
	public PhysicalGraphNode getNode() {
		return this.controller;
	}


	
}
