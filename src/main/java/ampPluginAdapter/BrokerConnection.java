package ampPluginAdapter;

import javax.lang.model.type.NullType;

import ampPluginAdapter.AdapterCore;

public class BrokerConnection {
    public String url = "";
    public String token = "";
    public AdapterCore adapter_core = NullType;
    /**
     * The constructor for the test agent.
     */
	public BrokerConnection(String url, String token) {
		this.url = url;
        this.token = token;
    }

    public void RegisterAdapterCore(AdapterCore adapter_core) {
        this.adapter_core = adapter_core;
    }
}
