package java_ai_gym.models_pong;

import java.util.List;

public class PongAgentDPSearch extends AgentDPSearch {

    final int ACTION_DEFAULT = 1;

    public PongAgentDPSearch(SinglePong env, long timeBudget, int searchDepthStep) {
        super(env, timeBudget, searchDepthStep);
    }

    public PongAgentDPSearch(SinglePong env, long timeBudget, int searchDepthStep, double explorationFactorLimit, double discountFactor) {
        super(env, timeBudget, searchDepthStep, explorationFactorLimit, discountFactor);
    }

    public int getActionDefault() {
        return ACTION_DEFAULT;
    }

    public List<Integer> getActionSet () {
        return envParams.discreteActionsSpace;
    }

}
