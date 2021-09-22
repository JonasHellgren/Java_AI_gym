# Java open AI gym
The [Open AI gym](https://gym.openai.com/envs/#classic_control) is an excellent toolkit for developing and comparing reinforcement learning algorithms. 
Some example environments are Atari games such as Pong. Classical control problems as inverted pendulum are also included. These environments are coded in Python.
The package described in this document is coded in Java, a Java version of Open AI gym.  
## Scope
The ambition is to cover a sub set of the environments in "original" Open AI gym, the focus is on problems with discrete actions. 


## Major classes 
The major clases in the package are the abstract classes: 1) Environment and 2) AgentNeuralNetwork.
As the class diagram below shows subclasses of Environment implements different environments. These are described later in the document.

![node](../java_ai_gym/mdpics/classes_environment.png)

All agents use the interface/behavior Learnable. This interface specifies, for example, that an agent shall implement the method chooseBestAction().
The abstract class AgentNeuralNetwork, is the working horse in the package. It has methods enabling using deep Q-learning.
Subclasses of AgentNeuralNetwork are agents specialized to solve a specific environment. In this subclasses are for example the network architecture and hyper parameters such as learning rate specified.

![node](../java_ai_gym/mdpics/classes_agent.png)

Additional comments and descriptions:
* Every environment has a variable parameters defining data in the class. For cart pole it can for example be the pole mass.
* The class EnvironmentParametersAbstract "forces" the constructor of an environment to define the names of the states in the environment. It also has methods needed by tabular agents.
* The abstract class Environment has multiple abstract methods. Some examples are render, step and isTerminalState. The implementing class, for ex CartPole, defines these classes.
* The step method, defined in all environments, is very critical. It defines the consequence of taking an action in a specific state.
* The abstract class Environment has a field templateState. It is of type State and includes all variables relevant for the environment. For example x, cart x-position, in CartPole environment. Handy when  
* Deep Q learning can get unstable, i.e. start to diverge from present best policy. Therefore, the best so far is saved by savePolicy, if criteria is fulfilled. For CartPole the criteria is few steps. 
* Additional classes not specified in above figures are State and ReplayBuffer. State includes hash maps defining values of the variables specific for an agent in a specific environment. ReplayBuffer can be seen as a class supporting network based agents.

## Environments
This chapter describes the modeled environments. 
### Six rooms
This can be regarded as a very simple maze problem. There are 6 rooms: room 0-room 5. The action specifies the new room. Non allowed destination rooms/actions gives bad reward.
Entering goal room 5 gives high reward and terminates an episode. Transitions are deterministic.

![node](../java_ai_gym/mdpics/6room_environment.png)

### Mountain car
A car is on a one-dimensional track, positioned between two "mountains". The goal is to drive up the mountain on the right; however, the car's engine is not strong enough to scale the mountain in a single pass. Therefore, the only way to succeed is to drive back and forth to build up momentum.

Car x position and speed are continuous states. Reward is minus o for all states except when vehicle enters the very right position. Then episode terminates and reward is zero.

![node](../java_ai_gym/mdpics/mountaincar_anim.png)
### Cart pole
A pole is attached by an un-actuated joint to a cart, which moves along a frictionless track. The system is controlled by applying a force of +1 or -1 to the cart. The pendulum starts upright, and the goal is to prevent it from falling over. A reward of +1 is provided for every timestep that the pole remains upright. The episode ends when the pole is more than 15 degrees from vertical, or the cart moves more than 2.4 units from the center.

![node](../java_ai_gym/mdpics/cartpole_anim.png)

## Agents
This chapter describes agents solving the previously presented environments.
### Six rooms agent
Layout and some parameters are given below. 

![node](../java_ai_gym/mdpics/agentNetwork_sixroom.png)

| **Parameter**      | **Value**  |
| ----------- | ----------- |
| Nof episodes      | 1000      |
| Learning rate {start,end}   | 0.01, 0.001        |
| Replay buffer size | 100       |
| Mini batch size  | 10       |

### Mountain car agent


![node](../java_ai_gym/mdpics/agentNetwork_mountaincar.png)
Layout and some parameters are given below.
| **Parameter**      | **Value**  |
| ----------- | ----------- |
| Nof episodes      | 200      |
| Learning rate {start,end}   | 0.01, 0.0001        |
| Replay buffer size | 2000       |
| Mini batch size  | 30       |


### Cart pole agent
Layout given below.
![node](../java_ai_gym/mdpics/agentNetwork_cartPole.png)

Similar parameters settings as mountain car.



## Example training

Below is an example print out from training a cart pole. Good policy found after 150 episoded.

![node](../java_ai_gym/mdpics/example_training.png)



