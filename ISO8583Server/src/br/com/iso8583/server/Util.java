package br.com.iso8583.server;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util {
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	private static int lastTrace = 0;
	private static int lastHostNsu = 0;

	/**
	 * @param s
	 * @param n
	 * @return
	 */
	public static String padRight(String s, int n) {
		return String.format("%-" + n + "s", s);
	}

	/**
	 * @param s
	 * @param n
	 * @return
	 */
	public static String padLeft(String s, int n) {
		return String.format("%" + n + "s", s);
	}


	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static byte[] hexToBytes(String s)
	{
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2)
		{
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
	
	public static byte[] new2byteHeaderPacket(byte[] data)
	{
		int len = data.length;
		byte[] buf = new byte[len + 2];

		buf[0] = ((byte) (len >> 8 & 0xFF));
		buf[1] = ((byte) (len & 0xFF));

		System.arraycopy(data, 0, buf, 2, len);

		return buf;
	}

	public static int get2byteBufferSize(byte[] header_buf)
	{
		return (header_buf[0] & 0xFF) << 8 | header_buf[1] & 0xFF;
	}
	
	
	
	public static int getLastTrace() {
		return lastTrace;
	}
	
	public static int getLastHostNsu() {
		return lastHostNsu;
	}

	/** Returns the next number in the sequence. This method is synchronized, because the counter
	 * is incremented in memory only. */
	public static synchronized int nextTrace() {
		lastTrace++;
		if (lastTrace > 999999) {
			lastTrace = 1;
		}
		return lastTrace;
	}
	
	
	public static synchronized int nextHostNsu() {
		lastHostNsu++;
		if (lastHostNsu > 999999) {
			lastHostNsu = 1;
		}
		return lastHostNsu;		
		
	}
}
