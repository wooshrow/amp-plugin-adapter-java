package ampPluginAdapter;

import java.util.ArrayList;
import ampPluginAdapter.AdapterCore;
//import ampPluginAdapter.Logger;
import java.lang.Thread;


public class Handler {
	
	static class SUT {

	}
		
    public SUT sut ;
    
    Boolean stop_sut_thread = false ;
    public AdapterCore AdapterCore;
    //private Logger Logger;
    private ArrayList<String> responses = new ArrayList<String>();
    private Thread sut_thread = new Thread();
    ArrayList<String> configuration = new ArrayList<String>();
    public Handler() {
    	throw new UnsupportedOperationException() ;
	//this.AdapterCore = new AdapterCore("name", broker_connection, this);

    }
    
    /**
     * The constructor for the test agent.
     */
	public Handler(String url, String token) {
        this.sut = new SUT();
    }
	
	public void RegisterAdapterCore(AdapterCore adapter_core) {
		throw new UnsupportedOperationException() ;
	}
	public void adapter_core(AdapterCore adapter_core) {
		this.AdapterCore = adapter_core;		
	}
	public void response_received() {
		//this.Logger.debug("Handler", "response received: {}".format(response));
		//this.adapter_core.send_response(this.responses());
	}
	
	public void start() {
		this.responses = null;
		//this.sut = Sut(this.responses,this.Logger);
		this.stop_sut_thread = false;
//      self.sut_thread = Thread(target=self.running_sut,args=(lambda : self.stop_sut_thread, ))	
		//this.sut_thread = new Thread(this.running_sut, String name)

		this.sut_thread.start();
	}
	

	public void reset() {
		//this.Logger.info("Handler", "Resetting the sut for new test cases");
		if(this.sut != null) {
//			return this.Sut.ble_reset();
		}		
	}
	

	public void stop() {
		//this.Logger.info("Handler", "Stopping the plugin adapter from plugin handler");
		//this.Sut.ble_reset();
		//this.Sut.stop();
		this.sut = null;
		this.stop_sut_thread = true;
		try {
			this.sut_thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.sut_thread = null;
		//this.logger.debug("Handler", "Finished stopping the plugin adapter from plugin handler");
	}

}
