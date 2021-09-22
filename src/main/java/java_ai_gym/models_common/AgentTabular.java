package java_ai_gym.models_common;


import com.google.common.primitives.Doubles;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/***
 * Tabular based agent. Sub classes do for example define Qsa.
 */

public abstract class AgentTabular implements Learnable {

    private static final Logger logger = Logger.getLogger(AgentTabular.class.getName());
    public State state;
    public double[][] Qsa;   //tabular memory
    public EnvironmentParametersAbstract envParams;  //sub class constructor must define this field

    private final Random random = new Random();
    public  double Q_INIT;
    public double GAMMA;  // gamma discount factor
    public  double ALPHA;  // learning rate
    public  double PROBABILITY_RANDOM_ACTION ;  //probability choosing random action
    public  int NUM_OF_EPISODES; // number of iterations

    @Override
    public State getState() {
        return state;
    }

    @Override
    public int chooseBestAction(State state) {

        double[] QsVec = readMemory(state);

        double maxQ = -Double.MAX_VALUE;
        int bestAction = envParams.discreteActionsSpace.get(0);
        for (int action : envParams.discreteActionsSpace) {
             if (QsVec[envParams.getIdxAction(action)] > maxQ) {
                maxQ = QsVec[envParams.getIdxAction(action)];
                bestAction = envParams.discreteActionsSpace.get(envParams.getIdxAction(action));
            }
        }
        return bestAction;
    }

    @Override
    public double findMaxQ(State state) {
        return Doubles.max(readMemory(state));
    }

    @Override
    public int chooseRandomAction(List<Integer> actions) {
        return actions.get(random.nextInt(actions.size()));
    }

    @Override
    public int chooseAction(double fractionEpisodesFinished,List<Integer> actions) {
        return (Math.random() < PROBABILITY_RANDOM_ACTION) ?
                chooseRandomAction(envParams.discreteActionsSpace) :
                chooseBestAction(state);
    }

    @Override
    public void writeMemory(State state, Integer action, Double value) {
        Qsa[envParams.getIdxState(state)][envParams.getIdxAction(action)] = value;
    }

    @Override
    public double readMemory(State state, int action) {
        return Qsa[envParams.getIdxState(state)][envParams.getIdxAction(action)];
    }

    public double[] readMemory(State state) {
        return Qsa[envParams.getIdxState(state)];
    }

}