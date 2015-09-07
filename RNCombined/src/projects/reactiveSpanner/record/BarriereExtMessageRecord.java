package projects.reactiveSpanner.record;

import java.io.Serializable;
import java.util.UUID;

import sinalgo.tools.Tools;

public class BarriereExtMessageRecord extends MessageRecord implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8260266302882051840L;
	
	protected int numNewNeighbourMessages;
	protected int numBeaconRequestMessages;
	protected int numBeaconReplyMessages;
	protected int numVirtualMessages;
	protected int numNewWitnessMessages;

	public BarriereExtMessageRecord(UUID tcID) {
		super(Tools.getNodeList().size(), tcID);
		
		numNewNeighbourMessages=0;
		numBeaconRequestMessages=0;
		numBeaconReplyMessages=0;
		numVirtualMessages=0;
		numNewWitnessMessages=0;
	}
	
	public BarriereExtMessageRecord(UUID tcID, int numNewNeighbourMessages,
			int numBeaconRequestMessages, int numBeaconReplyMessages,
			int numVirtualMessages, int numNewWitnessMessages){
		super(Tools.getNodeList().size(), tcID);
		this.numNewNeighbourMessages=numNewNeighbourMessages;
		this.numBeaconRequestMessages=numBeaconRequestMessages;
		this.numBeaconReplyMessages=numBeaconReplyMessages;
		this.numVirtualMessages=numVirtualMessages;
		this.numNewWitnessMessages=numNewWitnessMessages;
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

	public final int getNumNewWitnessMessages() {
		return numNewWitnessMessages;
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
	public void incNW(){
		numNewWitnessMessages++;
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
		out.append(valSeparator);
		out.append(numNewWitnessMessages);
		return out.toString();
	}
	
	public String toString() {
		return 	"Routing-ID=" + tcID
				+ ", sentNNExt=" + numNewNeighbourMessages
				+ ", sentReqExt=" + numBeaconRequestMessages
				+ ", sentRepExt=" + numBeaconReplyMessages
				+ ", sentVirtExt=" + numVirtualMessages
				+ ", sentNWExt=" + numNewWitnessMessages;
	}

}
