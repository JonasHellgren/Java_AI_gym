package java_ai_gym.models_pong;

import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.*;
import java_ai_gym.swing.FrameEnvironment;
import java_ai_gym.swing.Position2D;
import java_ai_gym.swing.ScaleLinear;
import org.jetbrains.annotations.NotNull;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class SinglePong extends EnvironmentForSearchAgent {

    private static final Logger logger = Logger.getLogger(SinglePong.class.getName());

    public SinglePong.EnvironmentParameters parameters = this.new EnvironmentParameters();

    final int plotFrameWidth=gfxSettings.FRAME_WEIGHT*2;
    final int plotFrameHeight=gfxSettings.FRAME_HEIGHT*3;
    public PanelPongAnimation animationPanel;

    public SearchTreePanel leftTreePanel;
    public SearchTreePanel rightTreePanel;
    public HistogramPanel leftChartPanel;
    public HistogramPanel rightChartPanel;
    HistogramDataSetGenerator histogramDataSetGenerator;
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
        public double REWARD_PER_STEP_STILL_BEFORE_BALL_HITS=1;

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
        parameters.discreteStateVariableNames.add("rapidRacketChange");
        parameters.discreteStateVariableNames.add("racketHasStopped");
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
        state.createDiscreteVariable("rapidRacketChange", 0);
        state.createDiscreteVariable("racketHasStopped", 0);
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
        double r1=(MathUtils.isZero((state.getContinuousVariable("xPosRacket") - stepReturnState.getContinuousVariable("xPosRacket"))))?
                parameters.NON_TERMINAL_REWARD_STILL:
                parameters.NON_TERMINAL_REWARD_MOTION;
        double r2=state.getDiscreteVariable("racketHasStopped")*parameters.REWARD_PER_STEP_STILL_BEFORE_BALL_HITS;
        return r1+r2;
    }

    @NotNull
    private State updateState(int action, StateForSearch state) {
        State newState = new StateForSearch(state);
        double xPosBall= state.getContinuousVariable("xPosBall");
        double yPosBall= state.getContinuousVariable("yPosBall");
        double xSpdBall= state.getContinuousVariable("xSpdBall");
        double ySpdBall= state.getContinuousVariable("ySpdBall");
        double xPosRacket= state.getContinuousVariable("xPosRacket");
        double xSpdRacket= state.getContinuousVariable("xSpdRacket");

        RacketPhysics racket =new RacketPhysics(xPosRacket,this);
        racket.updateStates(action);
        BallPhysics ball =new BallPhysics(xPosBall,yPosBall,xSpdBall,ySpdBall,this);
        ball.updateStates(racket);

        int rapidRacketChange=0;
        if (!MathUtils.isZero(racket.xSpd)) {
            rapidRacketChange=MathUtils.isNeg(racket.xSpd) && MathUtils.isPos(xSpdRacket)  ||
                    MathUtils.isPos(racket.xSpd) && MathUtils.isNeg(xSpdRacket)
                    ?1
                    :0;
        }


        newState.setVariable("xPosBall", ball.xPos);
        newState.setVariable("yPosBall", ball.yPos);
        newState.setVariable("xSpdBall", ball.xSpd);
        newState.setVariable("ySpdBall", ball.ySpd);
        newState.setVariable("xPosRacket", racket.xPos);
        newState.setVariable("xSpdRacket", racket.xSpd);
        newState.setVariable("rapidRacketChange", rapidRacketChange);
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
        boolean rapidRacketChange= (state.getDiscreteVariable("rapidRacketChange")==1);

        if (rapidRacketChange) {
            logger.warning("rapidRacketChange true");
        }

        return (yPosBall  < parameters.MIN_Y_POSITION_BALL || rapidRacketChange);
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
        state.setVariable("rapidRacketChange", 0.0);
        state.setVariable("racketHasStopped", 0.0);
        state.setVariable("nofSteps", state.getDiscreteVariable("nofSteps")+1);

    }

    private void setupFramesAndPanels() {
        animationFrame =new FrameEnvironment(gfxSettings.FRAME_WEIGHT, gfxSettings.FRAME_HEIGHT,"SinglePong animation");
        ScaleLinear xScaler=new ScaleLinear(parameters.MIN_X_POSITION,parameters.MAX_X_POSITION,
                gfxSettings.FRAME_MARGIN, gfxSettings.FRAME_WEIGHT - gfxSettings.FRAME_MARGIN,
                false,gfxSettings.FRAME_MARGIN);
        ScaleLinear yScaler=new ScaleLinear(parameters.MIN_Y_POSITION_BALL,parameters.MAX_Y_POSITION_BALL, gfxSettings.FRAME_MARGIN,
                gfxSettings.FRAME_HEIGHT - gfxSettings.FRAME_MARGIN,true, gfxSettings.FRAME_MARGIN);

        animationPanel =new PanelPongAnimation(xScaler, yScaler);
        animationPanel.setLayout(null);  //to enable tailor made position
        animationFrame.add(animationPanel);
        animationFrame.setVisible(true);

        plotFrame =new FrameEnvironment(plotFrameWidth, plotFrameHeight,"SinglePong plots");

        leftTreePanel =new SearchTreePanel();
        defineTreePanel(leftTreePanel, "upperPLotPanel");
        rightTreePanel =new SearchTreePanel();
        defineTreePanel(rightTreePanel, "middlePLotPanel");

        histogramDataSetGenerator =new HistogramDataSetGenerator();
        leftChartPanel = new HistogramPanel();
        DefaultCategoryDataset dataset1 = leftChartPanel.getDataset();
        histogramDataSetGenerator.defineDummyDataset(dataset1);
        rightChartPanel = new HistogramPanel();
        DefaultCategoryDataset dataset2 = rightChartPanel.getDataset();
        histogramDataSetGenerator.defineDummyDataset(dataset2);

        plotFrame.add(leftTreePanel);
        plotFrame.add(rightTreePanel);
        plotFrame.add(leftChartPanel);
        plotFrame.add(rightChartPanel);
        plotFrame.setLayout(new GridLayout(2,2));
        plotFrame.setVisible(true);
    }

    private void defineTreePanel(SearchTreePanel panel,String title) {
        panel.setBackground(Color.lightGray);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.createLabel(title);
        panel.createTreeWithOnlyRootNode(plotFrameWidth, plotFrameHeight,title);
    }


    public  void createHistogramsFromVisitedStatesBuffer(VisitedStatesBuffer vsb, List<Integer> evaluatedSearchDepths) {
        histogramDataSetGenerator.updateDatasetForDepthStatistics(leftChartPanel.getDataset(),vsb,evaluatedSearchDepths);
        histogramDataSetGenerator.updateDatasetForStatesPerDepth(rightChartPanel.getDataset(),vsb);
    }

}
