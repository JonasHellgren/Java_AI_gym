package java_ai_gym.models_pong;

import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.*;
import java_ai_gym.swing.FrameEnvironment;
import java_ai_gym.swing.Position2D;
import java_ai_gym.swing.ScaleLinear;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.logging.Logger;

public class SinglePong extends EnvironmentForSearchAgent {

    private static final Logger logger = Logger.getLogger(SinglePong.class.getName());

    public SinglePong.EnvironmentParameters parameters = this.new EnvironmentParameters();

    final int plotFrameWidth=gfxSettings.FRAME_WEIGHT*2;
    final int plotFrameHeight=gfxSettings.FRAME_HEIGHT*4;
    final int margin=gfxSettings.FRAME_MARGIN;
    int panelHeight;
    int panelWeight;
    public PanelPongAnimation animationPanel;
    public SearchTreePanel upperPLotPanel;
    public SearchTreePanel middlePLotPanel;
    public SearchTreePanel lowerPLotPanel;

    public JLabel labelBallPosX;
    public JLabel labelBallPosY;

    public class EnvironmentParameters extends EnvironmentParametersAbstract {

        public final double MIN_X_POSITION = 0;
        public final double MAX_X_POSITION = 1.0;

        public final double MIN_Y_POSITION_BALL = 0;
        public final double MAX_Y_POSITION_BALL = 0.5;
        public final double SPEED_BALL = 0.01;

        public final double Y_POSITION_RACKET = 0.02;  //shall not be at zero, fail state when ball hit then
        public  double MAX_SPEED_RACKET = 0.05;

        public int MAX_NOF_STEPS =200;
        public final int MAX_NOF_STEPS_POLICY_TEST=500;
        public  double TERMINAL_REWARD = -10.0;  //1.0
        public  double NON_TERMINAL_REWARD_MOTION = -.01;  //1.0
        public  double NON_TERMINAL_REWARD_STILL = 0.0;  //1.0

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

        super.templateState=new StateForSearch();
        createVariablesInState(getTemplateState());
        setupFramesAndPanels();
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
    public StepReturn step(int action, State state0)   {
        StateForSearch state=(StateForSearch) state0;
        State newState = updateState(action, state);

        StepReturn stepReturn = new StepReturn(new StateForSearch());
        stepReturn.state = newState;
        stepReturn.termState = isTerminalState(newState);
        stepReturn.reward = (stepReturn.termState)?
                parameters.TERMINAL_REWARD:
                calcRewardNotTerminal(state,stepReturn.state);

        state.totalNofSteps++;
        return stepReturn;
    }

    public double calcRewardNotTerminal(State state,State stepReturnState) {
        return (MathUtils.isZero((state.getContinuousVariable("xPosRacket") - stepReturnState.getContinuousVariable("xPosRacket"))))?
                parameters.NON_TERMINAL_REWARD_STILL:
                parameters.NON_TERMINAL_REWARD_MOTION;
    }

