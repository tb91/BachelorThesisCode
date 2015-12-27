package projects.reactiveSpanner;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.models.DistributionModel;
import sinalgo.models.Model;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

/**
 * Class for special handling of SINALGO framework, to simplify and unify the general access.
 * 
 * @author Mavs
 *
 */
public class Utilities {
	private static Logging logger = Logging.getLogger();
	
	final static Charset ENCODING = StandardCharsets.UTF_8;

	public static double R = -1;
	static {
		try {
			R = Configuration.getDoubleParameter("UDG/rMax");
		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}
	
	/**
	 * Used edge type
	 */
	private static Class<?> edgeClass;
	
	/**
	 * Getting all nodes of type <code>requestedNodeClass</code> from workingSet.
	 * @param requestedNodeClass Class of nodes that is required (has to be derived from {@link Node})
	 * @param workingSet Collection of Nodes we are working on
	 * @return Collection with all occurrences of <code>requestedNodeClass</code> or nodes derived from it.
	 * @throws RuntimeException if the requested class is not of type {@link Node} nor derived from {@link Node}
	 */
	@SuppressWarnings("unchecked")
	public static <T extends U, U extends Node> List<T> getNodeCollectionByClass(final Class<T> requestedNodeClass, final Iterable<U> workingSet)
	{
		if(!Node.class.isAssignableFrom(requestedNodeClass))
		{
			throw new RuntimeException("Requested node class " + requestedNodeClass + " is neither of type node nor derived from Node!"); 
		}
		List<T> out = new ArrayList<T>();
		for(U node: workingSet)
		{	
			if(requestedNodeClass.isInstance(node))
			{
				out.add((T) node);	//secure, because of check
			}
		}	
		return out;
	}
	
	/**
	 * Getting all nodes of type <code>requestedNodeClass</code> from all currently added nodes.
	 * @param requestedNodeClass Class of nodes that is required (has to be derived from {@link Node})
	 * @return Collection with all occurrences of <code>requestedNodeClass</code>
	 * @throws RuntimeException if the requested class is not of type {@link Node} nor derived from {@link Node}
	 */
	public static <T extends Node> Collection<T> getNodeCollectionByClass(final Class<T> requestedNodeClass)
	{
		return getNodeCollectionByClass(requestedNodeClass, Tools.getNodeList());
	}
	
	
//	/**
//	 * Get a random node of type of the <code>requestedNodeClass</code> from <code>workingSet</code>.
//	 * If the <code>workingSet</code> is empty or no node of the requested class is inherited by the workingSet, 
//	 * an error will be thrown.
//	 * @param requestedNodeClass Class of nodes that is required (has to be derived from {@link Node})
//	 * @param workingSet Collection of Nodes we are working on
//	 * @return random node from workingSet of requested node class
//	 * @throws RuntimeException if workingSet does not contain at least one member of <code>requestedNodeClass</code>
//	 */
//	public static <T extends Node> T getRandomNode(final Class<? extends Node> requestedNodeClass, final Iterable<T> workingSet)
//	{
//		List<? extends T> considerableNodes = getNodeCollectionByClass(requestedNodeClass, workingSet);	
//		if(considerableNodes.isEmpty())
//		{
//			throw new RuntimeException("No nodes of type " + requestedNodeClass.toString() + " are contained in passed working set!");
//		}
//		//generate randomized value in range [0, (size -1)] and return the corresponding node in list
//		return considerableNodes.get(Tools.getRandomNumberGenerator().nextInt(considerableNodes.size()));
//	}
	/**
	 * Get a random node of type of the <code>requestedNodeClass</code> from <code>workingSet</code>.
	 * If the <code>workingSet</code> is empty or no node of the requested class is inherited by the workingSet, 
	 * an error will be thrown.
	 * @param requestedNodeClass Class of nodes that is required (has to be derived from {@link Node})
	 * @param workingSet Collection of Nodes we are working on
	 * @return random node from workingSet of requested node class
	 * @throws RuntimeException if workingSet does not contain at least one member of <code>requestedNodeClass</code>
	 */
	public static <T extends U, U extends Node> T getRandomNode(final Class<T> requestedNodeClass, final Iterable<U> workingSet)
	{
		List<? extends T> considerableNodes = getNodeCollectionByClass(requestedNodeClass, workingSet);	
		if(considerableNodes.isEmpty())
		{
			throw new RuntimeException("No nodes of type " + requestedNodeClass.toString() + " are contained in passed working set!");
		}
		//generate randomized value in range [0, (size -1)] and return the corresponding node in list
		return considerableNodes.get(Tools.getRandomNumberGenerator().nextInt(considerableNodes.size()));
	}
		
	/**
	 * Select randomly a node that is at least <code>R</code> far away from borders of the graph to preserve node density
	 * @param requestedNodeClass Class of nodes that is required (has to be derived from {@link Node})
	 * @param workingSet Collection of Nodes we are working on
	 * @return randomly selected node of type T
	 * @throws RuntimeException if every node in the working set violates the condition of beeing farther away from border than UDG-radius
	 * (Tim) This method contained an error! It could return values which are too close to the border if x and/or y are almost
	 * Configuration.dimX and Configuration.dimY! It is fixed now.
	 */
	public static <T extends U, U extends Node> T getRandomNodeWithinGraphBorders(final Class<T> requestedNodeClass, final Iterable<U> workingSet) {
		T out = null;
		double x;
		double y;

		boolean matched = false;
		int numTestedNodes = 0;

		while (!matched && numTestedNodes < Tools.getNodeList().size()) {
			numTestedNodes++;

			out = getRandomNode(requestedNodeClass, workingSet);
			x = out.getPosition().xCoord;
			y = out.getPosition().yCoord;

			if (x > R && x+R < Configuration.dimX && y > R && y+R < Configuration.dimY) {
				matched = true;
			}else{
				out=null;
			}
		}
		if (out == null)
			throw new RuntimeException("Every node in graph violates the condition of beeing farther away from border than UDG-radius");
		else
			return out;
	}
	
	
	/**
	 * Ensure that the underlying graph is <b>connected</b> and there are <b>no three collinear nodes</b> in it. As long this 
	 * is not the case, new graphs will be generated.
	 * @param workingSet nodes that are considered
	 * @throws CorruptConfigurationEntryException when configuration value "Graph/NodeCollinearityThreshold" is invalid 
	 */
	public static void satisfyGraphConditions(final Iterable<? extends Node> workingSet) throws CorruptConfigurationEntryException
	{
		//test for graph connectivity and collinearity
		//as long graph is not connected a new graph will be generated
		//as long the graph has collinear nodes, collinear nodes will be replaced with new random nodes
		boolean graphConnected = false;
		boolean graphHasNoCollinearNodes = false;
		logger.logln(LogL.INFO, "Generating new graphs until a connected graph without three collinear nodes has been found..");
		while (!graphConnected && !graphHasNoCollinearNodes)
		{
			while(!GraphConnectivity.isGraphConnected(Tools.getNodeList()))
			{
				Utilities.spreadPositionsByDistributionModel(Model.getDistributionModelInstance("Random"), workingSet);
				graphHasNoCollinearNodes = false;
			}
			Set<Node> colNodes = Algorithms.checkGraphOfCollinearNodes();
			while(!graphHasNoCollinearNodes)
			{
				graphConnected = false;
				for(Node n: colNodes)
				{
					n.setPosition(Model.getDistributionModelInstance("Random").getNextPosition());
				}
				colNodes = Algorithms.checkGraphOfCollinearNodes();
				graphHasNoCollinearNodes = colNodes.isEmpty();
			}
		}
		logger.logln(LogL.INFO, "Graph is connected and has no three collinear nodes");
	}
	
	/**
	 * Change the positions of all given nodes by the given distribution model
	 * @param distributionModelName the distribution model
	 * @param workingSet Nodes which positions should change
	 */
	public static void spreadPositionsByDistributionModel(final DistributionModel distribution, final Iterable<? extends Node> workingSet)
	{
		for(Node n: workingSet)
		{
			n.setPosition(distribution.getNextPosition());
		}
	}
	
	/**
	 * Writing to File. In front of filepath
	 * 
	 * @param filepath
	 *            path of the file to write in
	 * @param lines
	 *            to write in file
	 * @param openOptions
	 *            Used options for writing to file
	 * @throws IOException
	 *             will be thrown if write operation is not successful
	 */
	public static void writeToFile(String filepath, List<String> lines, OpenOption[] openOptions) throws IOException {
		Path path = Paths.get(filepath);

		if (lines.size() < 100) {
			Files.write(path, lines, ENCODING, openOptions);
		} else {
			try (BufferedWriter out = Files.newBufferedWriter(path, ENCODING, openOptions)) // try-with-resources Statement (requires JDK
																							// 7+)
			{
				for (String line : lines) {
					out.write(line);
					out.newLine();
				}
			}
		}
	}
	
	public static Class<?> getUsedEdgeType() throws ClassNotFoundException
	{
		if(Configuration.hasEdgeTypeChanged() || edgeClass != null)
		{
			final String usedEdgeType = Configuration.getEdgeType();	
			edgeClass = Class.forName(usedEdgeType);
		}
			
//		if(!edgeClass.isAssignableFrom(Edge.class))
//		{
//			throw new ClassNotFoundException("The implementation of the edge '" + usedEdgeType + "' could not be found.\n" +
//		                "Change the Type in the XML-File or implement it." + "");
//		}	
		return edgeClass;
	}
	
}
