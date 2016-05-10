package es.deusto.ssdd.peer.vo;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Utils {

	private static Map<String,byte[]> infoHashes = new HashMap<String,byte[]> ();
	
	public static byte[] getInfoHash ( String infoHash )
	{
		if ( infoHashes.containsKey(infoHash))
			return infoHashes.get(infoHash);
		else
		{
			BigInteger value = new BigInteger(infoHash, 16); 
			return Arrays.copyOfRange(value.toByteArray(), 1, value.toByteArray().length );
		}
			
	}
	public static void putInfoHash ( String infoHash, byte[] infoHashB )
	{
		if ( !infoHashes.containsKey(infoHash) )
			infoHashes.put(infoHash, infoHashB);
	}
	
	public static String toHex(String arg) {
			  return String.format("%x", new BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
	}
}
