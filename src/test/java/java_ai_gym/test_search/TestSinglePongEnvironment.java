package java_ai_gym.test_search;

import java_ai_gym.models_common.StepReturn;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

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
        state.setVariable("xPosRacket",env.parameters.MAX_X_POSITION/2);  //put racket in middle
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
    public void testManySteps() {
        long startTime = System.currentTimeMillis();  //starting time, long <=> minimum value of 0

        int nofSteps = 0;
        while (System.currentTimeMillis()-startTime <1000 ) {
            StepReturn stepReturn=env.step(moves.get("left"),state);
            nofSteps++;
        }
        System.out.println("nofSteps = "+nofSteps);
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
    public void TestTimerLogic() {

        setBallInMiddleAndRacketInRightBallFallingDown();
        double sumRewardStartMoveLate=0;
        int STEP_WHEN_STARTING_TO_MOVE=5;
        sumRewardStartMoveLate = moveRacketLeftSequence(sumRewardStartMoveLate, STEP_WHEN_STARTING_TO_MOVE);
        System.out.println("sumRewardStartMoveLate = "+sumRewardStartMoveLate);

        setBallInMiddleAndRacketInRightBallFallingDown();
        double sumRewardStartMoveDirectly=0;
        STEP_WHEN_STARTING_TO_MOVE=0;
        sumRewardStartMoveDirectly = moveRacketLeftSequence(sumRewardStartMoveDirectly, STEP_WHEN_STARTING_TO_MOVE);
        System.out.println("sumRewardStartMoveDirectly = "+sumRewardStartMoveDirectly);


        Assert.assertTrue(sumRewardStartMoveDirectly>sumRewardStartMoveLate);
    //    Assert.assertEquals(state.getContinuousVariable("yPosBall")+state.getContinuousVariable("ySpdBall"),stepReturn.state.getContinuousVariable("yPosBall"),0.01d);
    }

    private double moveRacketLeftSequence(double sumReward, int STEP_WHEN_STARTING_TO_MOVE) {
        for (int i = 0; i < STEP_WHEN_STARTING_TO_MOVE; i++) {
            StepReturn stepReturn=env.step(moves.get("still"),state);
            sumReward =printSomeVariablesAndIncreaseSumReward(stepReturn, sumReward);
            state.copyState(stepReturn.state);

        }

        for (int i = 0; i <3 ; i++) {
            StepReturn stepReturn=env.step(moves.get("left"),state);
            sumReward =printSomeVariablesAndIncreaseSumReward(stepReturn, sumReward);
            state.copyState(stepReturn.state);
        }

        while (state.getDiscreteVariable("nofSteps")<10) {
            StepReturn stepReturn=env.step(moves.get("still"),state);
            sumReward =printSomeVariablesAndIncreaseSumReward(stepReturn, sumReward);
            state.copyState(stepReturn.state);
        }
        return sumReward;
    }

    private double printSomeVariablesAndIncreaseSumReward(StepReturn stepReturn, double sumReward) {
        System.out.println("nofSteps = "+state.getDiscreteVariable("nofSteps")+
                " ,xSpdRacket = "+state.getContinuousVariable("xSpdRacket")+
                " ,xPosRacket = "+state.getContinuousVariable("xPosRacket")+
                " ,yPosBall = "+state.getContinuousVariable("yPosBall")+
                " ,collision = "+state.getDiscreteVariable("collision")+
                " ,isTimerOn = "+state.getDiscreteVariable("isTimerOn")+
                " ,nofStepsStillBeforeCollision = "+state.getDiscreteVariable("nofStepsStillBeforeCollision")+
                ", reward = "+stepReturn.reward);
        return sumReward+stepReturn.reward*Math.pow(agent.getDiscountFactorReward(),state.getDiscreteVariable("nofSteps")-1);
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
    @Ignore("Takes time")
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
                logger.warning("Fail stateee");
                System.out.println(stepReturn);
            }

            state.copyState(stepReturn.state);

            env.render(state,0.0,0);
            TimeUnit.MILLISECONDS.sleep(10);
        }

    }

}
