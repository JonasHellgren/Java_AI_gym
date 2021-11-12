package java_ai_gym.models_pong;

import java_ai_gym.models_common.EnvironmentParametersAbstract;
import java_ai_gym.models_common.EnvironmentSearchAgent;
import java_ai_gym.models_common.State;
import java_ai_gym.models_common.StepReturn;
import java_ai_gym.swing.FrameEnvironment;
import java_ai_gym.swing.Position2D;
import java_ai_gym.swing.ScaleLinear;

import javax.swing.*;
import java.util.Arrays;
import java.util.logging.Logger;

public class SinglePong extends EnvironmentSearchAgent {

    private static final Logger logger = Logger.getLogger(SinglePong.class.getName());

    public SinglePong.EnvironmentParameters parameters = this.new EnvironmentParameters();

    public PanelPongAnimation animationPanel;
    public JLabel labelBallPosX;
    public JLabel labelBallPosY;

    public class EnvironmentParameters extends EnvironmentParametersAbstract {

        public final double MIN_X_POSITION = 0;
        public final double MAX_X_POSITION = 2.0;

        public final double MIN_Y_POSITION_BALL = 0;
        public final double MAX_Y_POSITION_BALL = 1.0;
        public final double SPEED_BALL = 0.01;

        public final double Y_POSITION_RACKET = 0;
        public final double MAX_SPEED_RACKET = 0.01;


        public int MAX_NOF_STEPS =200;
        public final int MAX_NOF_STEPS_POLICY_TEST=500;
        public  double TERMINAL_REWARD = -1.0;  //1.0
        public  double NON_TERMINAL_REWARD = 0.0;  //1.0

        public int NOF_ACTIONS;
        public int MIN_ACTION;

        public EnvironmentParameters() {
        }

        @Override
        protected int getIdxState(State state) {
            return 0;
        }

        @Override
        protected int getIdxAction(int action) {
            return 0;
        }
    }

    public SinglePong() {

        parameters.continuousStateVariableNames.add("xPosBall");
        parameters.continuousStateVariableNames.add("yPosBall");
        parameters.continuousStateVariableNames.add("xSpdBall");
        parameters.continuousStateVariableNames.add("ySpdBall");
        parameters.continuousStateVariableNames.add("xPosRacket");
        parameters.continuousStateVariableNames.add("xSpdRacket");
        parameters.discreteStateVariableNames.add("nofSteps");
        parameters.discreteActionsSpace.addAll(Arrays.asList(0,1,2));
        parameters.MIN_ACTION = parameters.discreteActionsSpace.stream().min(Integer::compare).orElse(0);
        parameters.NOF_ACTIONS = parameters.discreteActionsSpace.size();

        createVariablesInState(getTemplateState());
        setupFrameAndPanel();
        animationPanel.repaint();
    }

    @Override
    public void render(State state, double maxQ, int action) {
        Position2D ballPosition=new Position2D(state.getContinuousVariable("xPosBall"),state.getContinuousVariable("yPosBall"));
        Position2D racketXPos=new Position2D(state.getContinuousVariable("xPosRacket"),parameters.Y_POSITION_RACKET);
        animationPanel.setStates(ballPosition,racketXPos);
        animationPanel.repaint();
    }

    @Override
    public void createVariablesInState(State state) {
        state.createContinuousVariable("xPosBall", 0.0);
        state.createContinuousVariable("yPosBall", 0.0);
        state.createContinuousVariable("xSpdBall", 0.0);
        state.createContinuousVariable("ySpdBall", 0.0);
        state.createContinuousVariable("xPosRacket", 0d);
        state.createContinuousVariable("xSpdRacket", 0d);
        state.createDiscreteVariable("nofSteps", 0);
    }

