package java_ai_gym.models_sixrooms;

import java_ai_gym.models_common.AgentTabular;
import java_ai_gym.models_common.State;

import java.util.logging.Logger;

public class SixRoomsAgentTabular extends AgentTabular {

    private static final Logger logger = Logger.getLogger(SixRoomsAgentTabular.class.getName());
    //private final SixRooms.EnvironmentParameters envParams;  //reference to environment parameters

    public SixRoomsAgentTabular(SixRooms.EnvironmentParameters envParams) {
        this.envParams = envParams;
        state = new State();
        for (String varName : envParams.discreteStateVariableNames)
            state.createDiscreteVariable(varName, envParams.INIT_DEFAULT_ROOM_NUMBER);

        logger.info("Tabular based agent created. " + "nofStates:" + envParams.nofStates + ", nofActions:" + envParams.nofActions);
        createInitMemory(envParams);

        this.Q_INIT = 0;
        this.GAMMA=1.0;
        this.ALPHA = 0.1;
        this.PROBABILITY_RANDOM_ACTION = 0.2;
        this.NUM_OF_EPISODES = 10000;
    }


    protected void createInitMemory(SixRooms.EnvironmentParameters envParams) {
        Qsa = new double[envParams.nofStates][envParams.nofActions];
        State s = new State(state);
        for (int state : envParams.discreteStateSpace) {
            for (int action : envParams.discreteActionsSpace) {
                s.setVariable("roomNumber", state);
                writeMemory(s, action, Q_INIT);
            }
        }
    }

}
