package java_ai_gym.models_agent_search;

import java_ai_gym.models_common.StepReturn;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Getter
public class SetOfTerminalStatesDAO {

    protected final static Logger logger = Logger.getLogger(StateVisitsDAO.class.getName());

    Set<String> stateIds;

    public SetOfTerminalStatesDAO() {
        stateIds = new HashSet<>();
    }

    public List<String> getStateIdsAsList () {
        return List.copyOf(stateIds);
    }

    public void clear() {
        stateIds.clear();
    }

    public void copy(SetOfTerminalStatesDAO setOfTerminalStatesDAO) {
        this.stateIds.addAll(setOfTerminalStatesDAO.stateIds);
    }

    public void addIdIfTerminal(String newId, StepReturn stepReturn) {
        if (stepReturn.termState) {
            logger.fine("Added terminal state to SetOfTerminalStatesDAO, size = "+ stateIds.size());
            stateIds.add(newId);
        }
    }

    public boolean isTerminal(String id) {
      //
       return stateIds.contains(id);
    }

}
