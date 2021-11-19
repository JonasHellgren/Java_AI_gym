package java_ai_gym.models_pong;

import java_ai_gym.models_common.State;
import java_ai_gym.models_common.StateForSearch;
import java_ai_gym.models_common.StepReturn;
import lombok.ToString;

import java.util.*;
import java.util.logging.Logger;

public class VisitedStatesBuffer {

    protected final static Logger logger = Logger.getLogger(VisitedStatesBuffer.class.getName());
    public final String START_STATE_ID = "start";

    public static class StateExperience {
        int action;
        double reward;
        boolean termState;
        String idNewState;

        public StateExperience(int action, double reward, boolean termState, String idNewState) {
            this.action = action;
            this.reward = reward;
            this.termState = termState;
            this.idNewState = idNewState;
        }

        @Override
        public String toString() {
            return "(a=" + action + ", r=" + reward + ", ts=" + termState + ", idNew=" + idNewState + ")";
        }
    }

    Map<String, State> stateBuffer;   //<id,stat>
    Map<String, List<StateExperience>> expBuffer;   //<id,list of experiences >
    protected final Random random;

    public VisitedStatesBuffer() {
        stateBuffer = new HashMap<>();
        expBuffer = new HashMap<>();
        random = new Random();
    }

    public VisitedStatesBuffer(State startState) {
        this();
        addState(START_STATE_ID, (StateForSearch) startState);
        expBuffer.put(START_STATE_ID, new ArrayList<>());
    }

    public String selectRandomStateId() {

        if (stateBuffer.size() == 0) {
            logger.warning("Empty buffer");
            return "";
        } else {
            return getRandomSetElement(stateBuffer.keySet());
        }
    }

    public int nofStates() {
        return stateBuffer.size();
    }

    public void addState(String id, StateForSearch state) {
        state.id=id;
        stateBuffer.put(id, state);
    }

    public void clear() {
        stateBuffer.clear();
        expBuffer.clear();
    }

    public StateForSearch getState(String id) {
        return (StateForSearch) stateBuffer.get(id);
    }

    public void addNewStateAndExperienceFromStep(String idFromState, int action, StepReturn stepReturn) {
        String newId = idFromState + "." + action;

        addState(newId, (StateForSearch) stepReturn.state);
        StateExperience stateExperience = new StateExperience(action, stepReturn.reward, stepReturn.termState, newId);
        addExperience(idFromState, stateExperience);
    }

    public void addExperience(String id, StateExperience exp) {
        if (!expBuffer.containsKey(id)) {
            expBuffer.put(id, new ArrayList<>());
        }
        List<StateExperience> expList = expBuffer.get(id);
        expList.add(exp);
        expBuffer.put(id, expList);
    }

    public List<StateExperience> getExperienceList(String id) {
        if (!expBuffer.containsKey(id)) {
            logger.warning("No experience of state:" + id);
            return new ArrayList<>();
        }
        return expBuffer.get(id);
    }

    boolean isActionInStateTerminalAccordingToExperience(String id, int action) {
        List<StateExperience> expList = getExperienceList(id);

        for (StateExperience se : expList) {
            if (se.action==action) {
                return se.termState;
            }
        }
        return false;
    }

    static <E> E getRandomSetElement(Set<E> set) {
        return set.stream().skip(new Random().nextInt(set.size())).findFirst().orElse(null);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (String stateId : stateBuffer.keySet()) {
            StateForSearch state=  getState(stateId);
            sb.append(state.searchSpecificPropertiesAsString());
            sb.append("; experience:"+getExperienceList(stateId));

            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();

    }


}
