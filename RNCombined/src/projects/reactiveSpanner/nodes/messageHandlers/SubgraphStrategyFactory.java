package projects.reactiveSpanner.nodes.messageHandlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import projects.reactiveSpanner.exceptions.InvalidSubgraphStrategyException;
import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;
import projects.reactiveSpanner.nodes.messageHandlers.BCA.BCA;
import projects.reactiveSpanner.nodes.messageHandlers.BFP.BFP;
import projects.reactiveSpanner.nodes.messageHandlers.Barriere.Barriere;
import projects.reactiveSpanner.nodes.messageHandlers.BarriereExt.BarriereExt;
import projects.reactiveSpanner.nodes.messageHandlers.buildBackbone.BuildBackbone;
import projects.reactiveSpanner.nodes.messageHandlers.buildBackbone.CreateVirtuals;
import projects.reactiveSpanner.nodes.messageHandlers.reactivePDT.ReactivePDT;
import projects.reactiveSpanner.nodes.messageHandlers.simpleTopologyControls.LocalGG;
import projects.reactiveSpanner.nodes.messageHandlers.simpleTopologyControls.LocalPDT;
import projects.reactiveSpanner.nodes.messageHandlers.simpleTopologyControls.LocalUDG;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.rmys.nodes.messageHandler.RMYS;
import projects.rmys.nodes.nodeImplementations.NewPhysicalGraphNode;
import sinalgo.nodes.Node;
import sinalgo.runtime.nodeCollection.NodeCollectionInterface.NodeCollectionListener;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

/**
 * Factory of subgraph strategies that are called on the holding node of this factory. This factory manages requested subgraph strategy.
 * For each type of subgraph strategy only one instance per factory will be created.
 * 
 * @author Mavs
 */
public class SubgraphStrategyFactory
{
	/**
	 * Controller to manage all subgraph strategy factories. It is not intended to access this controller outside of the SubgraphStrategyFactory.
	 * <br>
	 * This controller implements the NodeCollectionListener to be informed about changes in the node list and therefore to clear deprecated topologies.
	 * To get notifications, this class has to be instantiated (here as singleton)
	 */
	private final static class SubgraphStrategyFactoryController implements NodeCollectionListener
	{
		private static SubgraphStrategyFactoryController instance = null;
		public static SubgraphStrategyFactoryController getInstance() {
			if(instance == null) {
				logger.logln(LogL.INFO, "Create SubgraphStrategyFactoryController");
				instance = new SubgraphStrategyFactoryController();
			}
			return instance;
		}
		
		private final Set<SubgraphStrategyFactory> subgraphStrategyFactories;
		private boolean hasGraphChanged = true;
		
		/** 
		 * Private constructor - this is a singleton implementation
		 */
		private SubgraphStrategyFactoryController()
		{
			subgraphStrategyFactories = new HashSet<SubgraphStrategyFactory>();
			hasGraphChanged = true;
			Tools.getNodeList().addCollectionListener(this);
		}
		
		/**
		 * Register a factory to this controller
		 * @param factory to register
		 */
		private void register(SubgraphStrategyFactory factory)
		{
			subgraphStrategyFactories.add(factory);
		}
		
		/**
		 * Clearing all topology controls of all registered SubgraphStrategyFactory instances
		 */
		private void clearTopologies()
		{
			logger.logln(LogL.INFO, "Graph is not up to date. Clearing all topology controls of all nodes.");
			for(SubgraphStrategyFactory factory: subgraphStrategyFactories)
			{
				factory.topologyControls.clear();
			}
			hasGraphChanged = false;
		}
		
		/**
		 * Checks whether the global graph has changed since the last request for this node
		 * @return graph has not changed
		 */
		private boolean isGraphUpToDate()
		{
			return hasGraphChanged? false : true;
		}

		@Override
		public void nodeUpdated(Node n) {
			hasGraphChanged = true;
		}

		@Override
		public void nodeAdded(Node n) {
			hasGraphChanged = true;
		}

		@Override
		public void nodeRemoved(Node n) {
			hasGraphChanged = true;
		}
	}
	
	private static Logging logger = Logging.getLogger();
	
	final private PhysicalGraphNode holder;
	private HashMap<EStrategy, SubgraphStrategy> topologyControls;

	/**
	 * The last subgraph strategy that was started by the holder
	 */
	private SubgraphStrategy lastSubgraphStrategy;

