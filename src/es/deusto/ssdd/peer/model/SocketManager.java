package es.deusto.ssdd.peer.model;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Random;

import es.deusto.ssdd.peer.metainfo.MetainfoFile;
import es.deusto.ssdd.peer.metainfo.handler.MetainfoHandler;
import es.deusto.ssdd.peer.metainfo.handler.MetainfoHandlerMultipleFile;
import es.deusto.ssdd.peer.metainfo.handler.MetainfoHandlerSingleFile;
import es.deusto.ssdd.peer.udp.messages.AnnounceRequest;
import es.deusto.ssdd.peer.udp.messages.AnnounceRequest.Event;
import es.deusto.ssdd.peer.udp.messages.AnnounceResponse;
import es.deusto.ssdd.peer.udp.messages.BitTorrentUDPMessage.Action;
import es.deusto.ssdd.peer.udp.messages.ConnectRequest;
import es.deusto.ssdd.peer.udp.messages.ConnectResponse;
import es.deusto.ssdd.peer.udp.messages.PeerInfo;
import es.deusto.ssdd.peer.udp.messages.ScrapeInfo;
import es.deusto.ssdd.peer.udp.messages.ScrapeRequest;
import es.deusto.ssdd.peer.udp.messages.ScrapeResponse;
import es.deusto.ssdd.peer.vo.Utils;
import es.deusto.ssdd.peer.udp.messages.Error;

public class SocketManager implements Runnable {

	private DatagramSocket socket;
	private InetAddress inetAddress;
	
	private int portForPeers = 0;
	private String trackerHost = null;
	//Variable to know what torrent file has requesting the peer
	private String torrentFile = null;
	private Map<Integer,List<String>> scrapeInfohashes;
	private Long connectionId = null;
	private int interval;
	public boolean stopListeningPackets = false;
	
	public static int DEFAULT_PORT_PEER = 3333;
	
	private String pathName = null;
	private List<Observer> observers;
	private HashMap<String, AnnounceResponse> infoHashesWithResponses;
	private Thread threadHandler;
	
	private static String ERROR_CONNECTION_ID_EXPIRED = "The connection id has been expired";
	
	public SocketManager() {
		observers = new ArrayList<Observer>();
		infoHashesWithResponses = new HashMap<String,AnnounceResponse>();
		scrapeInfohashes = new HashMap<Integer,List<String>>();
	}

	private void createSocket() {
		try {
			inetAddress = InetAddress.getByName(trackerHost);
			socket = new DatagramSocket();
			
		} catch (IOException e) {
			System.err.println("# IO EXCEPTION: Error creating socket "
					+ e.getMessage());
		}
	}
	
