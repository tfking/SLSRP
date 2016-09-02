//Importing Statements
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import javax.net.ssl.HostnameVerifier;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class ConfigurationNode extends JFrame{

	//Declaring the CN Variables
	static Container content; 
	
	private JButton addCn; 
	
	static JLabel added_successfully_label, cn_information_label, cnId_label, cnIp_label, cnPort_label, hostNameLabel, space_label,
					version_protocol_number_label;
	
	static JPanel cn_information_panel; 
	
	public static String deviceIpName, cnIp, cnId, deviceName; 
	
	public static int cnPort, versionNumber, lsSeqNum = 0; 
	
	static Queue<String> cnBeaconsReceived = new LinkedList<String>(); //store the ID of CN nodes received through the beacon
	static Queue<String> cnPacketQueue = new LinkedList<String>();
	static Queue<String> cnLatencyQueue = new LinkedList<String>();
	static Queue<String> lsaQueue = new LinkedList<String>();
	
	//Datastructure to store the info of the nodes attached to this current node. 
	public static LinkedList <String> attachedNodesInfo;
	public static LinkedList <String> linkStateDatabase;
	
	public ConfigurationNode() {
		//Getting the container and setting the layout manager
		content = getContentPane();
		content.setLayout(new FlowLayout()); 
		
		//Instantiating the component of the GUI
		added_successfully_label = new JLabel("The CN has been successfully added in the local Network!"); 
		cn_information_label = new JLabel("CN Information: ");
		space_label = new JLabel("            ");  
		
		addCn = new JButton("    Add Communication Node    "); 
		
		//Instantiating the different panels where components will be added. 
		cn_information_panel = new JPanel(); 
		
		//Setting the Panel's Layout
		cn_information_panel.setLayout(new BoxLayout(cn_information_panel, BoxLayout.Y_AXIS)); 
		
		//Instantiate the event handler 
		ButtonHandler bh = new ButtonHandler();
				
		//adding Listeners to Button 
		addCn.addActionListener(bh);
		
		//Adding the GUI components into the GUI interface. 
		content.add(addCn); 
	}
	
	public class ButtonHandler implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent ae) {
			// TODO Auto-generated method stub
			
			if (ae.getSource() == addCn) {
				//Create the array to store attached CN information. 
				attachedNodesInfo = new LinkedList<String>(); 
				linkStateDatabase = new LinkedList<String>();
				
				//Get the  informations of the Communication Node
				try {
					deviceIpName = "" + InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Extracting the device IP 
				cnIp= deviceIpName.substring(deviceIpName.indexOf("/")+1);
				
				//Extracting the device Name
				deviceName = deviceIpName.substring(deviceIpName.indexOf(" ")+1, deviceIpName.indexOf("/"));
				
				//Automatically finding a free port on the localhost to associate it with the CN
				ServerSocket test_port;
				try {
					test_port = new ServerSocket(0);
					cnPort = test_port.getLocalPort(); 
					test_port.close(); 
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Considering the port number as the ID
				cnId = "" + cnPort; 
				
				versionNumber = 1; //(int) (Math.random()*2); 
				
				//Instantiating the CN information labels
				cnId_label = new JLabel("                                  CN ID Number:             " + cnId);
				cnIp_label = new JLabel("                                  CN IP Address:            " + cnIp);
				cnPort_label = new JLabel("                                  CN Port Number:         " + cnPort);
				hostNameLabel= new JLabel("                                  Host Name:                   " + deviceName);
				version_protocol_number_label = new JLabel("                                  Version Number:         " + versionNumber); 
	
				//Start the broadcast socket to send broadcast messages  
				new Thread(new CnSendBroadcastMessages()).start();
				
				//start the broadcast socket to receive broadcast messages 
				new Thread(new CnReceiveBroadcastMessages()).start();
				
				//start the node_association thread
				new Thread(new NodeAssociation()).start();
				
				//start the node_association thread
				new Thread(new AliveChecking()).start();
				
				//start the node_association thread
				new Thread(new LatencyCalculation()).start();
			
				//start the thread that received packets 
				new Thread(new SocketReceivedPackets()).start();
				
				//start the thread that sends out packets 
				new Thread(new SocketSendPacket()).start();
				
				//List all nodes
				new Thread(new ListAllNodes()).start();
				
				//connectivity share
				new Thread(new LatencyStateDatabase()).start();
				
				content.remove(addCn);  
				cn_information_panel.add(added_successfully_label);
				cn_information_panel.add(space_label);
				cn_information_panel.add(space_label);
				cn_information_panel.add(cn_information_label);
				cn_information_panel.add(cnId_label);
				cn_information_panel.add(cnIp_label);
				cn_information_panel.add(cnPort_label);
				cn_information_panel.add(hostNameLabel);
				cn_information_panel.add(version_protocol_number_label);
				cn_information_panel.add(space_label);
				cn_information_panel.add(space_label);
				
				content.add(cn_information_panel);
				setSize(400, 150); 
			}
		}

	}
	
	public static void main(String[] args) {
		ConfigurationNode cn = new ConfigurationNode(); 
		cn.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		cn.setSize(350, 100); 
		cn.setVisible(true); 
		cn.setTitle("Distributed Communications System"); 
		cn.setResizable(false); 
	}

}
