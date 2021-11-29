package java_ai_gym.models_pong;

import java_ai_gym.models_common.StepReturn;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class SetOfTerminalStatesDAO {

    protected final static Logger logger = Logger.getLogger(StateVisitsDAO.class.getName());

    Set<String> stateIds;

    public SetOfTerminalStatesDAO() {
        stateIds = new HashSet<>();
    }

    public void clear() {
        stateIds.clear();
    }

    public void copy(SetOfTerminalStatesDAO setOfTerminalStatesDAO) {
        this.stateIds.addAll(setOfTerminalStatesDAO.stateIds);
    }

    public void addIdIfTerminal(String newId, StepReturn stepReturn) {
        if (stepReturn.termState) {
            logger.info("Added terminal state to SetOfTerminalStatesDAO, size = "+ stateIds.size());
            stateIds.add(newId);
        }
    }

    public boolean isTerminal(String id) {
      //
       return stateIds.contains(id);
    }

}
