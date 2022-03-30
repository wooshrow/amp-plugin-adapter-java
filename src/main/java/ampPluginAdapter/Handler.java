package ampPluginAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ampPluginAdapter.AdapterCore;
import ampPluginAdapter.protobuf.Api.LabelOuterClass.Label;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import ampPluginAdapter.protobuf.Api.ProtobufUtils;
import world.BeliefState;
import world.LabWorldModel;

//import ampPluginAdapter.Logger;
import java.lang.Thread;


public class Handler {
	
		
    //public CobaSUT sut ;
    public SUTLabRecruits sut ;
    
    // Dont need these. We will run the SUT on the main-thread:
    //Boolean stop_sut_thread = false ;
    //private Thread sut_thread = new Thread();
    //private ArrayList<String> responses = new ArrayList<String>();
    
    public AdapterCore adapterCore;
    Map<String,String> configuration = new HashMap<>();
    
    /**
     * The constructor for the test agent.
     */
    public Handler() { 
    	this.sut = new SUTLabRecruits();
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
		BeliefState obs = null ;
		try {
			switch (labelName) {
			  case "explore" : 
				  obs = sut.explore() ; break ;
			  case "push_button" : 
				  int bnr = (int) label.getParameters(0).getValue().getInteger() ;
				  obs = sut.pushButton(bnr);
				  break ;
			  case "pass_door" : 
				  int doornr = (int) label.getParameters(0).getValue().getInteger() ;
				  obs = sut.approachOpenDoor(doornr);
				  break ;
			}
		}
		catch(Exception e) {
			adapterCore.connectionBroker.sendError("The SUT-side throws an exception");
			e.printStackTrace();
			return null ;
		}
		if(!sut.testAgent.success()) {
			adapterCore.connectionBroker.sendError("The last goal is NOT solved.");
			return null ;
		}
		return mk_observation_Response(obs) ;
	}
	
	Label mk_observation_Response(BeliefState agentstate) {
		LabWorldModel wom = agentstate.worldmodel() ;
		
		// constructing the set of information to send to AMP:
		// the open doors:
		List<Integer> openDoors = new LinkedList<>() ;
		for(WorldEntity e : agentstate.knownDoors()) {
    		int offset = SUTLabRecruits.doorNamePrefix.length() ;
    		int id = Integer.parseInt(e.id.substring(offset)) ;
    		if (agentstate.isOpen(e.id)) {
    			openDoors.add(id) ;	    			
    		}
    	}
		// converting to array:
		int[] openDoors__ = new int[openDoors.size()] ;
		int k = 0 ;
		for (Integer d : openDoors) {
			openDoors__[k] = d ;
			k++;
		}
		
	    // the buttons:
		List<Integer> buttons = new LinkedList<>() ;
    	for(WorldEntity e : agentstate.knownButtons()) {
    		int offset = SUTLabRecruits.buttonNamePrefix.length() ;
    		int id = Integer.parseInt("" + e.id.charAt(offset)) ;
    		buttons.add(id) ;	
    	}
    	int[] buttons__ = new int[buttons.size()] ;
		k = 0 ;
		for (Integer b : buttons) {
			buttons__[k] = b ;
			k++;
		}
		
		List<KeyValuePair<String, String>> parametersNamesAndTypes = new LinkedList<>();
		parametersNamesAndTypes.add(pair("_buttons", "[integer]"));
		parametersNamesAndTypes.add(pair("_opendoors", "[integer]"));
		parametersNamesAndTypes.add(pair("_health", "integer"));
		Map<String, Object> values = new HashMap<>();
		values.put("_buttons", buttons__);
		values.put("_opendoors", openDoors__);
		values.put("_health", agentstate.worldmodel().health);
		Label lab = ProtobufUtils.mkValueLabel("observation", adapterCore.channel, Label.LabelType.RESPONSE,
				parametersNamesAndTypes, values);
		return lab;
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
		labeltypes.add(ProtobufUtils.mkStimulusTypeLabel(
				"finishlevel",adapterCore.channel)) ;
		labeltypes.add(ProtobufUtils.mkResponseTypeLabel(
				"observation",adapterCore.channel,
				pair("_buttons","[integer]"),
				pair("_opendoors","[integer]"),
				pair("_health","integer")
				))  ;
		labeltypes.add(ProtobufUtils.mkResponseTypeLabel(
				"dummy",adapterCore.channel,
				pair("_number","integer")
				))  ;
		
		return labeltypes ;
	}

}
