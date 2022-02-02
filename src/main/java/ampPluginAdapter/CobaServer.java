package ampPluginAdapter;

import org.glassfish.tyrus.server.Server;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Just a simple websocket server to test the client.
 */
public class CobaServer {
	
	@ServerEndpoint(value = "/app")
	static class MyServerEndPoint {
		@OnOpen
		public void onOpen(Session session) {
			DumbLogger.log(">> SERVER: Connected, sessionID = " + session.getId());
		}
		
		@OnMessage
		public String onMessage(String message, Session session) {
			DumbLogger.log(">> SERVER: recives a msg: " + message) ;
			if (message.equals("quit")) {
					try {
						session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE, "Bye!"));
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			return message ;
		}
		
		@OnClose
		public void onClose(Session session, CloseReason closeReason) {
			DumbLogger.log(">> SERVER: Session " + session.getId() +
				" closed because " + closeReason);
		}
	}
	
	public static void startCobaServer () {
		Server server;
		server = new Server ("localhost", 8025, "/folder", MyServerEndPoint.class);
		try {
			server.start();
			System.out.println("--- server is running");
			//System.out.println("--- press any key to stop the server");
			//BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			//bufferRead.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//server.stop();
		}
	}
	
	public static void main(String[] args) {
		Thread th = new Thread(() -> startCobaServer ()) ;
		th.start();
	}

}
