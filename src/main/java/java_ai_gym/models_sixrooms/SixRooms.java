package java_ai_gym.models_sixrooms;

import java_ai_gym.models_common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/***
 * Environment with 6 rooms. Inner class EnvironmentParameters defines data.
 * Class StepReturn is return protocol for step, transition method.
 * TransitionMatrix expresses new room number, for ex action 5 in room 0 gives new room is 5."
 * There are 6 rooms: room 0-room 5. The action specifies the new room. Non allowed destination rooms/actions gives bad reward.
 * Entering goal room 5 gives high reward and terminates an episode.
 */

public class SixRooms extends Environment {

    private static final Logger logger = Logger.getLogger(SixRooms.class.getName());

    public EnvironmentParameters parameters = this.new EnvironmentParameters();

    // inner classes
    public class EnvironmentParameters extends EnvironmentParametersAbstract {
        public List<Integer> discreteStateSpace = Arrays.asList(0, 1, 2, 3, 4, 5);

        public final double R_FAIL = -1;  //non allowed transition
        public final double R_MOVE = -0.1;  //move transition
        public final double R_EXIT = 1;  //finding exit state reward

        private final int ROOM_EXIT_NUMBER = 5;
        public final int INIT_DEFAULT_ROOM_NUMBER = 1;

        private final double[][] rewardMatrix = {   //R(s,a) function
                {R_FAIL, R_FAIL, R_FAIL, R_FAIL, 0, R_FAIL},
                {R_FAIL, R_FAIL, R_FAIL, 0, R_FAIL, R_EXIT},
                {R_FAIL, R_FAIL, R_FAIL, 0, R_FAIL, R_FAIL},
                {R_FAIL, 0, 0, R_FAIL, 0, R_FAIL},
                {0, R_FAIL, R_FAIL, 0, R_FAIL, R_EXIT},
                {R_FAIL, 0, R_FAIL, R_FAIL, 0, R_EXIT},
        };

        private final int[][] transitionMatrix = {   //newRoomNr <- T(roomNr,action) function
                {0, 0, 0, 0, 4, 0},
                {1, 1, 1, 3, 1, 5},
                {2, 2, 2, 3, 2, 2},
                {3, 1, 2, 3, 4, 3},
                {0, 4, 4, 3, 4, 5},
                {5, 1, 5, 5, 4, 5},
        };

        private int minRoomNumber;
        private int minAction;
        public int nofStates;
        public int nofActions;

        public EnvironmentParameters() {
        }

        protected int getIdxState(State state) {
            return state.getDiscreteVariable("roomNumber") - parameters.minRoomNumber;
        }

        protected int getIdxAction(int action) {
            return action - parameters.minAction;
        }
    }

    //constructor
    public SixRooms() {
        parameters.discreteStateVariableNames.add("roomNumber");
        parameters.discreteActionsSpace.addAll(Arrays.asList(0, 1, 2, 3, 4, 5));
        parameters.minRoomNumber = parameters.discreteStateSpace.stream().min(Integer::compare).orElse(0);
        parameters.minAction = parameters.discreteActionsSpace.stream().min(Integer::compare).orElse(0);
        parameters.nofStates = parameters.discreteStateSpace.size();
        parameters.nofActions = parameters.discreteActionsSpace.size();
        createVariablesInState(getTemplateState());
    }

    //methods

    public EnvironmentParameters getParameters() {
        return parameters;
    }

    @Override
    public StepReturn step(int action, State state) {
        State newState = new State(state);
        StepReturn stepReturn = new StepReturn();
        int newRoomNr=getNewRoomNr(state,action);
        newState.setVariable("roomNumber", newRoomNr);
        stepReturn.state = newState;
        stepReturn.reward = parameters.R_MOVE+parameters.rewardMatrix[parameters.getIdxState(state)][parameters.getIdxAction(action)];
        stepReturn.termState = isTerminalState(newState);
        state.totalNofSteps++;
        return stepReturn;
    }

    @Override
    public boolean isTerminalState(State state) {
        return (state.getDiscreteVariable("roomNumber") == parameters.ROOM_EXIT_NUMBER);
    }

    @Override
    public boolean isTerminalStatePolicyTest(State state) {
        return (isTerminalState(state));
    }

    @Override
    public boolean isFailsState(State state) {
        logger.warning("This method is not implemented");
        return true;
    }

    @Override
    public void setRandomStateValuesStart(State state) {
        logger.warning("This method is not implemented");
    }

    @Override
    public boolean isPolicyTestSuccessful(State state) {
        logger.warning("This method is not implemented");
        return  false;
    }

    @Override
    public void render(State state, double Qmax, int action) {
    }

    public List<Integer> getFeasibleActions(State state) {

        List<Integer> feasibleActions = new ArrayList<>();
        for (int action : parameters.discreteActionsSpace) {
            if (parameters.rewardMatrix[parameters.getIdxState(state)][parameters.getIdxAction(action)] > parameters.R_FAIL) {
                feasibleActions.add(parameters.discreteActionsSpace.get(parameters.getIdxAction(action)));
            }
        }
        return feasibleActions;
    }

    private int getNewRoomNr(State state, int action) {
        return parameters.transitionMatrix[parameters.getIdxState(state)][parameters.getIdxAction(action)];
    }


    public void PrintQsa(Learnable agent) {
        System.out.println("Qsa -----------------------------");
        State s = new State(agent.getState());
        for (int roomNr : parameters.discreteStateSpace) {
            for (int action : parameters.discreteActionsSpace) {
                s.setVariable("roomNumber", roomNr);
                System.out.printf("%.3f    ", agent.readMemory(s, action));
            }
            System.out.println();
        }
    }


    public void PrintQsaBestAction(Learnable agent) {
        System.out.println("Qsa(s,best action) ----------------------------- ");
        State s = new State(agent.getState());
        for (int roomNr : parameters.discreteStateSpace) {
            s.setVariable("roomNumber", roomNr);
            System.out.printf("roomNr:"+roomNr+", Q: %.1f"+", bestA:"+agent.chooseBestAction(s)+"  ", agent.findMaxQ(s));
        }
        System.out.println();
    }

    public void showPolicy(Learnable agent) {
        // we consider every single state as a starting state
        // until we find the terminal state: we walk according to best action
        final int MAX_NO_STEPS=10;
        State s = new State(agent.getState());
        System.out.println("Policy for every state -----------------------------");
        for(int starRoomNr=0; starRoomNr<parameters.nofStates; starRoomNr++) {
            StepReturn stepReturn;
            s.setVariable("roomNumber", starRoomNr);
            System.out.print("Policy: " + s.getDiscreteVariable("roomNumber"));
            int nofSteps=0;
            while (!isTerminalState(s) & nofSteps<MAX_NO_STEPS) {
                int bestA = agent.chooseBestAction(s);
          //      System.out.println();
           //     System.out.println("roomNumber: " +s.getDiscreteVariable("roomNumber")+", bestA: " +bestA);
                stepReturn=step(bestA,s);
                s.copyState(stepReturn.state);
                System.out.print(" -> " + s.getDiscreteVariable("roomNumber"));
                nofSteps++;
            }
            System.out.println();
        }
    }

    @Override
    public void createVariablesInState(State state)
    {
        state.createDiscreteVariable("roomNumber", 0);
    }


}
