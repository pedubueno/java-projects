package br.com.iso8583.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Arrays;

public class Iso8583DummyServerThreadWorkerReceiver extends Thread
{
	private Iso8583DummyServerThreadWorker myManager;
	private InputStream inFromServer;
	private OutputStream outToClient;
	private String proxyName;

	
	public Iso8583DummyServerThreadWorkerReceiver (InputStream inFromServer, OutputStream outToClient, Iso8583DummyServerThreadWorker myManager)
	{
		this.myManager = myManager;
		this.proxyName = myManager.getProxyName();
		this.inFromServer = inFromServer;
		this.outToClient = outToClient;
	}
	
	public void run()
	{		
		// current thread manages streams from server to client (DOWNLOAD)
		int bytesToRead;
		byte[] reply = new byte[4096];
		try
		{
			while ((bytesToRead = inFromServer.read(reply)) != -1)
			{
				//something received, we log the time
				long receiveTimestamp = System.currentTimeMillis();
				
				
				outToClient.write(reply, 0, bytesToRead);
				outToClient.flush();

				Util.logTimeStamp(proxyName, "MESSAGE RECV FROM HOST. TOTAL BYTES: " + bytesToRead + 
				 " | HEX: " + Util.bytesToHex(Arrays.copyOf(reply, bytesToRead)));
				//Util.logTimeStamp(myManager.getProxyName(), "CONNECTION-" + String.format("%04d", this.getId() + " MESSAGE SENT TO HOST"));

				myManager.informSendOrReceive(receiveTimestamp);
			}
		}
		catch (SocketException e)
		{
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Util.logTimeStamp(proxyName, e.getMessage());
		}

		finally
		{
			//we get -1 or the read thrown an exception, we inform the disconnection
			//Util.logTimeStamp(proxyName, "ProxyServerThreadWorkerReceiver finishing.");
			//indicates that the manager has to stop
			myManager.informStop(true);

		}
		
	}
	
	public void informStop(boolean shouldIStop)
	{
		//TODO: what we do when someone call us to stop?
	}
	
}
