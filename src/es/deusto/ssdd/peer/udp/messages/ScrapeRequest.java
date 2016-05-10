package es.deusto.ssdd.peer.udp.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.deusto.ssdd.peer.metainfo.handler.MetainfoHandler;

/**
 * 
 * Offset          Size            	Name            	Value
 * 0               64-bit integer  	connection_id
 * 8               32-bit integer  	action          	2 // scrape
 * 12              32-bit integer  	transaction_id
 * 16 + 20 * n     20-byte string  	info_hash
 * 16 + 20 * N
 *
 */

public class ScrapeRequest extends BitTorrentUDPRequestMessage {

	private Map<String,byte[]> infoHashes;
	
	public ScrapeRequest() {
		super(Action.SCRAPE);
		infoHashes = new HashMap<String,byte[]>();
	}
	
	@Override
	public byte[] getBytes() {
		int tamanyo = 16 + 20 * infoHashes.keySet().size();
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(tamanyo);

		byteBuffer.order(ByteOrder.BIG_ENDIAN);

		byteBuffer.putLong(0, getConnectionId() );
		byteBuffer.putInt(8, getAction().value() );
		byteBuffer.putInt(12, getTransactionId());
		int inicio = 16;
		for ( String infoHash : infoHashes.keySet() )
		{
			byteBuffer.position(inicio);
			byteBuffer.put(infoHashes.get(infoHash));
			inicio += 20;
		}
		
		byteBuffer.flip();
		
		return byteBuffer.array();
	}
	
	public static ScrapeRequest parse(byte[] byteArray) {
		ByteBuffer bufferReceive = ByteBuffer.wrap(byteArray);
		ScrapeRequest scrapeRequest = new ScrapeRequest();
		scrapeRequest.setConnectionId(bufferReceive.getLong(0));
		scrapeRequest.setAction(Action.valueOf(bufferReceive.getInt(8)));
		scrapeRequest.setTransactionId(bufferReceive.getInt(12));
		int inicio = 16;
		List<String> infoHashes = new ArrayList<String>();
		for ( inicio = 16; inicio < byteArray.length; inicio += 20 )
		{
			byte[] infoHashBytes = new byte [20];
			bufferReceive.position(inicio);
			bufferReceive.get(infoHashBytes);
			infoHashes.add( MetainfoHandler.toHexString( infoHashBytes ) );
		}
		return scrapeRequest;
	}
	
	public List<String> getInfoHashes() {
		return new ArrayList<String> ( infoHashes.keySet() );
	}

	public void addInfoHash(String infoHash, byte [] bytesInfoHash ) {
		if (infoHash != null && !infoHash.trim().isEmpty() && !this.infoHashes.containsKey(infoHash)) {
			infoHashes.put(infoHash, bytesInfoHash);
		}
	}
}