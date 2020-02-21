package br.com.iso8583.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.solab.iso8583.MessageFactory;

public class Iso8583DummyServerThreadManager extends Thread
{

	private String serverName;
	private int localPort;
	private String isoConfigFile;
	private Iso8583DummyServer myManager;
	private ArrayList<Iso8583DummyServerThreadWorker> isoWorkerThreadList;
	private boolean shouldIStop;
	private static final Logger log = LogManager.getLogger();


	public Iso8583DummyServerThreadManager(String serverName, int localPort, String isoConfigFile, Iso8583DummyServer myManager)
	{
		this.shouldIStop = false;
		this.serverName = serverName;
		this.localPort = localPort;
		this.isoConfigFile = isoConfigFile;
		// used when this manager stops itself
		this.myManager = myManager;
		this.isoWorkerThreadList = new ArrayList<Iso8583DummyServerThreadWorker>();
	}

	public void run()
	{
		ServerSocket server = null;
		Socket s = null;
		try
		{
			// Create the iso factory based on the config file received
			MessageFactory mf = new MessageFactory<>();
			mf.setCharacterEncoding("UTF-8");
			mf.setConfigPath(isoConfigFile);
			//mf.setBinaryHeader(true);
			//mf.setBinaryFields(true);
			
			// Print a start-up message
			log.info("Starting iso8583 server " + serverName + " for iso config file" + isoConfigFile + " on port " + localPort);
			server = new ServerSocket(localPort);
			server.setSoTimeout(1);
			while (!shouldIStop)
			{
				try
				{
					s = server.accept();
					Iso8583DummyServerThreadWorker t = new Iso8583DummyServerThreadWorker(s, mf, this);
					synchronized (isoWorkerThreadList)
					{
						isoWorkerThreadList.add(t);
						t.start();
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
					log.info("Stoping iso server  " + serverName + " on port " + localPort);
				}
				catch (IOException e)
				{
				}
			}

			// closes the child connections
			synchronized (isoWorkerThreadList)
			{
				for (Iso8583DummyServerThreadWorker isoServerThreadWorker : isoWorkerThreadList)
				{
					isoServerThreadWorker.informStop(true);
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
		synchronized (isoWorkerThreadList)
		{
			isoWorkerThreadList.remove(thread);

		}
	}

	public void informStop(boolean b)
	{
		shouldIStop = b;
	}

}
