package br.com.iso8583.server;

import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;

/**
 * Handles a socket connection with the current ISO implementation. Generate
 * responses based on message type received
 *
 * @author Paulo Bueno
 *
 */
public class Iso8583DummyServerThreadWorker extends Thread {

	private Socket sClient;
	private long sendTimestamp;
	private long receiveTimestamp;
	private Iso8583DummyServerThreadManager myManager;
	private MessageFactory mf;
	private boolean shouldIStop;
	private static final Logger log = LogManager.getLogger();

	Iso8583DummyServerThreadWorker(Socket sClient, MessageFactory mf, Iso8583DummyServerThreadManager myManager) {
		this.sClient = sClient;
		this.myManager = myManager;
		this.mf = mf;
		this.shouldIStop = false;

	}

	@Override
	public void run() {
		try {
			log.info("CONNECTION-" + (String.format("%04d", this.getId())) + " STARTED.");

			byte[] request;
			byte[] msgLen;
			int bytesToRead;

			while (!shouldIStop) {
				msgLen = new byte[2];
				try {

					// while ((bytesToRead = sClient.getInputStream().read(msgLen)) != -1)
					while ((bytesToRead = sClient.getInputStream().read(msgLen)) != -1) {
						// Something received, we allocate the byte[] with the size of the header
						request = new byte[Util.get2byteBufferSize(msgLen)];

						// wait for complete msg
						readCompleteMsg(request);

						log.info("MESSAGE RECV FROM HOST. TOTAL BYTES: " + request.length + " | HEX: "
								+ Util.bytesToHex(request));

						IsoMessage tst = mf.parseMessage(request, 0, true);

						// Sends response based on the request
						IsoMessage responseMsg = buildResponse(tst);

						// sends the response back
						sendResponseMsg(responseMsg.writeData());

					}
					if (bytesToRead == -1) {
						throw new ConnectException("Socket closed by the client");
					}
				}
				catch (ConnectException e) {
					shouldIStop = true;
				}
				catch (Exception e) {
					throw e;
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			shutdownRealClientConnection();
		}
		myManager.removeThreadFromList(this);
		log.info("CONNECTION-" + (String.format("%04d", this.getId())) + " FINISHED.");
	}

	private void sendResponseMsg(byte[] writeData) throws Exception {

		byte[] response = Util.new2byteHeaderPacket(writeData);
		
		sClient.getOutputStream().write(response);
		sClient.getOutputStream().flush();

		log.info("MESSAGE SENT TO CLIENT. TOTAL BYTES: " + response.length + " | HEX: " + Util.bytesToHex(response));

	}

	private void readCompleteMsg(byte[] request) throws SocketTimeoutException, Exception {
		// 2 seconds timeout
		long timeout = System.currentTimeMillis() + 2000;

		while (timeout > System.currentTimeMillis() && sClient.getInputStream().available() < request.length) {
			Thread.sleep(1);
		}

		if (timeout < System.currentTimeMillis()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Timeout waiting for entire message. Expected: ");
			sb.append(request.length + " bytes. Received: ");
			sb.append(sClient.getInputStream().available() + " bytes. ");
			throw new SocketTimeoutException(sb.toString());
		}

		// reads data into request buffer
		sClient.getInputStream().read(request, 0, request.length);

	}

	private IsoMessage buildResponse(IsoMessage tst) {

		IsoMessage responseMessage;
		responseMessage = mf.createResponse(tst);

		// remove default fields from request
		responseMessage.removeFields(2, 22, 35, 40, 42, 45);

		// put default response fields
		responseMessage.setField(38, new IsoValue<Integer>(IsoType.NUMERIC, Util.nextTrace(), 6));
		responseMessage.setField(127, new IsoValue<String>(IsoType.LLLVAR, String.format("%09d", Util.nextHostNsu())));

		// TODO: now increment with values that aren't present in the template
		switch (tst.getType()) {
		case 100:

			break;
		case 200:

			break;

		case 400:

			break;

		default:
			break;
		}
		return responseMessage;

	}

	private void shutdownRealClientConnection() {
		try {
			if (sClient != null) {
				// closes the server
				sClient.shutdownOutput();
				sClient.shutdownInput();
				sClient.close();
				sClient = null;
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
		sClient = null;

	}

	protected void informSendOrReceive(long timestamp) {
		receiveTimestamp = timestamp;

		if (sendTimestamp > 0 && receiveTimestamp > 0) {
			long totalTime = sendTimestamp - receiveTimestamp;
			log.info("TOTAL TIME - " + (totalTime < 0 ? -totalTime : totalTime) + "ms.");
			sendTimestamp = 0;
			receiveTimestamp = 0;
		}
	}

	public String getProxyName() {
		return myManager.getProxyName();
	}

	public void informStop(boolean shouldManagerStop) {
		// someone called us to close, close the connections
		shutdownRealClientConnection();
	}
}
