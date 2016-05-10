package es.deusto.ssdd.peer.controller;

import java.util.Observer;

import es.deusto.ssdd.peer.model.SocketManager;

public class PeerWindowController {
	private SocketManager socketManager;
	
	public PeerWindowController(SocketManager so){
		socketManager=so;
	}
	
	public void addObserver(Observer o) {
		socketManager.addObserver(o);
	}
	
	public void deleteObserver ( Observer o ) {
		socketManager.deleteObserver(o);
	}
	
	public void addTorrentFromFile( String path){
		socketManager.addTorrentFromFile( path );
	}
	
	public void scrapeTorrentFromFile (String ipAddress, int port, String path){
		socketManager.scrapeTorrentFromFile(ipAddress, port, path);
	}
	
	public void addTorrent ( String ipAddress, int port, String torrentFile )
	{
		socketManager.addTorrent(ipAddress, port , torrentFile );
	}
}
