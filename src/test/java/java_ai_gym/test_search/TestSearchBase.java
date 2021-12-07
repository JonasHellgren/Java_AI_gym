package java_ai_gym.test_search;

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
    final int SEARCH_DEPTH_UPPER =10;
    final double explorationFactorLimit=0.9;
    final double discountFactorReward=0.95;
    final double discountFactorExpFactor=.98;
    SinglePong env=new SinglePong();
    StateForSearch state = new StateForSearch((StateForSearch) env.getTemplateState());
    SinglePong.EnvironmentParameters p=env.parameters;
    Map<String,Integer> moves=new HashMap();
    PongAgentDPSearch agent= new PongAgentDPSearch(env,TIME_BUDGET_MS, SEARCH_DEPTH_UPPER,SEARCH_DEPTH_STEP,explorationFactorLimit,discountFactorReward,discountFactorExpFactor);

    public void setupMoves() {

        moves.put("left",0);
        moves.put("still",1);
        moves.put("right",2);
    }

    public void setBallAndRacketInMiddleBallFallingDown() {
        state.setVariable("nofSteps", 0);
        state.setVariable("xPosBall", env.parameters.MAX_X_POSITION/2);
        state.setVariable("yPosBall", env.parameters.MAX_Y_POSITION_BALL/2);
        state.setVariable("xSpdBall", 0d);
        state.setVariable("ySpdBall", -env.parameters.SPEED_BALL);
        state.setVariable("xPosRacket", env.parameters.MAX_X_POSITION/2);
        state.setVariable("xSpdRacket", 0d);
    }

    public void setBallInMiddleAndRacketInRightBallFallingDown() {
        setBallAndRacketInMiddleBallFallingDown();
        state.setVariable("yPosBall", env.parameters.MAX_Y_POSITION_BALL/6);
        state.setVariable("xPosRacket", env.parameters.MAX_X_POSITION*8/10);

    }

    public void setBallLeftAndRacketRightHasNoSolution() {
        setBallAndRacketInMiddleBallFallingDown();
        state.setVariable("yPosBall", env.parameters.SPEED_BALL*4);
        state.setVariable("xPosBall", 0.0);
        state.setVariable("xPosRacket", env.parameters.MAX_X_POSITION*1);
    }


}
