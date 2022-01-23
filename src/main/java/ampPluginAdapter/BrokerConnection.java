package ampPluginAdapter;

import java.io.*;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import com.google.protobuf.Descriptors.FieldDescriptor;

import ampPluginAdapter.protobuf.Api.ConfigurationOuterClass.Configuration;
import ampPluginAdapter.protobuf.Api.LabelOuterClass.Label;
import ampPluginAdapter.protobuf.Api.MessageOuterClass.Message;

@ClientEndpoint
public class BrokerConnection {
	
	public String url ;
	public String token ;
	public AdapterCore adapter_core ;	
	
	String haha = "" ;
	
	Session currentSession ;
	Basic syncRemoteEndpoint ;
	
	private static CountDownLatch latch;
	
	static class DummyProtbufMsg {
		
	}

	
	public BrokerConnection(String url, String token) {
		this.url = url ;
		this.token = token ;
		haha = "HAHAHA" ;
	}
	
	public void registerAdapterCore(AdapterCore adapter_core) {
	   this.adapter_core = adapter_core;
	}
	
	@OnOpen
	public void onOpen(Session session) throws IOException {
		DumbLogger.log(this, "Successfully opened a connection");
		currentSession = session ;
		syncRemoteEndpoint = session.getBasicRemote();
		syncRemoteEndpoint.sendText(haha);
		// TODO :
		// adapter_core.broker_connection_opened() ;
	}
		
	@OnMessage
	public void onMessage(String message, Session session) {
		DumbLogger.log(this, "Received " + message);
		parse_and_handle_message(message);
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		DumbLogger.log(this, "The server stopped connection with code: " + closeReason.getCloseCode());
		DumbLogger.log(this, "The server closed conenction because: " + closeReason);

		// this will be taken care-of i think:
		// websocket.close()

		// Stop the SUT response handler thread
		if (adapter_core != null && adapter_core.handler != null)
			// TODO:
			// adapter_core.handler.stop_sut_thread = True
			;

	}
	  
	/**
	 * Close the websocket with the given response close code and close reason.
	 * @throws IOException 
	 */
	public void close(String reason, Integer code) throws IOException {
		if (code == null) code = -1 ;
		if (syncRemoteEndpoint != null) {
			DumbLogger.log(this, "Closing connection with code: " + code);
			DumbLogger.log(this, "Closing because: " + reason);
			// not sure how to close... like this? :
			currentSession.close(); 
			// Stop the SUT response handler thread
            if (adapter_core != null && adapter_core.handler != null) {
            	adapter_core.handler.stop_sut_thread = true ;
            }
		}
	}

     /**
      *  Parses and handles a byte array from the web-socket into the correct protobuff object.
      */
	void parse_and_handle_message(String message) {
		
		// need proto-buffer stuff here....
		Charset charset = StandardCharsets.UTF_16;
		byte[] data = message.getBytes(charset) ;
		Message pb_message = null ;
		try {
			pb_message = Message.parseFrom(data) ;
		}
		catch(Exception e) {
			DumbLogger.log(this, "Could not decode message due to: " + e.getMessage()) ;
		}
		
		if (pb_message.getConfiguration() != null) {
			DumbLogger.log(this, "Received a configuration");
			adapter_core.configuration_received(pb_message.getConfiguration()) ;
		}		
		else if (pb_message.getLabel() != null) {
			DumbLogger.log(this, "Received a label");
			adapter_core.label_received(pb_message.getLabel(), pb_message.getLabel().getCorrelationId()) ;
		} else if (pb_message.getReset() != null) {
			DumbLogger.log(this, "Received a reset");
			adapter_core.reset_received() ;
		}
	}
	
	public void send_stimulus(Label label, Label physicalLabel, long timestamp, long correlationid) {
		DumbLogger.log(this,"Sending stimulus") ;
		Label labelToSend = label ;
		if (physicalLabel != null) {
			// HOW in java ??
			// labelToSend.physical_label = physical_label
		}
		// HOW in Java??
		//labelToSend.timestamp = timestamp
		//labelToSend.correlation_id = correlation_id
		
		// Need to build this msg from label ... HOW?
		Message msgToSend = null ;
		send_message(msgToSend) ;
	}
	
	
	public void send_message(Message pb_message) {
		if (syncRemoteEndpoint == null)  {
			DumbLogger.log(this,"No connection to websocket (yet). Is the adapter connected to AMP?") ;
		}
		else {
			try {
				syncRemoteEndpoint.sendObject(pb_message);
				// orig: self.websocket.send(pb_message.SerializeToString(), websocket.ABNF.OPCODE_BINARY)
				DumbLogger.log(this,"Success send") ;
			}
			catch (Exception e) {
				DumbLogger.log(this,"Failed sending message, exception: " + e) ;
			}
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