    @NotNull
    private State updateState(int action, StateForSearch state) {
        State newState = new StateForSearch(state);
        double xPosBall= state.getContinuousVariable("xPosBall");
        double yPosBall= state.getContinuousVariable("yPosBall");
        double xSpdBall= state.getContinuousVariable("xSpdBall");
        double ySpdBall= state.getContinuousVariable("ySpdBall");
        double xPosRacket= state.getContinuousVariable("xPosRacket");

        RacketPhysics racket =new RacketPhysics(xPosRacket,this);
        racket.updateStates(action);
        BallPhysics ball =new BallPhysics(xPosBall,yPosBall,xSpdBall,ySpdBall,this);
        ball.updateStates(racket);

        newState.setVariable("xPosBall", ball.xPos);
        newState.setVariable("yPosBall", ball.yPos);
        newState.setVariable("xSpdBall", ball.xSpd);
        newState.setVariable("ySpdBall", ball.ySpd);
        newState.setVariable("xPosRacket", racket.xPos);
        newState.setVariable("xSpdRacket", racket.xSpd);
        newState.setVariable("nofSteps", state.getDiscreteVariable("nofSteps")+1);
        return newState;
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
    public void setRandomStateValuesStart(State state) {
        state.setVariable("xPosBall", MathUtils.calcRandomFromIntervall(parameters.MIN_X_POSITION,parameters.MAX_X_POSITION));
        state.setVariable("yPosBall", MathUtils.calcRandomFromIntervall(parameters.MIN_Y_POSITION_BALL,parameters.MAX_Y_POSITION_BALL));
        state.setVariable("xSpdBall", parameters.SPEED_BALL);
        state.setVariable("ySpdBall", parameters.SPEED_BALL);
        state.setVariable("xPosRacket", MathUtils.calcRandomFromIntervall(parameters.MIN_X_POSITION,parameters.MAX_X_POSITION));
        state.setVariable("xSpdRacket", 0.0);
        state.setVariable("nofSteps", state.getDiscreteVariable("nofSteps")+1);

    }

    private void setupFramesAndPanels() {
        final int NOF_PLOT_PANELS=3;
        final int margin=gfxSettings.FRAME_MARGIN*1;
        animationFrame =new FrameEnvironment(gfxSettings.FRAME_WEIGHT, gfxSettings.FRAME_HEIGHT,"SinglePong animation");
        ScaleLinear xScaler=new ScaleLinear(parameters.MIN_X_POSITION,parameters.MAX_X_POSITION,
                gfxSettings.FRAME_MARGIN, gfxSettings.FRAME_WEIGHT - gfxSettings.FRAME_MARGIN,
                false,gfxSettings.FRAME_MARGIN);
        ScaleLinear yScaler=new ScaleLinear(parameters.MIN_Y_POSITION_BALL,parameters.MAX_Y_POSITION_BALL, gfxSettings.FRAME_MARGIN,
                gfxSettings.FRAME_HEIGHT - gfxSettings.FRAME_MARGIN,true, gfxSettings.FRAME_MARGIN);

        animationPanel =new PanelPongAnimation(xScaler, yScaler);
        animationPanel.setLayout(null);  //to enable tailor made position
        //addLabelsToAnimationPanel();
        animationFrame.add(animationPanel);
        animationFrame.setVisible(true);

        panelHeight=(int) ( plotFrameHeight-2*margin)/NOF_PLOT_PANELS;
        panelWeight=(int) ( plotFrameWidth-2*margin);

        System.out.println("panelHeight ="+panelHeight+", gfxSettings.FRAME_WEIGHT ="+gfxSettings.FRAME_HEIGHT);
        plotFrame =new FrameEnvironment(plotFrameWidth, plotFrameHeight,"SinglePoooooooooong plots");

        upperPLotPanel =new SearchTreePanel();
        defineTreePanel(upperPLotPanel, margin,"upperPLotPanel");

        middlePLotPanel =new SearchTreePanel();
        defineTreePanel(middlePLotPanel, panelHeight+margin,"middlePLotPanel");


        lowerPLotPanel =new SearchTreePanel();
        lowerPLotPanel.setBackground(Color.lightGray);
        lowerPLotPanel.setBounds(margin,panelHeight*2+margin, panelWeight-margin,panelHeight);
        lowerPLotPanel.createLabel(panelWeight,panelHeight,"lowerPLotPanel");

        plotFrame.add(upperPLotPanel);
        plotFrame.add(middlePLotPanel);
        plotFrame.add(lowerPLotPanel);
        plotFrame.setLayout(null);   //makes the label to not occupy all frame

        plotFrame.setVisible(true);

    }

    private void defineTreePanel(SearchTreePanel panel, int yPos, String title) {
        panel.setBackground(Color.lightGray);
        panel.setBounds(margin, yPos, panelWeight - margin, panelHeight);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.createLabel(panelWeight, panelHeight,title);
        panel.createTreeWithOnlyRootNode(panelWeight, panelHeight,title);
    }


}
