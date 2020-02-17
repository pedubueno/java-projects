package br.com.iso8583.server;

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
public class Iso8583DummyServer
{

	private List<Iso8583DummyServerThreadManager> isoServerManagerList;
	private static Iso8583DummyServer instance = null;

	public static void main(String[] args)
	{

			
		if (instance == null)
		{
			instance = new Iso8583DummyServer();
		}

		System.out.println("ISO8583 SERVER(s) STARTED. TYPE <STOP> TO FINISH...");
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

		synchronized (instance.isoServerManagerList)
		{
			// we need to stop. Close all managers on the list
			for (Iso8583DummyServerThreadManager t : instance.isoServerManagerList)
			{
				t.informStop(true);
			}
		}

	}

	public Iso8583DummyServer()
	{
		// init the list of proxy servers
		isoServerManagerList = new ArrayList<Iso8583DummyServerThreadManager>();

		System.out.println("Initializing ISO8583 Server(s). Loading config file...");
			
		try
		{
			// loads the proxy profile
			Wini profile = new Wini(new File("iso8583server.ini"));

			// load each iso server
			// for each server (section) configured into the profile, create a
			// new Iso8583DummyServerManager
			for (String serverName : profile.keySet())
			{

				Section section = profile.get(serverName);

				//String remoteHost = section.get("remoteHost");
				//int remotePort = Integer.parseInt(section.get("remotePort"));
				int localPort = Integer.parseInt(section.get("localPort"));
				String isoConfigFile = section.get("isoConfigFile");

				// creates a new proxyServerThread
				Iso8583DummyServerThreadManager pst = new Iso8583DummyServerThreadManager(serverName, localPort, isoConfigFile, this);

				// starts the thread
				pst.start();

				// puts into the list of active proxy servers
				synchronized (pst)
				{
					isoServerManagerList.add(pst);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void removeThreadFromList(Iso8583DummyServerThreadManager thread)
	{
		synchronized (isoServerManagerList)
		{
			isoServerManagerList.remove(thread);
		}
	}

}