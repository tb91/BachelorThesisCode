package projects.reactiveSpanner.models.connectivityModels;

import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;

public class NoConnectivity extends ConnectivityModelHelper {

	@Override
	protected boolean isConnected(Node from, Node to) {
		
		return from.outgoingConnections.contains(from, to);//add your connections to your nodes and this method will keep them alive
	}

}
