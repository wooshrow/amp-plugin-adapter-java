# AMP adapter for Lab Recruits

This adapter will allow you to run a test on the Lab Recruits from AMP. The adapter implements a set of AMP-labels. You can see the available labels in the model `demo.aml` in the project `Lab Recruit` in AMP.

When the adapter is launched, it will connect to a running instance of Lab Recruits and load a demo game-level. Then, it connects to the AMP server.

AMP will then control a player-agent in the Lab Recruits.

Currently implemented AMP-labels are:

  * stimulus `explore`: This will wipe the agent's memory and then send the agent to explore the currently loaded Lab Recruits level. `explore` will not push any button. Note that finishing exploration takes time, especially if the game-level is big.

  * response `observation`: contains information on what the Lab Recruits agent has been observing since the last `explore`. The observation will contain information about buttons and open doors that were seen during since the last exploration. A seen entity is, usually, also physically reachable (assuming the physical level does not have 'windows' that the agent can peek through).

  * stimulus `push_button(i)`: this will cause the agent to go to a button and interact with it. Again, note that going to a game entity takes time, as the agent needs to physically go there. Also, such a task can only be completed if the button is physically reachable from the current agent's location.

  * Stimulus `pass_door(i)`: this will cause an agent to go towards a door. Currently this will not cause the agent to actually pass the door; it will only come close enough so that it can see the door.

  * Stimulus 'finishlevel': I leave it to you to implement this :) It is supposed to make the agent to go to the goal-flag in the level (assuming there is exactly one in the level) and touch it.

## How to build this project

  For now, the most convenient way is to build it from the Eclipse IDE. From Eclipse, **import this project as a Maven project**. This will build the project.

## How to get the Lab Recruits game

You have to build it :) [Get the source from here](https://github.com/iv4xr-project/labrecruits). There are build-instructions there. To build it you need the UNITY game development environment. You will need a specific version of UNITY. This information is in the said build instructions.

## How try an AMP-demo on Lab Recruits

1. Go to AMP. Open the project `Lab Recruit`, and then choose the the model `demo.aml` in the in AMP.

1. Run the game Lab Recruits.

1. I assume you have imported this project into Eclipse. From Eclipse, run the main-method of the class `PluginAdapter` (in the package `ampPluginAdapter`). This will launch this Adapter, load a demo-level to Lab Recruits, and then connect to AMP.

1. You are now ready to run a test. From AMP, start a new test.

## Issues

* Currently we cannot run the next test without first stopping and re-running the `PluginAdapter`.
* Test replay does not work yet either.
