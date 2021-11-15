package java_ai_gym.models_poleoncart;

import java_ai_gym.models_common.*;
import java_ai_gym.swing.*;

import javax.swing.*;
import java.util.Arrays;
import java.util.logging.Logger;

/***
 * Description:
 *         A pole is attached by an un-actuated joint to a cart, which moves along
 *         a frictionless track. The pendulum starts upright, and the goal is to
 *         prevent it from falling over by increasing and reducing the cart's
 *         velocity.
 *     Source:
 *         This environment corresponds to the version of the cart-pole problem
 *         described by Barto, Sutton, and Anderson
 *     Observation:
 *         Type: Box(4)
 *         Num     Observation               Min                     Max
 *         0       Cart Position             -4.8                    4.8
 *         1       Cart Velocity             -Inf                    Inf
 *         2       Pole Angle                -0.418 rad (-24 deg)    0.418 rad (24 deg)
 *         3       Pole Angular Velocity     -Inf                    Inf
 *     Actions:
 *         Type: Discrete(2)
 *         Num   Action
 *         0     Push cart to the left
 *         1     Push cart to the right
 *         Note: The amount the velocity that is reduced or increased is not
 *         fixed; it depends on the angle the pole is pointing. This is because
 *         the center of gravity of the pole increases the amount of energy needed
 *         to move the cart underneath it
 *     Reward:
 *         Reward is 1 for every step taken, including the termination step
 *     Starting State:
 *         All observations are assigned a uniform random value in [-0.05..0.05]
 *     Episode Termination:
 *         Pole Angle is more than 12 degrees.
 *         Cart Position is more than 2.4 (center of the cart reaches the edge of
 *         the display).
 *         Episode length is greater than 200.
 *         Solved Requirements:
 *         Considered solved when the average return is greater than or equal to
 *         195.0 over 100 consecutive trials.

 */

public class CartPole extends EnvironmentForNetworkAgent {

    private static final Logger logger = Logger.getLogger(CartPole.class.getName());

    public EnvironmentParameters parameters = this.new EnvironmentParameters();
    public PanelCartPoleAnimation animationPanel;
    public JLabel labelX;
    public JLabel labelTheta;
    public JLabel labelXDot;
    public JLabel labelThetaDot;
    public JLabel labelNofSteps;
    public JLabel labelMaxQ;

    public PanelChartPolePlot plotPanel;
    public JLabel labelXaxis;
    public JLabel labelYaxis;

    // inner classes
    public class EnvironmentParameters extends EnvironmentParametersAbstract {
        public final double GRAVITY = 9.8;
        public final double MASSCART = 1.0;  //1.0
        public final double MASSPOLE = 0.1;
        public final double TOTAL_MASS = MASSPOLE + MASSCART;
        public final double LENGTH = 0.5;  // actually half the pole's length
        public final double POLEMASS_TIMES_LENGTH = MASSPOLE * LENGTH;
        public final double FORCE_MAG = 10.0;
        public final double TAU = 0.02;  // seconds between state updates  0.02

        public final double THETA_THRESHOLD_RADIANS  = 12 * 2 * 3.141592 / 360;
        public final double X_TRESHOLD = 2;
        public final double Y_MAX = 1;   //domain limit
        public final double Y_MIN = -0.5;
        public final double THETA_DOT_THRESHOLD_RADIANS  = 5; //Double.MAX_VALUE; 10
        public final double X_DOT_TRESHOLD = 5; //Double.MAX_VALUE;  10

        public final double ANY_VARIABLE_TRESHOLD_START =  0.05;

        public final double X_TERMINATION = 2.4;
        public final double THETA_TERMINATION  = 12  * Math.PI / 360;
        public  int MAX_NOF_STEPS =200;
        public final int MAX_NOF_STEPS_POLICY_TEST=300;
        public  double NON_TERMINAL_REWARD = 1.0;
        public int NOF_ACTIONS;

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

