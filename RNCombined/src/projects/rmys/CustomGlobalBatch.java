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
import java.util.List;

import javax.tools.Tool;

import com.sun.xml.internal.ws.dump.LoggingDumpTube.Position;

import projects.reactiveSpanner.GraphConnectivity;
import projects.reactiveSpanner.Utilities;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.io.graphFileIO.GraphFileWriter;
import sinalgo.io.positionFile.PositionFileIO;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

/**
 * CustomGlobal class for batch mode. This is a singleton class.
 * 
 * @author Mavs
 */
public class CustomGlobalBatch {
	private static Logging logger = Logging.getLogger();

	private static final String PATHPREFIX = "C:\\Users\\timmy\\bachelorarbeit_code\\RNCombined\\scripts\\graphs\\";
	private static double R = -1;

	static {
		try {
			R = Configuration.getDoubleParameter("UDG/rMax");

		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
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
			generateGraphs();
			Tools.exit();
		}else if(algorithm.toUpperCase().equals("MEASUREMENT")){
			System.out.println("STARTING ALGORITHM MEASURING!");
			try {
				src = Configuration.getStringParameter("positionFile/src");
				System.out.println("Found position File: " + src);
				
				numNodes = 0;
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
				
				
				Tools.generateNodes(numNodes, "rmys:NewPhysicalGraphNode", "PositionFile", "("+ src +")");
				Tools.reevaluateConnections();
				
				System.out.println("Loaded nodes from positionfile: " + src);
				
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
			
			//===========
			
			double ratio=Algorithms_ext.rmysSpan(false);
			System.out.println(ratio);
		}
			
	}

	public void postRound() {
		if(Global.currentTime >= exitAfterRounds){
			System.out.println("round " +  exitAfterRounds + " was reached.");
			Tools.exit();
		}

	}
	
	public void generateGraphs(){
		int numNodes = Tools.getNodeList().size();
		nodeDensity = (int) Math.round((Math.PI * R * R / (Configuration.dimX * Configuration.dimY)) * numNodes);
		if (GraphConnectivity.isGraphConnected(Tools.getNodeList())) {
			logger.logln(LogL.INFO, "connected graph found! Saving..");
			String path = PATHPREFIX + "\\Dens" + nodeDensity + "\\";
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

	public void onExit() {
		System.out.println("exiting!");
	}

}
