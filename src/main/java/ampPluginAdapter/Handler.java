package ampPluginAdapter;

public class Handler {
    public Sut sut = NullType;

    /**
     * The constructor for the test agent.
     */
	public Handler(String url, String token) {
        this.sut = new Sut();
    }
}
}
