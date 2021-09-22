package java_ai_gym;


import java_ai_gym.models_common.State;
import java_ai_gym.models_common.StepReturn;
import java_ai_gym.models_sixrooms.SixRooms;
import java_ai_gym.models_sixrooms.SixRoomsAgentTabular;
import org.junit.Test;

import java.util.Random;

public class TestLearningTabularSixRooms {

    State sNew = new State();
    SixRooms env = new SixRooms();
    SixRoomsAgentTabular agent = new SixRoomsAgentTabular(env.parameters);
    public final double SMALL = 0.001;
    private final Random random = new Random();


    @Test
    public void runLearningTextBook() {
        // episode: a full iteration when the agent starts from a random state and finds the terminal state
        for (int iEpisode = 0; iEpisode < agent.NUM_OF_EPISODES; ++iEpisode) {
            int startState = random.nextInt(env.parameters.nofStates);
            agent.state.setVariable("roomNumber", startState);
            if (env.isTerminalState(agent.state)) continue;  // we do not want to start with the terminal state
            simulateTextBook(false, (double) iEpisode / agent.NUM_OF_EPISODES);
        }

        env.PrintQsa(agent);
        env.PrintQsaBestAction(agent);
        env.showPolicy(agent);
    }

    private void simulateTextBook(boolean printFlag, double fEpisodes) {
        // Q learning equation: Q[s][a] = Q[s][a] + alpha ( R[s][a] + gamma (max Q[sNew]) - Q[s][a] )
        // a single episode: the agent finds a path from state s to the exit state

        StepReturn stepReturn;
        do {
            int aChosen = agent.chooseAction(fEpisodes,env.parameters.discreteActionsSpace);
            stepReturn = env.step(aChosen, agent.state);
            sNew.copyState(stepReturn.state);
            double maxQ = agent.findMaxQ(sNew);
            double qOld = agent.readMemory(agent.state,aChosen);
            double qNew = qOld + agent.ALPHA * (stepReturn.reward + agent.GAMMA * maxQ - qOld);

            if (printFlag) {
                System.out.println(agent.state);
                System.out.println(stepReturn);
            }

            agent.writeMemory(agent.state,
                    aChosen, stepReturn.termState ? stepReturn.reward : qNew);
            agent.state.copyState(sNew);
        } while (!stepReturn.termState);
    }

}