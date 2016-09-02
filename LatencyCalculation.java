/*###################################### LATENCY COMPUTATION ##################################################################*/
/*
 /*Loop through the different attached nodes,  
						- Send Packets and start time 
						- wait to receive acknowledgments and record the time 
						- average the time*/ 

class LatencyCalculation implements Runnable {
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			int numberOfPacketsSent = 0; 
			String [] cnLatency; 
			long timerStartCnCost = 0; 
				
			synchronized(ConfigurationNode.attachedNodesInfo) {		
				numberOfPacketsSent = 10*((ConfigurationNode.attachedNodesInfo).size()); 
				
				//System.out.println("number of packet sent " +numberOfPacketsSent); 
				//create an array to store the average delays
				cnLatency = new String [(ConfigurationNode.attachedNodesInfo).size()];
					
				//Initialize the array
				for (int l=0; l<cnLatency.length; l++) {
					String entry = "sum"+(((ConfigurationNode.attachedNodesInfo).get(l).split("-")[0])) + "=0-count=0-delay=0"; 
					cnLatency[l] = entry;  
				}
					
				if ((ConfigurationNode.attachedNodesInfo).size() > 0 ) {
					for (int i=0; i<(ConfigurationNode.attachedNodesInfo).size(); i++) {
						//Get the neighbor information 
						String cnToSendLinkCostMsg = (ConfigurationNode.attachedNodesInfo).get(i);
							
						//Extract the ip and port number to send the packet
						String neighborId = cnToSendLinkCostMsg.split("-")[0]; 
						String neighborIp = cnToSendLinkCostMsg.split("-")[1]; 
						String neighborPort = cnToSendLinkCostMsg.split("-")[2];
							
						//Build the packet to evaluate the latency: 
						String latency_cost_msg = ConfigurationNode.cnId + "-latencyMessage_evaluate-Hello" +"-"; 
						//Add the header before putting in the queue: Header is the source_ip-source_port-destination_ip-destination_port
						String packetAddedInQueue = ConfigurationNode.cnIp +"-"+ConfigurationNode.cnPort+"-"+neighborIp+"-"+neighborPort +"-"+ latency_cost_msg;
							
						synchronized(ConfigurationNode.cnPacketQueue) {
							for (int j = 0; j < 10; j ++) {
								//System.out.println("evaluating the latency  " + neighborId); 
								ConfigurationNode.cnPacketQueue.offer(packetAddedInQueue);  
							}
						}	
					}
					//Starting the timer for the 
					timerStartCnCost = System.currentTimeMillis(); 
				}
			}
			
			 while (numberOfPacketsSent > 0) {
				 	//System.out.println(numberOfPacketsSent); 
				 	
				 	long timerEndLinkCost1 = System.currentTimeMillis();
					long timeout = timerEndLinkCost1 - timerStartCnCost; 
					//System.out.println(timeout); 
					
					synchronized ((ConfigurationNode.cnLatencyQueue)) {
						if ((ConfigurationNode.cnLatencyQueue).size() > 0) {
							String latencyMessage = (ConfigurationNode.cnLatencyQueue).peek(); 
							
							if (latencyMessage.contains("evaluate")) {
								//Extract the Ip and port number to send the packet
								String neighborId = latencyMessage.split("-")[4]; 
								String neighborIp = latencyMessage.split("-")[0]; 
								String neighborPort = latencyMessage.split("-")[1];
								
								//Build the packet to check if the neighbor is still alive  ConfigurationNode.cnId-packet_type-device_name-device_ip-port_number
								String latencyMsg = ConfigurationNode.cnId + "-latencyMessage_reply-" + ConfigurationNode.cnPort + "-" + ConfigurationNode.cnIp + "-" +ConfigurationNode.deviceName + "-"; 
								//Add the header before putting in the queue: Header is the source_ip-source_port-destination_ip-destination_port
								String packetAddedInQueue = ConfigurationNode.cnIp +"-"+ConfigurationNode.cnPort+"-"+neighborIp+"-"+neighborPort +"-"+ latencyMsg;
								
								//System.out.println("I received a link cost evaluate, i am sending the acknowledgement to router" +  neighborId); 
								synchronized(ConfigurationNode.cnPacketQueue) {
									(ConfigurationNode.cnPacketQueue).offer(packetAddedInQueue);  
								}
								
								(ConfigurationNode.cnLatencyQueue).remove(); 
							}
							
							else if (latencyMessage.contains("latencyMessage_reply")) {
			
								numberOfPacketsSent --;
								//check which router send the reply 
								String neighborId = latencyMessage.split("-")[4];
								String neighborPort = latencyMessage.split("-")[6];
								String neighborIp = latencyMessage.split("-")[7];
								String hostName = latencyMessage.split("-")[8];
								
								long timerEndLinkCost = System.currentTimeMillis();
								long cost = timerEndLinkCost - timerStartCnCost; 
								
								for (int k=0; k<cnLatency.length; k++) {
									if ((cnLatency[k]).contains("sum"+neighborId)) {
										int sum =  (int) (Integer.parseInt((cnLatency[k].split("-")[0]).split("=")[1]) + cost); 
										int count = (int) (Integer.parseInt((cnLatency[k].split("-")[1]).split("=")[1]) + 1);
	                                    
										int delay = sum/count;
										
										cnLatency[k] = "sum"+neighborId+"="+sum+"-count="+count+"-delay="+delay + "-" + neighborIp + "-" + neighborPort + "-" + hostName + "-"; 
									}
								}
								 
								(ConfigurationNode.cnLatencyQueue).remove();
							}
							
							else 
								(ConfigurationNode.cnLatencyQueue).remove();
							
						}
					}
					if (timeout > 30000) {
						//System.out.println("action");
						//numberOfPacketsSent = 0;
						timerStartCnCost = System.currentTimeMillis();
						/*
						for (int k=0; k<cnLatency.length; k++) {
							if (!((cnLatency[k].split("-")[1]).split("=")[1]).equals("10")) { 
							
							}
						}*/
					}
				  }
			 //System.out.println("new " + cnLatency.length);
			 for (int i=0; i < cnLatency.length; i++) {
				 //System.out.println("latency" + cnLatency[i]);  
				  
			 }
			 
