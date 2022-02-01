package ampPluginAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ampPluginAdapter.AdapterCore;
import ampPluginAdapter.SUT.Observation;
import ampPluginAdapter.protobuf.Api.LabelOuterClass.Label;
import ampPluginAdapter.protobuf.Api.ProtobufUtils;

//import ampPluginAdapter.Logger;
import java.lang.Thread;


public class Handler {
	
		
    public SUT sut ;
    
    Boolean stop_sut_thread = false ;
    public AdapterCore adapterCore;
    //private Logger Logger;
    private ArrayList<String> responses = new ArrayList<String>();
    private Thread sut_thread = new Thread();
    Map<String,String> configuration = new HashMap<>();
    
    /**
     * The constructor for the test agent.
     */
    public Handler() { 
    	this.sut = new SUT();
    }
    	
	public void registerAdapterCore(AdapterCore adapter_core) {
		this.adapterCore = adapter_core;
	}
	
	/**
	 * Instruct the SUT to do the given label. If the execution of the Label is supposed
	 * to immediately trigger a response, return that response as a Label,
	 * else return null.
	 */
	public Label stimulate(Label label) {
		DumbLogger.log(this, "received " + label.getLabel() + ", for not not doing anything with it");
		String labelName = label.getLabel() ;
		switch (labelName) {
		  case "explore" : 
			  Observation obs = sut.explore() ;
			  if (obs.buttons.size()>0) {
				  return mk_observedButtons_Response(obs) ;
			  }
			  break ;
		  case "push_button" : 
			  int bnr = (int) label.getParameters(0).getValue().getInteger() ;
			  sut.pushButton(bnr);
			  break ;
		  case "pass_door" : break ;
		}
		return null ;
	}
	
	Label mk_observedButtons_Response(Observation obs) {
		if (obs.buttons.size() > 0) {
			List<KeyValuePair<String,String>> parametersNamesAndTypes = new LinkedList<>() ;
			parametersNamesAndTypes.add(pair("_buttons","[integer]")) ;
			Map<String,Object> values = new HashMap<>() ;
			values.put("_buttons", obs.getButtons()) ;
			Label lab = ProtobufUtils.mkValueLabel("observed_buttons", 
					adapterCore.channel, 
					Label.LabelType.RESPONSE, 
					parametersNamesAndTypes, 
					values) ;
			return lab ;
		}
		throw new IllegalArgumentException() ;
	}
	
	public void start() {		
		// we'll NOT run LR in a separate thread. Just call SUT start:	
		sut.start();
		
		// this.responses = null;
		//this.sut = Sut(this.responses,this.Logger);
		//this.stop_sut_thread = false;
        //self.sut_thread = Thread(target=self.running_sut,args=(lambda : self.stop_sut_thread, ))	
		//this.sut_thread = new Thread(this.running_sut, String name)
		// this.sut_thread.start();
	}
	

	public void reset() {
		DumbLogger.log(this,"Resetting the sut for new test cases");
		sut.reset();
	}
	

	public void stop() {
		DumbLogger.log(this,"Stopping the plugin adapter from plugin handler");
		
		// since LR is ran in the same thread, we can simply do this:
		sut.stop(); 
			
		//this.Sut.ble_reset();
		//this.Sut.stop();
		/*
		this.sut = null;
		this.stop_sut_thread = true;
		try {
			this.sut_thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.sut_thread = null;
		*/
		DumbLogger.log(this,"Finished stopping the plugin adapter from plugin handler");
	}
	
	static <A,B> KeyValuePair<A,B> pair(A name, B value) {
		return new KeyValuePair<A,B>(name,value) ;
	}
	
	public List<Label> supportedLabels() {
		List<Label> labeltypes = new LinkedList<>() ;
		labeltypes.add(ProtobufUtils.mkStimulusTypeLabel(
				"explore",
				adapterCore.channel)) ;
		labeltypes.add(ProtobufUtils.mkStimulusTypeLabel(
				"push_button",adapterCore.channel,
				pair("_number","integer")
				)) ;
		
		labeltypes.add(ProtobufUtils.mkStimulusTypeLabel(
				"pass_door",adapterCore.channel,
				pair("_number","integer")
				)) ;
		labeltypes.add(ProtobufUtils.mkResponseTypeLabel(
				"observed_buttons",adapterCore.channel,
				pair("_buttons","[integer]")
				))  ;
		labeltypes.add(ProtobufUtils.mkResponseTypeLabel(
				"observed_doors",adapterCore.channel,
				pair("_doors","[integer]")
				))  ;
		labeltypes.add(ProtobufUtils.mkResponseTypeLabel(
				"dummy",adapterCore.channel,
				pair("_number","integer")
				))  ;
		
		return labeltypes ;
	}

}
