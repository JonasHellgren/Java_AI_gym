package java_ai_gym.models_common;

import java_ai_gym.models_mountaincar.MountainCar;
import java_ai_gym.models_mountaincar.MountainCarAgentNeuralNetwork;
import java_ai_gym.swing.FrameEnvironment;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class  Environment {

    public class PolicyTestSettings {
        public int NOF_EPISODES_BETWEEN_POLICY_TEST = 10;
        public int NOF_TESTS_WHEN_TESTING_POLICY = 10;
    }

    public class GraphicsSettings  {
        public final  int FRAME_WEIGHT =600;
        public final  int FRAME_HEIGHT =300;
        public final int FRAME_MARGIN =50;  //frame margin
        public final int LABEL_WEIGHT =FRAME_WEIGHT/2;
        public final int LABEL_HEIGHT =15;
        public final int LABEL_XPOS =10;
        public final int LABEL_XPOSY_MIN =0;
        public int NOF_DOTS_PLOTTED_POLICY =1000;
        final long TIME_MILLIS_FRAME=100;
    }

    public class PolicyTestReturn {
        public double successRatio;
        public double minNofSteps;
        public double avgNofSteps;
        public double maxNofSteps;
        public double maxQaverage;
        public double bellmanErrAverage;
    }

    public class RunPolicyReturn {
        public double avgMaxQ;
        public double avgBellmannErr;
    }

    private State templateState=new State();
    protected FrameEnvironment animationFrame;
    protected FrameEnvironment plotFrame;
    public GraphicsSettings gfxSettings =new GraphicsSettings();
    public PolicyTestSettings policyTestSettings= new PolicyTestSettings();

    public abstract void render(State state,double maxQ, int action);
    public abstract void createVariablesInState(State state) ;

    public abstract StepReturn step(int action, State state);  //TODO protected
    protected abstract boolean isTerminalState(State state);
    protected abstract boolean isFailsState(State state);
    protected abstract boolean isTerminalStatePolicyTest(State state);
    protected abstract boolean isPolicyTestSuccessful(State state);
    protected abstract void setRandomStateValuesStart(State state);

    public State getTemplateState() {
        return templateState;
    }

    public boolean isTimeForPolicyTest(int iEpisode) {
       return (iEpisode % policyTestSettings.NOF_EPISODES_BETWEEN_POLICY_TEST == 0 | iEpisode == 0);
    }


}
