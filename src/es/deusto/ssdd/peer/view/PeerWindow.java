package es.deusto.ssdd.peer.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import es.deusto.ssdd.peer.controller.PeerWindowController;

public class PeerWindow extends JPanel implements ActionListener,Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Label lblIpAddress;
	private Label lblPort;
	private Label lblInfoHash;
	
	private JTextField txtIpAddress;
	private JTextField txtPort;
	private JTextField txtInfoHash;
	
	private JButton addTorrent;
	private JButton addTorrentFromFile;
	private JButton btnScrapeTorrentFromFile;
	
	private JTextArea responseTxtArea;
	
	private PeerWindowController peerWindowController;
	
	public PeerWindow(PeerWindowController peerWindowController) {
		super(new BorderLayout());
		this.peerWindowController = peerWindowController;
		peerWindowController.addObserver(this);
		setUpPanel();
	}
	
	public void setUpPanel () {
	// Specify the labels
	lblIpAddress = new Label("IP address");
	lblIpAddress.setFont(new Font("Serif", Font.BOLD, 14));
	lblPort = new Label("Port (Trackers)");
	lblPort.setFont(new Font("Serif", Font.BOLD, 14));
	lblInfoHash = new Label("Info_hash");
	lblInfoHash.setFont(new Font("Serif", Font.BOLD, 14));

	// Specify the box for IP ADDRESS
	Box boxForIpAddress = Box.createVerticalBox();
	boxForIpAddress.add(Box.createVerticalGlue());
	txtIpAddress = new JTextField();
	txtIpAddress.setColumns(20);
	txtIpAddress.setMaximumSize(new Dimension(Integer.MAX_VALUE,
			txtIpAddress.getPreferredSize().height));
	txtIpAddress.setText("224.0.0.4");
	boxForIpAddress.add(txtIpAddress);
	boxForIpAddress.add(Box.createVerticalGlue());

	// Specify the box for PORT
	Box boxForPort = Box.createVerticalBox();
	boxForPort.add(Box.createVerticalGlue());
	txtPort = new JTextField();
	txtPort.setColumns(20);
	txtPort.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtPort
			.getPreferredSize().height));
	txtPort.setText("1150");
	boxForPort.add(txtPort);
	boxForPort.add(Box.createVerticalGlue());

	// Specify the box for PORT FOR PEERS
	Box boxForInfoHash = Box.createVerticalBox();
	boxForInfoHash.add(Box.createVerticalGlue());
	txtInfoHash = new JTextField();
	txtInfoHash.setColumns(20);
	txtInfoHash.setMaximumSize(new Dimension(Integer.MAX_VALUE,
			txtInfoHash.getPreferredSize().height));
	txtInfoHash.setText("B415C913643E5FF49FE37D304BBB5E6E11AD5101");
	boxForInfoHash.add(txtInfoHash);
	boxForInfoHash.add(Box.createVerticalGlue());
	
	JPanel panelForResponse = new JPanel();
	responseTxtArea = new JTextArea();
	responseTxtArea.setRows(5);
	panelForResponse.add(responseTxtArea);

	// Specify a panel per label/input field
	JPanel panelIpAddress = new JPanel(new GridLayout(1, 0));
	panelIpAddress.add(lblIpAddress);
	panelIpAddress.add(boxForIpAddress);

	JPanel panelPort = new JPanel(new GridLayout(1, 0));
	panelPort.add(lblPort);
	panelPort.add(boxForPort);

	JPanel panelInfoHash = new JPanel(new GridLayout(1, 0));
	panelInfoHash.add(lblInfoHash);
	panelInfoHash.add(boxForInfoHash);

	JPanel panelData = new JPanel(new GridLayout(4, 0));
	panelData.add(panelIpAddress);
	panelData.add(panelPort);
	panelData.add(panelInfoHash);

	// Specify the button panel
	addTorrent = new JButton("Add Torrent");
	addTorrent.addActionListener(this);
	addTorrentFromFile = new JButton("Add Torrent From File");
	addTorrentFromFile.addActionListener(this);
	btnScrapeTorrentFromFile = new JButton("Scrape From File");
	btnScrapeTorrentFromFile.addActionListener(this);

	JPanel buttonPane = new JPanel(new FlowLayout());
	buttonPane.add(addTorrent);
	buttonPane.add(addTorrentFromFile);
	buttonPane.add(btnScrapeTorrentFromFile);

	setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	add(panelData, BorderLayout.NORTH);
	add(buttonPane, BorderLayout.SOUTH);
	add(responseTxtArea, BorderLayout.CENTER);
	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String ipAddress = txtIpAddress.getText();
		int port = Integer.valueOf(txtPort.getText());
		
		if (e.getSource().equals(addTorrent)) {
			
			String torrentFile = txtInfoHash.getText();
			
			String message = null;
			if ( ipAddress == null || ipAddress.isEmpty() || port == 0 || torrentFile == null || torrentFile.isEmpty() )
			{
				message = "No has especificado alguno de los valores necesarios";
			}
			if ( message != null )
				JOptionPane.showMessageDialog(null, message);
			else
				peerWindowController.addTorrent(ipAddress, port, torrentFile);
		}
		
		else if ( e.getSource().equals(addTorrentFromFile)) {
			
			
			String message = null;
			if ( ipAddress == null || ipAddress.isEmpty() | port == 0 )
			{
				message = "No has especificado alguno de los valores necesarios";
			}
			if ( message != null )
				JOptionPane.showMessageDialog(null, message);
			else
			{
				String pathname = getTorrentFile(ipAddress, port);
				peerWindowController.addTorrentFromFile(pathname);
			}

		}
		else if ( e.getSource().equals(btnScrapeTorrentFromFile))
		
		{
			String message = null;
			if ( ipAddress == null || ipAddress.isEmpty() || port == 0 )
			{
				message = "No has especificado alguno de los valores necesarios";
			}
			if ( message!= null )
				JOptionPane.showMessageDialog(null, message);
			else
			{
				String pathname = getTorrentFile(ipAddress, port);
				peerWindowController.scrapeTorrentFromFile(ipAddress, port, pathname);
			}
		}
	}
	
	public String getTorrentFile ( String ipAddress, int port ) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"TORRENT FILES", "torrent", "torrent");
		chooser.setFileFilter(filter);
		chooser.setApproveButtonText("Accept");
		chooser.setDialogTitle("Choose your torrent");
		chooser.showOpenDialog(null);
		String pathname = null;
		
		try {
			pathname = chooser.getSelectedFile().getAbsolutePath();
		} catch (Exception e) {
			System.out.println("No torrent file selected");
		}
		
		return pathname;

	}

	@Override
	public void update(Observable observable, Object object) {
		if ( object instanceof String )
		{
			responseTxtArea.setText((String) object);
		}
		
	}

}
