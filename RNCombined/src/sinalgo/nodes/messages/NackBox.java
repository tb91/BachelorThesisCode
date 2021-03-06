/*
 Copyright (c) 2007, Distributed Computing Group (DCG)
                    ETH Zurich
                    Switzerland
                    dcg.ethz.ch

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

 - Neither the name 'Sinalgo' nor the names of its contributors may be
   used to endorse or promote products derived from this software
   without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package sinalgo.nodes.messages;

/**
 * The equivalent to the inbox for the messages that have not reached
 * their destination.
 * <p>
 * Whenever a message is dropped, the sender is informed through the
 * handle <code>handleNAckMessages()</code> method.
 * <p>
 * This feature needs to be enabled in the project configuration:
 * set <code>generateNAckMessages</code> to true. If a sender node
 * needs not to be informed about dropped messages, you should turn off
 * this feature to save computing power. 
 */
@SuppressWarnings("serial")
public class NackBox extends Inbox {

	/**
	 * Constructor to create a NackBox containing a single packet
	 * @param p
	 */
	public NackBox(Packet p){
		super(p);
	}

	/**
	 * Constructor to create a NackBox containing a given list of packets
	 * @param list
	 */
	public NackBox(PacketCollection list) {
		super(list);
	}
	
	/**
	 * Dummy constructor to create empty nackBox.  
	 */
	public NackBox() {
	}
	
	/**
	 * <b>This is a framework internal method. Project developers should not need to call this method.</b><br>
	 * Resets the inbox to contain a single packet.
	 * @param p The packet to include in this inbox.
	 * @return This inbox object.
	 */
	@Override
	public NackBox resetForPacket(Packet p) {
		super.resetForPacket(p);
		return this;
	}
		
}
