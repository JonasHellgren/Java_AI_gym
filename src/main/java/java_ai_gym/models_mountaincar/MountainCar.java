package java_ai_gym.models_mountaincar;

import java_ai_gym.models_common.*;
import java_ai_gym.swing.*;

import javax.swing.*;
import java.util.*;
import java.util.List;

/***
 *         Num    Observation               Min            Max
 *         0      Car Position              -1.2           0.6
 *         1      Car Velocity              -0.07          0.07
 *
 *         min { sin(3*x) * 0.45 + 0.55 },   x=-1.2..0.6 => x=-0.5235
 *     Actions:
 *         Type: Discrete(3)
 *         Num    Action
 *         0      Accelerate to the Left
 *         1      Don't accelerate
 *         2      Accelerate to the Right
 *         Note: This does not affect the amount of velocity affected by the
 *         gravitational pull acting on the car.
 *     Reward:
 *          Reward of 0 is awarded if the agent reached the flag (position = 0.5)
 *          on top of the mountain.
 *          Reward of -1 is awarded if the position of the agent is less than 0.5.
 *     Starting State:
 *          The position of the car is assigned a uniform random value in
 *          [-0.6 , -0.4].
 *          The starting velocity of the car is always assigned to 0.
 *     Episode Termination:
 *          The car position is more than 0.5
 *          Episode length is greater than 200
 */

public class MountainCar extends EnvironmentForNetworkAgent {

    public EnvironmentParameters parameters = this.new EnvironmentParameters();

    final  double CAR_RADIUS=0.05;
    final  int CIRCLE_RADIUS_IN_DOTS =10;

    public PanelMountainCarAnimation animationPanel;
    public JLabel labelPosX;
    public JLabel labelPosY;
    public JLabel labelVelocity;
    public JLabel  labelMaxQ;

    public PanelMountainCarPlot plotPanel;
    public JLabel labelXaxis;
    public JLabel labelYaxis;

    // inner classes
    public class EnvironmentParameters extends EnvironmentParametersAbstract {

        public final double MIN_POSITION = -1.2;
        public final double MAX_POSITION = 0.6;
        public final double MAX_SPEED = 0.07;

        public final double POSITION_AT_MIN_HEIGHT = -0.5235;
        public  double MIN_START_POSITION = POSITION_AT_MIN_HEIGHT-0.3;  // -0.8;
        public  double MAX_START_POSITION = POSITION_AT_MIN_HEIGHT+0.3;  //0.5;
        public double MIN_START_VELOCITY = -0.06;
        public double MAX_START_VELOCITY = 0.06;

        public final double GOAL_POSITION = 0.5;
        public final double GOAL_VELOCITY = 0;
        public int MAX_NOF_STEPS =200;
        public final int MAX_NOF_STEPS_POLICY_TEST=500;
        public  double NON_TERMINAL_REWARD = -1.0;  //-1.0

        public final double FORCE = 0.001;
        public final double GRAVITY = 0.0025;

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

    public MountainCar() {
        parameters.continuousStateVariableNames.add("position");
        parameters.continuousStateVariableNames.add("velocity");
        parameters.discreteStateVariableNames.add("nofSteps");
        parameters.discreteActionsSpace.addAll(Arrays.asList(0, 1, 2));
        parameters.MIN_ACTION = parameters.discreteActionsSpace.stream().min(Integer::compare).orElse(0);
        parameters.NOF_ACTIONS = parameters.discreteActionsSpace.size();

        createVariablesInState(getTemplateState());
        LineData roadData=createRoadData();
        setupFrameAndPanel(roadData);
        animationPanel.repaint();
    }

