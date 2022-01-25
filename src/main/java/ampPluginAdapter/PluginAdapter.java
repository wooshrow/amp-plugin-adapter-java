package ampPluginAdapter;

import ampPluginAdapter.AdapterCore;
import ampPluginAdapter.Handler;
import ampPluginAdapter.BrokerConnection;

public class PluginAdapter {
	
    public static void startPluginAdapter(String name, String url, String token)
    {
    	// Create a connection to the AMP-server:
        BrokerConnection broker_connection = BrokerConnection.deployBrokerConnection(url, token);
        Handler handler = new Handler();
        
        // Create an instance of an adapter-core, attach the above connection to it,
        // then attach an "handler" as well:
        AdapterCore adapter_core = new AdapterCore(name, broker_connection, handler);
        broker_connection.registerAdapterCore(adapter_core);
        handler.registerAdapterCore(adapter_core);

        // everything is set, now start the core:
        adapter_core.start();
    }
    
    public static void main(String[] args) {
        // Todo replace the next hardcoded string by the input of the arguments here
        String name = "ICTwI2022";
        String url = "wss://demo03.axini.com:443/adapters";
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NTA3MjA0NTYsInN1YiI6InJvbmFsZC52YW4uZG9vcm5AYXhpbmkuY29tIiwiaXNzIjoidm1wdWJsaWNwcm9kMDEiLCJzY29wZSI6ImFkYXB0ZXIifQ.LuC24cm6D4MYZRx3RCE0CTJWuuLHTIoT-CH-3eSihPM";

        startPluginAdapter(name, url, token);
    }

    
}
