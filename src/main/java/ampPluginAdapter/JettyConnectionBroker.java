package ampPluginAdapter;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import com.google.protobuf.ByteString;

import ampPluginAdapter.protobuf.Api.AnnouncementOuterClass.Announcement;
import ampPluginAdapter.protobuf.Api.ConfigurationOuterClass.Configuration;
import ampPluginAdapter.protobuf.Api.LabelOuterClass.Label;
import ampPluginAdapter.protobuf.Api.MessageOuterClass.Message;
import ampPluginAdapter.protobuf.Api.MessageOuterClass.Message.Ready;

public class JettyConnectionBroker implements WebSocketListener {
	
	public String url ;
	public String token ;
	public AdapterCore adapterCore ;	
	String haha = "" ;
	
	
	/**
	 * The Websocket-client that this connection-broker uses.
	 */
	WebSocketClient wsclient  ;
	
	/**
	 * The HTTP-client the the websocket will use.
	 */
	HttpClient httpClient ;
	
	
	
	Session session;
	
	public JettyConnectionBroker(String url, String token) {
		this.url = url ;
		this.token = token ;
		haha = "HAHAHA" ;
	}
	
	public void connectToAMPServer() throws Exception {
		httpClient = new HttpClient();
		wsclient = new WebSocketClient(httpClient);
		wsclient.start();
		
		ClientUpgradeRequest customRequest = new ClientUpgradeRequest();
		customRequest.setHeader("Authorization", "Bearer " + token);
		URI serverURI = URI.create(this.url) ;
		CompletableFuture<Session> clientSessionPromise = wsclient.connect(this,serverURI,customRequest) ;
	}
	
	@Override
    public void onWebSocketConnect(Session session)
    {
		this.session = session ;
		DumbLogger.log(this, "Successfully opened a connection");	
		adapterCore.connectionBrokerOpened();
    }
	
	@Override
    public void onWebSocketClose(int statusCode, String closeReason)
    {
		DumbLogger.log(this, "The server stopped connection with code: " + statusCode);
		DumbLogger.log(this, "The server closed conenction because: " + closeReason);

        // The WebSocket connection is closed.

        // You may dispose resources.
        disposeResources();
    }
	
	@Override
    public void onWebSocketError(Throwable cause) {
		DumbLogger.log(this, "The WebSocket connection failed. Cause: " + cause.toString());
		cause.printStackTrace();
        // You may log the error.
        //cause.printStackTrace();
        disposeResources();
    }
	
	@Override
	public void onWebSocketText(String message) {
		DumbLogger.log(this, "Received " + message);
		//parseAndHandleMessage(message);		
	}
	
	 @Override
	 public void onWebSocketBinary(byte[] payload, int offset, int length){
		 DumbLogger.log(this, "Received msg");
		 parseAndHandleMessage(payload);			   
	}
	
	public void registerAdapterCore(AdapterCore adapter_core) {
		   this.adapterCore = adapter_core;
	}
	
	
	/**
	 * Close the websocket with the given response close code and close reason.
	 * @throws IOException 
	 */
	public void close(String reason, Integer code) throws IOException {
		if (code == null) code = -1 ;
		if (session != null) {
			DumbLogger.log(this, "Closing connection with code: " + code);
			DumbLogger.log(this, "Closing because: " + reason);
			// not sure how to close... like this? :
			session.close();
			// Stop the SUT response handler thread
            if (adapterCore != null && adapterCore.handler != null) {
            	adapterCore.handler.stop_sut_thread = true ;
            }
		}
	}
	
    /**
     *  Parses and handles a byte array from the web-socket into the correct protobuff object.
     */
	void parseAndHandleMessage(byte[] message) {
		//Charset charset = StandardCharsets.UTF_16;
		//byte[] data = message.getBytes(charset) ;
		Message pb_message = null ;
		try {
			pb_message = Message.parseFrom(message) ;
		}
		catch(Exception e) {
			DumbLogger.log(this, "Could not decode message due to: " + e.getMessage()) ;
		}
		
		if (pb_message.hasConfiguration()) {
			DumbLogger.log(this, "Received a configuration");
			adapterCore.configurationReceived(pb_message.getConfiguration()) ;
		}		
		else if (pb_message.hasLabel()) {
			DumbLogger.log(this, "Received a label");
			adapterCore.labelReceived(pb_message.getLabel(), pb_message.getLabel().getCorrelationId()) ;
		} else if (pb_message.hasReset()) {
			DumbLogger.log(this, "Received a reset");
			adapterCore.resetReceived() ;
		}
	}
	