    private void setupFrameAndPanel(LineData roadData) {
        animationFrame =new FrameEnvironment(gfxSettings.FRAME_WEIGHT, gfxSettings.FRAME_HEIGHT,"MountainCar animation");
        plotFrame =new FrameEnvironment(gfxSettings.FRAME_WEIGHT, gfxSettings.FRAME_HEIGHT,"MountainCar plots");
        ScaleLinear xScaler=new ScaleLinear(parameters.MIN_POSITION,parameters.MAX_POSITION,
                gfxSettings.FRAME_MARGIN, gfxSettings.FRAME_WEIGHT - gfxSettings.FRAME_MARGIN,
                false,gfxSettings.FRAME_MARGIN);
        ScaleLinear yScaler=new ScaleLinear(0,1, gfxSettings.FRAME_MARGIN,
                gfxSettings.FRAME_HEIGHT - gfxSettings.FRAME_MARGIN,true, gfxSettings.FRAME_MARGIN);

        Position2D carPositionInit=new Position2D(parameters.POSITION_AT_MIN_HEIGHT,height(parameters.POSITION_AT_MIN_HEIGHT));
        animationPanel =new PanelMountainCarAnimation(xScaler,yScaler, roadData, carPositionInit,CAR_RADIUS);
        animationPanel.setLayout(null);  //to enable tailor made position
        addLabelsToAnimationPanel();
        animationFrame.add(animationPanel);
        animationFrame.setVisible(true);

        List<Position2D> circlePositionList=new ArrayList<>();
        List<Integer> actionList=new ArrayList<>();
        circlePositionList.add(new Position2D(0.5,0));
        actionList.add(1);

        ScaleLinear yScalerVelocity=new ScaleLinear(-parameters.MAX_SPEED,parameters.MAX_SPEED,
                gfxSettings.FRAME_MARGIN,gfxSettings.FRAME_HEIGHT - gfxSettings.FRAME_MARGIN,true, gfxSettings.FRAME_MARGIN);
        plotPanel =new PanelMountainCarPlot(xScaler,yScalerVelocity, circlePositionList, actionList, CIRCLE_RADIUS_IN_DOTS);
        plotPanel.setLayout(null);
        addLabelsToPlotPanel();
        plotFrame.add(plotPanel);
        plotFrame.setVisible(true);
    }

    private void addLabelsToAnimationPanel() {
        int labelIndex=0;

        labelPosX = new JLabel("pos x");
        animationPanel.labelPosX=labelPosX;
        addLabelToPanel(labelPosX, labelIndex);
        labelIndex++;

        labelPosY = new JLabel("pos y");
        animationPanel.labelPosY=labelPosY;
        addLabelToPanel(labelPosY, labelIndex);
        labelIndex++;

        labelVelocity = new JLabel("velocity");
        animationPanel.labelVelocity=labelVelocity;
        addLabelToPanel(labelVelocity, labelIndex);
        labelIndex++;

        labelMaxQ = new JLabel("max Q");
        animationPanel.labelMaxQ=labelMaxQ;
        addLabelToPanel(labelMaxQ, labelIndex);
        labelIndex++;
    }

    private void addLabelsToPlotPanel() {
        labelXaxis = new JLabel("position (x)");
        addLabelToPanel(
                labelXaxis,
                gfxSettings.FRAME_WEIGHT/2,
                gfxSettings.FRAME_HEIGHT- gfxSettings.FRAME_MARGIN*1);
        labelYaxis = new JLabel("velocity (y)");
        addLabelToPanel(labelYaxis, 0, 0);
    }

    private void addLabelToPanel(JLabel label, int posX, int posY) {
        plotPanel.add(label);
        label.setBounds(posX, posY, gfxSettings.LABEL_WEIGHT, gfxSettings.LABEL_HEIGHT);
    }

    private void addLabelToPanel(JLabel label, int labelIndex) {
        animationPanel.add(label);
        label.setBounds(
                gfxSettings.LABEL_XPOS,
             gfxSettings.LABEL_XPOSY_MIN+ labelIndex *gfxSettings.LABEL_HEIGHT,
                gfxSettings.LABEL_WEIGHT,
                gfxSettings.LABEL_HEIGHT);
    }


    private LineData createRoadData() {

        final int NOF_POINTS=100;
        List<Double> xList=new ArrayList<>();
        List<Double> yList=new ArrayList<>();
        for (int i = 0; i < NOF_POINTS ; i++) {
            double f=(double) i/NOF_POINTS;
            double x=parameters.MIN_POSITION *(1-f)+parameters.MAX_POSITION *f;
            double y=height(x);
            xList.add(x);
            yList.add(y);
        }
        return new LineData(
                xList.stream().mapToDouble(d -> d).toArray(),
                yList.stream().mapToDouble(d -> d).toArray());
    }



