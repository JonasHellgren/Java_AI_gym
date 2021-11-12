package java_ai_gym.test_dqn;


import java_ai_gym.models_common.Experience;
import java_ai_gym.models_common.State;
import java_ai_gym.models_common.StepReturn;
import java_ai_gym.models_mountaincar.MountainCar;
import java_ai_gym.models_mountaincar.MountainCarAgentNeuralNetwork;
import java_ai_gym.swing.Position2D;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/*** These tests focus on the function approximator of the agent, i.e. training of neural network.
 * By "mocking" of the reward tests become standalone, independent of the actual reward function.
 *
 */

public class TestLFunctionApproximationMountainCarNetwork {

    private static final Logger logger = Logger.getLogger(TestLFunctionApproximationMountainCarNetwork.class.getName());

    MountainCar env = new MountainCar();
    MountainCarAgentNeuralNetwork agent = new MountainCarAgentNeuralNetwork(env.parameters,env.getTemplateState());
    private static final int NOF_EPISODES_BETWEEN_PRINTOUTS =10;


    @Test  @Ignore
    public void learnAtSameInputBigNegativeRewardZeroGamma() {

        agent.GAMMA=0;
        env.parameters.MAX_START_POSITION=env.parameters.POSITION_AT_MIN_HEIGHT;
        env.parameters.MIN_START_POSITION=env.parameters.POSITION_AT_MIN_HEIGHT;
        env.parameters.MIN_START_VELOCITY=0;
        env.parameters.MAX_START_VELOCITY=0;
        agent.MINI_BATCH_SIZE=10;

      while (agent.replayBuffer.size()<agent.REPLAY_BUFFER_SIZE) {
            env.setRandomStateValuesAny(agent.state);
            int aChosen=agent.chooseRandomAction(env.parameters.discreteActionsSpace);
            StepReturn stepReturn = env.step(aChosen, agent.state);
            stepReturn.reward=(double) -100*aChosen;  //mocking reward
            Experience experience = new Experience(new State(agent.state), aChosen, stepReturn,agent.BE_ERROR_INIT);
            agent.replayBuffer.addExperience(experience);
        }

        List<Experience> miniBatch=agent.replayBuffer.getMiniBatchPrioritizedExperienceReplay(agent.MINI_BATCH_SIZE,0.5);
        for (int i = 0; i < 50 ; i++) {
            agent.fitFromMiniBatch(miniBatch,1);
            agent.printQsa(env.parameters);
        }

        Assert.assertEquals(0.0,agent.readMemory(agent.state,0),50);
        Assert.assertEquals(-100,agent.readMemory(agent.state,1),50);
        Assert.assertEquals(-200,agent.readMemory(agent.state,2),50);

    }




    @Test
    public void learnFromRandomInputsZeroGammaMockedReward() throws InterruptedException {

        agent.GAMMA=0;
        createReplayBuffer();
        trainAgent();
        System.out.println("state:"+agent.state);
        System.out.println("nofFits:"+agent.nofFits+", totalNofSteps:"+agent.state.totalNofSteps);

        setCirclesToPlot();
        env.plotPanel.repaint();
        TimeUnit.MILLISECONDS.sleep(10000);

    }

    private void createReplayBuffer() {
        while (agent.replayBuffer.size()<agent.REPLAY_BUFFER_SIZE) {
                    for (int a:env.parameters.discreteActionsSpace) {
                        env.setRandomStateValuesAny(agent.state);
                        StepReturn stepReturn = env.step(a, agent.state);
                        stepReturn.reward=calcRuleBasedReward(
                                agent.state.getContinuousVariable("position"),
                                agent.state.getContinuousVariable("velocity"),
                                a);  //mocking reward
                        Experience experience = new Experience(new State(agent.state), a, stepReturn,agent.BE_ERROR_INIT);
                        agent.replayBuffer.addExperience(experience);
            }
        }
    }

    private void trainAgent() {
        agent.LEARNING_RATE_START=1e-2;
        agent.LEARNING_RATE_END=1e-3;
        agent.NUM_OF_EPISODES=1000;
        agent.createLearningRateScaler();
        for (int i = 0; i < agent.NUM_OF_EPISODES ; i++) {
            if (i % 10 ==0) {
                System.out.print("episode: "+ i);
                System.out.printf(", learning rate: %.5f", agent.calcLearningRate(agent.calcFractionEpisodes(i)));
                System.out.printf(", fraction Episodes: %.5f", agent.calcFractionEpisodes(i));
                System.out.println();
            }
            List<Experience> miniBatch=agent.replayBuffer.getMiniBatchPrioritizedExperienceReplay(agent.MINI_BATCH_SIZE,agent.calcFractionEpisodes(i));
            agent.fitFromMiniBatch(miniBatch,agent.calcFractionEpisodes(i));
        }
    }

    private void setCirclesToPlot() {
        List<Position2D> circlePositionList = new ArrayList<>();
        List<Integer> actionList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            //env.setRandomStateValuesAny(agent.state);
            env.setRandomStateValuesStart(agent.state);

            double pos=agent.state.getContinuousVariable("position");
            double vel=agent.state.getContinuousVariable("velocity");
            circlePositionList.add(new Position2D(pos,vel));
            actionList.add(agent.chooseBestAction(agent.state));

            agent.printPositionAndVelocity();
            agent.printQsa(env.parameters);

            for (int a : env.parameters.discreteActionsSpace)
                Assert.assertEquals(calcRuleBasedReward(pos, vel, a),
                        agent.readMemory(agent.state, a),
                        0.5);

        }

        env.plotPanel.setCircleData(circlePositionList,actionList);
    }


    private double calcMockReward(double pos, double vel, int a) {
        return pos * 100 +
                vel * 100 +
                (double) 1 * a;
    }

    private double calcRuleBasedReward(double pos, double vel, int a) {
        int desiredAction=(vel <-0.001)?0:2;
        double rAction=(a==desiredAction)?1.0:0.0;
        return -10*1+rAction;
    }


    private void printBellmanError(int iEpisode, int nofSteps, double maxPosition) {
        if (iEpisode % NOF_EPISODES_BETWEEN_PRINTOUTS == 0 | iEpisode == 0)
            System.out.println("iEpisode:"+ iEpisode +" bellmanError:"+
                    agent.getBellmanErrorAverage(NOF_EPISODES_BETWEEN_PRINTOUTS)+
                    ", nofSteps:"+nofSteps+
                    ", max position:"+maxPosition);
    }


}
