package ampPluginAdapter;

import javax.lang.model.type.NullType;

import ampPluginAdapter.Handler;
import ampPluginAdapter.BrokerConnection;

public class AdapterCore {
    public String name = "";
    public Handler handler = NullType;
    public BrokerConnection broker_connection = NullType;

    /**
     * The constructor for the adapter core.
     */
	public AdapterCore(String name, BrokerConnection broker_connection, Handler handler) {
		this.name = name;
        this.handler = handler;
        this.broker_connection = broker_connection;
    }
}
