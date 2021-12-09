package java_ai_gym.models_pong;

import java_ai_gym.models_common.StateForSearch;
import java_ai_gym.models_agent_search.AgentDPSearch;

import java.util.List;

public class PongAgentDPSearch extends AgentDPSearch {

    final int ACTION_DEFAULT = 1;

    public PongAgentDPSearch(SinglePong env, long timeBudget, int searchDepthStep) {
        super(env, timeBudget, searchDepthStep);
    }

    public PongAgentDPSearch(SinglePong env, long timeBudget, int searchStepUpper, int searchDepthStep, double explorationFactorLimit, double discountFactorReward,double discountFactorExpFactor) {
        super(env, timeBudget,searchStepUpper, searchDepthStep, explorationFactorLimit, discountFactorReward,discountFactorExpFactor);
    }

    @Override
    public int getActionDefault(StateForSearch selectState) {
        return ACTION_DEFAULT;
    }

    @Override
    public List<Integer> getActionSet (StateForSearch selectState) {
        return envParams.discreteActionsSpace;
    }

}
