package projects.reactiveSpanner.record;

import java.io.Serializable;
import java.util.UUID;

import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

public class ReactivePDTMessageRecord extends MessageRecord implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7560697745346452854L;
	
	private final Node forwarderNode;
	protected final int numRTSMessages;
	protected final int numCTSMessages;
	
	/**
	 * @param forwarder		current forwarder node of the current BFP round
	 * @param numRTSMessages	all sent RTS messages in this round
	 * @param numCTSMessages	all sent CTS messages in this round
	 */
	public ReactivePDTMessageRecord(final Node forwarder, final UUID tcID, final int numRTSMessages, final int numCTSMessages)
	{
		super(Tools.getNodeList().size(), tcID);
		this.forwarderNode = forwarder;
		this.numRTSMessages = numRTSMessages;
		this.numCTSMessages = numCTSMessages;
	}
	
	public Node getForwarderNode() {
		return forwarderNode;
	}
	public int getNumRTSMessages() {
		return numRTSMessages;
	}
	public int getNumCTSMessages() {
		return numCTSMessages;
	}

	/**
	 * @param value separator e. g. ',' or '|'
	 * @return comma separated values in the following order:
	 * - number of sent RTS messages
	 * - number of sent CTS messages
	 */
	@Override
	public String toRecord(char valSeparator) {
		StringBuffer out = new StringBuffer();
		out.append(numRTSMessages);
		out.append(valSeparator);
		out.append(numCTSMessages);
		return out.toString();
	}
	
	@Override
	public String toString() {
		return 	"Routing-ID=" + tcID
				+ ", NodeNumber=" + numNodes
				+ ", sentRTS=" + numRTSMessages
				+ ", sentCTS=" + numCTSMessages;
	}
}
