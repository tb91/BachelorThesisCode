package projects.reactiveSpanner;

import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import projects.reactiveSpanner.FloydWarshall.FloydWarshall;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.Barriere.BarriereMessageHandler;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.record.MessageRecord;
import projects.reactiveSpanner.routing.RoutingProtocol;
import projects.reactiveSpanner.routing.RoutingProtocol.ERouting;
import projects.reactiveSpanner.routing.RoutingProtocol.ERoutingState;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.io.positionFile.PositionFileIO;
import sinalgo.models.DistributionModel;
import sinalgo.models.Model;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.Distribution;

/**
 * CustomGlobal class for batch mode.
 * This is a singleton class.
 * 
 * @author Mavs
 */
public class CustomGlobalBatch
{
	private static Logging logger = Logging.getLogger();
	
	private static double R = -1;
	private static double rMin = -1;
	private static double rMax = -1;
	private static String activeAlgorithms = null; 
	private static double connectionProbability=0.0;
	static {
		try {
			activeAlgorithms = Configuration.getStringParameter("BatchMode/ActiveAlgorithms");
			R = Configuration.getDoubleParameter("UDG/rMax");
			rMin = Configuration.getDoubleParameter("QUDG/rMin");
			rMax = Configuration.getDoubleParameter("QUDG/rMax");
			connectionProbability = Configuration.getDoubleParameter("QUDG/connectionProbability");
			
		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}
	
	private static CustomGlobalBatch instance = null;
	public static CustomGlobalBatch getInstance() {
		if(instance == null) {
			logger.logln(LogL.INFO, "Create CustomGlobalBatch");
			instance = new CustomGlobalBatch();
		}
		return instance;
	}
	
	/** 
	 * Private constructor - this is a singleton implementation
	 */
	private CustomGlobalBatch(){}

	// for recording:
	private static int nodeDensity;
	private static PhysicalGraphNode forwarder;
	
	//reactive calculations
	private static Set<Node> neighborsGG;
	private static Set<Node> neighborsReactivePDT;
	private static SubgraphStrategy BFP;
	private static SubgraphStrategy reactivePDT;
	
	//barrier
	private static SubgraphStrategy barriere;
	private static SubgraphStrategy barriereExt;
	
	
	//routing
	private static PhysicalGraphNode destination;
	private static RoutingProtocol<PhysicalGraphNode> faceRouting_GG;
	private static RoutingProtocol<PhysicalGraphNode> faceRouting_PDT;
	private static RoutingProtocol<PhysicalGraphNode> GreedyFaceRouting_GG;
	private static RoutingProtocol<PhysicalGraphNode> GreedyFaceRouting_PDT;
	
	private static double PDTSpan = -1;
	private static double GGSpan = -1;
	
	public boolean hasTerminated()
	{
		return false;
	}
	
	public void preRun()
	{
//		PDTGGSpanPreRun();
		barrierPreRun();
//		routingPreRun();
	}

	public void postRound()
	{
//		PDTGGSpanPostRound();
		barrierPostRound();
//		routingPostRound();
	}

	public void onExit()
	{		
//		PDTGGSpanOnExit();
		barrierOnExit();
//		routingOnExit();
	}
	
	/**
	 * writes a minimal record for barriere and barriere extended graph
	 * with density and max hops over virtual edges in barriere and barriere extended
	 * 
	 * @param density density of the nodes in the graph
	 * @param barriereMaxHop maximum physical hops over virtual edges for barriere
	 * @param barriereExtMaxHop maximum physical hops over virtual edges for barriereExt
	 */
	public static void write_barriere_minimal_Record(int density, int barriereMaxHop, int barriereExtMaxHop){
		logger.logln(LogL.INFO, "Writing message to record file...");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();

		final char valSep = ' ';
		final String fileExtension = "dat";

		StringBuffer line = new StringBuffer();
		line.append(density);
		line.append(valSep);
		line.append(barriereMaxHop);
		line.append(valSep);
		line.append(barriereExtMaxHop);
		
		String filePathString = dateFormat.format(date) + "-record" + '.' + fileExtension;
		OpenOption[] openOptions = new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.APPEND };
		List<String> lines = new ArrayList<String>();
		lines.add(line.toString());

		try {
			Utilities.writeToFile(filePathString, lines, openOptions);
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.logln(LogL.INFO, "Saved message successfully to file " + filePathString);
	}
	
	/**
	 * writes full record for barriere and barriere extended graphs
	 * 
	 * @param density density of the nodes in graph
	 * @param numNodes actual number of nodes in graph
	 * @param spanningRatioVirtual spanning ratio of barriere with standard virtual connections
	 * @param spanningRatioShortest spanning ratio of barriere with shortest path between neighbours with virtual edge
	 * @param spanningRatioVirtualExt spanning ratio of barriereExt with standard virtual connections
	 * @param spanningRatioShortestExt spanning ratio of barriereExt with shortest path between neighbours with virtual edge
	 * @param barriereRecord message record of barriere graph
	 * @param barriereExtRecord message record of barriereExt graph
	 * @param isSymetric symetry property of barriere graph (a-->b ==> b-->a)
	 * @param isSymetricExt symetry property of barriereExt graph (a-->b ==> b-->a)
	 * @param seeed the seed from sinalgo for the randomisation of node positions
	 */
	public static void write_barriere_Record(int density, int numNodes, double spanningRatioVirtual, double spanningRatioShortest, double spanningRatioVirtualExt, double spanningRatioShortestExt, MessageRecord barriereRecord, MessageRecord barriereExtRecord,int maxHop, int maxHopExt, double spanningRatioImprovement, boolean isSymetric, boolean isSymetricExt, long seeed) {
		logger.logln(LogL.INFO, "Writing message record to file...");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();

		final char valSep = ' ';
		final String fileExtension = "dat";

		StringBuffer line = new StringBuffer();
		line.append(density);
		line.append(valSep);
		line.append(numNodes);
		line.append(valSep);
		line.append(spanningRatioVirtual);
		line.append(valSep);
		line.append(spanningRatioShortest);
		line.append(valSep);
		line.append(spanningRatioVirtualExt);
		line.append(valSep);
		line.append(spanningRatioShortestExt);
		line.append(valSep);
		line.append(barriereRecord.toRecord(valSep));
		line.append(valSep);
		line.append(barriereExtRecord.toRecord(valSep));
		line.append(valSep);
		line.append(maxHop);
		line.append(valSep);
		line.append(maxHopExt);
		line.append(valSep);
		line.append(spanningRatioImprovement);
		line.append(valSep);
		line.append(isSymetric);
		line.append(valSep);
		line.append(isSymetricExt);
		line.append(valSep);
		line.append(seeed);

		String filePathString = dateFormat.format(date) + "-record" + '.' + fileExtension;
		OpenOption[] openOptions = new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.APPEND };
		List<String> lines = new ArrayList<String>();
		lines.add(line.toString());

		try {
			Utilities.writeToFile(filePathString, lines, openOptions);
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.logln(LogL.INFO, "Saved message successfully to file " + filePathString);
	}
	
	/**
	 * writes a record for spanning ratios of Gabriel Graph and Partial Delaunay Triangulation in respect
	 * to the underlying Unit Disc Graph.
	 * 
	 * @param density density of the nodes in graph
	 * @param numNodes actual number of nodes in graph
	 * @param GGSpan spanning ratio of GG to UDG
	 * @param PDTSpan spanning ratio of PDT to UDG
	 */
	public static void write_GG_PDT_Record(final int density, final int numNodes, double GGSpan, double PDTSpan){
		logger.logln(LogL.INFO, "Writing message record to file...");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();

		final char valSep = ' ';
		final String fileExtension = "dat";

		StringBuffer line = new StringBuffer();
		line.append(density);
		line.append(valSep);
		line.append(numNodes);
		line.append(valSep);
		line.append(GGSpan);
		line.append(valSep);
		line.append(PDTSpan);

		String filePathString = dateFormat.format(date) + "-record" + '.' + fileExtension;
		OpenOption[] openOptions = new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.APPEND };
		List<String> lines = new ArrayList<String>();
		lines.add(line.toString());

		try {
			Utilities.writeToFile(filePathString, lines, openOptions);
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.logln(LogL.INFO, "Saved message successfully to file " + filePathString);
	}

	public static void write_BFP_reactivePDT_Record(final int density, final int numNodes, final int numUDGneighbors, 
			final int numGGneighbors, final int numPDTneighbors, 
			final MessageRecord bfpMsgRec, final MessageRecord rPDTMsgRec) {
		logger.logln(LogL.INFO, "Writing message record to file...");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();

		final char valSep = ',';
		final String fileExtension = "csv";

		StringBuffer line = new StringBuffer();
		line.append(density);
		line.append(valSep);
		line.append(numNodes);
		line.append(valSep);
		line.append(numUDGneighbors);
		line.append(valSep);
		line.append(numGGneighbors);
		line.append(valSep);
		line.append(numPDTneighbors);
		line.append(valSep);
		line.append(bfpMsgRec.toRecord(valSep));
		line.append(valSep);
		line.append(rPDTMsgRec.toRecord(valSep));
		line.append(valSep);
		line.append((new Long(Distribution.getSeed()).toString()));
		line.append(valSep);
		line.append(forwarder.toString());

		String filePathString = dateFormat.format(date) + "-record" + '.' + fileExtension;
		OpenOption[] openOptions = new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.APPEND };
		List<String> lines = new ArrayList<String>();
		lines.add(line.toString());

		try {
			Utilities.writeToFile(filePathString, lines, openOptions);
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.logln(LogL.INFO, "Saved message successfully to file " + filePathString);
	}
	
	public static void write_routing_Record(final int density, final int numNodes, final double euclDistance, 
			final double minEuclDis_UDG, final int minNumHops_UDG, 
			final double minEuclDis_GG, final int minNumHops_GG, 
			final double minEuclDis_PDT, final int minNumHops_PDT, 
			final double euclDisFaceR_GG, final int hopsFaceR_GG, 
			final double euclDisFaceR_PDT, final int hopsFaceR_PDT,
			final double euclDisGFG_GG, final int hopsGFG_GG, 
			final double euclDisGFG_PDT, final int hopsGFG_PDT)
	{
		logger.logln(LogL.INFO, "Writing message record to file...");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();

		final char valSep = ',';
		final String fileExtension = "csv";

		StringBuffer line = new StringBuffer();
		line.append(density);
		line.append(valSep);
		line.append(numNodes);
		line.append(valSep);
		line.append(euclDistance);
		line.append(valSep);
		line.append(minEuclDis_UDG);
		line.append(valSep);
		line.append(minNumHops_UDG);
		line.append(valSep);
		line.append(minEuclDis_GG);
		line.append(valSep);
		line.append(minNumHops_GG);
		line.append(valSep);
		line.append(minEuclDis_PDT);
		line.append(valSep);
		line.append(minNumHops_PDT);
		line.append(valSep);
		line.append(euclDisFaceR_GG);
		line.append(valSep);
		line.append(hopsFaceR_GG);
		line.append(valSep);
		line.append(euclDisFaceR_PDT);
		line.append(valSep);
		line.append(hopsFaceR_PDT);
		line.append(valSep);
		line.append(euclDisGFG_GG);
		line.append(valSep);
		line.append(hopsGFG_GG);
		line.append(valSep);
		line.append(euclDisGFG_PDT);
		line.append(valSep);
		line.append(hopsGFG_PDT);
		line.append(valSep);
		line.append(forwarder.toString());
		line.append(valSep);
		line.append(destination.toString());
		line.append(valSep);
		line.append((new Long(Distribution.getSeed()).toString()));
		
//		String filePathString = dateFormat.format(date) + "-Record-routing-dist" + Math.round(euclDistance) + '-' + density + '.' + fileExtension;
		String filePathString = dateFormat.format(date) + "-Record-routing-dist" + Math.round(euclDistance) + '.' + fileExtension;
		OpenOption[] openOptions = new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.APPEND };
		List<String> lines = new ArrayList<String>();
		lines.add(line.toString());

		try {
			Utilities.writeToFile(filePathString, lines, openOptions);
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.logln(LogL.INFO, "Saved message successfully to file " + filePathString);
	}
	
	public int getMaxHopBarriere(UUID tcID){
		int maxHops = 0;
		
		try {
			for (PhysicalGraphNode p : Utilities.getNodeCollectionByClass(PhysicalGraphNode.class)) {
				// unsafe cast --> try catch
				BarriereMessageHandler bmh = (BarriereMessageHandler) p.getMessageHandler(tcID);
				
				int currentHop = bmh.getMaxHop();
				
				if(currentHop>maxHops)
					maxHops=currentHop;
			}
		} catch (Exception e) {
			System.out.println("No barriere message handler last!");
		}
		
		return maxHops;
	}
	
	private void barrierPreRun()
	{
		
		System.out.println(Tools.getRandomNode().getConnectivityModel());
//		int A = Configuration.dimX * Configuration.dimY;
		int numNodes = Tools.getNodeList().size();
//		int numNodes = (int) (density * A / R*R*Math.PI);

		//nodeDensity = (int) Math.round(((double)numNodes) / ((double)Configuration.dimX)*((double)Configuration.dimY) * (Math.PI*rMin*rMin + (connectionProbability*(Math.PI*rMax*rMax-Math.PI*rMin*rMin))));
		// assuming rMin rMax relation is 1/sqrt(2)
		nodeDensity = (int) (((double)numNodes) / (Configuration.dimX*Configuration.dimY) * (1+connectionProbability)*(Math.PI*Math.pow(rMin, 2)));
		logger.logln(LogL.INFO, "Examine connectiviy of graph with " + numNodes + " nodes...");
		
		while(!GraphConnectivity.isGraphConnected(Tools.getNodeList())){
			Tools.removeAllNodes();
			Tools.generateNodes(numNodes, "reactiveSpanner:PhysicalGraphNode", "Random");
		}
		
		/*try {
			while(!GraphConnectivity.isGraphConnected(Tools.getNodeList())){
				Utilities.spreadPositionsByDistributionModel(Model.getDistributionModelInstance("Random"), Tools.getNodeList());
			}*/
			/*Set<Node> unconnectedNodes;
			Node randomNode = Tools.getRandomNode();
			DistributionModel randomSpread = Model.getDistributionModelInstance("Random");
			do{
				unconnectedNodes = GraphConnectivity.getUnconnectedNodes(randomNode, Tools.getNodeList());
				System.out.println("unconnected"+unconnectedNodes);
				//special handling if the only node that is not connected is the destination
				for(Node n: unconnectedNodes)
				{
					n.setPosition(randomSpread.getNextPosition());
				}
			}
			while(!unconnectedNodes.isEmpty());
//			Utilities.satisfyGraphConditions(Tools.getNodeList());
		} catch (Exception e) {
//			CorruptConfigurationEntryException
			e.printStackTrace();
		}*/


		forwarder = Utilities.getRandomNodeWithinGraphBorders(PhysicalGraphNode.class, Tools.getNodeList());
		//reactive comparison
		//forwarder = Utilities.getRandomNodeWithinGraphBorders(PhysicalGraphNode.class, Tools.getNodeList());

		//barrier
		barriere = forwarder.startBarriere();
		barriereExt = forwarder.startBarriereExt();
	}
	
	private void barrierPostRound()
	{
		if (barriere.hasTerminated() && barriereExt.hasTerminated()) {
		Tools.exit();
		}
		//Tools.exit();
	}
	
	private void barrierOnExit()
	{
		Algorithms.makeContinuousIDS();
		
		int numNodes = Tools.getNodeList().size();
		UUID barriereID = barriere.getTopologyControlID();
		UUID barriereExtID = barriereExt.getTopologyControlID();
		
		// true for hops as distances, false for euclidean distances
		FloydWarshall physicalGraph = new FloydWarshall(Algorithms.getAdjMatrixEdgeWeightedDigraphBarrierePhysicalGraph(barriereID, true));
		
		double spanningRatioVirtual = Algorithms.spanningRatio(physicalGraph, new FloydWarshall(Algorithms.getAdjMatrixEdgeWeightedDigraphBarriereSubgraphActualVirtual(barriereID, true)), numNodes);
		double spanningRatioShortest = Algorithms.spanningRatio(physicalGraph, new FloydWarshall(Algorithms.getAdjMatrixEdgeWeightedDigraphBarriereSubgraphShortestVirtual(barriereID, physicalGraph, true)), numNodes);
		double spanningRatioVirtualExt = Algorithms.spanningRatio(physicalGraph, new FloydWarshall(Algorithms.getAdjMatrixEdgeWeightedDigraphBarriereSubgraphActualVirtual(barriereExtID, true)), numNodes);
		double spanningRatioShortestExt = Algorithms.spanningRatio(physicalGraph, new FloydWarshall(Algorithms.getAdjMatrixEdgeWeightedDigraphBarriereSubgraphShortestVirtual(barriereExtID, physicalGraph, true)), numNodes);
		
		MessageRecord barriereRecord = forwarder.getMessageHandler(barriereID).getCurrentMessageRecord();
		MessageRecord barriereExtRecord = forwarder.getMessageHandler(barriereExtID).getCurrentMessageRecord();
		
		boolean isSymetric = CustomGlobal.checkBarriereSymetry(barriereID);
		boolean isSymetricExt = CustomGlobal.checkBarriereSymetry(barriereExtID);
		
		long seeed = Distribution.getSeed();
		
		//barriere
		int barriereMaxHop = getMaxHopBarriere(barriereID);
		int barriereExtMaxHop = getMaxHopBarriere(barriereExtID);
		
		double spanningRatioImprovement = (spanningRatioVirtual-spanningRatioVirtualExt)/spanningRatioVirtualExt*100;
//		
//		write_barriere_minimal_Record(nodeDensity, barriereMaxHop, barriereExtMaxHop);
		//write_GG_PDT_Record(nodeDensity, numNodes, GGSpan, PDTSpan);
		
		//int density, int numNodes, double spanningRatioVirtual, double spanningRatioShortest, double spanningRatioVirtualExt, double spanningRatioShortestExt, MessageRecord barriereRecord, MessageRecord barriereExtRecord, boolean isSymetric, boolean isSymetricExt, long seeed
		write_barriere_Record(nodeDensity, numNodes, spanningRatioVirtual, spanningRatioShortest, spanningRatioVirtualExt, spanningRatioShortestExt, barriereRecord, barriereExtRecord,barriereMaxHop, barriereExtMaxHop, spanningRatioImprovement, isSymetric, isSymetricExt, seeed);
	}
	
	private void PDTGGSpanPreRun(){
		int numNodes = Tools.getNodeList().size();
		nodeDensity = (int) Math.round((Math.PI * R * R / (Configuration.dimX * Configuration.dimY)) * numNodes);
		
		GGSpan = Algorithms.GGSpan();
		PDTSpan = Algorithms.PDTSpan();
	}
	
	private void PDTGGSpanPostRound(){
		Algorithms.makeContinuousIDS();
		int numNodes = Tools.getNodeList().size();
		write_GG_PDT_Record(nodeDensity, numNodes, GGSpan, PDTSpan);
	}
	
	private void PDTGGSpanOnExit(){
		Tools.exit();
	}
	
	private void routingPreRun()
	{
		assert(R > 0);
		int numNodes = Tools.getNodeList().size();
		nodeDensity = (int) Math.round((Math.PI * R * R / (Configuration.dimX * Configuration.dimY)) * numNodes);
	
		logger.logln(LogL.INFO, "Examine connectiviy of graph with " + numNodes + " nodes...");

		final int numNodesWithoutFixedOnes = numNodes - 2;
		List<PhysicalGraphNode> fixedNodes = new ArrayList<PhysicalGraphNode>();
		List<PhysicalGraphNode> randomNodes = new ArrayList<PhysicalGraphNode>();
		//find the fixed nodes (they were added last, so they have to be at the end of the list)
		for(int i = numNodesWithoutFixedOnes +1; i <= Tools.getNodeList().size(); i++)
		{
			fixedNodes.add((PhysicalGraphNode) Tools.getNodeByID(i));
		}
		for(int i = 0; i < Tools.getNodeList().size() - 2; i++)
		{
			randomNodes.add((PhysicalGraphNode) Tools.getNodeByID(i+1));
		}
		
		forwarder = fixedNodes.get(0);
		destination = fixedNodes.get(1);
		
		logger.logln(LogL.INFO, "Generating new graphs until a connected graph has been found..");
		DistributionModel randomSpread = Model.getDistributionModelInstance("Random");
		
		Set<Node> unconnectedNodes;
		//we observe the connectivity of the graph here and spread those nodes that are not connected to the forwarder again
		//until the complete graph is connected
		do{
			unconnectedNodes = GraphConnectivity.getUnconnectedNodes(forwarder, Tools.getNodeList());
			//special handling if the only node that is not connected is the destination
			if(unconnectedNodes.remove(destination) && unconnectedNodes.isEmpty())
			{
				Utilities.spreadPositionsByDistributionModel(randomSpread, randomNodes);
				unconnectedNodes.add(randomNodes.get(0)); // add any node, so the break condition of the loop will not be fulfilled
				continue;
			}
			for(Node n: unconnectedNodes)
			{
				n.setPosition(randomSpread.getNextPosition());
			}
		}
		while(!unconnectedNodes.isEmpty());

		faceRouting_GG = forwarder.sendRoutingMsg(destination, EStrategy.GG, ERouting.FACE_ROUTING);
		faceRouting_PDT = forwarder.sendRoutingMsg(destination, EStrategy.PDT, ERouting.FACE_ROUTING);
		GreedyFaceRouting_GG = forwarder.sendRoutingMsg(destination, EStrategy.GG, ERouting.GREEDY_FACE);
		GreedyFaceRouting_PDT = forwarder.sendRoutingMsg(destination, EStrategy.PDT, ERouting.GREEDY_FACE);
	}
	
	private void routingPostRound()
	{
		if(Tools.getGlobalTime() > 15000)
		{
			logger.logln(LogL.ERROR_DETAIL, "Maximum number of " + 15000 + " rounds reached!");
			processStuckedRouting();
		}
		//routing
		if (faceRouting_GG.getCurrentState().equals(ERoutingState.FINISHED) && GreedyFaceRouting_GG.getCurrentState().equals(ERoutingState.FINISHED) &&
				faceRouting_PDT.getCurrentState().equals(ERoutingState.FINISHED) && GreedyFaceRouting_PDT.getCurrentState().equals(ERoutingState.FINISHED)) {
			Tools.exit();
		} else if (faceRouting_GG.getCurrentState().equals(ERoutingState.STUCKED) || GreedyFaceRouting_GG.getCurrentState().equals(ERoutingState.STUCKED) ||
				faceRouting_PDT.getCurrentState().equals(ERoutingState.STUCKED) || GreedyFaceRouting_PDT.getCurrentState().equals(ERoutingState.STUCKED))
		{
			processStuckedRouting();
		}	
	}
	
	private void routingOnExit()
	{	
		final FloydWarshall fw_UDG_hopD = Algorithms.generateFloydWarshall(Algorithms.getAdjMatrixEdgeWeightedDigraph(EStrategy.UDG, true));
		final FloydWarshall fw_UDG_euclD = Algorithms.generateFloydWarshall(Algorithms.getAdjMatrixEdgeWeightedDigraph(EStrategy.UDG, false));
		final FloydWarshall fw_GG_hopD = Algorithms.generateFloydWarshall(Algorithms.getAdjMatrixEdgeWeightedDigraph(EStrategy.GG, true));
		final FloydWarshall fw_GG_euclD = Algorithms.generateFloydWarshall(Algorithms.getAdjMatrixEdgeWeightedDigraph(EStrategy.GG, false));
		final FloydWarshall fw_PDT_hopD = Algorithms.generateFloydWarshall(Algorithms.getAdjMatrixEdgeWeightedDigraph(EStrategy.PDT, true));
		final FloydWarshall fw_PDT_euclD = Algorithms.generateFloydWarshall(Algorithms.getAdjMatrixEdgeWeightedDigraph(EStrategy.PDT, false));
	
		final double euclDis = forwarder.getPosition().distanceTo(destination.getPosition());
		
		final int forwarderIDinFloydW = forwarder.ID -1;
		final int destinationIDinFloydW = destination.ID -1;
		
		final double minEuclDis_UDG = fw_UDG_euclD.dist(forwarderIDinFloydW, destinationIDinFloydW);
		final int minHopDis_UDG = (int) Math.round(fw_UDG_hopD.dist(forwarderIDinFloydW, destinationIDinFloydW));
		final double minEuclDis_GG = fw_GG_euclD.dist(forwarderIDinFloydW, destinationIDinFloydW);
		final int minHopDis_GG = (int) Math.round(fw_GG_hopD.dist(forwarderIDinFloydW, destinationIDinFloydW));
		final double minEuclDis_PDT = fw_PDT_euclD.dist(forwarderIDinFloydW, destinationIDinFloydW);
		final int minHopDis_PDT = (int) Math.round(fw_PDT_hopD.dist(forwarderIDinFloydW, destinationIDinFloydW));
		write_routing_Record(nodeDensity, Tools.getNodeList().size(), euclDis, 
				minEuclDis_UDG, minHopDis_UDG, 
				minEuclDis_GG, minHopDis_GG, 
				minEuclDis_PDT, minHopDis_PDT, 
				faceRouting_GG.getTraveledDistance(), faceRouting_GG.getNumberOfHops(), 
				faceRouting_PDT.getTraveledDistance(), faceRouting_PDT.getNumberOfHops(), 
				GreedyFaceRouting_GG.getTraveledDistance(), GreedyFaceRouting_GG.getNumberOfHops(), 
				GreedyFaceRouting_PDT.getTraveledDistance(), GreedyFaceRouting_PDT.getNumberOfHops());
	}
	
	private void processStuckedRouting()
	{
		logger.logln(LogL.WARNING, "Routing stucked. This seems due to an miscalculation of the routing path. Selecting new forwarder and destination node and repeating.");
	
		faceRouting_GG.cancelProcessing();
		faceRouting_PDT.cancelProcessing();
		GreedyFaceRouting_GG.cancelProcessing();
		GreedyFaceRouting_PDT.cancelProcessing();
		
		
		PositionFileIO.printPos("stuckedGraph.sav");
		Tools.showMessageDialog("STUCKED!");
		routingPreRun();
		
//		forwarder = Utilities.getRandomNode(PhysicalGraphNode.class, Tools.getNodeList());
//		do{
//			destination = Utilities.getRandomNode(PhysicalGraphNode.class, Tools.getNodeList());
//		} while(forwarder.equals(destination)); //forwarder have to differ from the destination
//			
//		faceRouting_GG = forwarder.sendRoutingMsg(destination, EStrategy.GG, ERouting.FACE_ROUTING);
//		faceRouting_PDT = forwarder.sendRoutingMsg(destination, EStrategy.PDT, ERouting.FACE_ROUTING);
//		GFGRouting_GG = forwarder.sendRoutingMsg(destination, EStrategy.GG, ERouting.GREEDY_FACE);
//		GFGRouting_PDT = forwarder.sendRoutingMsg(destination, EStrategy.PDT, ERouting.GREEDY_FACE);
	}
}
