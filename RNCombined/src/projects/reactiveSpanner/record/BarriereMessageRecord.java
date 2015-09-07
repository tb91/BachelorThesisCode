package projects.reactiveSpanner.record;

import java.io.Serializable;
import java.util.UUID;

import sinalgo.tools.Tools;

public class BarriereMessageRecord extends MessageRecord implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5885593336288463550L;
	
	protected int numNewNeighbourMessages;
	protected int numBeaconRequestMessages;
	protected int numBeaconReplyMessages;
	protected int numVirtualMessages;
	

	public BarriereMessageRecord(UUID tcID) {
		super(Tools.getNodeList().size(), tcID);
		
		numNewNeighbourMessages=0;
		numBeaconRequestMessages=0;
		numBeaconReplyMessages=0;
		numVirtualMessages=0;
	}
	
	public BarriereMessageRecord(UUID tcID, int numNewNeighbourMessages,
			int numBeaconRequestMessages, int numBeaconReplyMessages,
			int numVirtualMessages){
		super(Tools.getNodeList().size(), tcID);
		this.numNewNeighbourMessages=numNewNeighbourMessages;
		this.numBeaconRequestMessages=numBeaconRequestMessages;
		this.numBeaconReplyMessages=numBeaconReplyMessages;
		this.numVirtualMessages=numVirtualMessages;
		
	}
	public final int getNumNewNeighbourMessages() {
		return numNewNeighbourMessages;
	}
	public final int getNumBeaconRequestMessages() {
		return numBeaconRequestMessages;
	}
	public final int getNumBeaconReplyMessages() {
		return numBeaconReplyMessages;
	}
	public final int getNumVirtualMessages() {
		return numVirtualMessages;
	}
	
	public void incNN(){
		numNewNeighbourMessages++;
	}
	public void incReq(){
		numBeaconRequestMessages++;
	}
	public void incRep(){
		numBeaconReplyMessages++;
	}
	public void incVirt(){
		numVirtualMessages++;
	}

	@Override
	public String toRecord(char valSeparator) {
		StringBuffer out = new StringBuffer();
		out.append(numNewNeighbourMessages);
		out.append(valSeparator);
		out.append(numBeaconRequestMessages);
		out.append(valSeparator);
		out.append(numBeaconReplyMessages);
		out.append(valSeparator);
		out.append(numVirtualMessages);
		return out.toString();
	}
	
	public String toString() {
		return 	"Routing-ID=" + tcID
				+ ", sentNN=" + numNewNeighbourMessages
				+ ", sentReq=" + numBeaconRequestMessages
				+ ", sentRep=" + numBeaconReplyMessages
				+ ", sentVirt=" + numVirtualMessages;
	}

}
