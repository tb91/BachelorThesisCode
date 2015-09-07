package projects.reactiveSpanner.record;

import java.util.UUID;

import sinalgo.configuration.Configuration;

public abstract class MessageRecord {

	protected final int numNodes;
	protected final UUID tcID;
	protected final static int dimX = Configuration.dimX;
	protected final static int dimY = Configuration.dimY;;

	public MessageRecord(final int numNodes, final UUID tcID)
	{
		this.numNodes = numNodes;
		this.tcID = tcID;
	}

	public int getNumNodes() {
		return numNodes;
	}

	public final UUID getTopologyControlID() {
		return tcID;
	}

	/**
	 * @return separated values
	 **/
	public abstract String toRecord(char valSeparator);

}