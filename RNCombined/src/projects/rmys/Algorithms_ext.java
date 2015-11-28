package projects.rmys;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import projects.reactiveSpanner.Algorithms;
import projects.reactiveSpanner.FloydWarshall.AdjMatrixEdgeWeightedDigraph;
import projects.reactiveSpanner.FloydWarshall.DirectedEdge;
import projects.reactiveSpanner.FloydWarshall.FloydWarshall;
import projects.rmys.nodes.messageHandler.RMYS;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;
import sinalgo.tools.statistics.Distribution;


public class Algorithms_ext {

	private static Logger logger = null;
	private static String runLogFile;
	
	private static boolean inbatchmode;
	
	static {
		
		try {
				inbatchmode = Configuration.getBooleanParameter("RMYS/batchmode");
			} catch (CorruptConfigurationEntryException e) {
				Tools.fatalError("Option 'RMYS/batchmode' is missing or in wrong format.");
			}
		
		try {
			runLogFile = Configuration.getStringParameter("RMYS/runLogFile");
			if(!runLogFile.isEmpty()){
				logger=Algorithms_ext.getLogger(runLogFile);
				
			}else{
				logger=Algorithms_ext.getLogger();
			}
			
		} catch (CorruptConfigurationEntryException e) {
			logger=Algorithms_ext.getLogger();
		}
		
		
	}

	
	public static double rmysSpan(boolean hopdistance) {
		return rmysSpan(createMYSNeighborhood(), hopdistance);
	}
	
