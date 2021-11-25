package java_ai_gym.models_pong;

import java_ai_gym.models_common.StateForSearch;
import java_ai_gym.models_common.StepReturn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class SetOfTerminalStatesDAO {

    protected final static Logger logger = Logger.getLogger(StateVisitsDAO.class.getName());

    Set<String> set;

    public SetOfTerminalStatesDAO() {
        set = new HashSet<>();
    }

    public void clear() {
        set.clear();
    }

    public void addIdIfTerminal(String newId, StepReturn stepReturn) {
        if (stepReturn.termState) {
            logger.info("Added terminal state to SetOfTerminalStatesDAO, size = "+set.size());
            set.add(newId);
        }
    }

    public boolean isTerminal(String id) {
      //
       return set.contains(id);
    }

}
