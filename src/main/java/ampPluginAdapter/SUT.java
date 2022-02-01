package ampPluginAdapter;

import java.util.LinkedList;
import java.util.List;

//Todo implement domain specific interface here.
public class SUT {
	
	public static class Observation {
		public List<Integer> doors = new LinkedList<>() ;
		public List<Integer> buttons = new LinkedList<>() ;	
		
		public int[] getButtons() {
			int[] bs = new int[buttons.size()] ;
			int k = 0 ;
			for (Integer b : buttons) {
				bs[k] = b ;
				k++;
			}
			return bs ;
		}
		
		@Override
		public String toString() {
			String o = "doors: " + doors  + "; buttons: " + buttons ;
			return o ;
		}
	}
 
    public SUT() { }
    
    
    public void start() { 
    	DumbLogger.log(this,"started");
    }
    
    public void reset() {  
    	DumbLogger.log(this,"reset");

    }
    
    public void stop() { 
    	DumbLogger.log(this,"stopped");
    }
    
    public Observation explore() {
    	DumbLogger.log(this,"exploring...");
    	Observation obs = new Observation() ;
    	obs.buttons.add(0) ;
    	obs.doors.add(0) ;
    	DumbLogger.log(this,"explore done. Sending back observation " + obs);
    	return obs ;
    }
    
    public Observation observe() {
    	DumbLogger.log(this,"observe");
    	Observation obs = new Observation() ;
    	return obs ;
    }
    
    public void pushButton(int button) {
    	DumbLogger.log(this,"pushButton " + button);
    }
}