	public static double rmysSpan(HashMap<Node, Set<NewPhysicalGraphNode>> graph, boolean hopdistance){
		AdjMatrixEdgeWeightedDigraph UDGMatrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size());
		AdjMatrixEdgeWeightedDigraph RMYSMatrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size());

	
		HashMap<Node, Set<NewPhysicalGraphNode>> completeRMYSGraph = createMYSNeighborhood();

		for (Node n : Tools.getNodeList()) {
			Collection<Node> neighborhood = Algorithms.getNeighborNodes(n, Tools.getNodeList());

			for (Node v : neighborhood) {
				DirectedEdge de = null;
				// add edge UDG
				// ID starts with 1, matrix with index 0
				if (hopdistance) {
					de = new DirectedEdge(n.ID - 1, v.ID - 1, 1);
				} else {
					de = new DirectedEdge(n.ID - 1, v.ID - 1, n.getPosition().distanceTo(v.getPosition()));
				}

				UDGMatrix.addEdge(de);
			}

			for (Node v : completeRMYSGraph.get(n)) {
				DirectedEdge de = null;
				// add edge GG
				// ID starts with 1, matrix with index 0
				if (hopdistance) {
					de = new DirectedEdge(n.ID - 1, v.ID - 1, 1);
				} else {
					de = new DirectedEdge(n.ID - 1, v.ID - 1, n.getPosition().distanceTo(v.getPosition()));
				}
				RMYSMatrix.addEdge(de);

			}

		}

		int V = UDGMatrix.V();

		FloydWarshall UDGfw = new FloydWarshall(UDGMatrix);
		FloydWarshall RMYSfw = new FloydWarshall(RMYSMatrix);

		double ratio = Algorithms.spanningRatio(UDGfw, RMYSfw, V);
		return ratio;
	}

	private static Set<NewPhysicalGraphNode> buildRMYS(Set<NewPhysicalGraphNode> neighborhood, NewPhysicalGraphNode n) {
		return RMYS.calculateMYS((NewPhysicalGraphNode) n, neighborhood);

	}

	public static HashMap<Node, Set<NewPhysicalGraphNode>> createMYSNeighborhood() {
		HashMap<Node, Set<NewPhysicalGraphNode>> completeRMYSGraph = new HashMap<>();

		for (Node n : Tools.getNodeList()) {
			Collection<Node> neighborhood = Algorithms.getNeighborNodes(n, Tools.getNodeList());
			Set<Node> PDTNodes = Algorithms.buildPartialDelaunayTriangulation(neighborhood, n);
			Set<NewPhysicalGraphNode> castedPDTNodes = new HashSet<>();

			for (Node p : PDTNodes) {
				if (p instanceof NewPhysicalGraphNode) {
					castedPDTNodes.add((NewPhysicalGraphNode) p);
				}
			}

			Set<NewPhysicalGraphNode> RMYSNodes = buildRMYS(castedPDTNodes, (NewPhysicalGraphNode) n);
			completeRMYSGraph.put(n, RMYSNodes);
		}

		// ensure bidrectional edges
		for (Node n : completeRMYSGraph.keySet()) {
			for (Node p : completeRMYSGraph.get(n)) {
				if (!completeRMYSGraph.get(p).contains(n)) {
					String s=n.toString() + " has a unidirectional edge to " + p.toString() + " " + n.toString() + ": "
							+ completeRMYSGraph.get(n) + " " + p.toString() + ": " + completeRMYSGraph.get(p)
							+ '\n' + "Values may be inaccurate! \nSeed is: " + Distribution.getSeed();
					if(CustomGlobalBatch.inbatchmode){
						String src="";
						try {
							src = Configuration.getStringParameter("positionFile/src");
						} catch (CorruptConfigurationEntryException e) {
							logger.log(Level.SEVERE, e.getMessage());
						}
						s+="\nFile is: " + src; 
					}
					
					logger.log(Level.SEVERE,s);
				}
			}
		}
		return completeRMYSGraph;
	}
	
	//====================UDG CREATION===========
	public static HashMap<Node, Set<Node>> createUDGNeighborhood(){
		HashMap<Node, Set<Node>> udgNeighbors = new HashMap<>();
		
		for(Node n : Tools.getNodeList()){
			udgNeighbors.put(n, Algorithms.getNeighborNodes(n, Tools.getNodeList()));
		}
		return udgNeighbors;
	}
	
	
	//====================PDT CREATION===========
	
	public static double PDTSpan(HashMap<Node, Set<Node>> graph, boolean hopdistance){
		AdjMatrixEdgeWeightedDigraph UDGMatrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size()+1);
		AdjMatrixEdgeWeightedDigraph PDTMatrix = new AdjMatrixEdgeWeightedDigraph(Tools.getNodeList().size()+1);
		
		HashMap<Node, Set<Node>> completePDTGraph = createPDTNeighborhood();
		
		for(Node n : Tools.getNodeList()){
			Collection<Node> neighborhood = Algorithms.getNeighborNodes(n, Tools.getNodeList());
			
			for(Node v: neighborhood)
			{
				//add edge UDG
				DirectedEdge de;
				if(hopdistance){
					de = new DirectedEdge(n.ID, v.ID, 1);
				}else{
					de = new DirectedEdge(n.ID, v.ID, n.getPosition().distanceTo(v.getPosition()));
				}
				UDGMatrix.addEdge(de);
			}
			for(Node v: completePDTGraph.get(n)){
				//add edge GG
				DirectedEdge de;
				if(hopdistance){
					de = new DirectedEdge(n.ID, v.ID, 1);
				}else{
					de = new DirectedEdge(n.ID, v.ID, n.getPosition().distanceTo(v.getPosition()));
				}
				PDTMatrix.addEdge(de);
			}
		}
		
		int V = UDGMatrix.V();
		
		FloydWarshall UDGfw = new FloydWarshall(UDGMatrix);
		FloydWarshall PDTfw = new FloydWarshall(PDTMatrix);
		
		return Algorithms.spanningRatio(UDGfw, PDTfw, V);
		
	}
	
	public static HashMap<Node, Set<Node>> createPDTNeighborhood(){
	
		HashMap<Node, Set<Node>> pdtneighbors=new HashMap<>();
		
		for(Node n : Tools.getNodeList()){
			Collection<Node> neighborhood = Algorithms.getNeighborNodes(n, Tools.getNodeList());
			Set<Node> pdtNodes = Algorithms.buildPartialDelaunayTriangulation(neighborhood, n);
			pdtneighbors.put(n, pdtNodes);
		}
		return pdtneighbors;
	}
	
	public static Logger getLogger(String filename){
		if(logger==null){
			logger=Logger.getLogger(Algorithms_ext.class.getName());
			FileHandler logFileOut=null;
			try {
				logFileOut = new FileHandler(filename, true);
			} catch (SecurityException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(inbatchmode){
				logger.setUseParentHandlers(false);
				logger.setLevel(Level.WARNING);
			}
			
			java.util.logging.Formatter simpleformatter = new java.util.logging.Formatter (){
				//the following formatter is copied from: 
				//http://stackoverflow.com/questions/2950704/java-util-logging-how-to-suppress-date-line
				@Override
			    public String format(final LogRecord r) {
			        StringBuilder sb = new StringBuilder();
			        sb.append(formatMessage(r)).append(System.getProperty("line.separator"));
			        if (null != r.getThrown()) {
			            sb.append("Throwable occurred: "); //$NON-NLS-1$
			            Throwable t = r.getThrown();
			            PrintWriter pw = null;
			            try {
			                StringWriter sw = new StringWriter();
			                pw = new PrintWriter(sw);
			                t.printStackTrace(pw);
			                sb.append(sw.toString());
			            } finally {
			                if (pw != null) {
			                    try {
			                        pw.close();
			                    } catch (Exception e) {
			                        // ignore
			                    }
			                }
			            }
			        }
			        return sb.toString();
			    }
			};
			
			logFileOut.setFormatter(simpleformatter);
			
			
			logger.addHandler(logFileOut);
			
			logger.log(Level.INFO, "Logger has started and prints into file:\n" + filename);	
		}
		
		return logger;
	}
	
	public static Logger getLogger(){
		if(logger==null){
			logger=Logger.getLogger(Algorithms_ext.class.getName());
			
			SimpleFormatter formatter = new SimpleFormatter();
			
			
			logger.log(Level.INFO, "Logger is running.");	
		}
		
		return logger;
	}
	
}
