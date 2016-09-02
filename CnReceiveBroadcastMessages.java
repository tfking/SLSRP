import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

//Create a socket to receive broadcast messages
				class CnReceiveBroadcastMessages implements Runnable {
					
					DatagramSocket broadcast_socket_receive;
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						//port number for broadcast messages 
						int server_port = 50008;
						
						String broadcast_message_received;
						byte[] message_received = new byte[1500];
						
						//Open a socket to listen to all incoming broadcast message 
						try {
							DatagramPacket p = new DatagramPacket(message_received, message_received.length);
							broadcast_socket_receive = new DatagramSocket(null);
							broadcast_socket_receive.setReuseAddress(true);
							broadcast_socket_receive.setBroadcast(true); 
							broadcast_socket_receive.bind(new InetSocketAddress(server_port)); 
							
							while (true) {
								broadcast_socket_receive.receive(p); 
								broadcast_message_received = new String(message_received, 0, p.getLength()); 
								//System.out.println(broadcast_message_received);
								
								String pkt_type = broadcast_message_received.split("-")[1]; // Get the packet type 
								
								//check if it a beacon message that we received and put in the linkedlist. they will be 
								//processed later. 
								if (pkt_type.contains("beacon")) {
									synchronized(ConfigurationNode.cnBeaconsReceived) {
										(ConfigurationNode.cnBeaconsReceived).offer(broadcast_message_received); 
									}
								}
							}
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
					}
					
				}