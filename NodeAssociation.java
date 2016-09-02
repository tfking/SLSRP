	/*This class will:
	 * 	- Loop through the cn_beacon_received Linkedlist, 
	 *  - check the version number present in the beacon, if equal to the one of the cn, 
	 *  - associate the two (add the cn in the neighbor_linked_list)
	*/
class NodeAssociation implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true) {
				synchronized(ConfigurationNode.cnBeaconsReceived) {
					if ((ConfigurationNode.cnBeaconsReceived).size() > 0) {
						String cn_beacon_packet = (ConfigurationNode.cnBeaconsReceived).peek(); 
						
						//extract the version number received to compare
						String cn_version_received = cn_beacon_packet.split("-")[4];
						String cnId_received = cn_beacon_packet.split("-")[0];  
						String cnIp_received = cn_beacon_packet.split("-")[2];
						String cnPort_received = cn_beacon_packet.split("-")[3]; 
						String alive_interval_received = cn_beacon_packet.split("-")[5];
						String hostName = cn_beacon_packet.split("-")[6];
						
						if (cn_version_received.equals(""+ConfigurationNode.versionNumber) && !cnId_received.equals(ConfigurationNode.cnId)) {
							
							String adjacent_node_info = cnId_received+ "-" + cnIp_received + "-" + cnPort_received + "-" + alive_interval_received + "-" + hostName + "-"; 
							synchronized(ConfigurationNode.attachedNodesInfo) {
								boolean cn_present = false; 
								for (int i=0; i<(ConfigurationNode.attachedNodesInfo).size(); i++) {
									//Get the neighbor information 
									String neighbor_information = (ConfigurationNode.attachedNodesInfo).get(i);
									if ((neighbor_information.split("-")[0]).equals(cnId_received)) {
										cn_present = true;
										(ConfigurationNode.attachedNodesInfo).set(i, adjacent_node_info);
									}
								}
								if (cn_present == false)
									(ConfigurationNode.attachedNodesInfo).offer(adjacent_node_info); 
							}
						}
						
						(ConfigurationNode.cnBeaconsReceived).remove(); 
						
						synchronized(ConfigurationNode.attachedNodesInfo) {
						if ((ConfigurationNode.attachedNodesInfo).size() >0 ) {
							for (int i=0; i<(ConfigurationNode.attachedNodesInfo).size(); i++) {
								//Get the neighbor information 
								String neighbor_information = (ConfigurationNode.attachedNodesInfo).get(i);
								//System.out.println("neighbor info is: " +neighbor_information); 
							}
							
							//System.out.println("new ");
						}}
						//System.out.println("next round"); 
					}
				}
				try {
					Thread.sleep(9000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}