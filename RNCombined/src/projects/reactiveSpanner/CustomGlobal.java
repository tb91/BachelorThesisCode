/*
 Copyright (c) 2007, Distributed Computing Group (DCG)
                    ETH Zurich
                    Switzerland
                    dcg.ethz.ch

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

 - Neither the name 'Sinalgo' nor the names of its contributors may be
   used to endorse or promote products derived from this software
   without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package projects.reactiveSpanner;

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import projects.defaultProject.models.connectivityModels.StaticQUDG;
import projects.defaultProject.models.interferenceModels.NoInterference;
import projects.defaultProject.models.mobilityModels.NoMobility;
import projects.defaultProject.models.reliabilityModels.ReliableDelivery;
import projects.reactiveSpanner.FloydWarshall.AdjMatrixEdgeWeightedDigraph;
import projects.reactiveSpanner.FloydWarshall.DirectedEdge;
import projects.reactiveSpanner.FloydWarshall.FloydWarshall;
import projects.reactiveSpanner.models.connectivityModels.NoConnectivity;
import projects.reactiveSpanner.nodes.messageHandlers.BeaconTopologyControl;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.Barriere.BarriereMessageHandler;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.nodes.edges.Edge;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

/**
 * This class holds customized global state and methods for the framework. The only mandatory method to overwrite is <code>hasTerminated</code> <br>
 * Optional methods to override are
 * <ul>
 * <li><code>customPaint</code></li>
 * <li><code>handleEmptyEventQueue</code></li>
 * <li><code>onExit</code></li>
 * <li><code>preRun</code></li>
 * <li><code>preRound</code></li>
 * <li><code>postRound</code></li>
 * <li><code>checkProjectRequirements</code></li>
 * </ul>
 * 
 * @see sinalgo.runtime.AbstractCustomGlobal for more details. <br>
 *      In addition, this class also provides the possibility to extend the framework with custom methods that can be called either through the menu or via a button that is added to the GUI.
 */
public class CustomGlobal extends AbstractCustomGlobal {
	static Logging logger = Logging.getLogger();

	public static CustomGlobalBatch batch = CustomGlobalBatch.getInstance();
	public static double R = -1;

	// Lilli's grid
	boolean showGrid = false;
	public static boolean showWangLiBlue = true;// TODO REMOVE
	public static boolean showWangLiRed = false;// TODO REMOVE
	public static boolean showWangLiGreen = false;// TODO REMOVE
	public static boolean bbstarted = false;// TODO REMOVE

