package ampPluginAdapter;

import java.io.*;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

import java.net.URISyntaxException;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

@ClientEndpoint
public class BrokerConnection {
	
	public String url ;
	public String token ;
	public AdapterCore adapter_core ;	
	
	String haha = "" ;
	
	Basic syncRemoteEndpoint ;
	
	private static CountDownLatch latch;

	
	public BrokerConnection(String url, String token) {
		this.url = url ;
		this.token = token ;
		haha = "HAHAHA" ;
	}
	
	public void RegisterAdapterCore(AdapterCore adapter_core) {
	   this.adapter_core = adapter_core;
	}
	
	@OnOpen
	public void onOpen(Session session) throws IOException {
		System.out.println("BrokerConnection: --- Successfully opened a connection");
		syncRemoteEndpoint = session.getBasicRemote();
		syncRemoteEndpoint.sendText(haha);
		// TODO :
		// adapter_core.broker_connection_opened() ;
	}
		
	@OnMessage
	public void onMessage(String message, Session session) {
		System.out.println("BrokerConnection: --- Received " + message);
		parse_and_handle_message(message);
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		System.out.println("BrokerConnection: --- Stopped connection with code: " + closeReason.getCloseCode());
		System.out.println("BrokerConnection: --- Closing because: " + closeReason);

		// this will be taken care-of i think:
		// websocket.close()

		// Stop the SUT response handler thread
		if (adapter_core != null && adapter_core.handler != null)
			// TODO:
			// adapter_core.handler.stop_sut_thread = True
			;

	}
	  

	void parse_and_handle_message(String message) {

		// need proto-buffer stuff here....
		String pb_message = message;
		// dummy processing:

		if (pb_message == "label") {
			System.out.println("BrokerConnection: --- Received a label");
			// TODO:
			// adapter_core.label_received(pb_message.label,
			// pb_message.label.correlation_id)
		} else if (pb_message == "reset") {
			System.out.println("BrokerConnection: --- Received a reset");
			// TODO:
			// adapter_core.reset_received()
		}
	}
	
	/**
	 * Create an instance of BrokerConnection, and connect it to the remote-side specified
	 * by the url.
	 * 
	 * Return the created instance of BrokerConnection.
	 */
	
	public static BrokerConnection deployBrokerConnection(String url, String token) {	
		latch = new CountDownLatch(1);
		ClientManager client = ClientManager.createClient();
		try {
			BrokerConnection bc = new BrokerConnection(url,token);
			client.connectToServer(bc, new URI(bc.url));

			latch.await();
			
			return bc ;

		} catch (DeploymentException | URISyntaxException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	  
	// just for testing that the websocket work;
	public static void main(String[] args) throws IOException {
		// start a coba server here:
		Thread th = new Thread(() -> CobaServer.startCobaServer());
		th.start();

		// deploy the client, connecting to the above server:
		BrokerConnection bc = deployBrokerConnection("ws://localhost:8025/folder/app", "token");

	}

}
