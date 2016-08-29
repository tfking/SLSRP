//Importing Statements
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;




public class Configuration_Node extends JFrame{

	//Declaring the CN Variables
	private Container content; 
	
	private JButton add_cn; 
	
	private JLabel added_successfully_label, cn_information_label, cn_id_label, cn_ip_label, cn_port_label, space_label,
					version_protocol_number_label;
	
	private JPanel cn_information_panel; 
	
	String device_ip_name, cn_ip, cn_id; 
	
	int cn_port, version_number; 
	
	static Queue<String> cn_beacons_received = new LinkedList<String>(); //store the ID of CN nodes received through the beacon
	static Queue<String> cn_packet_queue = new LinkedList<String>();
	
	//Datastructure to store the info of the nodes attached to this current node. 
	LinkedList <String> attached_nodes_info;
	
	/*This class will:
	 * 	- Loop through the cn_beacon_received Linkedlist, 
	 *  - check the version number present in the beacon, if equal to the one of the cn, 
	 *  - associate the two (add the cn in the neighbor_linked_list)
	*/
	class Node_Association implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true) {
				synchronized(cn_beacons_received) {
					if (cn_beacons_received.size() > 0) {
						String cn_beacon_packet = cn_beacons_received.peek(); 
						
						//extract the version number received to compare
						String cn_version_received = cn_beacon_packet.split("-")[4];
						String cn_id_received = cn_beacon_packet.split("-")[0];  
						String cn_ip_received = cn_beacon_packet.split("-")[2];
						String cn_port_received = cn_beacon_packet.split("-")[3]; 
						String alive_interval_received = cn_beacon_packet.split("-")[5];
						
						if (cn_version_received.equals(""+version_number) && !cn_id_received.equals(cn_id)) {
							
							String adjacent_node_info = cn_id_received+ "-" + cn_ip_received + "-" + cn_port_received + "-" + alive_interval_received + "-"; 
							synchronized(attached_nodes_info) {
								boolean cn_present = false; 
								for (int i=0; i<attached_nodes_info.size(); i++) {
									//Get the neighbor information 
									String neighbor_information = attached_nodes_info.get(i);
									if ((neighbor_information.split("-")[0]).equals(cn_id_received)) {
										cn_present = true;
										attached_nodes_info.set(i, adjacent_node_info);
									}
								}
								if (cn_present == false)
									attached_nodes_info.offer(adjacent_node_info); 
							}
						}
						
						cn_beacons_received.remove(); 
						
						synchronized(attached_nodes_info) {
						if (attached_nodes_info.size() >0 ) {
							for (int i=0; i<attached_nodes_info.size(); i++) {
								//Get the neighbor information 
								String neighbor_information = attached_nodes_info.get(i);
								System.out.println("neighbor info is: " +neighbor_information); 
							}
						}}
						
						System.out.println("next round"); 
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	//###################################### ALIVE CHECKING STATUS ##################################################################
	class Alive_checking implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				synchronized (attached_nodes_info) {		//lock the attached_nodes_info datastructures to process it. 
					/*Loop through the different attached nodes, check the value of the alive field 
						- Decrease it
						- check if it is 0
							- if 0, then the node is declared dead, take it out from the list of attached nodes. 
							- else, replace the value of the alive by (previous value - 1)
							- sleep for 10 seconds and do the same. 
						*/
					for (int i=0; i<attached_nodes_info.size(); i++) {
						//Get the neighbor information 
						String neighbor_information = attached_nodes_info.get(i);
						
						//Decrease the value of the alive message 
						int alive_time_remaining = Integer.parseInt(neighbor_information.split("-")[3])-1;
						
						if (alive_time_remaining == 0) {
							attached_nodes_info.remove(i); 	
						}
						else {
							String new_adjacent_node_info = neighbor_information.split("-")[0] + "-" + neighbor_information.split("-")[1] + "-" + neighbor_information.split("-")[2] + "-" + alive_time_remaining + "-"; 
							attached_nodes_info.set(i, new_adjacent_node_info);
						}
					}
				}
				try {
					Thread.sleep(10000); 
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	//############################################################################################################################
	/*###################################### LATENCY COMPUTATION ##################################################################*/
	/*
	 /*Loop through the different attached nodes,  
						- Send Packets and start time 
						- wait to receive acknowledgments and record the time 
						- average the time*/ 
	class Latency_Calculation implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				
				int number_of_packets_sent = 0; 
				String [] cn_latency; 
				long timer_start_cn_cost = 0; 
				
				synchronized(attached_nodes_info) {
					
					int number_of_acks_received = 0;
					number_of_packets_sent = 10*attached_nodes_info.size(); 
					
					//create an array to store the average delays
					cn_latency = new String [attached_nodes_info.size()];
					
					//Initialize the array
					for (int l=0; l<cn_latency.length; l++) {
						String entry = "sum"+((attached_nodes_info.get(l).split("-")[0])) + "=0-count=0-delay=0"; 
						cn_latency[l] = entry;  
					}
					
					if (attached_nodes_info.size() > 0 ) {
						for (int i=0; i<attached_nodes_info.size(); i++) {
							//Get the neighbor information 
							String cn_to_send_link_cost_msg = attached_nodes_info.get(i);
							
							//Extract the ip and port number to send the packet
							String neighbor_id = cn_to_send_link_cost_msg.split("-")[0]; 
							String neighbor_ip = cn_to_send_link_cost_msg.split("-")[1]; 
							String neighbor_port = cn_to_send_link_cost_msg.split("-")[2];
							
							//Build the packet to evaluate the latency: 
							String latency_cost_msg = cn_id + "-latency_evaluate_message_evaluate-Hello" +"-"; 
							//Add the header before putting in the queue: Header is the source_ip-source_port-destination_ip-destination_port
							String packet_added_in_queue = cn_ip +"-"+cn_port+"-"+neighbor_ip+"-"+neighbor_port +"-"+ latency_cost_msg;
							
							synchronized(cn_packet_queue) {
								for (int j = 0; j < 10; j ++) {
									//System.out.println("evaluating the cost of the link for  " + neighbor_id); 
									router_packet_queue.offer(packet_added_in_queue);  
								}
							}	
						}
						//Starting the timer for the 
						timer_start_cn_cost = System.currentTimeMillis(); 
					}
				}
			}
		}
		
	}
	//##############################################################################################################################
	
	public Configuration_Node() {
		//Getting the container and setting the layout manager
		content = getContentPane();
		content.setLayout(new FlowLayout()); 
		
		//Instantiating the component of the GUI
		added_successfully_label = new JLabel("The CN has been successfully added in the local Network!"); 
		cn_information_label = new JLabel("CN Information: ");
		space_label = new JLabel("            ");  
		
		add_cn = new JButton("    Add Communication Node    "); 
		
		//Instantiating the different panels where components will be added. 
		cn_information_panel = new JPanel(); 
		
		//Setting the Panel's Layout
		cn_information_panel.setLayout(new BoxLayout(cn_information_panel, BoxLayout.Y_AXIS)); 
		
		//Instantiate the event handler 
		ButtonHandler bh = new ButtonHandler();
				
		//adding Listeners to Button 
		add_cn.addActionListener(bh);
		
		//Adding the GUI components into the GUI interface. 
		content.add(add_cn); 
	}
	
	public class ButtonHandler implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent ae) {
			// TODO Auto-generated method stub
			
			if (ae.getSource() == add_cn) {
				//Create the array to store attached CN information. 
				attached_nodes_info = new LinkedList<String>(); 
				
				//Get the  informations of the Communication Node
				try {
					device_ip_name = "" + InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Extracting the device IP 
				cn_ip= device_ip_name.substring(device_ip_name.indexOf("/")+1);
				
				//Extracting the device Name
				String device_name = device_ip_name.substring(device_ip_name.indexOf(" ")+1, device_ip_name.indexOf("/"));
				
				//Automatically finding a free port on the localhost to associate it with the CN
				ServerSocket test_port;
				try {
					test_port = new ServerSocket(0);
					cn_port = test_port.getLocalPort(); 
					test_port.close(); 
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Considering the port number as the ID
				cn_id = "" + cn_port; 
				
				version_number = (int) (Math.random()*2); 
				
				//Instantiating the CN information labels
				cn_id_label = new JLabel("                                  CN ID Number:  " + cn_id);
				cn_ip_label = new JLabel("                                  CN IP Address:  " + cn_ip);
				cn_port_label = new JLabel("                                  CN Port Number:  " + cn_port);
				version_protocol_number_label = new JLabel("                                  Version Number: " + version_number);
				
				//Create a socket to send broadcast messages
				class Cn_send_broadcast_messages implements Runnable {
					
					DatagramSocket broadcast_socket;
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						//port number for broadcast messages 
						int server_port = 50008;
						
						//Open a socket to listen to all incoming broadcast message 
						try {
							broadcast_socket = new DatagramSocket();
							broadcast_socket.setBroadcast(true);
							
							while (true) {
								Enumeration<NetworkInterface> interfaces =
									    NetworkInterface.getNetworkInterfaces();
									while (interfaces.hasMoreElements()) {
									  NetworkInterface networkInterface = interfaces.nextElement();
									  if (networkInterface.isLoopback())
									    continue;    // Don't want to broadcast to the loopback interface
									  for (InterfaceAddress interfaceAddress :
									           networkInterface.getInterfaceAddresses()) {
									    InetAddress broadcast = interfaceAddress.getBroadcast(); 
									    InetAddress broadcast1 = interfaceAddress.getAddress();
									    
									    if ((""+broadcast1).contains(".")) {
									    	String broadcast_subnet_part = (""+broadcast1).substring(1,(""+broadcast1).lastIndexOf('.'));
									    	String broadcast_address = broadcast_subnet_part + ".255"; 
									    	
									    	InetAddress local = InetAddress.getByName(broadcast_address); 
			
									    	//Message to broadcast 
											String beacon_message = cn_id + "-beacon-" + cn_ip + "-" + cn_port + "-" + version_number + "-10" + "-"; 
											
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
				//Start the broadcast socket to send broadcast messages  
				new Thread(new Cn_send_broadcast_messages()).start();
				
				//Create a socket to receive broadcast messages
				class Cn_receive_broadcast_messages implements Runnable {
					
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
									synchronized(cn_beacons_received) {
										cn_beacons_received.offer(broadcast_message_received); 
									}
								}
							}
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
					}
					
				}
				//start the broadcast socket to receive broadcast messages 
				new Thread(new Cn_receive_broadcast_messages()).start();
				
				//start the node_association thread
				new Thread(new Node_Association()).start();
				
				//start the node_association thread
				new Thread(new Alive_checking()).start();
				
				//Create a socket to receive packets (stream of bytes); 
				class Cn_receive_socket implements Runnable {
					@Override
					public void run() {
						
						int max_packet_length = 1500; 
						
						String received_packet, received_message; 
						
						try {
							ServerSocket receive_socket = new ServerSocket(cn_port);
							
							while(true) {
								Socket socket = receive_socket.accept(); 
								InputStream input_stream = socket.getInputStream(); 
								byte [] received_pkt = new byte[max_packet_length]; 
								input_stream.read(received_pkt); 
								received_message = new String(received_pkt);
								//System.out.println(received_message); 
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
					}
				}
				
				new Thread(new Cn_receive_socket()).start(); 
				
				
				//Create a socket to send packets
				class Cn_send_packets implements Runnable {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						
					}
					
				}
				
				content.remove(add_cn);  
				cn_information_panel.add(added_successfully_label);
				cn_information_panel.add(space_label);
				cn_information_panel.add(space_label);
				cn_information_panel.add(cn_information_label);
				cn_information_panel.add(cn_id_label);
				cn_information_panel.add(cn_ip_label);
				cn_information_panel.add(cn_port_label);
				cn_information_panel.add(version_protocol_number_label);
				
				content.add(cn_information_panel);
				setSize(400, 150); 
			}
		}

	}
	
	public static void main(String[] args) {
		
		Configuration_Node cn = new Configuration_Node(); 
		cn.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		cn.setSize(350, 100); 
		cn.setVisible(true); 
		cn.setTitle("Distributed Communications System"); 
		cn.setResizable(false); 
	}

}