	static {
		try {
			R = Configuration.getDoubleParameter("UDG/rMax");
		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}

	public static boolean showBCAGrid = false;
	
	private static Map<String, Set<Node>> graphCache = new HashMap<String, Set<Node>>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see runtime.AbstractCustomGlobal#hasTerminated()
	 */
	@Override
	public boolean hasTerminated() {
		if (Tools.isSimulationInGuiMode())
			return false;
		else
			return batch.hasTerminated();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see runtime.AbstractCustomGlobal#preRun()
	 */
	@Override
	public void preRun() {
		if (Tools.isSimulationInGuiMode())
			return;
		else
			batch.preRun();
	}

	@Override
	public void postRound() {

		if (Tools.isSimulationInGuiMode()) {
			return;
		} else
			batch.postRound();
	}

	@Override
	public void onExit() {
		if (Tools.isSimulationInGuiMode())
			return;
		else
			batch.onExit();
	}

	@SuppressWarnings("unused")
	@Deprecated
	private void saveGraphInCache(String key) {
		logger.logln(LogL.INFO, "Saving \"" + key + "\" Graph in cache");

		Set<Node> graph;
		if (graphCache.containsKey(key)) {
			graph = graphCache.get(key);
			graphCache.get(key).clear();
		} else {
			graph = new HashSet<Node>();
		}

		for (Node v : Tools.getNodeList()) {
			graph.add(v);
		}
		graphCache.put(key, graph);
	}

	private void recalculateByCache(String key, Color edgeColor) {
		logger.logln(LogL.INFO, "Recalculating \"" + key + "\" Graph by cached graph due to no changes.");
		for (Node cachedNode : graphCache.get(key)) {
			SimpleNode original = (SimpleNode) Tools.getNodeByID(cachedNode.ID);
			original.resetConnectionToThisNode();
			for (Edge e : cachedNode.outgoingConnections) {
				original.addBidirectionalConnectionTo(e.endNode);
				drawEdge(original, e.endNode, edgeColor);
			}
		}
		if (Tools.isSimulationInGuiMode()) {
			Tools.repaintGUI();
		}
	}

	private boolean isAlreadyCalculated(String key) {
		// first we test if there is a entry for the key in the hashmap
		if (!graphCache.containsKey(key)) {
			logger.logln(LogL.INFO, "\"" + key + "\" Graph will be calculated the first time");
			return false;
		}

		// it's possible that we have deleted or addedsome node, so we have to
		// test if the number of nodes is identically
		if (Tools.getNodeList().size() != graphCache.get(key).size()) {
			logger.logln(LogL.INFO, "Nodes have been deleted or added. \"" + key + "\" Graph has to be recalculated.");
			return false;
		}

		// we test if all points of the current graph are also in the cached
		// graph
		for (Node v : Tools.getNodeList()) {
			boolean isInGraphCache = false;
			for (Node w : graphCache.get(key)) {
				if (v.equals(w)) {
					isInGraphCache = true;
					if (!v.getPosition().equals(w.getPosition())) {
						logger.logln(LogL.INFO, "Node " + v.ID + " is on different position as in cached " + key + " Graph. Graph has to be recalculated.");
						return false;
					}
					break;
				}
			}
			if (!isInGraphCache) {
				logger.logln(LogL.INFO, "Node " + v.ID + " is new in current \"" + key + "\" Graph. Graph has to be recalculated.");
				return false;
			}
		}
		logger.logln(LogL.INFO, "\"" + key + "\" Graph is already calculated");
		return true;
	}

	@AbstractCustomGlobal.CustomButton(
			buttonText = "BuildBackbone", toolTipText = "starts the BuildBackbone protocol")
	public void startBuildBackbone() {
		logger.logln(LogL.WARNING, "Invoking startBuildbackbone on random Node");
		Utilities.getRandomNode(PhysicalGraphNode.class, Tools.getNodeList()).startBuildBackbone();
		bbstarted = true;
		Tools.repaintGUI();
	}

	@AbstractCustomGlobal.CustomButton(
			buttonText = "Update", toolTipText = "updates all node connections")
	public void update() {
		logger.logln(LogL.WARNING, "updating all Node connections");
		for (Node n : Tools.getNodeList()) {
			try {
				StaticQUDG neu = new StaticQUDG();
				for (Edge e : n.outgoingConnections) {
					n.outgoingConnections.remove(n, e.endNode);
				}
				n.setConnectivityModel(neu);
			} catch (CorruptConfigurationEntryException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Starts the barriere extended Algorithm on a random node
	 */
	@AbstractCustomGlobal.CustomButton(
			buttonText = "barriere ext")
	public void barriereExtGlobal() {
		Utilities.getRandomNode(PhysicalGraphNode.class, Tools.getNodeList()).startBarriereExt();
		//((PhysicalGraphNode) Tools.getRandomNode()).startBarriereExt();
		Tools.repaintGUI();
	}

	/**
	 * checks the connection symetry (if a has b as neighbour, b has a as neighbour) of a barriere graph
	 * and shows it to the sinalgo output window
	 */
	@AbstractCustomGlobal.CustomButton(
			buttonText = "barriere symetry")
	public void barriereSymetryCheck() {
		try {
			Tools.clearOutput();
			
			// DEPRECATED
			Tools.appendToOutput("is symetrical=" + checkBarriereSymetry(Utilities.getRandomNode(PhysicalGraphNode.class, Tools.getNodeList()).subgraphStrategyFactory.request(EStrategy.BARRIERE).getTopologyControlID()));
			Tools.repaintGUI();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * checks the connection symetry (if a has b as neighbour, b has a as neighbour) of a barriere extended graph
	 */
	@AbstractCustomGlobal.CustomButton(
			buttonText = "barriereext symetry")
	public void barriereExtSymetryCheck() {
		try {
			Tools.clearOutput();
			
			// DEPRECATED
			Tools.appendToOutput("is symetrical=" + checkBarriereSymetry(Utilities.getRandomNode(PhysicalGraphNode.class, Tools.getNodeList()).subgraphStrategyFactory.request(EStrategy.BARRIERE_EXT).getTopologyControlID()));
			Tools.repaintGUI();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * prints the spanning ratio of a barriere graph in regard to the original physical
	 * graph handling the distance of virtual connections as follows: 1. the actual physical 
	 * routing path of the virtual connection 2. the shortest physical path between both nodes
	 * from the original graph
	 */
	@AbstractCustomGlobal.CustomButton(
			buttonText = "fw barriere")
	public void floydWarshallBarriere() {
		try {
			
			Tools.clearOutput();
			PhysicalGraphNode pgn = Utilities.getRandomNode(PhysicalGraphNode.class, Tools.getNodeList());

			// DEPRECATED
			// pgn.subgraphStrategyFactory.getLastRequestedSubgraphStrategy().getTopologyControlID()
			
			AdjMatrixEdgeWeightedDigraph matrix = Algorithms.getAdjMatrixEdgeWeightedDigraphBarriereSubgraphActualVirtual(pgn.subgraphStrategyFactory.request(EStrategy.BARRIERE).getTopologyControlID(), false);

			// DEPRECATED
			AdjMatrixEdgeWeightedDigraph pMatrix = Algorithms.getAdjMatrixEdgeWeightedDigraphBarrierePhysicalGraph(pgn.subgraphStrategyFactory.request(EStrategy.BARRIERE).getTopologyControlID(), false);
			
			//System.out.println(pgn.subgraphStrategyFactory.getLastRequestedSubgraphStrategy().getTopologyControlID());
			//System.out.println("floyd: "+pgn.subgraphStrategyFactory.request(EStrategy.BARRIERE).getTopologyControlID());
			//System.out.println("floyd2: "+pgn.subgraphStrategyFactory.request(EStrategy.BARRIERE).getStrategyType());
			
			int V = matrix.V();
			FloydWarshall fw = new FloydWarshall(matrix);

			FloydWarshall pfw = new FloydWarshall(pMatrix);

			// DEPRECATED (fixed)
			AdjMatrixEdgeWeightedDigraph sMatrix = Algorithms.getAdjMatrixEdgeWeightedDigraphBarriereSubgraphShortestVirtual(pgn.subgraphStrategyFactory.request(EStrategy.BARRIERE).getTopologyControlID(), pfw, false);

			FloydWarshall sfw = new FloydWarshall(sMatrix);

			double longestPath = 0;
			double shortestPath = 999;

			double spanningRatio = Algorithms.spanningRatio(pfw, fw, V);

			double spanningRatioV = Algorithms.spanningRatio(pfw, sfw, V);

			/*
			 * for(int x = 0; x<V; x++){ for(int y = x+1; y<V; y++){ double distance = fw.dist(x, y); if(distance!=Double.POSITIVE_INFINITY){ if(longestPath<distance){ longestPath=distance; } if(shortestPath>distance){ shortestPath=distance; } } } }
			 */

			Tools.appendToOutput("spanningRatio with virtual routing paths: " + spanningRatio + "\n");
			Tools.appendToOutput("spanningRatio with real shortest paths: " + spanningRatioV + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * prints the spanning ratio of a barriere extended graph in regard to the original physical
	 * graph handling the distance of virtual connections as follows: 1. the actual physical 
	 * routing path of the virtual connection 2. the shortest physical path between both nodes
	 * from the original graph
	 */
	@AbstractCustomGlobal.CustomButton(
			buttonText = "fw barriere ext")
	public void floydWarshallBarriereExt() {
		try {
			
			Tools.clearOutput();
			PhysicalGraphNode pgn = Utilities.getRandomNode(PhysicalGraphNode.class, Tools.getNodeList());

			// DEPRECATED
			// pgn.subgraphStrategyFactory.getLastRequestedSubgraphStrategy().getTopologyControlID()
			
			AdjMatrixEdgeWeightedDigraph matrix = Algorithms.getAdjMatrixEdgeWeightedDigraphBarriereSubgraphActualVirtual(pgn.subgraphStrategyFactory.request(EStrategy.BARRIERE_EXT).getTopologyControlID(), false);

			// DEPRECATED
			AdjMatrixEdgeWeightedDigraph pMatrix = Algorithms.getAdjMatrixEdgeWeightedDigraphBarrierePhysicalGraph(pgn.subgraphStrategyFactory.request(EStrategy.BARRIERE_EXT).getTopologyControlID(), false);
			
			//System.out.println(pgn.subgraphStrategyFactory.getLastRequestedSubgraphStrategy().getTopologyControlID());
			//System.out.println("floyd: "+pgn.subgraphStrategyFactory.request(EStrategy.BARRIERE).getTopologyControlID());
			//System.out.println("floyd2: "+pgn.subgraphStrategyFactory.request(EStrategy.BARRIERE).getStrategyType());
			
			int V = matrix.V();
			FloydWarshall fw = new FloydWarshall(matrix);

			FloydWarshall pfw = new FloydWarshall(pMatrix);

			// DEPRECATED (fixed)
			AdjMatrixEdgeWeightedDigraph sMatrix = Algorithms.getAdjMatrixEdgeWeightedDigraphBarriereSubgraphShortestVirtual(pgn.subgraphStrategyFactory.request(EStrategy.BARRIERE_EXT).getTopologyControlID(), pfw, false);

			FloydWarshall sfw = new FloydWarshall(sMatrix);

			double longestPath = 0;
			double shortestPath = 999;

			double spanningRatio = Algorithms.spanningRatio(pfw, fw, V);

			double spanningRatioV = Algorithms.spanningRatio(pfw, sfw, V);

			/*
			 * for(int x = 0; x<V; x++){ for(int y = x+1; y<V; y++){ double distance = fw.dist(x, y); if(distance!=Double.POSITIVE_INFINITY){ if(longestPath<distance){ longestPath=distance; } if(shortestPath>distance){ shortestPath=distance; } } } }
			 */

			Tools.appendToOutput("spanningRatio with virtual routing paths: " + spanningRatio + "\n");
			Tools.appendToOutput("spanningRatio with real shortest paths: " + spanningRatioV + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Calculates the spanning ratio of a Gabriel Graph in respect to its Unit Disc Graph
	 * and shows it to the sinalgo output window
	 */
	@AbstractCustomGlobal.CustomButton(
			buttonText = "GG span", toolTipText = "Spanning ratio of gabriel graph in UDG")
	public void GGSpan(){
		
		Tools.appendToOutput("Spanningratio UDG/GG: "+Algorithms.GGSpan()+"\n");
	}
	
	/**
	 * Calculates the spanning ratio of a Partial Delaunay Triangulation in respect to its 
	 * Unit Disc Graph and shows it to the sinalgo output window
	 */
	@AbstractCustomGlobal.CustomButton(
			buttonText = "PDT span", toolTipText = "Spanning ratio of PDT in UDG")
	public void PDTSpan(){
		
		Tools.appendToOutput("Spanningratio UDG/PDT: "+Algorithms.PDTSpan(false)+"\n");
	}
	
/*
	@AbstractCustomGlobal.CustomButton(
			buttonText = "DebugCase1_BuildBackbone", toolTipText = "starts the BuildBackbone protocol on a example graph")
	public void startBuildBackboneDebugCase1() {
		try {

			PhysicalGraphNode p1 = new PhysicalGraphNode();
			p1.setPosition(180, 190, 0);
			p1.setConnectivityModel(new StaticQUDG());
			p1.setReliabilityModel(new ReliableDelivery());
			p1.setInterferenceModel(new NoInterference());
			p1.setMobilityModel(new NoMobility());
			p1.setColor(Color.blue);

			PhysicalGraphNode p2 = new PhysicalGraphNode();
			p2.setPosition(190, 195, 0);
			p2.setConnectivityModel(new StaticQUDG());
			p2.setReliabilityModel(new ReliableDelivery());
			p2.setInterferenceModel(new NoInterference());
			p2.setMobilityModel(new NoMobility());
			p2.setColor(Color.blue);

			PhysicalGraphNode p3 = new PhysicalGraphNode();
			p3.setPosition(220, 195, 0);
			p3.setConnectivityModel(new StaticQUDG());
			p3.setReliabilityModel(new ReliableDelivery());
			p3.setInterferenceModel(new NoInterference());
			p3.setMobilityModel(new NoMobility());
			p3.setColor(Color.blue);

			PhysicalGraphNode p4 = new PhysicalGraphNode();
			p4.setPosition(210, 190, 0);
			p4.setConnectivityModel(new StaticQUDG());
			p4.setReliabilityModel(new ReliableDelivery());
			p4.setInterferenceModel(new NoInterference());
			p4.setMobilityModel(new NoMobility());
			p4.setColor(Color.blue);

			Tools.getNodeList().addNode(p1);
			Tools.getNodeList().addNode(p2);
			Tools.getNodeList().addNode(p3);
			Tools.getNodeList().addNode(p4);

			sinalgo.runtime.Runtime.reevaluateConnections();

			this.showGrid = true;

			Tools.repaintGUI();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/
	@AbstractCustomGlobal.CustomButton(
			buttonText = "UDG", toolTipText = "Unit Disk Graph")
	public void UDGButton() {
		for (SimpleNode v : Utilities.getNodeCollectionByClass(SimpleNode.class)) {
			v.neighborUDG();
		}
		// saveGraphInCache("UDG");
	}

	@AbstractCustomGlobal.CustomButton(
			buttonText = "GG", toolTipText = "Gabriel Graph")
	public void GabrielGraphButton() {
		if (isAlreadyCalculated("GG")) {
			recalculateByCache("GG", Color.green);
			return; // nothing more to do here
		}
		for (SimpleNode v : Utilities.getNodeCollectionByClass(SimpleNode.class)) {
			v.neighborGG();
		}
		// saveGraphInCache("GG");
	}

	@AbstractCustomGlobal.CustomButton(
			buttonText = "PDT", toolTipText = "Partial Delaunay Triangulation")
	public void PartialDelaunayTriangulationButton() {
		// Algorithms.clearGraphOfCollinearNodes();
		if (isAlreadyCalculated("PDT")) {
			recalculateByCache("PDT", Color.magenta);
			return; // nothing more to do here
		}
		for (SimpleNode v : Utilities.getNodeCollectionByClass(SimpleNode.class)) {
			v.neighborPDT();
		}
		// saveGraphInCache("PDT");
	}

	@AbstractCustomGlobal.CustomButton(
			buttonText = "Diff-PDT/GG", toolTipText = "Diff Graph of Partial Delaunay Triangulation and Gabriel Graph")
	public void DiffPDTAndGGButton() {
		// Algorithms.clearGraphOfCollinearNodes();
		if (isAlreadyCalculated("DiffPDTandGG")) {
			recalculateByCache("DiffPDTandGG", Color.BLUE);
			return; // nothing more to do here
		}
		for (SimpleNode v : Utilities.getNodeCollectionByClass(SimpleNode.class)) {
			v.neighborDiffPDTandGG();
		}
		// saveGraphInCache("DiffPDTandGG");
	}

	@AbstractCustomGlobal.CustomButton(
			buttonText = "Graph Connectiviy", toolTipText = "Examine if the graph is fully connected")
	public void isGraphConnectedButton() {
		if (GraphConnectivity.isGraphConnected(Tools.getNodeList())) {
			Tools.getTextOutputPrintStream().println("Graph is connected");
		} else {
			Tools.getTextOutputPrintStream().println("Graph is not connected");
		}
	}

	@AbstractCustomGlobal.CustomButton(
			buttonText = "BlueWangli", toolTipText = "show wangli")
	public void showWangLiBlue() {
		showWangLiBlue = !showWangLiBlue;
		showWangLiRed = false;
		showWangLiGreen = false;
		Tools.getGUI().redrawGUI();
	}

	@AbstractCustomGlobal.CustomButton(
			buttonText = "RedWangli", toolTipText = "show wangli")
	public void showWangLiRed() {
		showWangLiRed = !showWangLiRed;
		showWangLiBlue = false;
		showWangLiGreen = false;
		Tools.getGUI().redrawGUI();
	}

	@AbstractCustomGlobal.CustomButton(
			buttonText = "GreenWangli", toolTipText = "show wangli")
	public void showWangLiGreen() {
		showWangLiGreen = !showWangLiGreen;
		showWangLiBlue = false;
		showWangLiRed = false;
		Tools.getGUI().redrawGUI();
	}

	@AbstractCustomGlobal.CustomButton(buttonText = "BCA Grid")
	public void showBCAGrid() {
		showBCAGrid = !showBCAGrid;
		Tools.getGUI().redrawGUI();
	}
	
	@AbstractCustomGlobal.GlobalMethod(
			menuText = "Number of sent RTS, CTS and Protest Messages")
	public void printSentMessages() {
		// Show an information message
		// JOptionPane.showMessageDialog(null, "Sent RTS messages: " +
		// RTS.numberOfSentMessages()
		// + ", Sent CTS messages: " + CTS.numberOfSentMessages()
		// + ", Sent Protest messages: " +
		// ProtestMessage.numberOfSentMessages(),
		// "Sent Messages Echo", JOptionPane.INFORMATION_MESSAGE);
	}

	/*
	 * @AbstractCustomGlobal.GlobalMethod(menuText = "quick serialize nodes") public void serialize() { saveNodes(System.getProperty("user.dir") + "/quickSave.ser"); //not yet implemented }
	 * 
	 * @AbstractCustomGlobal.GlobalMethod(menuText = "deserialize nodes from quickSlot") public void deserialize() { //not yet implemented }
	 */

	@AbstractCustomGlobal.CustomButton(
			buttonText = "Clear Edges", toolTipText = "Clear all edges")
	public void clearAllConnections() {

		for (SimpleNode v : Utilities.getNodeCollectionByClass(SimpleNode.class)) {
			v.outgoingConnections.removeAndFreeAllEdges();
//			v.resetConnectionToThisNode();
		}
		Tools.getGUI().redrawGUI();
	}

	/**
	 * shows the grid used by BuildBackbone (Lillis)
	 */
	@AbstractCustomGlobal.CustomButton(
			buttonText = "toogle Grid", toolTipText = "toogle grid's visibility")
	public void showGrid() {
		showGrid = !showGrid;
		Tools.getGUI().redrawGUI();
	}

	public static boolean checkBarriereSymetry(UUID tcID) {
		int asymCon = 0;
		// UUID tcID = Utilities.getRandomNode(PhysicalGraphNode.class, Tools.getNodeList()).getLastSubgraphStrategy().getTopologyControlID();
		try {
			for (PhysicalGraphNode p : Utilities.getNodeCollectionByClass(PhysicalGraphNode.class)) {
				// unsafe cast --> try catch
				BarriereMessageHandler bmh = (BarriereMessageHandler) p.getMessageHandler(tcID);
				HashSet<SimpleNode> neighbours = bmh.getAllKnownSubgraphNeighbourNodes();
				for (SimpleNode n : neighbours) {
					PhysicalGraphNode p2 = (PhysicalGraphNode) n;
					BarriereMessageHandler bmh2 = (BarriereMessageHandler) p2.getMessageHandler(tcID);

					if (!bmh2.hasSubgraphNeighbour(p)) {
						System.out.println(p.ID + " to " + p2.ID);
						asymCon++;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("No barriere message handler last!");
		}
		System.out.println(asymCon);
		if (asymCon == 0)
			return true;
		return false;
	}

	/*
	 * 
	 * 
	 * needed to draw some stuff
	 */
	@Override
	public void customPaint(Graphics g, PositionTransformation pt) {
		
		if(!last.isEmpty()){
			
			g.setColor(Color.green);
			
			Position old=last.get(0).getPos();
			for(RoutingEntry re:last){
				pt.drawLine(g, old, re.getPos());
				old=re.getPos();
			}
		}
		
	if (showBCAGrid) {
			
			double rMax = R;
			
//			try {
//				rMax = Configuration.getDoubleParameter("QUDG/rMax");
//			} catch (CorruptConfigurationEntryException e) {
//				e.getMessage();
//				e.printStackTrace();
//			}
			
			drawGrid(Color.blue, rMax, g, pt);
		}
		
		if (Tools.getNodeList().size() <= 0 || bbstarted == false) { // prevent from drawing if there is no node or the last strategy is not BB
			return;
		}

		float diagonal = 1;
		try {

			diagonal = (float) Configuration.getDoubleParameter("QUDG/rMin");
		} catch (CorruptConfigurationEntryException e) {
			logger.logln(LogL.WARNING, "Parameter QUDG/rMin could not be read!");
			e.printStackTrace();
		}
		if (showGrid) {
			drawGrid(Color.blue, diagonal, g, pt);
			drawGrid(Color.red, diagonal, g, pt);
			drawGrid(Color.green, diagonal, g, pt);
		} else {
			if (showWangLiBlue) {
				drawGrid(Color.blue, diagonal, g, pt);
			}
			if (showWangLiRed) {
				drawGrid(Color.red, diagonal, g, pt);
			}
			if (showWangLiGreen) {
				drawGrid(Color.green, diagonal, g, pt);
			}
		}

	}

	private void drawGrid(Color c, double diagonal, Graphics g, PositionTransformation pt) {
		float gridSize = (float) (diagonal / Math.sqrt(2));
		g.setColor(c);

		float ifloat = 0;
		float save = 0;
		if (c == Color.red) {
			ifloat = (float) (BeaconTopologyControl.qr / (3 * Math.sqrt(2))) - gridSize;
			save = ifloat;
		} else if (c == Color.green) {
			ifloat = (float) (2 * BeaconTopologyControl.qr / (3 * Math.sqrt(2))) - gridSize;
			save = ifloat;
		}

		for (int i = (int) ifloat; i < Configuration.dimY - gridSize; i += gridSize) {
			ifloat += gridSize;
			if (c == Color.red) {
				pt.drawDottedLine(g, new Position(0, ifloat, 0), new Position(Configuration.dimX, ifloat, 0));
			} else if (c == Color.green) {
				pt.drawBoldLine(g, new Position(0, ifloat, 0), new Position(Configuration.dimX, ifloat, 0), 2);
			} else {
				pt.drawLine(g, new Position(0, ifloat, 0), new Position(Configuration.dimX, ifloat, 0));
			}
		}
		ifloat = save;
		for (int i = (int) ifloat; i < Configuration.dimX - gridSize; i += gridSize) {
			ifloat += gridSize;
			if (c == Color.red) {
				pt.drawDottedLine(g, new Position(ifloat, 0, 0), new Position(ifloat, Configuration.dimY, 0));
			} else if (c == Color.green) {
				pt.drawBoldLine(g, new Position(ifloat, 0, 0), new Position(ifloat, Configuration.dimY, 0), 2);
			} else {
				pt.drawLine(g, new Position(ifloat, 0, 0), new Position(ifloat, Configuration.dimY, 0));
			}
		}
	}

	/**
	 * Draw the edge from Node v to Node u (and vice versa from Node u to node v) in Color color
	 * 
	 * @param v
	 * @param u
	 * @param color
	 */
	public static void drawEdge(Node v, Node u, Color color) {
		// there is no function to get directly a Edge (v, u) therefore
		// we need the workaround to iterate over all edges until we find the
		// right one
		for (Edge e : v.outgoingConnections) {
			if (e.endNode.equals(u)) {
				e.defaultColor = color;
				e.oppositeEdge.defaultColor = color;
				return;
			}
		}
		// if there is no edge between v and u, we get here
//		logger.logln(LogL.WARNING, "There is no Edge between Node with ID " + v.ID + " and Node with ID " + u.ID + " that could be paint.");
	}

	/**
	 * Draw all Edges of 'srcNode' to connected nodes in 'nodes' with Color 'color'
	 * @param <T> type of nodes
	 * 
	 * @param srcNode
	 * @param nodes
	 * @param color
	 */
	public static void drawEdges(Node srcNode, Iterable<? extends Node> nodes, Color color) {
		for (Node v : nodes) {
			drawEdge(srcNode, v, color);
		}
		if (Tools.isSimulationInGuiMode()) {
			Tools.repaintGUI();
		}
	} // end drawConnections

	@AbstractCustomGlobal.GlobalMethod(
			menuText = "Quickload Graph from File")
	public void loadGraphFromFile() {
		System.out.println("Trying to load Data from: " + System.getProperty("user.dir"));
		try {
			loadGraphFromFile(System.getProperty("user.dir") + "\\GraphQuickSave.sav");
			logger.logln(LogL.INFO, "Nodes loaded successfully.");
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	@AbstractCustomGlobal.GlobalMethod(
			menuText = "QuickSave Graph to File")
	public void saveCurrentGraphToFile() {
		ArrayList<String> nodes = new ArrayList<String>();
		for (Node n : Tools.getNodeList()) {
			String s = n.getPosition().xCoord + ";" + n.getPosition().yCoord;
			for (Edge e : n.outgoingConnections) {
				s += ";" + e.endNode.ID;
			}
			nodes.add(s);
		}
		try {
			OpenOption[] openOptions = new OpenOption[] { StandardOpenOption.CREATE };

			String path = System.getProperty("user.dir") + "\\GraphQuickSave.sav";
			Utilities.writeToFile(path, nodes, openOptions);
			logger.logln(LogL.INFO, "Wrote all Nodes successfully into " + path);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	private void loadGraphFromFile(String s) throws IOException {
		FileInputStream in = null;
		BufferedReader reader = null;

		try {
			in = new FileInputStream(s);
			reader = new BufferedReader(new FileReader(new File(s)));
			ArrayList<String> lines = new ArrayList<String>();
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
				createNode(line.split(";"));
			}
			int idcount = 1;
			for (String li : lines) {
				createEdges(idcount, li.split(";"));
				idcount++;
			}

		} catch (IOException e) {
			throw new IOException(e.getMessage());
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Tools.repaintGUI();
	}

	private void createEdges(int nodeid, String[] li) {
		Node n = Tools.getNodeByID(nodeid);

		for (int i = 2; i < li.length; i++) {

			n.addConnectionTo(Tools.getNodeByID(Integer.parseInt(li[i])));
		}

	}

	private void createNode(String[] lines) {
		PhysicalGraphNode neu = new PhysicalGraphNode();

		neu.setPosition(new Position(Double.parseDouble(lines[0]), Double.parseDouble(lines[1]), 0));

		/*
		 * try { neu.setConnectivityModel(new StaticQUDG()); } catch (CorruptConfigurationEntryException e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */
		neu.setConnectivityModel(new NoConnectivity());
		neu.setReliabilityModel(new ReliableDelivery());
		neu.setInterferenceModel(new NoInterference());
		neu.setMobilityModel(new NoMobility());
		neu.setColor(Color.blue);
		Tools.getNodeList().addNode(neu);
		neu.init();

	}

	// can be added later if needed
	/*
	 * public void saveNodes(String path) {
	 * 
	 * FileOutputStream out = null; ObjectOutputStream objout = null; try { out = new FileOutputStream(path); objout = new ObjectOutputStream(out); objout.writeObject(Tools.getNodeList()); } catch (FileNotFoundException e) { // TODO Auto-generated catch block e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); } finally { try { objout.close(); } catch (IOException e) { e.printStackTrace(); } try { out.close(); } catch (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); } }
	 * 
	 * }
	 * 
	 * public NodeCollectionInterface readNodes(String path) throws ClassNotFoundException, IOException { FileInputStream in = null; ObjectInputStream objin = null; NodeCollectionInterface nci = null; try { in = new FileInputStream(path); objin = new ObjectInputStream(in); Object o = objin.readObject(); nci = (NodeCollectionInterface) o; } catch (IOException e) { throw new IOException(e.getMessage()); } catch (ClassNotFoundException e) { throw new ClassNotFoundException(e.getMessage()); } finally { try { objin.close(); } catch (IOException e) { e.printStackTrace(); } try { in.close(); } catch (IOException e) { e.printStackTrace(); } } return nci; }
	 */

	@AbstractCustomGlobal.CustomButton(
			buttonText = "DT", toolTipText = "Delaunay Triangulation")
	public void DelaunayTriangulationButton() {
		// Algorithms.clearGraphOfCollinearNodes();
		if (isAlreadyCalculated("DT")) {
			recalculateByCache("DT", Color.darkGray);
			return; // nothing more to do here
		}

		 Set<Node> workingSet = Algorithms.getNodeListCopy();

		HashMap<Node, Set<Node>> delaunay = WangLiSpanner.buildDelaunayTriangulationNew(workingSet);

//		 "boeses", aber schnelles Malen der Kanten zu Testzwecken
		for (Node n1 : workingSet) {
			for (Node n2 : delaunay.get(n1)) {
				if (!n1.outgoingConnections.contains(n1, n2)) {
					n1.addBidirectionalConnectionTo(n2);
				}
			}
			CustomGlobal.drawEdges(n1, workingSet, Color.BLACK);
		}

		// saveGraphInCache("DT");
	}
	
	
	ArrayList<RoutingEntry> last=new ArrayList<RoutingEntry>();
	
	@AbstractCustomGlobal.CustomButton(
			buttonText = "Dijkstra", toolTipText = "Dijkstras algorithm")
	public void dijkstra() {
		
		Set<Node> nodes=new HashSet<Node>();
		
		for(Node n:Tools.getNodeList()){
			nodes.add(n);
		}
		last=Dijkstra.dijkstra(nodes, Tools.getNodeByID(1), Tools.getNodeByID(2));
		System.out.println(last);

	}
	

	
	@AbstractCustomGlobal.CustomButton(buttonText = "WLS", toolTipText = "Wang-Li Spanner")
	public void WangLiButton() {
		// Algorithms.clearGraphOfCollinearNodes();
		if (isAlreadyCalculated("WLS")) {
			recalculateByCache("WLS", Color.WHITE);
			return; // nothing more to do here
		}

		Set<Node> workingSet = Algorithms.getNodeListCopy();
		
		HashMap<Node, Set<Node>> wangLi = WangLiSpanner.buildWangLiSpanner(workingSet);

		// "böses", aber schnelles Malen der Kanten zu Testzwecken
		for (Node n1 : workingSet) {
			for (Node n2 : wangLi.get(n1)) {
				if (!n1.outgoingConnections.contains(n1, n2)) {
					n1.addBidirectionalConnectionTo(n2);
				}
			}
			CustomGlobal.drawEdges(n1, workingSet, Color.BLACK);
		}
		
		// saveGraphInCache("DT");

	}

}
