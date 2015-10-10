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
package projects.rmys;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import projects.defaultProject.models.connectivityModels.StaticQUDG;
import projects.defaultProject.models.connectivityModels.UDG;
import projects.defaultProject.models.interferenceModels.NoInterference;
import projects.defaultProject.models.mobilityModels.NoMobility;
import projects.defaultProject.models.reliabilityModels.ReliableDelivery;
import projects.reactiveSpanner.GraphConnectivity;
import projects.reactiveSpanner.Utilities;
import projects.reactiveSpanner.models.connectivityModels.NoConnectivity;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;
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
	public static boolean showids = true;


	static {
		try {
			R = Configuration.getDoubleParameter("UDG/rMax");
		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}

	

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



	@AbstractCustomGlobal.CustomButton(buttonText = "create example", toolTipText = "creates simple example")
	public void example1() throws CorruptConfigurationEntryException {
		NewPhysicalGraphNode p1 = new NewPhysicalGraphNode();
		p1.setPosition(31, 31, 0);
		p1.setConnectivityModel(new UDG());
		p1.setReliabilityModel(new ReliableDelivery());
		p1.setInterferenceModel(new NoInterference());
		p1.setMobilityModel(new NoMobility());
		p1.setColor(Color.blue);

		NewPhysicalGraphNode p2 = new NewPhysicalGraphNode();
		p2.setPosition(35, 32, 0);
		p2.setConnectivityModel(new UDG());
		p2.setReliabilityModel(new ReliableDelivery());
		p2.setInterferenceModel(new NoInterference());
		p2.setMobilityModel(new NoMobility());
		p2.setColor(Color.blue);

		p1.init();
		p2.init();

		p2.RMYS();
		Tools.getNodeList().addNode(p1);
		Tools.getNodeList().addNode(p2);


		sinalgo.runtime.Runtime.reevaluateConnections();

		Tools.repaintGUI();
	}
	

	@AbstractCustomGlobal.CustomButton(
			buttonText = "UDG", toolTipText = "Unit Disk Graph")
	public void UDGButton() {
		for (SimpleNode v : Utilities.getNodeCollectionByClass(SimpleNode.class)) {
			v.neighborUDG();
		}
		// saveGraphInCache("UDG");
	}

	@AbstractCustomGlobal.CustomButton(buttonText = "IDS", toolTipText = "draws ids in red next to each node")
	public void toogleShowIds() {
		showids = !showids;
		Tools.repaintGUI();
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
			buttonText = "Graph Connectiviy", toolTipText = "Examine if the graph is fully connected")
	public void isGraphConnectedButton() {
		if (GraphConnectivity.isGraphConnected(Tools.getNodeList())) {
			Tools.getTextOutputPrintStream().println("Graph is connected");
		} else {
			Tools.getTextOutputPrintStream().println("Graph is not connected");
		}
	}

	

	@AbstractCustomGlobal.CustomButton(
			buttonText = "Clear Edges", toolTipText = "Clear all edges")
	public void clearAllConnections() {

		for (SimpleNode v : Utilities.getNodeCollectionByClass(SimpleNode.class)) {
			v.outgoingConnections.removeAndFreeAllEdges();
//			v.resetConnectionToThisNode();
		}
		Tools.getGUI().redrawGUI();
	}


	/*
	 * 
	 * 
	 * needed to draw some stuff
	 */
	@Override
	public void customPaint(Graphics g, PositionTransformation pt) {
		if (showids) {
		for (SimpleNode n : Utilities.getNodeCollectionByClass(NewPhysicalGraphNode.class)) {
			g.setColor(Color.red);

			pt.translateToGUIPosition(new Position(Configuration.dimX + 1.0, Configuration.dimY - 0.75, 0));
			final int startX = (int) (pt.guiX - Configuration.dimX * pt.getZoomFactor());
			final int startY = (int) (pt.guiY - Configuration.dimY * pt.getZoomFactor());
			g.drawString("" + n.ID + "", (int) (startX + n.getPosition().xCoord * pt.getZoomFactor()),
					(int) (startY + n.getPosition().yCoord * pt.getZoomFactor()));

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
			logger.logln(LogL.INFO, "Wrote all Nodes successfully to " + path);
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


}
