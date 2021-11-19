package java_ai_gym.test_dqn;

import java_ai_gym.models_common.State;
import java_ai_gym.models_common.StateBasic;
import java_ai_gym.models_common.StepReturn;
import java_ai_gym.models_poleoncart.CartPole;
import org.jcodec.common.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TestCartPoleEnvrionment {


    CartPole env=new CartPole();
    State state = new StateBasic(env.getTemplateState());
    final Random random = new Random();

    @Test
    public void setLeftAction() throws InterruptedException {

        //state.copyState();
        env.setRandomStateValuesStart(state);
        state.setVariable("x",-1.0);
        System.out.println(state);
        StepReturn stepReturn; //=env.step(0,state);
        int action=1;
        double xStart;
        double thetaStart;

        do {

            double x=state.getContinuousVariable("x");  xStart=x;
            double theta=state.getContinuousVariable("theta");  thetaStart=theta;

            action=0; //
            stepReturn=env.step(action,state);
            state.copyState(stepReturn.state);
            env.animationPanel.setCartPoleStates(state,action,0);
            env.animationPanel.repaint();
            TimeUnit.MILLISECONDS.sleep(1000);
        } while (!stepReturn.termState);

        Assert.assertTrue(state.getContinuousVariable("x")<xStart);
        Assert.assertTrue(state.getContinuousVariable("theta")>thetaStart);
        Assert.assertTrue(state.getContinuousVariable("xDot")<0);
        Assert.assertTrue(state.getContinuousVariable("thetaDot")>0);

    }

    @Test
    public void setRandomAction() throws InterruptedException {

        //state.copyState();
        env.setRandomStateValuesStart(state);
        state.setVariable("x",-1.0);
        System.out.println(state);
        StepReturn stepReturn; //=env.step(0,state);
        int action=1;
        do {
            double x=state.getContinuousVariable("x");
            double theta=state.getContinuousVariable("theta");

            action=random.nextInt(env.parameters.discreteActionsSpace.size());
            stepReturn=env.step(action,state);
            state.copyState(stepReturn.state);
            env.animationPanel.setCartPoleStates(state,action,0);
            env.animationPanel.repaint();
            TimeUnit.MILLISECONDS.sleep(100);
        } while (!stepReturn.termState);

        Assert.assertTrue(state.getDiscreteVariable("nofSteps")<env.parameters.MAX_NOF_STEPS);

    }

}
