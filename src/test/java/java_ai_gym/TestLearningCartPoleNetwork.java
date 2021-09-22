package java_ai_gym;

import java_ai_gym.models_common.Environment;
import java_ai_gym.models_common.Experience;
import java_ai_gym.models_common.State;
import java_ai_gym.models_poleoncart.CartPole;
import java_ai_gym.models_poleoncart.CartPoleAgentNeuralNetwork;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class TestLearningCartPoleNetwork {

    private static final Logger logger = Logger.getLogger(TestLearningCartPoleNetwork.class.getName());
    State sNew = new State();
    CartPole env = new CartPole();
    CartPoleAgentNeuralNetwork agent = new CartPoleAgentNeuralNetwork(env.parameters,env.getTemplateState());
    private static final int NOF_EPISODES_BETWEEN_PRINTOUTS = 10;

    String filePathBest = "c:/temp/best/";
    String filePathInit = "c:/temp/init/";

    @Test //@Ignore
    //https://www.saashanair.com/dqn-code/
    public void runLearningTextBook() throws InterruptedException, IOException {
        // episode: a full iteration when the agent starts from a random state and finds the terminal state

        logger.info("Init policy defined and saved");
        agent.savePolicy(filePathInit);
        double bestNofSteps=0;
        for (int iEpisode = 0; iEpisode <= agent.NUM_OF_EPISODES; ++iEpisode) {
            env.setRandomStateValuesStart(agent.state);
            env.simulateEpisode(agent, iEpisode, env, env.parameters);

            List<Experience> miniBatch =
                    agent.replayBuffer.getMiniBatchPrioritizedExperienceReplay(
                            agent.MINI_BATCH_SIZE,
                            agent.calcFractionEpisodes(iEpisode));

              if (env.isTimeForPolicyTest(iEpisode)) {
                Environment.PolicyTestReturn policyTestReturn = env.testPolicy(agent, env.parameters, env.policyTestSettings.NOF_TESTS_WHEN_TESTING_POLICY);
                env.printPolicyTest(iEpisode, agent, policyTestReturn, env.parameters.MAX_NOF_STEPS_POLICY_TEST);

                if (policyTestReturn.avgNofSteps>bestNofSteps) {
                    bestNofSteps=policyTestReturn.avgNofSteps;
                    logger.info("New best policy found and saved");
                    agent.savePolicy(filePathBest);
                }
            }
        }


        animateBestPolicy();
        Environment.PolicyTestReturn policyTestReturn = env.testPolicy(agent, env.parameters, env.policyTestSettings.NOF_TESTS_WHEN_TESTING_POLICY);
        env.printPolicyTest(agent.NUM_OF_EPISODES, agent, policyTestReturn, env.parameters.MAX_NOF_STEPS_POLICY_TEST);
        System.out.println("nofFits:"+agent.nofFits+", totalNofSteps:"+agent.state.totalNofSteps);
        System.out.println(agent.network.summary());
        Assert.assertTrue(env.testPolicy(agent, env.parameters, env.policyTestSettings.NOF_TESTS_WHEN_TESTING_POLICY).successRatio>0.8);

    }

    @Test //@Ignore
    public void animateInitPolicy() throws IOException, InterruptedException {
        logger.info("Loading init policy");
        agent.loadPolicy(filePathInit);
        env.animatePolicy(agent);
    }

    @Test   @Ignore
    public void animateBestPolicy() throws IOException, InterruptedException {
        logger.info("Loading best policy");
        agent.loadPolicy(filePathBest);
        env.parameters.MAX_NOF_STEPS=(int) 1e6;
        env.animatePolicy(agent);
        System.out.println(agent.state);
        System.out.println("isCartPoleInBadState:"+env.isFailsState(agent.state));
    }


}
