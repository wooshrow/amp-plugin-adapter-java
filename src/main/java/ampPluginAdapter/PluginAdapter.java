package ampPluginAdapter;

import ampPluginAdapter.AdapterCore;
import ampPluginAdapter.Handler;
import ampPluginAdapter.BrokerConnection;

public class PluginAdapter {
	
    public static void main(String[] args) {
        // Todo replace the next hardcoded string by the input of the arguments here
        String name = "ICTwI2022";
        String url = "wss://demo03.axini.com:443/adapters";
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NTA3MjA0NTYsInN1YiI6InJvbmFsZC52YW4uZG9vcm5AYXhpbmkuY29tIiwiaXNzIjoidm1wdWJsaWNwcm9kMDEiLCJzY29wZSI6ImFkYXB0ZXIifQ.LuC24cm6D4MYZRx3RCE0CTJWuuLHTIoT-CH-3eSihPM";

        StartPluginAdapter(name, url, token);
    }

    public static void StartPluginAdapter(String name, String url, String token)
    {
        BrokerConnection broker_connection = BrokerConnection(url, token);
        Handler handler = Handler();
        AdapterCore adapter_core = AdapterCore(name, broker_connection, handler);

        broker_connection.RegisterAdapterCore(adapter_core);
        handler.RegisterAdapterCore(adapter_core);

        adapter_core.start();
    }
}
