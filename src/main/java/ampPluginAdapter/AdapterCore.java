package ampPluginAdapter;


import ampPluginAdapter.Handler;
import ampPluginAdapter.protobuf.Api.ConfigurationOuterClass.Configuration;
import ampPluginAdapter.protobuf.Api.LabelOuterClass.Label;

import com.google.protobuf.ByteString;

public class AdapterCore {
	
    public String channel ;
    public String uname ;
    public Handler handler ;
    public JettyConnectionBroker connectionBroker ;
    

    /**
     * The constructor for the adapter core.
     */
	public AdapterCore(String name, String uname, JettyConnectionBroker connectionBroker, Handler handler) {
		this.channel = name;
		this.uname = uname ;
        this.handler = handler;
        this.connectionBroker = connectionBroker;
    }
	
	public void start() throws Exception {
		if (connectionBroker == null) {
			throw new IllegalArgumentException("No connectiont to an AMP-server is present") ;
		}
		if (handler == null)  {
			throw new IllegalArgumentException("No handler is attached.") ;
		}
		connectionBroker.connectToAMPServer();
	}
	
	/**
	 * A call-back to send an initial announcement to the AMP-sever.
	 */
	public void connectionBrokerOpened() {
		connectionBroker.sendAnnouncement(channel + "@" + uname, 
				handler.supportedLabels(),
				handler.configuration);
	}
	
	public void configurationReceived(Configuration config) {
		DumbLogger.log(this,"Configuration received") ;
		// we should now do something with the received configuration, but for now
		// this is ignored, assuming some fixed presumed config.
		
		// tell the handler to start the SUT:
		handler.start();
		// done.
	}
	
	
	public void labelReceived(Label label, long correlation_id) {
		if (label.getType() != Label.LabelType.STIMULUS) {
			DumbLogger.log(this,"Label is not a stimulus") ;
			handler.stimulate(label);
			ByteString physicalLabel = null ;
			// send back a confirmation, don't bother with returnedLabel... just null:
			connectionBroker.sendStimulus(label, 
					physicalLabel, 
					System.nanoTime(),
					correlation_id);
		}	
	}
	
	public void resetReceived() {
		handler.reset(); 
	}
	
}
