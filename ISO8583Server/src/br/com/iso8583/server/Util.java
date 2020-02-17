package br.com.iso8583.server;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

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

	/**
	 * @param proxyName
	 * @param message
	 */
	public static void logTimeStamp(String proxyName, String message) {
		StringBuilder sb = new StringBuilder();
		sb.append(sdf.format(new Date()));
		sb.append(": ");
		sb.append("[");
		sb.append(padLeft(proxyName, 15));
		sb.append("]");
		sb.append(" - ");
		sb.append(message);

		System.out.println(sb.toString());
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

}
