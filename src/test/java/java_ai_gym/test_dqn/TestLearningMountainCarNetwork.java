package java_ai_gym.test_dqn;

import java_ai_gym.models_common.Environment;
import java_ai_gym.models_common.Experience;
import java_ai_gym.models_common.State;
import java_ai_gym.models_mountaincar.MountainCar;
import java_ai_gym.models_mountaincar.MountainCarAgentNeuralNetwork;
import java_ai_gym.swing.Position2D;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TestLearningMountainCarNetwork {

    private static final Logger logger = Logger.getLogger(TestLearningMountainCarNetwork.class.getName());
    State sNew = new State();
    MountainCar env = new MountainCar();
    MountainCarAgentNeuralNetwork agent = new MountainCarAgentNeuralNetwork(env.parameters,env.getTemplateState());
    private static final int NOF_EPISODES_BETWEEN_PRINTOUTS = 10;
    String filePathBest = "c:/temp/montcar/best/";
    String filePathInit = "c:/temp/montcar/init/";


    @Test
    @Ignore
    //https://www.saashanair.com/dqn-code/
    public void runLearningTextBook() throws InterruptedException, IOException {
        // episode: a full iteration when the agent starts from a random state and finds the terminal state

        logger.info("Init policy defined and saved");
        agent.savePolicy(filePathInit);
        plotPolicy();
        double bestNofSteps=Double.MAX_VALUE;
        for (int iEpisode = 0; iEpisode < agent.NUM_OF_EPISODES; ++iEpisode) {
            env.setRandomStateValuesAny(agent.state);
            env.simulateEpisode(agent, iEpisode, env, env.parameters);

            List<Experience> miniBatch =
                    agent.replayBuffer.getMiniBatchPrioritizedExperienceReplay(
                            agent.MINI_BATCH_SIZE,
                            agent.calcFractionEpisodes(iEpisode));
            plotMiniBatch(miniBatch);

            if (env.isTimeForPolicyTest(iEpisode)) {
                Environment.PolicyTestReturn policyTestReturn = env.testPolicy(agent, env.parameters, env.policyTestSettings.NOF_TESTS_WHEN_TESTING_POLICY);
                env.printPolicyTest(iEpisode, agent, policyTestReturn, env.parameters.MAX_NOF_STEPS_POLICY_TEST);

                if (policyTestReturn.avgNofSteps<bestNofSteps) {
                    bestNofSteps=policyTestReturn.avgNofSteps;
                    logger.info("New best policy found and saved");
                    agent.savePolicy(filePathBest);
                }

            }
        }

        plotPolicy();
        TimeUnit.MILLISECONDS.sleep(1000);
        agent.loadPolicy(filePathBest);
        env.animatePolicy(agent);

        System.out.println("nofFits:"+agent.nofFits+", totalNofSteps:"+agent.state.totalNofSteps);
        System.out.println(agent.network.summary());
        Assert.assertTrue(env.testPolicy(agent, env.parameters, env.policyTestSettings.NOF_TESTS_WHEN_TESTING_POLICY).successRatio>0.8);

    }


    public void plotPolicy() {
        List<Position2D> circlePositionList = new ArrayList<>();
        List<Integer> actionList = new ArrayList<>();
        for (int i = 0; i < env.gfxSettings.NOF_DOTS_PLOTTED_POLICY; i++) {
            env.setRandomStateValuesAny(agent.state);
            double pos=agent.state.getContinuousVariable("position");
            double vel=agent.state.getContinuousVariable("velocity");
            circlePositionList.add(new Position2D(pos,vel));
            actionList.add(agent.chooseBestAction(agent.state));
        }
        env.plotPanel.setCircleData(circlePositionList,actionList);
        env.plotPanel.repaint();
    }

    public void plotMiniBatch(List<Experience> miniBatch) {
        List<Position2D> circlePositionList = new ArrayList<>();
        List<Integer> actionList = new ArrayList<>();
        for (Experience exp: miniBatch) {
            double pos=exp.s.getContinuousVariable("position");
            double vel=exp.s.getContinuousVariable("velocity");
            circlePositionList.add(new Position2D(pos,vel));
            actionList.add(exp.action);
        }
        env.plotPanel.setCircleData(circlePositionList,actionList);
        env.plotPanel.repaint();
    }


    @Test @Ignore
    public void animateInitPolicy() throws IOException, InterruptedException {
        logger.info("Loading init policy");
        agent.loadPolicy(filePathInit);
        env.animatePolicy(agent);
    }

    @Test   //@Ignore
    public void animateBestPolicy() throws IOException, InterruptedException {
        logger.info("Loading best policy");
        agent.loadPolicy(filePathBest);
        env.parameters.MAX_NOF_STEPS=(int) 1e6;
        env.animatePolicy(agent);
        System.out.println(agent.state);
        System.out.println("isBadState:"+env.isFailsState(agent.state));
    }

}
