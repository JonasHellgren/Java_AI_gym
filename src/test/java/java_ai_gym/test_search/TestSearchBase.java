package java_ai_gym.test_search;

import java_ai_gym.models_common.State;
import java_ai_gym.models_pong.SinglePong;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class TestSearchBase {

    static final Logger logger = Logger.getLogger(TestSearchBase.class.getName());

    SinglePong env=new SinglePong();
    State state = new State(env.getTemplateState());
    SinglePong.EnvironmentParameters p=env.parameters;
    Map<String,Integer> moves=new HashMap();

    public void setupMoves() {

        moves.put("left",0);
        moves.put("still",1);
        moves.put("right",2);

    }

}
