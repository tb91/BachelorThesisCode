/**
 * @author Mavs
 */
package projects.reactiveSpanner.record;

import java.io.Serializable;
import java.util.UUID;

import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

/**
 * Record data class for representic statistics of sent RTS, CTS and protest messages of the BFP algorithm
 */
public class BFPMessageRecord extends MessageRecord implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 461656648186247350L;
	
	private final Node forwarderNode;
	protected int numRTSMessages;
	protected int numCTSMessages;
	protected int numProtestMessages;
	
	public BFPMessageRecord(final Node forwarder, final UUID routingID)
	{
		super(Tools.getNodeList().size(), routingID);
		this.forwarderNode = forwarder;
		this.numRTSMessages = 0;
		this.numCTSMessages = 0;
		this.numProtestMessages = 0;
		
		//TODO further statistic, for example which node has send CTS/protest etc.?
	}
	
	/**
	 * @param forwarder		current forwarder node of the current BFP round
	 * @param numRTSMessages	all sent RTS messages in this round
	 * @param numCTSMessages	all sent CTS messages in this round
	 * @param numProtestMessages	all sent protest messages in this round
	 */
	public BFPMessageRecord(final Node forwarder, final UUID tcID, final int numRTSMessages, final int numCTSMessages, final int numProtestMessages)
	{
		super(Tools.getNodeList().size(), tcID);
		this.forwarderNode = forwarder;
		this.numRTSMessages = numRTSMessages;
		this.numCTSMessages = numCTSMessages;
		this.numProtestMessages = numProtestMessages;
	}
	
	public final Node getForwarderNode() {
		return forwarderNode;
	}
	public final int getNumRTSMessages() {
		return numRTSMessages;
	}
	public final int getNumCTSMessages() {
		return numCTSMessages;
	}
	public final int getNumProtestMessages() {
		return numProtestMessages;
	}
	
	public void incRTS(){
		numRTSMessages++;
	}
	public void incCTS(){
		numCTSMessages++;
	}
	public void incProtest(){
		numProtestMessages++;
	}
	
	
	/**
	 * @param value separator e. g. ',' or '|'
	 * @return comma separated values in the following order:
	 * - number of sent RTS messages
	 * - number of sent CTS messages
	 * - number of sent protest messages
	 */
	@Override
	public String toRecord(char valSeparator) {
		StringBuffer out = new StringBuffer();
		out.append(numRTSMessages);
		out.append(valSeparator);
		out.append(numCTSMessages);
		out.append(valSeparator);
		out.append(numProtestMessages);
		return out.toString();
	}
	
	@Override
	public String toString() {
		return 	"Routing-ID=" + tcID
				+ ", NodeNumber=" + numNodes
				+ ", sentRTS=" + numRTSMessages
				+ ", sentCTS=" + numCTSMessages
				+ ", sentProtest=" + numProtestMessages;
	}
	
}