    public CartPole() {
        parameters.continuousStateVariableNames.add("x");
        parameters.continuousStateVariableNames.add("xDot");
        parameters.continuousStateVariableNames.add("theta");
        parameters.continuousStateVariableNames.add("thetaDot");

        parameters.discreteActionsSpace.addAll(Arrays.asList(0, 1));
        parameters.NOF_ACTIONS = parameters.discreteActionsSpace.size();
        policyTestSettings.NOF_EPISODES_BETWEEN_POLICY_TEST=25;

        createVariablesInState(getTemplateState());
        setupFrameAndPanel();
        //animationPanel.repaint();
    }

    private void setupFrameAndPanel() {
        animationFrame =new FrameEnvironment(gfxSettings.FRAME_WEIGHT, gfxSettings.FRAME_HEIGHT,"CartPole animation");
        plotFrame =new FrameEnvironment(gfxSettings.FRAME_WEIGHT, gfxSettings.FRAME_HEIGHT,"CartPole plots");
        ScaleLinear xScaler=new ScaleLinear(-parameters.X_TRESHOLD,parameters.X_TRESHOLD,
                gfxSettings.FRAME_MARGIN, gfxSettings.FRAME_WEIGHT - gfxSettings.FRAME_MARGIN,
                false, gfxSettings.FRAME_MARGIN);
        ScaleLinear yScaler=new ScaleLinear(parameters.Y_MIN,parameters.Y_MAX, gfxSettings.FRAME_MARGIN,
                gfxSettings.FRAME_HEIGHT - gfxSettings. FRAME_MARGIN,true, gfxSettings.FRAME_MARGIN);

        animationPanel =new PanelCartPoleAnimation(xScaler,yScaler);
        animationPanel.setLayout(null);  //to enable tailor made position
        addLabelsToAnimationPanel();
        animationFrame.add(animationPanel);
        animationFrame.setVisible(true);
    }

    private void addLabelsToAnimationPanel() {
        int labelIndex=0;

        labelX = new JLabel("x");
        animationPanel.labelX=labelX;
        addLabelToPanel(labelX, labelIndex);
        labelIndex++;

        labelTheta = new JLabel("theta");
        animationPanel.labelTheta=labelTheta;
        addLabelToPanel(labelTheta, labelIndex);
        labelIndex++;

        labelXDot = new JLabel("xDot");
        animationPanel.labelXDot=labelXDot;
        addLabelToPanel(labelXDot, labelIndex);
        labelIndex++;

        labelThetaDot = new JLabel("thetaDot");
        animationPanel.labelThetaDot=labelThetaDot;
        addLabelToPanel(labelThetaDot, labelIndex);
        labelIndex++;

        labelNofSteps = new JLabel("nofSteps");
        animationPanel.labelNofSteps=labelNofSteps;
        addLabelToPanel(labelNofSteps, labelIndex);
        labelIndex++;

        labelMaxQ = new JLabel("maxQ");
        animationPanel.labelMaxQ=labelMaxQ;
        addLabelToPanel(labelMaxQ, labelIndex);
        labelIndex++;

    }

    private void addLabelToPanel(JLabel label, int labelIndex) {
        animationPanel.add(label);
        label.setBounds(
                gfxSettings.LABEL_XPOS,
             gfxSettings.LABEL_XPOSY_MIN+ labelIndex * gfxSettings.LABEL_HEIGHT,
                gfxSettings.LABEL_WEIGHT,
                gfxSettings.LABEL_HEIGHT);
    }


