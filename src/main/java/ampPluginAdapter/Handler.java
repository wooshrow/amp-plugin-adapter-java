package ampPluginAdapter;

public class Handler {
	
	static class SUT {

	}
		
    public SUT sut ;

    /**
     * The constructor for the test agent.
     */
	public Handler(String url, String token) {
        this.sut = new SUT();
    }

}
