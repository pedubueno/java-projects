package br.com.iso8583.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class Iso8583DummyServerThreadManager extends Thread
{

	private String serverName;
	private int localPort;
	private String isoConfigFile;
	private Iso8583DummyServer myManager;
	private ArrayList<Iso8583DummyServerThreadWorker> threadProxyList;
	private boolean shouldIStop;

	public Iso8583DummyServerThreadManager(String serverName, int localPort, String isoConfigFile, Iso8583DummyServer myManager)
	{

		this.shouldIStop = false;
		this.serverName = serverName;
		this.localPort = localPort;
		this.isoConfigFile = isoConfigFile;
		// used when this manager stops itself
		this.myManager = myManager;
		this.threadProxyList = new ArrayList<Iso8583DummyServerThreadWorker>();
	}

	public void run()
	{

		ServerSocket server = null;
		Socket s = null;
		try
		{
			// create the iso factory based on the config file received
			
			// Print a start-up message
			System.out.println("Starting proxy " + serverName + " for iso config file" + isoConfigFile + " on port " + localPort);
			server = new ServerSocket(localPort);
			server.setSoTimeout(1);
			while (!shouldIStop)
			{
				try
				{
					s = server.accept();
					Iso8583DummyServerThreadWorker t = new Iso8583DummyServerThreadWorker(s, this);
					synchronized (threadProxyList)
					{
						threadProxyList.add(t);
					}
				}
				catch (SocketTimeoutException e)
				{

				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (IllegalArgumentException e)
		{
			// System.err.println("Usage: java -jar ProxyServerTimer " +
			// "<remotehost> <remoteport> <localport>");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (server != null)
			{
				try
				{
					server.close();
					System.out.println("Stoping server  " + serverName + " on port " + localPort);
				}
				catch (IOException e)
				{
				}
			}

			// closes the child connections
			synchronized (threadProxyList)
			{
				for (Iso8583DummyServerThreadWorker proxyServerThreadWorker : threadProxyList)
				{
					proxyServerThreadWorker.informStop(true);
				}
			}
		}

	}

	public String getProxyName()
	{
		return serverName;
	}

	public void removeThreadFromList(Iso8583DummyServerThreadWorker thread)
	{
		synchronized (threadProxyList)
		{
			threadProxyList.remove(thread);

		}
	}

	public void informStop(boolean b)
	{
		shouldIStop = b;
	}

}
