import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

//Create a socket to receive packets (stream of bytes); 
/*
 * This class will continuously: 
 * 	- listen on the assigned port number 
 *  - */

class SocketReceivedPackets implements Runnable {
					@Override
					public void run() {
						
						int maxPacketLength = 1500; 
						String receivedPacket, receivedMessage; 
						
						try {
							ServerSocket receiveSocket = new ServerSocket(ConfigurationNode.cnPort);
							
							while(true) {
								Socket socket = receiveSocket.accept(); 
								InputStream inputStream = socket.getInputStream(); 
								byte [] receivedPkt = new byte[maxPacketLength]; 
								inputStream.read(receivedPkt); 
								receivedMessage = new String(receivedPkt);
								
								//System.out.println("through assigned port " + receivedMessage); 
								
								String receivedPktField [] = receivedMessage.split("-"); 
								String pktType = receivedPktField[5];
								
								if (pktType.contains("latencyMessage")) {
									synchronized (ConfigurationNode.cnLatencyQueue) {
										(ConfigurationNode.cnLatencyQueue).offer(receivedMessage); 
									}
								}
								
								if (pktType.contains("lsa")) {
									synchronized (ConfigurationNode.lsaQueue) {
										(ConfigurationNode.lsaQueue).offer(receivedMessage); 
									}
									//System.out.println("through assigned port " + receivedMessage);
								}
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
					}
				}