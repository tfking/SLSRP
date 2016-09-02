class LatencyStateDatabase implements Runnable  {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true) {
			synchronized (ConfigurationNode.lsaQueue) {
					if ((ConfigurationNode.lsaQueue).size() > 0 ) {
						String receivedLsaMessage = (ConfigurationNode.lsaQueue).peek();  
						
						if (receivedLsaMessage.contains("lsa_advertising_node")) {
							boolean match_found = false; 
							
							//retrieve the needed information
							String advertisingNodeId = receivedLsaMessage.split("-")[6];
							String senderNodeId = receivedLsaMessage.split("-")[4];
							String lsaSequenceNumber = receivedLsaMessage.split("-")[8];
							String lsa_lenght = receivedLsaMessage.split("-")[9];
							String numberOfLinksLsa = receivedLsaMessage.split("-")[10];
							String ageLsa = receivedLsaMessage.split("-")[7];
							String infoLink = receivedLsaMessage.split("-")[11];
							
							String [] infoLinkArray = infoLink.split(":"); 
							
							for (int i=0; i<infoLinkArray.length; i++) {
								String currentInfoLink = infoLinkArray[i]; 
								
								String sourceDest = currentInfoLink.split(";")[0];
								String latencyVal = currentInfoLink.split(";")[1];
								
								//data entry 
								String dataToBeAdded = sourceDest+"-"+latencyVal+"-"+lsaSequenceNumber+"-"+ageLsa; 
								
								synchronized(ConfigurationNode.linkStateDatabase) {
									if ((ConfigurationNode.linkStateDatabase).size() == 0) {
										(ConfigurationNode.linkStateDatabase).add(dataToBeAdded); 
										 
									}
									else {
										for (int j=0; j<(ConfigurationNode.linkStateDatabase).size(); j++) {
											if ((((ConfigurationNode.linkStateDatabase).get(j)).split("-")[0]).equalsIgnoreCase(sourceDest)) {
												match_found = true; 
												String databaseEntry = (ConfigurationNode.linkStateDatabase).get(j); 
												String databaseEntrySequenceNumber =  databaseEntry.split("-")[2]; 
												
												if (Integer.parseInt(databaseEntrySequenceNumber) < Integer.parseInt(lsaSequenceNumber)) {
													(ConfigurationNode.linkStateDatabase).set(j, dataToBeAdded); 
												}
											}
										}
										if (match_found == false) {
											(ConfigurationNode.linkStateDatabase).add(dataToBeAdded); 
										}
									}
									
									String database = ""; 
									for (int k=0; k<(ConfigurationNode.linkStateDatabase).size(); k++) {
										database = database + "_"+(ConfigurationNode.linkStateDatabase).get(k); 
									}
									System.out.println("The actual link database is:  " + database);

								}
					    	}
							
							//Send the received lsa to all your neigbors; except the neigbor who sent it 
							String lsaToBeForwarded = ConfigurationNode.cnId+"-lsa_advertising_node-"+advertisingNodeId+"-"+ageLsa+"-"+lsaSequenceNumber+"-"+lsa_lenght+"-"+numberOfLinksLsa+"-"+infoLink+"-"; 
							
							synchronized (ConfigurationNode.attachedNodesInfo) {
								for (int b=0; b<(ConfigurationNode.attachedNodesInfo).size(); b++) {
									
									//Get the neighbor information 
									String router_to_send_link_cost_msg = (ConfigurationNode.attachedNodesInfo).get(b);
									
									//Extract the ip and port number to send the packet
									String neighbor_id = (router_to_send_link_cost_msg.split("-")[0])/*.substring(6)*/;  
									
									if (!neighbor_id.equals(advertisingNodeId) && !neighbor_id.equals(senderNodeId)) {
										String neighbor_ip = router_to_send_link_cost_msg.split("-")[1]; 
										String neighbor_port = router_to_send_link_cost_msg.split("-")[2];
										
										String packetAddedInQueue = ConfigurationNode.cnId +"-"+ConfigurationNode.cnPort+"-"+neighbor_ip+"-"+neighbor_port +"-"+ lsaToBeForwarded;
										
										synchronized(ConfigurationNode.cnPacketQueue) {
											(ConfigurationNode.cnPacketQueue).offer(packetAddedInQueue);   
									}
								  }
								}
							}
							
							(ConfigurationNode.lsaQueue).remove(); 
						}
						else 
							(ConfigurationNode.lsaQueue).remove(); 
					}
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}