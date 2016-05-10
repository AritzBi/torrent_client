package es.deusto.ssdd.peer.model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Random;

import es.deusto.ssdd.peer.udp.messages.AnnounceRequest;
import es.deusto.ssdd.peer.udp.messages.AnnounceRequest.Event;
import es.deusto.ssdd.peer.udp.messages.PeerInfo;

public class AnnounceHandler implements Runnable{
	private SocketManager socketManager;
	private long startTime;
	private long elapsedTime;
	private boolean stop;
	public AnnounceHandler(SocketManager socketManager){
		this.socketManager=socketManager;
		startTime=System.currentTimeMillis();
		elapsedTime=0;
		stop=false;
	}
	@Override
	public void run() {
		intervalCounter();
	}
	
private void sendAnnounceRequestMessage() {
		
		AnnounceRequest msgAnnounceRequest = new AnnounceRequest();
		msgAnnounceRequest.setConnectionId(socketManager.getConnectionId());
		msgAnnounceRequest.setInfoHash( socketManager.getTorrentFile() );
		int i=AnnounceHandler.randInt(0, 1);
		if(i==0){
			msgAnnounceRequest.setEvent(Event.STARTED);
		}else{
			msgAnnounceRequest.setEvent(Event.COMPLETED);
		}
		msgAnnounceRequest.setTransactionId(new Random().nextInt(Integer.MAX_VALUE));
		msgAnnounceRequest.setDownloaded(AnnounceHandler.randInt(0, 200) );
		msgAnnounceRequest.setUploaded(AnnounceHandler.randInt(0, 200) );
		
		PeerInfo peerInfo = new PeerInfo();
		//I want that the tracker uses the sender of the upd packet
		peerInfo.setIpAddress(0);
		peerInfo.setPort(SocketManager.DEFAULT_PORT_PEER);
		msgAnnounceRequest.setPeerInfo(peerInfo);
		//The default value -1
		msgAnnounceRequest.setNumWant(-1);
		
		//Send the message over the socket...
		try {
			byte [] message = msgAnnounceRequest.getBytes();
			DatagramPacket announceRequest = new DatagramPacket(message, message.length, socketManager.getInetAddress(), socketManager.getPortForPeers());
			socketManager.sendMessageToSocket(announceRequest);
		} catch (IOException e) {
			System.err.println("# IO EXCEPTION (sendConnectRequestMessage) "
					+ e.getMessage());
		}
		
	}

	public void intervalCounter(){
		while(!stop){
			elapsedTime=System.currentTimeMillis()-startTime;
			if(elapsedTime>=(socketManager.getInterval())*1000){
				System.out.println("Sending new announce");
				elapsedTime=0;
				this.sendAnnounceRequestMessage();
				startTime=System.currentTimeMillis();
			}
		}
	}
	
	public static int randInt(int min, int max) {
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
}
