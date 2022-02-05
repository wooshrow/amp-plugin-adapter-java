package ampPluginAdapter;

import java.util.LinkedList;
import java.util.List;

import world.BeliefState;
import world.LabEntity;

/**
 * Just a dummy SUT for testing the adapter.
 */
public class CobaSUT {
	
    public CobaSUT() { }
    
    
    public void start() { 
    	DumbLogger.log(this,"started");
    }
    
    public void reset() {  
    	DumbLogger.log(this,"reset");

    }
    
    public void stop() { 
    	DumbLogger.log(this,"stopped");
    }
    
    public BeliefState explore() {
    	DumbLogger.log(this,"exploring...");
    	DumbLogger.log(this,"explore done. Sending back observation");
    	return observe() ;
    }
    
    public BeliefState observe() {
    	DumbLogger.log(this,"observe");
    	BeliefState state = new BeliefState() ;
    	return state ;
    }
    
    public BeliefState pushButton(int button) {
    	DumbLogger.log(this,"pushButton " + button);
    	return observe() ;
    }
}
