package java_ai_gym;


import java_ai_gym.models_common.State;
import java_ai_gym.models_sixrooms.SixRooms;
import java_ai_gym.models_sixrooms.SixRoomsAgentNeuralNetwork;
import org.jcodec.common.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.logging.Logger;

public class TestLearningNeuralNetworkSixRooms {
    private static final Logger logger = Logger.getLogger(TestLearningNeuralNetworkSixRooms.class.getName());
    State sNew = new State();
    SixRooms env = new SixRooms();
    SixRoomsAgentNeuralNetwork agent = new SixRoomsAgentNeuralNetwork(env.parameters);
    private final Random random = new Random();
    private static final int NOF_EPISODES_BETWEEN_PRINTOUTS =50;

    @Test
    //https://www.saashanair.com/dqn-code/
    public void runLearningTextBook() {
        // episode: a full iteration when the agent starts from a random state and finds the terminal state

        //logger.info("Nd4jBackend.BACKEND_PRIORITY_CPU: {}", Nd4jBackend.BACKEND_PRIORITY_CPU);
        //logger.info("Nd4jBackend.BACKEND_PRIORITY_GPU: {}", Nd4jBackend.BACKEND_PRIORITY_GPU);

        env.PrintQsa(agent);
        for (int iEpisode = 0; iEpisode < agent.NUM_OF_EPISODES; ++iEpisode) {

            int startState = random.nextInt(env.parameters.nofStates);
            agent.state.setVariable("roomNumber", startState);
            if (env.isTerminalState(agent.state)) continue;  // we do not want to start with the terminal state

            env.simulateEpisode(agent,iEpisode,env,env.parameters);
            printBellmanError(iEpisode);
        }

        env.PrintQsa(agent);
        //System.out.println("nofFits:"+agent.nofFits);
        System.out.println("nofFits:"+agent.nofFits+", totalNofSteps:"+agent.state.totalNofSteps);
        System.out.println(agent.network.summary());
        env.showPolicy(agent);

        agent.state.setVariable("roomNumber", 0);
        Assert.assertEquals(4,agent.chooseBestAction(agent.state));

        agent.state.setVariable("roomNumber", 1);
        Assert.assertEquals(5,agent.chooseBestAction(agent.state));

        agent.state.setVariable("roomNumber", 2);
        Assert.assertEquals(3,agent.chooseBestAction(agent.state));

        agent.state.setVariable("roomNumber", 4);
        Assert.assertEquals(5,agent.chooseBestAction(agent.state));



    }

    private void printBellmanError(int iEpisode) {
        if (iEpisode % NOF_EPISODES_BETWEEN_PRINTOUTS == 0 | iEpisode == 0)
            System.out.println("iEpisode:"+ iEpisode +" bellmanError:"+
                    agent.getBellmanErrorAverage(NOF_EPISODES_BETWEEN_PRINTOUTS)
            );
    }



}
