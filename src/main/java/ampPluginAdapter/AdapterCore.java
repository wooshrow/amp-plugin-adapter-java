package ampPluginAdapter;


import ampPluginAdapter.Handler;
import ampPluginAdapter.protobuf.Api.ConfigurationOuterClass.Configuration;
import ampPluginAdapter.protobuf.Api.LabelOuterClass.Label;

import java.io.IOException;

import com.google.protobuf.ByteString;

public class AdapterCore {
	
    public String channel ;
    public String uname ;
    public Handler handler ;
    public JettyConnectionBroker connectionBroker ;
    

    /**
     * The constructor for the adapter core.
     */
	public AdapterCore(String channel, String uname, JettyConnectionBroker connectionBroker, Handler handler) {
		this.channel = channel;
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
	
	/**
	 * After {@link #connectionBrokerOpened()} send an opening announcement to AMP,
	 * the AMP will respond by sending a 'configuration'. For now this config is
	 * ignored.
	 * 
	 * This method then sends a 'ready'-msg to the AMP to indicate that this side
	 * is ready to engage in testing with AMP.
	 */
	public void configurationReceived(Configuration config) {
		DumbLogger.log(this,"Configuration received") ;
		// we should now do something with the received configuration, but for now
		// this is ignored, assuming some fixed presumed config.
		handler.start();
		connectionBroker.sendReady();
	}
	
	/**
	 * Handle an incoming stimulus from the AMP.
	 */
	public void stimulusReceived(Label label, long correlation_id) {
		if (label.getType() != Label.LabelType.STIMULUS) {
			DumbLogger.log(this,"Label is not a stimulus") ;
			connectionBroker.sendError("Label is not a stimulus");
			return ;
		}
		// well... we don't do physicalLabel for now. So it is always null:
		ByteString physicalLabel = null ;
		// send back a confirmation, don't bother with returnedLabel... just null:
		connectionBroker.sendStimulusConfirmation(label, 
				physicalLabel, 
				System.nanoTime(),
				correlation_id);
		// if the stimulus also generates a response, send it to AMP:
		Label response = handler.stimulate(label);
		if (response != null) {
			this.sendResponse(response, null, System.nanoTime());
		}
	}
	
	public void resetReceived() {
		handler.reset(); 
		connectionBroker.sendReady();
	}
	
	public void errorReceived(String msg) throws IOException {
		connectionBroker.close("AMP sent an error: " + msg , null);
	}
	
	public void sendResponse(Label label, Label physical_label, long timestamp) {
		connectionBroker.sendResponse(label, physical_label, timestamp);
	}
	
}