    @Override
    public StepReturn step(int action, State state) {

        if (!parameters.discreteActionsSpace.contains(action))
            logger.warning("Not valid action");

        State newState = new State(state);
        StepReturn stepReturn = new StepReturn();

        double force=(action==0)?-parameters.FORCE_MAG:parameters.FORCE_MAG;

        double theta=state.getContinuousVariable("theta");
        double costheta = Math.cos(theta);
        double sintheta = Math.sin(theta);
        double x=state.getContinuousVariable("x");
        double xDot=state.getContinuousVariable("xDot");
        double thetaDot=state.getContinuousVariable("thetaDot");

        //For the interested reader: https://coneural.org/florian/papers/05_cart_pole.pdf
        double temp =  force + parameters.POLEMASS_TIMES_LENGTH * Math.pow(thetaDot,2) * sintheta;
        double thetaacc = (parameters.GRAVITY * sintheta - costheta * temp) / (
                parameters.LENGTH * (4.0 / 3.0 - parameters.MASSPOLE * Math.pow(costheta,2) / parameters.TOTAL_MASS));

        double xacc = temp - parameters.POLEMASS_TIMES_LENGTH * thetaacc * costheta / parameters.TOTAL_MASS;

        x = x + parameters.TAU * xDot;
        xDot = xDot + parameters.TAU * xacc;
        theta = theta + parameters.TAU * thetaDot;
        thetaDot = thetaDot + parameters.TAU * thetaacc;

        newState.setVariable("x", x);
        newState.setVariable("xDot", xDot);
        newState.setVariable("theta", theta);
        newState.setVariable("thetaDot", thetaDot);
        newState.setVariable("nofSteps", state.getDiscreteVariable("nofSteps")+1);

        stepReturn.state = newState;
        stepReturn.termState = isTerminalState(newState);
        //stepReturn.reward = (stepReturn.termState)?0:parameters.NON_TERMINAL_REWARD;
        stepReturn.reward = (isFailsState(newState))?0:parameters.NON_TERMINAL_REWARD;

        state.totalNofSteps++;
        return stepReturn;

    }


    @Override
    public void setRandomStateValuesStart(State state) {
        double low=-parameters.ANY_VARIABLE_TRESHOLD_START;
        double high=parameters.ANY_VARIABLE_TRESHOLD_START;
        state.setVariable("x", MathUtils.calcRandomFromIntervall(low,high));
        state.setVariable("xDot", MathUtils.calcRandomFromIntervall(low,high));
        state.setVariable("theta", MathUtils.calcRandomFromIntervall(low,high));
        state.setVariable("thetaDot", MathUtils.calcRandomFromIntervall(low,high));
        state.setVariable("nofSteps", 0);
    }


    @Override
    public boolean isTerminalState(State state) {
        return (isFailsState(state) |
                state.getDiscreteVariable("nofSteps") >= parameters.MAX_NOF_STEPS);
    }


    @Override
    public boolean isTerminalStatePolicyTest(State state) {
        return (isFailsState(state) |
                state.getDiscreteVariable("nofSteps") >= parameters.MAX_NOF_STEPS_POLICY_TEST);
    }

    @Override
    public boolean isFailsState(State state)
    {
        return (state.getContinuousVariable("x") >= parameters.X_TRESHOLD |
                state.getContinuousVariable("x") <= -parameters.X_TRESHOLD |
                state.getContinuousVariable("theta") >= parameters.THETA_THRESHOLD_RADIANS |
                state.getContinuousVariable("theta") <=-parameters.THETA_THRESHOLD_RADIANS);
    }

    @Override
    public void render(State state, double maxQ, int action) {
        animationPanel.setCartPoleStates(state,action,maxQ);
        animationPanel.repaint();
    }

    @Override
    public boolean isPolicyTestSuccessful(State state) {
        return   (state.getDiscreteVariable("nofSteps") >=
                parameters.MAX_NOF_STEPS_POLICY_TEST);
    }

    @Override
    public void createVariablesInState(State state)
    {
        state.createContinuousVariable("x", 0.0);
        state.createContinuousVariable("xDot", 0.0);
        state.createContinuousVariable("theta", 0.0);
        state.createContinuousVariable("thetaDot", 0.0);
        state.createDiscreteVariable("nofSteps", 0);
    }

}
