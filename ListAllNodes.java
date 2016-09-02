import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

class ListAllNodes extends JFrame  implements Runnable{
	
	@Override
	public void run() {
		
		Random r = new Random(); 
		JScrollPane scrollPane;
		JPanel topPanel, lastUpdatePanel;
		JTable table;
		JLabel nodesReachableOnLanLabel; 
		
		topPanel = new JPanel();
		topPanel.setLayout( new BorderLayout()); 
		
		lastUpdatePanel = new JPanel(); 		
		lastUpdatePanel.setLayout(new BoxLayout(lastUpdatePanel, BoxLayout.Y_AXIS)); 
		
		// TODO Auto-generated method stub
		while (true) {	
			
			topPanel.removeAll(); 
			lastUpdatePanel.removeAll(); 
			
			DateFormat dateFormat = new SimpleDateFormat("HH:mm:s"); 
			Date date = new Date(); 
			nodesReachableOnLanLabel = new JLabel("List of other CNs on the Reacheable LAN:                                 Last Update! " + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds());
			
			lastUpdatePanel.add(nodesReachableOnLanLabel); 
			
			// Create columns names
			String columnNames[] = { "Node ID", "Node Ip", "Node Port", "Host Name", "Latency (ms)" };
			
			// Create some data
			synchronized(ConfigurationNode.linkStateDatabase) {
				
				String dataValues[][] = new String [(ConfigurationNode.linkStateDatabase).size()][5]; 
				
				for (int i = 0; i < (ConfigurationNode.linkStateDatabase).size(); i ++) {
					int k = 0; 
					String nodeProcessed = (ConfigurationNode.linkStateDatabase).get(i); 
					
					System.out.println((nodeProcessed.split("-")[0]).split("%")[0]);
					
					if (((nodeProcessed.split("-")[0]).split("%")[0]).equals(ConfigurationNode.cnId)) {
						
						dataValues[i][k] = ((nodeProcessed.split("-")[0]).split("%")[1]).split("'")[0]; 
						dataValues[i][k+1] =((nodeProcessed.split("-")[0]).split("%")[1]).split("'")[1];
						dataValues[i][k+2] = ((nodeProcessed.split("-")[0]).split("%")[1]).split("'")[2];
						dataValues[i][k+3] = ((nodeProcessed.split("-")[0]).split("%")[1]).split("'")[3];
						dataValues[i][k+4] = (nodeProcessed.split("-")[1]);
					}
						
				}
				
				// Create a new table instance
				table = new JTable(dataValues, columnNames );
			}
			// Add the table to a scrolling pane
			scrollPane = new JScrollPane( table );
			scrollPane.setPreferredSize(new Dimension(480, 300)); 
			
			topPanel.add(scrollPane, BorderLayout.CENTER );
			
			ConfigurationNode.content.add(lastUpdatePanel);
			ConfigurationNode.content.add(topPanel);
		
			int randWidth = r.nextInt(9) + 1;  
			int randHeight = r.nextInt(9) + 1;
			
			ConfigurationNode.getFrames()[0].setSize(550 + randWidth, 500 + randHeight);
			
			 try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
		}
	}
}
