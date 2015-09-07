package projects.defaultProject.models.connectivityModels;

import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;

/**
 * A QUDG connectivity model for static network. The connections are only evaluated
 * the very first time according to the QUDG connectivity model, and then reused all 
 * over again.
 * @see projects.defaultProject.models.connectivityModels.UDG
 */
public class StaticQUDG extends QUDG {
	private boolean firstTime = true; // detect when the connections are evaluated for the first time
	
	@Override
	public boolean updateConnections(Node n) throws WrongConfigurationException {
		if(firstTime) {
			firstTime = false;
			return super.updateConnections(n); // let UDG do its work
		} else {
			return false; // keep all existing connections
		}
	}
	
	/**
	 * The default constructor for this class.  
	 * @throws CorruptConfigurationEntryException If one of the initialization steps fails.
	 */
	public StaticQUDG() throws CorruptConfigurationEntryException {
		// all done in QUDG.
	}
}

