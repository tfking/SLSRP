//###################################### ALIVE CHECKING STATUS ##################################################################
	class AliveChecking implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				synchronized (ConfigurationNode.attachedNodesInfo) {		//lock the attachedNodesInfo datastructures to process it. 
					/*Loop through the different attached nodes, check the value of the alive field 
						- Decrease it
						- check if it is 0
							- if 0, then the node is declared dead, take it out from the list of attached nodes. 
							- else, replace the value of the alive by (previous value - 1)
							- sleep for 10 seconds and do the same. 
						*/
					for (int i=0; i<(ConfigurationNode.attachedNodesInfo).size(); i++) {
						//Get the neighbor information 
						String neighbor_information = (ConfigurationNode.attachedNodesInfo).get(i);
						
						//Decrease the value of the alive message 
						int alive_time_remaining = Integer.parseInt(neighbor_information.split("-")[3])-1;
						
						if (alive_time_remaining == 0) {
							(ConfigurationNode.attachedNodesInfo).remove(i); 	
						}
						else {
							String new_adjacent_node_info = neighbor_information.split("-")[0] + "-" + neighbor_information.split("-")[1] + "-" + neighbor_information.split("-")[2] + "-" + alive_time_remaining + "-"; 
							(ConfigurationNode.attachedNodesInfo).set(i, new_adjacent_node_info);
						}
					}
				}
				try {
					Thread.sleep(8000); 
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	//##############################################################################################################################