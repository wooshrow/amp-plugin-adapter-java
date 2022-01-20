package ampPluginAdapter;


import ampPluginAdapter.Handler;
import ampPluginAdapter.BrokerConnection;

public class AdapterCore {
	
    public String name = "";
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
		throw new UnsupportedOperationException() ;
	}
	
}
