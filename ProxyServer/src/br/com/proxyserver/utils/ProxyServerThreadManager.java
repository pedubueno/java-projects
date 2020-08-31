package br.com.proxyserver.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class ProxyServerThreadManager extends Thread
{

	private String proxyName;
	private String remoteHost;
	private int remotePort;
	private int localPort;
	private ProxyServer myManager;
	private ArrayList<ProxyServerThreadWorker> threadProxyList;
	private boolean shouldIStop;

	public ProxyServerThreadManager(String proxyName, String remoteHost, int remotePort, int localPort, ProxyServer myManager)
	{

		this.shouldIStop = false;
		this.proxyName = proxyName;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.localPort = localPort;

		// used when this manager stops itself
		this.myManager = myManager;
		this.threadProxyList = new ArrayList<ProxyServerThreadWorker>();
	}

	public void run()
	{

		ServerSocket server = null;
		Socket s = null;
		try
		{
			// Print a start-up message
			System.out.println("Starting proxy " + proxyName + " for " + remoteHost + ":" + remotePort + " on port " + localPort);
			server = new ServerSocket(localPort);
			server.setSoTimeout(1);
			while (!shouldIStop)
			{
				try
				{
					s = server.accept();
					ProxyServerThreadWorker t = new ProxyServerThreadWorker(s, remoteHost, remotePort, this);
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
					System.out.println("Stoping proxy  " + proxyName + " for " + remoteHost + ":" + remotePort + " on port " + localPort);
				}
				catch (IOException e)
				{
				}
			}

			// closes the child connections
			synchronized (threadProxyList)
			{
				for (ProxyServerThreadWorker proxyServerThreadWorker : threadProxyList)
				{
					proxyServerThreadWorker.informStop(true);
				}
			}
		}

	}

	public String getProxyName()
	{
		return proxyName;
	}

	public void removeThreadFromList(ProxyServerThreadWorker thread)
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
