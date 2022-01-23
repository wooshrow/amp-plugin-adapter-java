package ampPluginAdapter;


import ampPluginAdapter.Handler;
import ampPluginAdapter.protobuf.Api.ConfigurationOuterClass.Configuration;
import ampPluginAdapter.protobuf.Api.LabelOuterClass.Label;
import ampPluginAdapter.BrokerConnection;

public class AdapterCore {
	
    public String name ;
    public Handler handler ;
    public BrokerConnection broker_connection ;
    

    /**
     * The constructor for the adapter core.
     */
	public AdapterCore(String name, BrokerConnection broker_connection, Handler handler) {
		this.name = name;
        this.handler = handler;
        this.broker_connection = broker_connection;
    }
	
	public void start() {
		if (broker_connection == null || broker_connection.syncRemoteEndpoint == null) {
			throw new IllegalArgumentException("No connectiont to an AMP-server is present") ;
		}
		if (handler == null)  {
			throw new IllegalArgumentException("No handler is attached.") ;
		}
	}
	
	public void configuration_received(Configuration config) {
		DumbLogger.log(this,"Configuration received") ;
		// we should now do something with the received configuration, but for now
		// this is ignored, assuming some fixed presumed config.
		
		// tell the handler to start the SUT:
		handler.start();
		// done.
	}
	
	
	public void label_received(Label label, long correlation_id) {
		if (label.getType() != Label.LabelType.STIMULUS) {
			DumbLogger.log(this,"Label is not a stimulus") ;
			handler.stimulate(label);
			Label returnedLabel = null ;
			// send back a confirmation, don't bother with returnedLabel... just null:
			broker_connection.send_stimulus(label, 
					returnedLabel, 
					System.nanoTime(),
					correlation_id);
		}	
	}
	
	public void reset_received() {
		handler.reset(); 
	}
	
}
