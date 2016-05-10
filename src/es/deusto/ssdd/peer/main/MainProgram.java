package es.deusto.ssdd.peer.main;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import es.deusto.ssdd.peer.controller.PeerTorrentInfoController;
import es.deusto.ssdd.peer.controller.PeerWindowController;
import es.deusto.ssdd.peer.model.SocketManager;
import es.deusto.ssdd.peer.view.MainView;
import es.deusto.ssdd.peer.view.PeerTorrentInfoWindow;
import es.deusto.ssdd.peer.view.PeerWindow;

public class MainProgram {

	public static void main ( String [] args ){
		
		SocketManager socketManager = new SocketManager();
		
		PeerWindowController peerWindowController = new PeerWindowController(socketManager);
		
		PeerWindow peerWindow = new PeerWindow(peerWindowController);
		
		PeerTorrentInfoController peerTorrentInfoController = new PeerTorrentInfoController(socketManager);
		
		PeerTorrentInfoWindow peerTorrentInfoWindow = new PeerTorrentInfoWindow(peerTorrentInfoController);


		Map<String, JPanel> panels = new HashMap<String, JPanel>();
		panels.put("2.-Torrent List", peerTorrentInfoWindow);
		panels.put("1.-Configuration", peerWindow);

		MainView mainWindow = new MainView(panels);
		
		mainWindow.setVisible(true);
	}
}
