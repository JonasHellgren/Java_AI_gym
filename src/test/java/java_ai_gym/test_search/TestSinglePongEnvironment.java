package java_ai_gym.test_search;

import java_ai_gym.models_common.State;
import java_ai_gym.models_common.StepReturn;
import java_ai_gym.models_mountaincar.MountainCar;
import java_ai_gym.models_pong.SinglePong;
import lombok.SneakyThrows;
import org.jcodec.common.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TestSinglePongEnvironment {

    SinglePong env=new SinglePong();
    State state = new State(env.getTemplateState());

    @SneakyThrows
    @Test
    public void testSetup() {
        TimeUnit.MILLISECONDS.sleep(1000);
    }

    @Test
    public void testMoveRight() {

        state.setVariable("yPosBall",0.5);
        System.out.println(state);
        int action=2;
        StepReturn stepReturn=env.step(action,state);
        System.out.println(stepReturn.state);

        Assert.assertTrue(state.getContinuousVariable("xPosRacket")<stepReturn.state.getContinuousVariable("xPosRacket"));
    }

}