			 synchronized(cnLatency) {
					//create the LSA packet to be sent out 
					//Build the LSA packet to be sent out   ConfigurationNode.cnId-packet_type-device_name-device_ip-port_number
					String a = ConfigurationNode.cnId;
					int age = 0; 
					int lsa_Length = 300;
					int number_of_links = cnLatency.length; 
					String links_info = ""; 
					//getting the links information 
					for (int w=0; w<cnLatency.length; w++) {
						
						String latencyCnId = ((cnLatency[w].split("="))[0]).substring(3);
						String latencyCnIp = ((cnLatency[w].split("-"))[3]);
						String latencyCnPort = ((cnLatency[w].split("-"))[4]);
						String latencyCnHostName = ((cnLatency[w].split("-"))[5]);
						
						String latencyCnId_combined = ConfigurationNode.cnId+"%"+latencyCnId + "'" + latencyCnIp + "'" + latencyCnPort + "'" + latencyCnHostName;
						String latencyCnCost = ((cnLatency[w].split("="))[3]); 
						String latencyIdCost = latencyCnId_combined + ";"+latencyCnCost; 
						
						//data entry 
						String data_to_be_added = latencyCnId_combined+"-"+latencyCnCost+"-0-0-"; 
						boolean match_found1 = false; 
						
						synchronized(ConfigurationNode.linkStateDatabase) {
							if (ConfigurationNode.linkStateDatabase.size() == 0) {
								ConfigurationNode.linkStateDatabase.add(data_to_be_added); 
								 
							}
							else {
								for (int j=0; j<ConfigurationNode.linkStateDatabase.size(); j++) {
									if (((ConfigurationNode.linkStateDatabase.get(j)).split("-")[0]).equalsIgnoreCase(latencyCnId_combined)) {
										match_found1 = true; 
										String database_entry = ConfigurationNode.linkStateDatabase.get(j); 
										String database_entry_sequence_number =  database_entry.split("-")[2]; 
										ConfigurationNode.linkStateDatabase.set(j, data_to_be_added);
									}
								}
								if (match_found1 == false) {
									ConfigurationNode.linkStateDatabase.add(data_to_be_added); 
								}
							}
						}
						links_info =  links_info + latencyIdCost + ":"; 
					}
					String lsa_msg = ConfigurationNode.cnId + "-lsa_advertising_node"+"-"+a+"-" +age+"-"+ConfigurationNode.lsSeqNum+"-"+lsa_Length+"-"+number_of_links+"-"+links_info+"-"; 
					//Add the header before putting in the queue: Header is the source_ip-source_port-destination_ip-destination_port
					synchronized (ConfigurationNode.attachedNodesInfo) {
						for (int b=0; b<(ConfigurationNode.attachedNodesInfo).size(); b++) {
							
							//Get the neighbor information 
							String router_to_send_link_cost_msg = (ConfigurationNode.attachedNodesInfo).get(b);
							
							//Extract the ip and port number to send the packet
							String neighbor_id = router_to_send_link_cost_msg.split("-")[0]; 
							String neighbor_ip = router_to_send_link_cost_msg.split("-")[1]; 
							String neighbor_port = router_to_send_link_cost_msg.split("-")[2];
							
							String packet_added_in_queue = ConfigurationNode.cnIp +"-"+ConfigurationNode.cnPort+"-"+neighbor_ip+"-"+neighbor_port +"-"+ lsa_msg;
							
							synchronized(ConfigurationNode.cnPacketQueue) {
								(ConfigurationNode.cnPacketQueue).offer(packet_added_in_queue);  
								(ConfigurationNode.lsSeqNum) ++; 
							}
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