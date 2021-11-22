package java_ai_gym.test_search;

import java_ai_gym.models_common.AgentSearch;
import java_ai_gym.models_common.State;
import java_ai_gym.models_common.StateForSearch;
import java_ai_gym.models_pong.PongAgentDPSearch;
import java_ai_gym.models_pong.SinglePong;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class TestSearchBase {

    static final Logger logger = Logger.getLogger(TestSearchBase.class.getName());

    final int NOF_ACTIONS=3;
    SinglePong env=new SinglePong();
    StateForSearch state = new StateForSearch((StateForSearch) env.getTemplateState());
    SinglePong.EnvironmentParameters p=env.parameters;
    Map<String,Integer> moves=new HashMap();
    PongAgentDPSearch agent= new PongAgentDPSearch(env,100,5);

    public void setupMoves() {

        moves.put("left",0);
        moves.put("still",1);
        moves.put("right",2);


    }
}
