package java_ai_gym.models_pong;

import java_ai_gym.models_common.AgentSearch;
import java_ai_gym.models_common.Environment;
import java_ai_gym.models_common.EnvironmentParametersAbstract;
import java_ai_gym.models_common.State;

public class PongAgentDPSearch extends AgentSearch  {

    int searchDepthStep;

    VisitedStatesBuffer vsb=new VisitedStatesBuffer();
    State state;


    public PongAgentDPSearch(SinglePong env,long timeBudget, int searchDepthStep) {
        super(timeBudget, env, env.parameters);
        this.searchDepthStep=searchDepthStep;
    }

    @Override
    public SearchResults search(State startState) {
        return null;
    }
}
