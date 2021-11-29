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
    final int TIME_BUDGET_MS=1000;
    final int SEARCH_DEPTH_STEP=5;
    SinglePong env=new SinglePong();
    StateForSearch state = new StateForSearch((StateForSearch) env.getTemplateState());
    SinglePong.EnvironmentParameters p=env.parameters;
    Map<String,Integer> moves=new HashMap();
    PongAgentDPSearch agent= new PongAgentDPSearch(env,TIME_BUDGET_MS,SEARCH_DEPTH_STEP);

    public void setupMoves() {

        moves.put("left",0);
        moves.put("still",1);
        moves.put("right",2);
    }

    public void setBallAndRacketInMiddleBallFallingDown() {

        state.setVariable("xPosBall", env.parameters.MAX_X_POSITION/2);
        state.setVariable("yPosBall", env.parameters.MAX_Y_POSITION_BALL/2);
        state.setVariable("xSpdBall", 0d);
        state.setVariable("ySpdBall", -env.parameters.SPEED_BALL);
        state.setVariable("xPosRacket", env.parameters.MAX_X_POSITION/2);
        state.setVariable("xSpdRacket", 0d);
    }

}
