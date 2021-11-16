package java_ai_gym.test_search;

import java_ai_gym.models_common.State;
import java_ai_gym.models_common.StepReturn;
import java_ai_gym.models_mountaincar.MountainCar;
import java_ai_gym.models_pong.SinglePong;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TestSinglePongEnvironment extends TestSearchBase {


    @Before
    public void setup() {
        super.setupMoves();
        env.setRandomStateValuesStart(state);

    }

    @SneakyThrows
    @Test
    @Ignore("Takes time")
    public void testSetup() {
        env.render(state,0.0,0);
        TimeUnit.MILLISECONDS.sleep(1000);
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

    @Test
    public void testGameOver() {

        state.setVariable("xPosBall", 0);
        state.setVariable("yPosBall", 0d);
        state.setVariable("xSpdBall", 0d);
        state.setVariable("ySpdBall", -env.parameters.SPEED_BALL);
        state.setVariable("xPosRacket", env.parameters.MAX_X_POSITION);
        state.setVariable("xSpdRacket", 0d);
        StepReturn stepReturn=env.step(moves.get("still"),state);
        System.out.println(stepReturn);
        Assert.assertTrue(stepReturn.termState);

      }

    @Test
    public void testNotGameOver() {

        state.setVariable("yPosBall", env.parameters.MAX_Y_POSITION_BALL);
        StepReturn stepReturn=env.step(moves.get("still"),state);
        System.out.println(stepReturn);
        Assert.assertFalse(stepReturn.termState);

    }

    @SneakyThrows
    @Test
    //@Ignore("Takes time")
    public void testAnimate() {

        StepReturn  stepReturn;
        for (int i = 0; i <1000 ; i++) {

            if (state.getContinuousVariable("xPosRacket")<state.getContinuousVariable("xPosBall")) {
                stepReturn=env.step(moves.get("right"),state);
            } else if (state.getContinuousVariable("xPosRacket")>state.getContinuousVariable("xPosBall")) {
                stepReturn=env.step(moves.get("left"),state);
            } else
            {
                stepReturn=env.step(moves.get("still"),state);
            }

            if (stepReturn.termState) {
                logger.warning("Fail state");
                System.out.println(stepReturn);
            }

            state.copyState(stepReturn.state);

            env.render(state,0.0,0);
            TimeUnit.MILLISECONDS.sleep(10);
        }

    }

}
