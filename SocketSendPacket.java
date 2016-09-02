import java.io.OutputStream;
import java.net.Socket;

/*
 * This class will continuously: 
 * 	- lock the cnPacketQueue (datastructure where all the threads insert packets they want to send )
 *  - check if there is a packet to be forwarded through the socket
 *  - get the packet and extract the destination and port IP number 
 *  - send the packet to the destination using the IP/port combination
 *  - remove that packet from the queue 
 *  - move to the next packet 
 *  - repeat this until the queue becomes empty*/

class SocketSendPacket implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try { 
			while (true) {
				synchronized(ConfigurationNode.cnPacketQueue) {
					
					if ((ConfigurationNode.cnPacketQueue).size() > 0) {
						String packetOut = (ConfigurationNode.cnPacketQueue).peek();
						
						String [] packetFields = packetOut.split("-"); 
						String destIp = packetFields[2]; 
						int  destPort = Integer.parseInt(packetFields[3]); 
						
						//create the socket to send the packet out 
						Socket routerSocketSend = new Socket (destIp, destPort); 
						OutputStream o = routerSocketSend.getOutputStream(); 
						
						byte [] pkt = packetOut.getBytes(); 
						pkt = packetOut.getBytes();
						
						o.write(pkt); 
					    routerSocketSend.close(); 
					    
					    (ConfigurationNode.cnPacketQueue).remove();
				} 
			}
				Thread.sleep(100); 
			}
		
	}
	catch (Exception err) {
		System.err.println(err);  
	}				
	}
					
}