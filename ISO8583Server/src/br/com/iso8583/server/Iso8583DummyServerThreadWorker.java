package br.com.iso8583.server;

import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

/**
 * Handles a socket connection to the proxy server from the client and uses 2
 * threads to proxy between server and client
 *
 * @author jcgonzalez.com
 *
 */
public class Iso8583DummyServerThreadWorker extends Thread
{

	private Socket sClient;
	private Socket realServerConnection;
	private long sendTimestamp;
	private long receiveTimestamp;
	private Iso8583DummyServerThreadManager myManager;
	private Iso8583DummyServerThreadWorkerReceiver myServerReceiver = null;

	Iso8583DummyServerThreadWorker(Socket sClient,  Iso8583DummyServerThreadManager myManager)
	{
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