	public SubgraphStrategyFactory(final PhysicalGraphNode holder) {
		this.holder = holder;
		topologyControls = new HashMap<SubgraphStrategy.EStrategy, SubgraphStrategy>();
		SubgraphStrategyFactoryController.getInstance().register(this);
	}

	/**
	 * Send an request for a desired topology control. If this topology control was already generated, the one will be returned, otherwise a new one will be generated. In both cases, the subclass strategy will be returned.
	 * @param controlStrategy
	 *            requested subgraph strategy represented by the enumeration.
	 * @return requested SubgraphStrategy
	 */
	public SubgraphStrategy request(final EStrategy controlStrategy)
	{
		if(!SubgraphStrategyFactoryController.getInstance().isGraphUpToDate())
		{
			SubgraphStrategyFactoryController.getInstance().clearTopologies();	
		}
		
		if (!topologyControls.containsKey(controlStrategy)) {
			SubgraphStrategy newSubgraphStrategy = null;
			switch (controlStrategy) {
			case BARRIERE:
				newSubgraphStrategy = new Barriere(holder);
				break;
			case BARRIERE_EXT:
				newSubgraphStrategy = new BarriereExt(holder);
				break;
			case BFP:
				newSubgraphStrategy = new BFP(holder);
				break;
			case BUILD_BACKBONE:
				newSubgraphStrategy = new BuildBackbone(holder);
				break;
			case RMYS:
				NewPhysicalGraphNode node = null;
				try {
					node = (NewPhysicalGraphNode) holder;
					newSubgraphStrategy = new RMYS(node);
				} catch (ClassCastException e) {
					System.err.println(
							holder.toString() + " must be of type NewPhysicalGraphNode.\n Starting of RMYS failed.");
				}

				break;
			case CREATE_VIRTUALS:
				newSubgraphStrategy = new CreateVirtuals(holder, topologyControls.get(EStrategy.BUILD_BACKBONE).getTopologyControlID());//FIXME hack to test. Need to find a better solution: 																															//a Subgraphstrategy must be able to invoke another one.
				break;
			case REACTIVE_PDT:
				newSubgraphStrategy = new ReactivePDT(holder);
				break;
			case BCA:
				newSubgraphStrategy = new BCA(holder);
				break;
			case UDG:
				newSubgraphStrategy = new LocalUDG(holder);
				break;
			case GG:
				newSubgraphStrategy = new LocalGG(holder);
				break;
			case PDT:
				newSubgraphStrategy = new LocalPDT(holder);
				break;
			default:
				throw new InvalidSubgraphStrategyException("No or invalid selected control strategy!");
			}
			topologyControls.put(controlStrategy, newSubgraphStrategy);
			lastSubgraphStrategy = newSubgraphStrategy;
			return lastSubgraphStrategy;
		} else {
			lastSubgraphStrategy = topologyControls.get(controlStrategy);
			return lastSubgraphStrategy;
		}
	}

	/**
	 * @return The last requested subgraph strategy
	 */
	public SubgraphStrategy getLastRequestedSubgraphStrategy() {
		return lastSubgraphStrategy;
	}

	/**
	 * @param controlStrategy to be drawn
	 * draws controlStrategy if exists.
	 */
	public void drawSubgraphStrategy(EStrategy controlStrategy) {
		if (topologyControls.containsKey(controlStrategy)) {
			lastSubgraphStrategy = topologyControls.get(controlStrategy);
		} else {
			throw new RuntimeException("Cannot find controlStrategy: " + controlStrategy);
		}
	}

	/**
	 * Force the holding node to take the specified subgraphStrategy <br>
	 * This case is unusual use of subgraph strategies and should be only considered if its necessary to spread a single subgraphStrategy among different nodes
	 * 
	 * @param subgraphStrategy
	 *            that is forced to use
	 */
	public void handOverSubgraphStrategy(SubgraphStrategy subgraphStrategy) {
		if (topologyControls.containsKey(subgraphStrategy.getStrategyType())) {
			logger.logln(LogL.WARNING, "Subgraph Strategy controller of node " + holder.toString() + " is forced to take over subgraph strategy of type " + subgraphStrategy.getStrategyType() + " and ID " + subgraphStrategy.getTopologyControlID() + " but has already a subgraph strategy of this type. " + "The old strategy will be overwrited.");
		}
		topologyControls.put(subgraphStrategy.getStrategyType(), subgraphStrategy);
	}
}
