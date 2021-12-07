package java_ai_gym.models_pong;

import java.util.List;

public class PongAgentDPSearch extends AgentDPSearch {

    final int ACTION_DEFAULT = 1;

    public PongAgentDPSearch(SinglePong env, long timeBudget, int searchDepthStep) {
        super(env, timeBudget, searchDepthStep);
    }

    public PongAgentDPSearch(SinglePong env, long timeBudget, int searchStepUpper, int searchDepthStep, double explorationFactorLimit, double discountFactorReward,double discountFactorExpFactor) {
        super(env, timeBudget,searchStepUpper, searchDepthStep, explorationFactorLimit, discountFactorReward,discountFactorExpFactor);
    }

    public int getActionDefault() {
        return ACTION_DEFAULT;
    }

    public List<Integer> getActionSet () {
        return envParams.discreteActionsSpace;
    }

}