	public void sendAnnouncement(String name, List<Label> supportedLabels,  Map<String,String> configuration) {
        DumbLogger.log(this, "Announcing") ;
        Configuration emptyConf =Configuration.newBuilder().build() ;
        // ignoring suported labels for now:
        Announcement.Builder announcementBuilder = Announcement.newBuilder()
        		.setName(name)
        		.setConfiguration(emptyConf) ;
        
        for(Label lab : supportedLabels) {
        	announcementBuilder.addLabels(lab) ;
        }
        
        Message msg = Message.newBuilder().setAnnouncement(announcementBuilder.build()).build() ;
        sendMessage(msg) ;
	}
	
	public void sendReady() {
		Message msg = Message.newBuilder()
				.setReady(Ready.newBuilder().build())
				.build();
		sendMessage(msg) ;
	}
	
	public void sendError(String message) {
		Message.Error err = Message.Error.newBuilder()
				.setMessage(message)
				.build() ;
		Message msg = Message.newBuilder()
				.setError(err)
				.build() ;
		sendMessage(msg) ;
	}
	
	public void sendStimulusConfirmation(Label label, ByteString physicalLabel, long timestamp, long correlationid) {
		DumbLogger.log(this,"Sending stimulus confirmation back to AMP") ;
		Label.Builder labelToSend = Label.newBuilder(label) 
				. setTimestamp(timestamp) 
				. setCorrelationId(correlationid) ;
		
		if (physicalLabel != null) {
			labelToSend.setPhysicalLabel(physicalLabel) ;
		}
		
		Message.Builder msg = Message.newBuilder().setLabel(labelToSend) ;
		sendMessage(msg.build()) ;
	}
	
	public void sendResponse(Label label, Label physicalLabel, long timestamp) {
		DumbLogger.log(this,"Sending response") ;
		Label labelToSend = Label.newBuilder(label) 
				.setTimestamp(timestamp)  
				.build();
		
		if (physicalLabel != null) {
			labelToSend = physicalLabel ;
		}	
		DumbLogger.log(this,"Sending response to AMP: " + labelToSend.getLabel());
		Message msg = Message.newBuilder().setLabel(labelToSend).build() ;
		sendMessage(msg) ;
	}
	
	
	public void sendMessage(Message pb_message) {
		if (session == null)  {
			DumbLogger.log(this,"No connection to websocket (yet). Is the adapter connected to AMP?") ;
		}
		else {
			try {
				session.getRemote().sendBytes(ByteBuffer.wrap(pb_message.toByteArray())); ;
				//session.getRemote().sendBytes(ByteBuffer.wrap(pb_message.toString().getBytes())); ;
				//session.getRemote().sendString(pb_message.toString());
				//syncRemoteEndpoint.sendObject(pb_message);
				// orig: self.websocket.send(pb_message.SerializeToString(), websocket.ABNF.OPCODE_BINARY)
				DumbLogger.log(this,"Success send") ;
			}
			catch (Exception e) {
				DumbLogger.log(this,"Failed sending message, exception: " + e) ;
			}
		}		
	}
	
	void disposeResources() {
		
	}
	 
	
	// just for testing that the websocket work;
	public static void main(String[] args) throws Exception {
		// start a coba server here:
		Thread th = new Thread(() -> CobaServer.startCobaServer());
		th.start();

		String name = "ICTwI2022";
        String url = "wss://demo03.axini.com:443/adapters";
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NTA3MjA0NTYsInN1YiI6InJvbmFsZC52YW4uZG9vcm5AYXhpbmkuY29tIiwiaXNzIjoidm1wdWJsaWNwcm9kMDEiLCJzY29wZSI6ImFkYXB0ZXIifQ.LuC24cm6D4MYZRx3RCE0CTJWuuLHTIoT-CH-3eSihPM";

		JettyConnectionBroker connectionBroker = new JettyConnectionBroker(url,token) ;
		connectionBroker.connectToAMPServer();	

	}

}
