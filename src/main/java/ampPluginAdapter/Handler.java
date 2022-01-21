package ampPluginAdapter;

public class Handler {
	
	static class SUT {

	}
		
    public SUT sut ;
    
    Boolean stop_sut_thread = false ;

    public Handler() {
    	throw new UnsupportedOperationException() ;
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

}
