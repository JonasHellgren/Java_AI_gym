package java_ai_gym.test_search;

import java_ai_gym.models_common.State;
import java_ai_gym.models_common.StepReturn;
import java_ai_gym.models_mountaincar.MountainCar;
import java_ai_gym.models_pong.SinglePong;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TestSinglePongEnvironment {

    SinglePong env=new SinglePong();
    State state = new State(env.getTemplateState());

    Map<String,Integer> moves=new HashMap();

    @Before
    public void setup() {

        moves.put("left",0);
        moves.put("still",1);
        moves.put("right",2);

        env.setRandomStateValuesStart(state);

    }

    @SneakyThrows
    @Test
    public void testSetup() {
        env.render(state,0.0,0);
        TimeUnit.MILLISECONDS.sleep(5000);
    }

    @Test
    public void testRacketMoveS() {
        StepReturn stepReturn=env.step(moves.get("left"),state);
        System.out.println(stepReturn.state);
        Assert.assertTrue(state.getContinuousVariable("xPosRacket")>stepReturn.state.getContinuousVariable("xPosRacket"));

        stepReturn=env.step(moves.get("still"),state);
        System.out.println(stepReturn.state);
        Assert.assertEquals(state.getContinuousVariable("xPosRacket"),stepReturn.state.getContinuousVariable("xPosRacket"),0.01d);

        stepReturn=env.step(moves.get("right"),state);
        System.out.println(stepReturn.state);
        Assert.assertTrue(state.getContinuousVariable("xPosRacket")<stepReturn.state.getContinuousVariable("xPosRacket"));
    }

    @Test
    public void testBallMove() {
        StepReturn stepReturn=env.step(moves.get("left"),state);

        System.out.println(state);
        System.out.println(stepReturn.state);

        Assert.assertEquals(state.getContinuousVariable("xPosBall")+state.getContinuousVariable("xSpdBall"),stepReturn.state.getContinuousVariable("xPosBall"),0.01d);
        Assert.assertEquals(state.getContinuousVariable("yPosBall")+state.getContinuousVariable("ySpdBall"),stepReturn.state.getContinuousVariable("yPosBall"),0.01d);
    }




}
