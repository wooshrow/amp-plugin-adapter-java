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

/**
 * Providing a websocket client to connect to the AMP server, along with
 * basic methods to let the {@link AdapterCode} send various types of msg
 * to the AMP server.
 * 
 * Upon receiving a msg from the AMP server, the websocket client provided
 * by this class will forward the msg to the {@link AdapterCode}, e.g. to
 * eventually be translated to a call to the SUT.
 */
public class JettyConnectionBroker implements WebSocketListener {
	
	/**
	 * URL of the server-side.
	 */
	public String url ;
	
	/**
	 * Bearer's token for authentication.
	 */
	public String token ;
	
	public AdapterCore adapterCore ;	
	
	/**
	 * The Websocket-client that this connection-broker uses.
	 */
	WebSocketClient wsclient  ;
	
	/**
	 * The HTTP-client the the websocket will use.
	 */
	HttpClient httpClient ;
	
	Session session;
	
	static boolean autoReconnectWhenAMPServerCloses = true ;
	
	/**
	 * Create an instance of the connection-broker; it includes inside it 
	 * a websocket client that communicates with AMP sever.
	 * 
	 * @param url    The URL of the AMP server.
	 * @param token  Bearer-token to show to the server to authenticate this broker.
	 * @throws Exception 
	 */
	public JettyConnectionBroker(String url, String token) throws Exception {
		this.url = url ;
		this.token = token ;
		
		httpClient = new HttpClient();
		wsclient = new WebSocketClient(httpClient);
		wsclient.start();
	}
	
	public void connectToAMPServer() throws Exception {
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
        if(autoReconnectWhenAMPServerCloses) {
            try {
            	connectToAMPServer() ;
            }
            catch(Exception e) {
            	DumbLogger.log(this,"swallowing exception..");
            	e.printStackTrace();
            }
        }
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
			// Stop the SUT response handler thread --> not needed in this impl
            //if (adapterCore != null && adapterCore.handler != null) {
            //	adapterCore.handler.stop_sut_thread = true ;
            ///}
		}
	}
	
    /**
     *  Parse and handle a byte array from the web-socket into the correct protobuff object.
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
			adapterCore.stimulusReceived(pb_message.getLabel(), pb_message.getLabel().getCorrelationId()) ;
		} else if (pb_message.hasReset()) {
			DumbLogger.log(this, "Received a reset from AMP");
			adapterCore.resetReceived() ;
		} else if (pb_message.hasError()) {
			DumbLogger.log(this, "Received an error from AMP");
			try {
				adapterCore.errorReceived(pb_message.getError().getMessage()) ;
			}
			catch (Exception e){
				DumbLogger.log(this, "Swallowing exception " + e.toString());
			}
		}
	}
	
	/**
	 * When the connection to the AMP server is established, {@link #onWebSocketConnect(Session)}
	 * will trigger the {@link AdapterCore} to send an "announcement" to the AMP server. This is
	 * done by calling this method. Among ther things, it will announce what are the "labels"
	 * supported by the SUT.
	 */
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
	
	/**
	 * To send a Ready-signal to the AMP server.
	 */
	public void sendReady() {
		DumbLogger.log(this, "Sending READY");
		Message msg = Message.newBuilder()
				.setReady(Ready.newBuilder().build())
				.build();
		sendMessage(msg) ;
	}
	
	/**
	 * The let the AMP-server know that the SUT thinks there is an error,
	 * e.g. when the server sends a msg the SUT does not expect.
	 */
	public void sendError(String message) {
		Message.Error err = Message.Error.newBuilder()
				.setMessage(message)
				.build() ;
		Message msg = Message.newBuilder()
				.setError(err)
				.build() ;
		sendMessage(msg) ;
	}
	
	/**
	 * Whenever the client-side receives a Label-msg from the AMP-server, it is supposed
	 * to acknowledge this by sending a confirmation. This method is for that.
	 */
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
	
	/**
	 * To send a response-Label from the SUT to the AMP-server.
	 */
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
	
	/**
	 * A generic method to send a msg to the AMP server.
	 */
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
	
	void disposeResources() { }
	 
	
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
