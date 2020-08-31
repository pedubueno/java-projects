package br.com.proxyserver.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.ini4j.Wini;
import org.ini4j.Profile.Section;

/**
 *
 * @author Paulo Bueno
 *
 */
public class ProxyServer
{

	private List<ProxyServerThreadManager> proxyServerManagerList;
	private static ProxyServer instance = null;

	public static void main(String[] args)
	{

			
		if (instance == null)
		{
			instance = new ProxyServer();
		}

		System.out.println("PROXY SERVER(s) STARTED. TYPE <STOP> TO FINISH...");
		boolean shouldIStop = false;
		Scanner scanner = new Scanner(System.in);
		try
		{

			while (!shouldIStop)
			{
				String line = scanner.nextLine();
				if (line.equals("STOP"))
				{
					shouldIStop = true;
				}
				else
				{
					System.out.println("INPUT NOT ALLOWED. TYPE <STOP> TO FINISH...");
				}

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			scanner.close();
		}

		synchronized (instance.proxyServerManagerList)
		{
			// we need to stop. Close all managers on the list
			for (ProxyServerThreadManager t : instance.proxyServerManagerList)
			{
				t.informStop(true);
			}
		}

	}

	public ProxyServer()
	{
		// init the list of proxy servers
		proxyServerManagerList = new ArrayList<ProxyServerThreadManager>();

		System.out.println("Initializing Proxy Server(s). Loading config file...");
			
		try
		{
			// loads the proxy profile
			Wini profile = new Wini(new File("proxy.ini"));

			// load each proxys
			// for each proxy (section) configured into the profile, create a
			// new ProxyServerManager
			for (String proxyName : profile.keySet())
			{

				Section section = profile.get(proxyName);

				String remoteHost = section.get("remoteHost");
				int remotePort = Integer.parseInt(section.get("remotePort"));
				int localPort = Integer.parseInt(section.get("localPort"));

				// creates a new proxyServerThread
				ProxyServerThreadManager pst = new ProxyServerThreadManager(proxyName, remoteHost, remotePort, localPort, this);

				// starts the thread
				pst.start();

				// puts into the list of active proxy servers
				synchronized (pst)
				{
					proxyServerManagerList.add(pst);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void removeThreadFromList(ProxyServerThreadManager thread)
	{
		synchronized (proxyServerManagerList)
		{
			proxyServerManagerList.remove(thread);
		}
	}

}