    @Override
    public StepReturn step(int action, State state) {

        State newState = new State(state);
        StepReturn stepReturn = new StepReturn();
        //newState.copyState(state);
        double position=state.getContinuousVariable("position");
        double velocity=state.getContinuousVariable("velocity");
        velocity += (action - 1) * parameters.FORCE + Math.cos(3 * position) * (-parameters.GRAVITY);
        position += velocity;
        position = MathUtils.clip(position, parameters.MIN_POSITION, parameters.MAX_POSITION);
        velocity=(position <= parameters.MIN_POSITION & velocity < 0)?0:velocity;
        newState.setVariable("position", position);
        newState.setVariable("velocity", velocity);
        newState.setVariable("nofSteps", state.getDiscreteVariable("nofSteps")+1);
        stepReturn.state = newState;
        stepReturn.termState = isTerminalState(newState);
        stepReturn.reward = (stepReturn.termState)?
                0:
                parameters.NON_TERMINAL_REWARD;

        int desiredAction=(state.getContinuousVariable("velocity") <-0.001)?0:2;
        double desActionReward= (action==desiredAction)?1.0:0.0;
        stepReturn.reward=stepReturn.reward+0.0*desActionReward;

        state.totalNofSteps++;
        return stepReturn;
    }

    public boolean isGoalState(StepReturn stepReturn) {
        return (stepReturn.termState &
                stepReturn.state.getDiscreteVariable("nofSteps")<parameters.MAX_NOF_STEPS);
    }


    @Override
    public void setRandomStateValuesStart(State state) {
        setRandomStateValues(state, true);
    }

    public void setRandomStateValuesAny(State state) {
        setRandomStateValues(state, false);
    }


    private void setRandomStateValues(State state, boolean startState) {

        if (startState) {
            state.setVariable("position", MathUtils.calcRandomFromIntervall(parameters.MIN_START_POSITION, parameters.MAX_START_POSITION));
            state.setVariable("velocity", MathUtils.calcRandomFromIntervall(parameters.MIN_START_VELOCITY, parameters.MAX_START_VELOCITY));
        } else {
            state.setVariable("position", MathUtils.calcRandomFromIntervall(parameters.MIN_POSITION, parameters.MAX_POSITION));
            state.setVariable("velocity", MathUtils.calcRandomFromIntervall(-parameters.MAX_SPEED, parameters.MAX_SPEED));
        }

        state.setVariable("nofSteps", 0);
    }


    @Override
    public boolean isTerminalState(State state) {
        return  (isFailsState(state) |
                state.getDiscreteVariable("nofSteps")>=parameters.MAX_NOF_STEPS);
    }

    @Override
    public boolean isTerminalStatePolicyTest(State state) {
        return (isFailsState(state) |
                state.getDiscreteVariable("nofSteps")>=parameters.MAX_NOF_STEPS_POLICY_TEST);
    }

    @Override
    public boolean isFailsState(State state)  {
        return (state.getContinuousVariable("position")>=parameters.GOAL_POSITION &
                state.getContinuousVariable("velocity")>=parameters.GOAL_VELOCITY);
    }

    @Override
    public void render(State state, double maxQ, int action) {
        double position=state.getContinuousVariable("position");
        double velocity=state.getContinuousVariable("velocity");
        animationPanel.setCarStates(position,height(position),velocity,action,maxQ);
        animationPanel.repaint();
    }

    public double height(State state) {
        return Math.sin(3 * state.getContinuousVariable("position")) * 0.45 + 0.55;
    }

    public double height(double position) {
        State state = new State();
        state.createContinuousVariable("position",position);
        return height(state);
    }


    @Override
    public boolean isPolicyTestSuccessful(State state) {
        return   (state.getDiscreteVariable("nofSteps") <
                    parameters.MAX_NOF_STEPS_POLICY_TEST);
    }

    @Override
    public void createVariablesInState(State state)
    {
        state.createDiscreteVariable("nofSteps", 0);
        state.createContinuousVariable("position", 0.0);
        state.createContinuousVariable("velocity", 0.0);
    }


}