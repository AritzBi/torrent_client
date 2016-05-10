package es.deusto.ssdd.peer.controller;

import java.util.HashMap;
import java.util.Observer;

import es.deusto.ssdd.peer.model.SocketManager;
import es.deusto.ssdd.peer.udp.messages.AnnounceResponse;

public class PeerTorrentInfoController {

	private SocketManager socketManager;
	
	public PeerTorrentInfoController ( SocketManager socketManager) {
		this.socketManager = socketManager;
	}
	
	public void addObserver(Observer o) {
		socketManager.addObserver(o);
	}
	
	public void deleteObserver ( Observer o ) {
		socketManager.deleteObserver(o);
	}
	
	public HashMap<String,AnnounceResponse> getInfoHashesWithResponse() {
		return socketManager.getInfoHashesWithResponse();
	}
}