	public void addTorrent ( String ipAddress, int port, String torrentFile ) {
		this.trackerHost = ipAddress;
		this.portForPeers = port;
		this.torrentFile = torrentFile;
		Thread socketThread = new Thread(this);
		socketThread.start();
		try {
			Thread.sleep(1000);
			sendAnnounceRequestMsg();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	private void sendAnnounceRequestMsg () {
		//depending if the user has chosen "Add torrent" or "Add torrent from file"
		if ( pathName != null ){ //"Add torrent from File"
			sendAnnounceRequestMessage(connectionId, getInfoFromTorrentFile( pathName ), null );
			pathName = null;
		}
		else //"Add Torrent"
			sendAnnounceRequestMessage(connectionId, null, torrentFile );
	}

	
	public void addTorrentFromFile ( String pathName ){
		this.pathName = pathName;
		MetainfoFile<?> torrentFile=this.getInfoFromTorrentFile(pathName);
		System.out.println(torrentFile.getUDPAnnounceList());
		if(!torrentFile.getUDPAnnounceList().isEmpty()){
			String udpURI=torrentFile.getUDPAnnounceList().get(0);
			udpURI=udpURI.replace("udp://","");
			udpURI=udpURI.split("/")[0];
			String[] infoURI=udpURI.split(":");
			this.trackerHost=infoURI[0];
			this.portForPeers=Integer.parseInt(infoURI[1]);
			System.out.println(trackerHost);
			System.out.println(portForPeers);
			Thread socketThread = new Thread(this);
			socketThread.start();
			
			try {
				Thread.sleep(1000);
				sendAnnounceRequestMsg();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void scrapeTorrentFromFile ( String ipAddress, int port, String pathName ){
		this.pathName = pathName;
		this.trackerHost = ipAddress;
		this.portForPeers = port;
		
		Thread socketThread = new Thread(this);
		socketThread.start();
		
		try {
			Thread.sleep(3000);
			sendScrapeRequestMessage();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	

	private MetainfoFile<?> getInfoFromTorrentFile(String pathname) {
		MetainfoFile<?> info = null;
		try {
			File folder = new File(pathname);
			MetainfoHandler<?> handler = null;

			if (folder.isDirectory()) {
				for (File torrent : folder.listFiles()) {
					try {
						if (torrent.getPath().contains(".torrent")) {
							handler = new MetainfoHandlerSingleFile();
							handler.parseTorrenFile(torrent.getPath());
						}
					} catch (Exception ex) {
						if (torrent.getPath().contains(".torrent")) {
							handler = new MetainfoHandlerMultipleFile();
							handler.parseTorrenFile(torrent.getPath());
						}
					}
					if (handler != null) {
						info = handler.getMetainfo();
					}
				}
			}else{
				try {
						handler = new MetainfoHandlerSingleFile();
						handler.parseTorrenFile(pathname);

				} catch (Exception ex) {
						handler = new MetainfoHandlerMultipleFile();
						handler.parseTorrenFile(pathname);
					
				}
				if (handler != null) {
					info = handler.getMetainfo();
				}
			}

		} catch (Exception ex) {
			System.err
					.println("# SocketMaanger (method getInfoFromTorrentFile): "
							+ ex.getMessage());
		}
		return info;
	}

	private void sendConnectRequestMessage() {
		ConnectRequest msgConnectRequest = new ConnectRequest(); 
		msgConnectRequest.setTransactionId(new Random().nextInt(Integer.MAX_VALUE));
		try {
			DatagramPacket connectRequest = new DatagramPacket(msgConnectRequest.getBytes(), msgConnectRequest.getBytes().length, inetAddress, portForPeers);
			sendMessageToSocket(connectRequest);
		} catch (IOException e) {
			System.err.println("# IO EXCEPTION (sendConnectRequestMessage) "
					+ e.getMessage());
		}
		
	}
	
	private void sendScrapeRequestMessage() {
		ScrapeRequest scrapeRequest = new ScrapeRequest();
		if ( connectionId != null )
		{
			scrapeRequest.setConnectionId(connectionId );
			scrapeRequest.setTransactionId(new Random().nextInt(Integer.MAX_VALUE));
			
			MetainfoFile<?> infoFile = getInfoFromTorrentFile(pathName);
			scrapeRequest.addInfoHash(infoFile.getInfo().getHexInfoHash(), infoFile.getInfo().getInfoHash());
			scrapeInfohashes.put( scrapeRequest.getTransactionId(), scrapeRequest.getInfoHashes());
			try {
				byte [] message = scrapeRequest.getBytes();
				DatagramPacket scrapeRequestPckge = new DatagramPacket(message, message.length, inetAddress, portForPeers);
				sendMessageToSocket(scrapeRequestPckge);
			} catch (IOException e) {
				System.err.println("# IO EXCEPTION (sendScrapeRequestMessage) "
						+ e.getMessage());
			}
			
		}

	}
	
	/**
	 * If infohash arrives null means that the user has chosen a torrent from file
	 * @param connectionId
	 * @param metaInfoFile
	 * @param infoHash
	 */
	private void sendAnnounceRequestMessage( long connectionId, MetainfoFile<?> metaInfoFile, String infoHash ) {
		
		AnnounceRequest msgAnnounceRequest = new AnnounceRequest();
		msgAnnounceRequest.setConnectionId(connectionId);
		
		if ( infoHash == null )
		{
			Utils.putInfoHash(metaInfoFile.getInfo().getHexInfoHash(), metaInfoFile.getInfo().getInfoHash() );
			msgAnnounceRequest.setInfoHash( metaInfoFile.getInfo().getHexInfoHash() );
			torrentFile = metaInfoFile.getInfo().getHexInfoHash();
		}
		else
		{
			msgAnnounceRequest.setInfoHash( infoHash );
		}
		msgAnnounceRequest.setEvent(Event.NONE);
		msgAnnounceRequest.setTransactionId(new Random().nextInt(Integer.MAX_VALUE));
		msgAnnounceRequest.setDownloaded(new Long ( 0 ) );
		msgAnnounceRequest.setUploaded(new Long ( 0 ) );
		if ( infoHash == null )
			msgAnnounceRequest.setLeft(new Long ( metaInfoFile.getInfo().getLength() ) );
		
		PeerInfo peerInfo = new PeerInfo();
		//I want that the tracker uses the sender of the upd packet
		peerInfo.setIpAddress(0);
		peerInfo.setPort(DEFAULT_PORT_PEER);
		msgAnnounceRequest.setPeerInfo(peerInfo);
		//The default value -1
		msgAnnounceRequest.setNumWant(-1);
		
		//Send the message over the socket...
		try {
			byte [] message = msgAnnounceRequest.getBytes();
			DatagramPacket announceRequest = new DatagramPacket(message, message.length, inetAddress, portForPeers);
			sendMessageToSocket(announceRequest);
		} catch (IOException e) {
			System.err.println("# IO EXCEPTION (sendConnectRequestMessage) "
					+ e.getMessage());
		}
		
	}
	
	public synchronized void sendMessageToSocket ( DatagramPacket datagramPacket ) throws IOException
	{
		socket.send(datagramPacket);
	}

	@Override
	public void run() {
		createSocket();
		if ( connectionId == null )
		{
			sendConnectRequestMessage();
		}
		socketListeningPackets();

	}
	
	private void socketListeningPackets() {
		try {
			
			while (!stopListeningPackets) {
				
				byte[] buffer = new byte[256];
				DatagramPacket response = new DatagramPacket(buffer, buffer.length);
				socket.receive(response);
				System.out.println(response);
				
				if ( isConnectResponseMessage(response) )
				{
					ConnectResponse msgConnectResponse = ConnectResponse.parse(  Arrays.copyOfRange(response.getData(), 0, response.getLength()));
					connectionId = msgConnectResponse.getConnectionId();
					System.out.println(connectionId);
				}
				 else if (isAnnounceResponseMessage(response))
				 {
					 System.out.println("Is announce request");
					 AnnounceResponse announceResponse = AnnounceResponse.parse( Arrays.copyOfRange(response.getData(), 0, response.getLength()));
					 infoHashesWithResponses.put(torrentFile, announceResponse);
					 interval = announceResponse.getInterval();
					 notifyObservers(announceResponse);
					 if(threadHandler==null){
						 threadHandler= new Thread(new AnnounceHandler(this));
						 threadHandler.start();
						 
					 }
				}
				 else if (isScrapeResponseMessage(response))
				 {
					 ScrapeResponse scrapeResponse = ScrapeResponse.parse(Arrays.copyOfRange ( response.getData(), 0, response.getLength() ));
					 List<String> infoHashes = scrapeInfohashes.get( scrapeResponse.getTransactionId() );
					 List<ScrapeInfo> scrapeInfos = scrapeResponse.getScrapeInfos();
					 String responseForTxtArea = null;
					 int i = 0;
					 for ( String infoHash: infoHashes )
					 {
						 ScrapeInfo scrapeInfo = scrapeInfos.get(i);
						 if ( scrapeInfo != null )
						 {
							 if ( responseForTxtArea == null)
								 responseForTxtArea = "Infohash: " + infoHash + " Seeders: " + scrapeInfo.getSeeders() + " Leechers: " + scrapeInfo.getLeechers() + " Completed: " +  scrapeInfo.getCompleted() +"\n";
							 else
								 responseForTxtArea += "Infohash: " + infoHash + " Seeders: " + scrapeInfo.getSeeders() + " Leechers: " + scrapeInfo.getLeechers() + " Completed: " +  scrapeInfo.getCompleted() +"\n";
						 }
					 }
					 notifyObservers(responseForTxtArea);
				 }
				 else if ( isErrorMessage(response) )
				 {
					 Error error = Error.parse(Arrays.copyOfRange ( response.getData(), 0, response.getLength() ));
					 if ( error.getMessage() != null )
					 {
						if ( error.getMessage().equals(ERROR_CONNECTION_ID_EXPIRED))
						{
							connectionId = null;
						}
						notifyObservers(error.getMessage());
					 }
				 }
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*** METHODS USED TO VALIDATE THE DIFFERENT RESPONSE MESSAGES RECEIVED BY THE PEER **/
	private boolean isScrapeResponseMessage ( DatagramPacket packet ) {
		boolean isScrapeResponse = true;
		
		if ( packet.getLength() < 8 )
		{
			isScrapeResponse = false;
		}
		if ( isScrapeResponse )
		{
			ScrapeResponse scrapeResponse = ScrapeResponse.parse( Arrays.copyOfRange(packet.getData(), 0, packet.getLength() ) );
			if ( !scrapeResponse.getAction().equals(Action.SCRAPE) )
			{
				isScrapeResponse = false;
			}
		}
		return isScrapeResponse;
	}
	
	private boolean isAnnounceResponseMessage ( DatagramPacket packet ) {
		boolean isAnnounceResponse = true;
		
		if ( packet.getLength() < 20)
		{
			isAnnounceResponse = false;
		}
		if ( isAnnounceResponse )
		{
			AnnounceResponse announceResponse = AnnounceResponse.parse( Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
			if ( !announceResponse.getAction().equals(Action.ANNOUNCE) )
			{
				isAnnounceResponse = false;
			}
		}
		
		return isAnnounceResponse;
	}
	
	private boolean isConnectResponseMessage(DatagramPacket packet) {
		boolean isConnectResponse = true;
		
		if ( packet.getLength() != 16 )
		{
			isConnectResponse = false;
		}
		if ( isConnectResponse ) {
			ConnectResponse msgConnectResponse = ConnectResponse.parse(Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
			if ( !msgConnectResponse.getAction().equals(Action.CONNECT) )
			{
				isConnectResponse = false;
			}
		}
		return isConnectResponse;
	}
	
	private boolean isErrorMessage (DatagramPacket packet) {
		boolean  isCorrect = true;
		
		if ( packet.getLength() < 8 )
		{
			isCorrect = false;
		}
		if ( isCorrect )
		{
			Error errorMessage = Error.parse(Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
			if ( !errorMessage.getAction().equals(Action.ERROR) )
			{
				isCorrect =  false;
			}
		}
		return isCorrect;
	}
	
	/*** [END] METHODS USED TO VALIDATE THE DIFFERENT RESPONSE MESSAGES RECEIVED BY THE PEER **/
	public void addObserver(Observer o) {
		if (o != null && !this.observers.contains(o)) {
			this.observers.add(o);
		}
	}
	

	public void deleteObserver(Observer o) {
		this.observers.remove(o);
	}
	
	public void notifyObservers(Object param) {
		for (Observer observer : this.observers) {
			if (observer != null) {
				observer.update(null, param);
			}
		}
	}
	
	public HashMap<String,AnnounceResponse> getInfoHashesWithResponse() 
	{
		return infoHashesWithResponses;
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	public int getPortForPeers() {
		return portForPeers;
	}

	public void setPortForPeers(int portForPeers) {
		this.portForPeers = portForPeers;
	}

	public Long getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(Long connectionId) {
		this.connectionId = connectionId;
	}

	public String getTorrentFile() {
		return torrentFile;
	}

	public void setTorrentFile(String torrentFile) {
		this.torrentFile = torrentFile;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	
	
}
