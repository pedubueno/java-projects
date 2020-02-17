package br.com.proxyserver.utils;

import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

/**
 * Handles a socket connection to the proxy server from the client and uses 2
 * threads to proxy between server and client
 *
 */
public class ProxyServerThreadWorker extends Thread
{

	private Socket sClient;
	private Socket realServerConnection;
	private final String SERVER_URL;
	private final int SERVER_PORT;
	private long sendTimestamp;
	private long receiveTimestamp;
	private ProxyServerThreadManager myManager;
	private ProxyServerThreadWorkerReceiver myServerReceiver = null;

	ProxyServerThreadWorker(Socket sClient, String ServerUrl, int ServerPort, ProxyServerThreadManager myManager)
	{
		this.SERVER_URL = ServerUrl;
		this.SERVER_PORT = ServerPort;
		this.sClient = sClient;
		this.myManager = myManager;
		this.start();
	}

	@Override
	public void run()
	{
		try
		{
			Util.logTimeStamp(getProxyName(), "CONNECTION-" + (String.format("%04d", this.getId())) + " STARTED.");
			byte[] request = new byte[4096];
			int bytesToRead;
			
			realServerConnection = null;

			// connects a socket to the real server
			realServerConnection = new Socket(SERVER_URL, SERVER_PORT);

			// connected to the server, now we start the listener thread.
			// We pass the inputStream from server to it, to receive, and
			// the output from client, to send
			myServerReceiver = new ProxyServerThreadWorkerReceiver(realServerConnection.getInputStream(), sClient.getOutputStream(), this);
			myServerReceiver.start();

			// This thread focus on send to the server (upload)
			while ((bytesToRead = sClient.getInputStream().read(request)) != -1)
			{
					
				// Send it
				realServerConnection.getOutputStream().write(request, 0, bytesToRead);
				realServerConnection.getOutputStream().flush();

				sendTimestamp = System.currentTimeMillis();
				
				Util.logTimeStamp(myManager.getProxyName(), "MESSAGE SENT TO HOST. TOTAL BYTES:  " + bytesToRead
						+ " | HEX: " + Util.bytesToHex(Arrays.copyOf(request, bytesToRead)));
				// Util.logTimeStamp(myManager.getProxyName(), "CONNECTION-" +
				// String.format("%04d", this.getId() + " MESSAGE SENT TO HOST
				// "));
				//clear the byte array
				request = new byte[4096];
			}
		}
		catch (SocketException e)
		{
			//socket exception, connection closed. So, don't do anything
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			shutdownRealServerConnection();
			shutdownRealClientConnection();
			
			myServerReceiver.interrupt();
		}
		myManager.removeThreadFromList(this);
		Util.logTimeStamp(getProxyName(), "CONNECTION-" + (String.format("%04d", this.getId())) + " FINISHED.");
	}

	private void shutdownRealClientConnection()
	{
		try
		{
			if (sClient != null)
			{
				// closes the server
				sClient.shutdownOutput();
				sClient.shutdownInput();
				sClient.close();
				sClient = null;
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}
		sClient = null;

	}

	private void shutdownRealServerConnection()
	{
		try
		{
			if (realServerConnection != null)
			{
				// closes the server
				realServerConnection.shutdownOutput();
				realServerConnection.shutdownInput();
				realServerConnection.close();
				realServerConnection = null;
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}
		realServerConnection = null;

	}

	protected void informSendOrReceive(long timestamp)
	{
		receiveTimestamp = timestamp;

		if (sendTimestamp > 0 && receiveTimestamp > 0)
		{
			long totalTime = sendTimestamp - receiveTimestamp;
			Util.logTimeStamp(getProxyName(), "TOTAL TIME - " + (totalTime < 0 ? -totalTime : totalTime) + "ms.");
			sendTimestamp = 0;
			receiveTimestamp = 0;
		}
	}

	public String getProxyName()
	{
		return myManager.getProxyName();
	}

	public void informStop(boolean shouldManagerStop)
	{
		//someone called us to close, close the connections
		shutdownRealServerConnection();
		shutdownRealClientConnection();
	}
}
