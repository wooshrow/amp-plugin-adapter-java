package ampPluginAdapter;

import static agents.TestSettings.USE_INSTRUMENT;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import game.LabRecruitsTestServer;
import world.BeliefState;
import world.LabEntity;

import static nl.uu.cs.aplib.AplibEDSL.*;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.Goal;

/**
 * This class will connect to a running Lab Recruits client and load a level.
 * The class provides several high-level interaction with the game.
 */
public class SUTLabRecruits {
	
	public static String levelName = "buttons_doors_flag" ;
	//public static String levelName = "lab1b" ;
	public static String agentId = "agent1" ;
	
	/**
	 * The maximum number of agent-update-cycles to execution a single
	 * AMP-transition.
	 */
	public static int singleTransitionBudget = 300 ;
	public static int sleepTimeBetweenUpdates = 50 ;
	public static String doorNamePrefix = "door" ;
	public static String buttonNamePrefix = "button" ;
	public static String goalFlagName = "leveldone" ;
	
	
	
	
    private LabRecruitsTestServer labRecruitsTestServer;
    
    public LabRecruitsTestAgent testAgent ;

	
    public SUTLabRecruits() { 
    	// Use a running LR:
    	TestSettings.USE_SERVER_FOR_TEST = false ;
    	// Uncomment this to make the game's graphic visible:
    	TestSettings.USE_GRAPHICS = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
    	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir) ;   	
    	this.reset();
    }

      
    public synchronized void start() { 
    	DumbLogger.log(this,"started");
    }
    
    public synchronized void reset() {  
    	DumbLogger.log(this,"reset; reloading level");
    	// Create a fresh environment ; this will reload the level to test:
    	var config = new LabRecruitsConfig(levelName) ;
    	//config.light_intensity = 0.5f ;
    	var environment = new LabRecruitsEnvironment(config);
    	// Create a fresh agentL
    	testAgent = new LabRecruitsTestAgent(agentId) // matches the ID in the CSV file
    		    . attachState(new BeliefState())
    		    . attachEnvironment(environment)
    		    . setTestDataCollector(new TestDataCollector())
    		    ;
    }
    
    public synchronized void stop() { 
    	DumbLogger.log(this,"stop");
    	if(labRecruitsTestServer!=null) 
    		labRecruitsTestServer.close(); 
    }
    
    /**
     * This will wipe the agent's memory on seen navigation nodes and game-entities,
     * and then it will explore the level as much as it can, and without interacting
     * with any button.
     * 
     * It returns the BeliefState at the end of the exploration.
     */
    public synchronized BeliefState explore() throws InterruptedException {
    	DumbLogger.log(this,"exploring...");
    	// wipe memorized navigation nodes and entities:
    	testAgent.getState().pathfinder().wipeOutMemory();
    	testAgent.getState().worldmodel().elements.clear();
    	Goal g = goal("keep exploring...") 
    			.toSolve_(S -> false)
    			.withTactic(
    				FIRSTof(
    				  TacticLib.explore(), 
    				  ABORT()
    				)
    			 ) ;
    	GoalStructure G = FIRSTof(
    			g.lift(),
    			SUCCESS()
    		) ;
    	runAgent(G,"exploring") ;
    	return observe() ;
    }
    
    /**
     * Run the agent to try to solve the given goal-structure. The budget to
     * do this (in terms of the maximum number of agent-update-cycles. Is
     * limited by {@link #singleTransitionBudget}.
     */
    void runAgent(GoalStructure G, 
    		String goalStructureName) 
    	throws InterruptedException {
    	DumbLogger.log(this,"Deploying a goal-structure: " + goalStructureName);
    	testAgent.setGoal(G) ;
    	int i = 0 ;
    	while(G.getStatus().inProgress()) {
    		System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
            Thread.sleep(sleepTimeBetweenUpdates);
            i++ ;
        	testAgent.update();
        	if (i>singleTransitionBudget) {
        		break ;
        	}
    	}
    	if (G.getStatus().success()) {
    		DumbLogger.log(this,"" + goalStructureName + " was succesful.");
    	}
    	else {
    		DumbLogger.log(this,"The agent COULD NOT solve: " + goalStructureName);
    	}
    }
    
    public BeliefState observe() {
    	DumbLogger.log(this,"observe");
    	testAgent.update();
    	return testAgent.getState() ;
    }
    
    /**
     * This will make the agent to go to the given button, and interact with it.
     * Note that the agent can only go to a button that is physically reachable
     * for it.
     * It returns the agent's BeliefState at the end.
     */
    public synchronized BeliefState pushButton(int button) throws InterruptedException {
    	DumbLogger.log(this,"going to push button" + button);
    	GoalStructure G = GoalLib.entityInteracted(buttonNamePrefix + button) ;
    	runAgent(G,"push-button" + button) ;
    	return observe() ;
    }
    
    /**
     * This will make the agent to approach the given door, until the agent can see it.
     * Note that the agent can only go to a door that is physically reachable
     * for it.
     * It returns the agent's BeliefState at the end.
     */
    public synchronized BeliefState approachOpenDoor(int door) throws InterruptedException {
    	DumbLogger.log(this,"approaching door" + door);
    	GoalStructure G = GoalLib.entityStateRefreshed(doorNamePrefix + door) ;
    	runAgent(G,"approach-door " + door) ;
    	return observe() ;
    }
    
    /**
     * This should make the agent to go to a/the goal-flag and touch it.
     * Note that the agent can only go to a door that is physically reachable
     * for it.
     * It returns the agent's BeliefState at the end.
     */
    public synchronized BeliefState touchGoalFlag() throws InterruptedException {
    	DumbLogger.log(this,"approaching goal flag");
    	GoalStructure G = GoalLib.atBGF(goalFlagName, 0.5f,true) ;
    	runAgent(G,"approach-goalflag") ;
    	return observe() ;
    }
}
