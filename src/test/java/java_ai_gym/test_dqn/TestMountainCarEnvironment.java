package java_ai_gym.test_dqn;

import java_ai_gym.models_common.State;
import java_ai_gym.models_common.StateBasic;
import java_ai_gym.models_common.StepReturn;
import java_ai_gym.models_mountaincar.MountainCar;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TestMountainCarEnvironment {


    MountainCar env=new MountainCar();
    State state = new StateBasic(env.getTemplateState());

    @Test
    public void setRuleBasedAction() throws InterruptedException {
        env.setRandomStateValuesAny(state);
        System.out.println(state);
        StepReturn stepReturn=env.step(0,state);
        int action=1;

        do {

            double position=state.getContinuousVariable("position");
            double velocity=state.getContinuousVariable("velocity");

            action=(velocity <0)?0:2;
            stepReturn=env.step(action,state);
            state.copyState(stepReturn.state);

            env.animationPanel.setCarStates(position,env.height(position),velocity,action,0);
            env.animationPanel.repaint();
            TimeUnit.MILLISECONDS.sleep(100);

            if (env.isGoalState(stepReturn)) {
                System.out.println("Goal state reached");
                System.out.println(state);
                System.out.println(stepReturn);
            }

        } while (!stepReturn.termState);

        TimeUnit.MILLISECONDS.sleep(1000);

    }


}
