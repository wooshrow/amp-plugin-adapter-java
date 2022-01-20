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
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

@ClientEndpoint
public class BrokerConnection {
	
	private static CountDownLatch latch;
	
	@OnOpen
	  public void onOpen(Session session) {
	    System.out.println ("CLIENT: --- Connected " + session.getId());
	    try {
	      session.getBasicRemote().sendText("start");
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	  }
		
	  @OnMessage
	  public String onMessage(String message, Session session) {
	    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
	    try {
	      System.out.println ("CLIENT: --- Received " + message);
	      
	      String userInput = bufferRead.readLine();
	      return userInput;
	    } catch (IOException e) {
	      throw new RuntimeException(e);
	    }
	  }

	  @OnClose
	  public void onClose(Session session, CloseReason closeReason) {
	    System.out.println("CLIENT: --- Session: " + session.getId());
	    System.out.println("CLIENT: --- Closing because: " + closeReason);
	  }
	  
	  public static void main(String[] args) throws IOException {
		  
		     // start a coba server here:
		    Thread th = new Thread(() -> CobaServer.startCobaServer ()) ;
			th.start();
		  
			
			latch = new CountDownLatch(1);
			
			ClientManager client = ClientManager.createClient();
			try {
				URI uri = new URI("ws://localhost:8025/folder/app");
				
				client.connectToServer(BrokerConnection.class, uri);
				
				latch.await();
				
				//client.
				
				
				
			} catch (DeploymentException | URISyntaxException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	

}
