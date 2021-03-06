package ampPluginAdapter;

import ampPluginAdapter.AdapterCore;
import ampPluginAdapter.Handler;

public class PluginAdapter {
	
    public static void startPluginAdapter(String channel, String uname, String url, String token) throws Exception
    {
    	// Create a connection to the AMP-server:
    	JettyConnectionBroker connectionBroker = new JettyConnectionBroker(url, token);
        Handler handler = new Handler();
        
        // Create an instance of an adapter-core, attach the above connection to it,
        // then attach an "handler" as well:
        AdapterCore adapter_core = new AdapterCore(channel, uname, connectionBroker, handler);
        connectionBroker.registerAdapterCore(adapter_core);
        handler.registerAdapterCore(adapter_core);

        // everything is set, now start the core:
        adapter_core.start();
    }
    
    public static void main(String[] args) throws Exception {
        // Todo replace the next hardcoded string by the input of the arguments here
        String name  = "ICTwI2022"; // is this channel name??
        String channel = "agent" ;
            	
        String url = "wss://demo03.axini.com:443/adapters";
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NTA3MjA0NTYsInN1YiI6InJvbmFsZC52YW4uZG9vcm5AYXhpbmkuY29tIiwiaXNzIjoidm1wdWJsaWNwcm9kMDEiLCJzY29wZSI6ImFkYXB0ZXIifQ.LuC24cm6D4MYZRx3RCE0CTJWuuLHTIoT-CH-3eSihPM";
        
        startPluginAdapter(channel, name, url, token);
    }

    
}
