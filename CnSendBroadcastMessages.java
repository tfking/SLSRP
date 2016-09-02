import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/*Create a socket to send broadcast messages
 *  - loop through all the interfaces of the Network interface card, 
 *  - get the broadcast address of the network 
 *  - and transmit a becaon every 5 seconds to acknowledge the presence of the node. 
 *  
 */
class CnSendBroadcastMessages implements Runnable {
					
	DatagramSocket broadcast_socket;
					
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//port number for broadcast messages 
		int server_port = 50008;
		String broadcast_subnet_part = ""; 
		
		//Open a socket to listen to all incoming broadcast message 
		try {
			broadcast_socket = new DatagramSocket();
			broadcast_socket.setBroadcast(true);
							
			while (true) {
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				while (interfaces.hasMoreElements()) {
					  NetworkInterface networkInterface = interfaces.nextElement();
					  
					  if (networkInterface.isLoopback())
					    continue;    // Don't want to broadcast to the loopback interface
					  
					  for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
						  InetAddress broadcast = interfaceAddress.getBroadcast(); 
						  InetAddress broadcast1 = interfaceAddress.getAddress(); 
						  
						    if ((""+broadcast).contains(".")) {
						    	if ((""+broadcast).equals("/0.0.0.0")) {
						    		broadcast_subnet_part = (""+broadcast1).substring(1,(""+broadcast1).lastIndexOf('.'));
							    	broadcast_subnet_part = broadcast_subnet_part + ".255"; 
						    	}
						    	else 
						    		broadcast_subnet_part = (""+broadcast).split("/")[1];
						    	
						    	InetAddress local = InetAddress.getByName(broadcast_subnet_part); 

						    	//Message to broadcast 
								String beacon_message = ConfigurationNode.cnId + "-beacon-" + ConfigurationNode.cnIp + "-" + ConfigurationNode.cnPort + "-" + ConfigurationNode.versionNumber + "-10" + "-" + ConfigurationNode.deviceName + "-"; 
											
								int msg_length = beacon_message.length(); 
								byte [] becaon_message_bytes = beacon_message.getBytes(); 
											
								DatagramPacket p = new DatagramPacket(becaon_message_bytes, msg_length, local, server_port);
											
								broadcast_socket.send(p);
											
								Thread.sleep(5000);
							}
									     
						    if (broadcast == null)
						    	continue;
								// Use the address
					}
			  }
							 
			}
							
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
					
}