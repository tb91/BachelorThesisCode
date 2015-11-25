package projects.rmys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.tools.Tool;

import com.sun.xml.internal.ws.dump.LoggingDumpTube.Position;

import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.GraphConnectivity;
import projects.reactiveSpanner.Utilities;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.io.graphFileIO.GraphFileWriter;
import sinalgo.io.positionFile.PositionFileIO;
import sinalgo.nodes.Node;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

/**
 * CustomGlobal class for batch mode. This is a singleton class.
 * 
 * @author Tim
 */
public class CustomGlobalBatch {
	private static Logging logger = null;

	private static double R = -1;
	private static String runLogFile = "";

	static {
		try {
			R = Configuration.getDoubleParameter("UDG/rMax");

		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}

		try {
			runLogFile = Configuration.getStringParameter("runLogFile");
			
		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
		
		logger=Logging.getLogger(runLogFile, true);
		logger.log(LogL.ALWAYS, "Logger is running!");
	}

	private static CustomGlobalBatch instance = null;

	public static CustomGlobalBatch getInstance() {
		if (instance == null) {
			logger.logln(LogL.INFO, "Create CustomGlobalBatch");
			instance = new CustomGlobalBatch();
		}
		return instance;
	}

	/**
	 * Private constructor - this is a singleton implementation
	 */
	private CustomGlobalBatch() {
	}

	// for recording:
	private static int nodeDensity;
	private static boolean finished = false;
	
	private double exitAfterRounds=0;
	private String src = "";
	private int numNodes;
	private String resultsLog;
	

	public boolean hasTerminated() {
		return finished;
	}

	public void preRun() {
		String algorithm = "";
		try {
			algorithm = Configuration.getStringParameter("algorithm/name");
		} catch (CorruptConfigurationEntryException e) {
			Tools.fatalError("Option 'algorithm/name' is missing or in wrong format.");
		}
		if (algorithm.toUpperCase().equals("GENERATEGRAPHS")) {
			System.out.println("Generating nodes!");
			generateNewGraph();
			Tools.exit();
		}else if(algorithm.toUpperCase().equals("EXPERIMENT1")){
			System.out.println("STARTING EXPERIMENT_1");
			
			initGlobalParameters();
			loadNodes(numNodes); //load Nodes from given sourcefile
			
			//===========
			experimentRun1();
			
			
			
		}
			
	}
	public void postRound() {
		if(Global.currentTime >= exitAfterRounds){
			System.out.println("round " +  exitAfterRounds + " was reached.");
			Tools.exit();
		}

	}
	
	public void experimentRun1(){
		//calculate all needed values and save them to a file
		
		HashMap<Node, Set<Node>> udgNeighbors = Algorithms_ext.createUDGNeighborhood();
		
		HashMap<Node, Set<NewPhysicalGraphNode>> rmysNeighbors = Algorithms_ext.createMYSNeighborhood();
		double euklidRatioRMYS=Algorithms_ext.rmysSpan(rmysNeighbors, false);
		int hopRatioRMYS=(int) Algorithms_ext.rmysSpan(rmysNeighbors, true);
		
		HashMap<Node, Set<Node>> pdtNeighbors = Algorithms_ext.createPDTNeighborhood();
		double euklidRatioPDT=Algorithms_ext.PDTSpan(pdtNeighbors , false);
		int hopRatioPDT=(int) Algorithms_ext.PDTSpan(pdtNeighbors, true);
		
		double averageRMYSNeighbors = calculateAverageNeighborsPerNode(rmysNeighbors);
		double averagePDTNeighbors = calculateAverageNeighborsPerNode(pdtNeighbors);
		double averageUDGNeighbors = calculateAverageNeighborsPerNode(udgNeighbors); // should be similar to density
		
		int maximalRMYSNeighbors = calculateMaximalNeighborsPerNode(rmysNeighbors);
		int maximalPDTNeighbors = calculateMaximalNeighborsPerNode(pdtNeighbors);
		
		double neighborsCountRMYSUDGRatio = calculateNeighborsCountGraphUDGRatio(rmysNeighbors, udgNeighbors);
		double neighborsCountPDTUDGRatio = calculateNeighborsCountGraphUDGRatio(pdtNeighbors, udgNeighbors);
		
		ArrayList<String> values= new ArrayList<>();
		values.add(numNodes + "");
		values.add(nodeDensity + "");
		values.add(averageUDGNeighbors + "");
		values.add(euklidRatioRMYS + "");
		values.add(hopRatioRMYS + "");
		values.add(euklidRatioPDT + "");
		values.add(hopRatioPDT + "");
		
		values.add(averageRMYSNeighbors + "");
		values.add(averagePDTNeighbors + "");
		
		values.add(maximalRMYSNeighbors + "");
		values.add(maximalPDTNeighbors + "");
		
		values.add(neighborsCountRMYSUDGRatio + "");
		values.add(neighborsCountPDTUDGRatio + "");
		
		write_data(values);
	}
	
	public static <T extends Node, E extends Node> double calculateNeighborsCountGraphUDGRatio(HashMap<Node, Set<T>> graphNeighborhood,
				HashMap<Node, Set<E>> UDGNeighborhood){
		
		double sum = 0;
		for(Node v: UDGNeighborhood.keySet()){
			sum+=graphNeighborhood.get(v).size()/(1.0*UDGNeighborhood.get(v).size());
		}
		return sum / (1.0 * UDGNeighborhood.keySet().size());
		
	}
	
	public static <T extends Node> double calculateAverageNeighborsPerNode(HashMap<Node, Set<T>> neighborhood){
		double sumNeighbors=0;
		for(Node v: neighborhood.keySet()){
			sumNeighbors+=neighborhood.get(v).size();
		}
		return sumNeighbors / Tools.getNodeList().size();
	}
	
	public static <T extends Node> int calculateMaximalNeighborsPerNode(HashMap<Node, Set<T>> neighborhood){
		int maximum=0;
		for(Node v: neighborhood.keySet()){
			int current=neighborhood.get(v).size();
			if(current > maximum){
				maximum=current;
			}
		}
		return maximum;
	}
	
	public void initGlobalParameters(){
		try {
			src = Configuration.getStringParameter("positionFile/src");
			System.out.println("Found position Fileentry in configuration: " + src);
			
			
			try {
				BufferedReader br = null;
				br = new BufferedReader(new FileReader(new File(src)));
				String line = br.readLine();
				br.close();
				numNodes = Integer.parseInt(line.split(":")[1].trim());
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}		
		} catch (CorruptConfigurationEntryException e) {
			Tools.fatalError("Option 'positionFile/src' is missing or in wrong format.");
		}
		
		try{
			exitAfterRounds= Configuration.getDoubleParameter("exitAfterRounds");
			System.out.println("Exiting after " + exitAfterRounds + " rounds");
		}catch (CorruptConfigurationEntryException e){
			Tools.fatalError("Could not find or read: exitAfterRounds in Configurationfile");
		}
		
		try{
			resultsLog= Configuration.getStringParameter("resultsLog");
			System.out.println("path to log: " + resultsLog);
		}catch (CorruptConfigurationEntryException e){
			Tools.fatalError("Could not find or read: resultsLog in Configurationfile");
		}
		
		nodeDensity = (int) Math.round((Math.PI * R * R / (Configuration.dimX * Configuration.dimY)) * numNodes);
	}
	
	public void loadNodes(int nodeCount){
		Tools.generateNodes(nodeCount, "rmys:NewPhysicalGraphNode", "PositionFile", "("+ src +")");
		Tools.reevaluateConnections();
		System.out.println("Loaded nodes from positionfile: " + src);
	}

	
	public void generateNewGraph(){
		int numNodes = Tools.getNodeList().size();
		nodeDensity = (int) Math.round((Math.PI * R * R / (Configuration.dimX * Configuration.dimY)) * numNodes);
		if (GraphConnectivity.isGraphConnected(Tools.getNodeList())) {
			logger.logln(LogL.INFO, "connected graph found! Saving..");
			String path = resultsLog;
			File dest = new File(path);
			if (!dest.exists()) {
				dest.mkdir();
			}
			int graphid = new File(path).list().length;
			while (new File(path + graphid + ".graph").exists()) {
				graphid++;
			}
			path = path + graphid + ".graph";
			
			PositionFileIO.printPos(path);
			finished = true;

		} else {
			logger.logln(LogL.INFO, "Graph is not connected!");
		}
	}

	public void write_data(ArrayList<String> values) {
		logger.logln(LogL.INFO, "Writing message record to file...");
	
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();

		final char valSep = ' ';
		final String fileExtension = "dat";
				
		StringBuffer line = new StringBuffer();
		/*line.append(nodeDensity);
		line.append(valSep);
		line.append(numNodes);
		line.append(valSep);
		line.append(ratio);
		line.append(valSep);*/
		for(String value:values){
			line.append(value);
			line.append(valSep);
		}

		String filePathString = resultsLog;
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

	public void onExit() {
		System.out.println("exiting!");
	}

}
