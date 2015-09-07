package projects.reactiveSpanner.nodes.messages;

import java.util.UUID;

import projects.reactiveSpanner.nodes.messageHandlers.SubgraphStrategy.EStrategy;import projects.reactiveSpanner.nodes.messages.data.Data;
import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.nodes.messages.Message;

public class DataMessage<T extends Data> extends AbstractMessage{

	T payload;
	
	public DataMessage(UUID tcID, PhysicalGraphNode transmitter, EStrategy strategy, T payload) {
		super(tcID, transmitter, strategy);
		this.payload=payload;
	}



	public T getPayload() {
		return payload;
	}



	@Override
	public Message clone() {
		
		return this;
	}
	
	
}