    @Override
    public StepReturn step(int action, State state) {

        State newState = new State(state);
        StepReturn stepReturn = new StepReturn();

        double xPosBall= state.getContinuousVariable("xPosBall");
        double yPosBall= state.getContinuousVariable("yPosBall");
        double xSpdBall= state.getContinuousVariable("xSpdBall");
        double ySpdBall= state.getContinuousVariable("ySpdBall");
        double xPosRacket=state.getContinuousVariable("xPosRacket");

        Racket racket=new Racket(xPosRacket,this);
        racket.updateStates(action);
        Ball ball=new Ball(xPosBall,yPosBall,xSpdBall,ySpdBall,this);
        ball.updateStates(racket);

        newState.setVariable("xPosBall", ball.xPos);
        newState.setVariable("yPosBall", ball.yPos);
        newState.setVariable("xSpdBall", ball.xSpd);
        newState.setVariable("ySpdBall", ball.ySpd);
        newState.setVariable("xPosRacket", racket.xPos);
        newState.setVariable("xSpdRacket", racket.xSpd);
        newState.setVariable("nofSteps", state.getDiscreteVariable("nofSteps")+1);

        stepReturn.state = newState;
        stepReturn.termState = isTerminalState(newState);
        stepReturn.reward = (stepReturn.termState)?
                parameters.TERMINAL_REWARD:
                parameters.NON_TERMINAL_REWARD;

        state.totalNofSteps++;
        return stepReturn;
    }

    @Override
    protected boolean isTerminalState(State state) {
        return  isFailsState(state);
    }

    @Override
    protected boolean isFailsState(State state) {

        double yPosBall= state.getContinuousVariable("yPosBall");
        return (yPosBall  < parameters.MIN_Y_POSITION_BALL);
    }

    @Override
    protected boolean isTerminalStatePolicyTest(State state) {
        return false;
    }

    @Override
    protected boolean isPolicyTestSuccessful(State state) {
        return false;
    }

    @Override
    protected void setRandomStateValuesStart(State state) {
    }

    private void setupFrameAndPanel() {
        animationFrame =new FrameEnvironment(gfxSettings.FRAME_WEIGHT, gfxSettings.FRAME_HEIGHT,"SinglePong animation");


        plotFrame =new FrameEnvironment(gfxSettings.FRAME_WEIGHT, gfxSettings.FRAME_HEIGHT,"MountainCar plots");
        ScaleLinear xScaler=new ScaleLinear(parameters.MIN_X_POSITION,parameters.MAX_X_POSITION,
                gfxSettings.FRAME_MARGIN, gfxSettings.FRAME_WEIGHT - gfxSettings.FRAME_MARGIN,
                false,gfxSettings.FRAME_MARGIN);
        ScaleLinear yScaler=new ScaleLinear(parameters.MIN_Y_POSITION_BALL,parameters.MAX_Y_POSITION_BALL, gfxSettings.FRAME_MARGIN,
                gfxSettings.FRAME_HEIGHT - gfxSettings.FRAME_MARGIN,true, gfxSettings.FRAME_MARGIN);

        Position2D ballPositionInit=new Position2D(parameters.MIN_X_POSITION/2,parameters.MIN_Y_POSITION_BALL/2);
        Position2D racketXPosInit=new Position2D(parameters.MIN_X_POSITION/2,parameters.Y_POSITION_RACKET);
        animationPanel =new PanelPongAnimation(xScaler, yScaler, ballPositionInit,  racketXPosInit);
        animationPanel.setLayout(null);  //to enable tailor made position
        //addLabelsToAnimationPanel();
        animationFrame.add(animationPanel);
        animationFrame.setVisible(true);

/*
        ScaleLinear yScalerVelocity=new ScaleLinear(-parameters.MAX_SPEED,parameters.MAX_SPEED,
                gfxSettings.FRAME_MARGIN,gfxSettings.FRAME_HEIGHT - gfxSettings.FRAME_MARGIN,true, gfxSettings.FRAME_MARGIN);

        plotPanel =new PanelMountainCarPlot(xScaler,yScalerVelocity, circlePositionList, actionList, CIRCLE_RADIUS_IN_DOTS);
        plotPanel.setLayout(null);
        addLabelsToPlotPanel();
        plotFrame.add(plotPanel);
        */
        plotFrame.setVisible(true);
    }


}
