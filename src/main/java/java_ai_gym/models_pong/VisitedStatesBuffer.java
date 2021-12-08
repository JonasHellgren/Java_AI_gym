package java_ai_gym.models_pong;
import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.StateForSearch;
import java_ai_gym.models_common.StepReturn;
import lombok.Getter;

import java.util.*;
import java.util.logging.Logger;

@Getter
public class VisitedStatesBuffer {

    protected final static Logger logger = Logger.getLogger(VisitedStatesBuffer.class.getName());


    StateVisitsDAO stateVisitsDAO;
    ExperiencesDAO experiencesDAO;
    SetOfTerminalStatesDAO setOfTerminalStatesDAO;
    int depthMax;
    ExplorationFactorCalculator explorationFactorCalculator;

    public VisitedStatesBuffer() {
        stateVisitsDAO = new StateVisitsDAO();
        experiencesDAO = new ExperiencesDAO();
        setOfTerminalStatesDAO = new SetOfTerminalStatesDAO();
        depthMax = 0;
        explorationFactorCalculator = new ExplorationFactorCalculator(this);
    }

    public VisitedStatesBuffer(StateForSearch startState) {
        this();
        StateForSearch startStateClone = new StateForSearch(startState);
        addState(startStateClone.START_STATE_ID, startStateClone);
        experiencesDAO.addStateWithNoExp(startStateClone.START_STATE_ID);
    }

    public VisitedStatesBuffer(VisitedStatesBuffer vsb) {
        this();
        stateVisitsDAO.copy(vsb.getStateVisitsDAO());
        experiencesDAO.copy(vsb.getExperiencesDAO());
        setOfTerminalStatesDAO.copy(vsb.getSetOfTerminalStatesDAO());
        depthMax = vsb.depthMax;
    }

    public void clear() {
        stateVisitsDAO.clear();
        experiencesDAO.clear();
        setOfTerminalStatesDAO.clear();
    }

    public StateForSearch getState(String id) {
        return stateVisitsDAO.get(id);
    }

    public StateForSearch selectRandomState() {
        return getState(selectRandomStateId());
    }

    public String selectRandomStateId() {
        if (stateVisitsDAO.idList.size()==0) {
            logger.warning("No state visits");
            return "";
        }
        return MathUtils.getRandomItemFromList(stateVisitsDAO.idList);
    }

    public int nofStates() {
        return stateVisitsDAO.size();
    }

    public void addState(String id, StateForSearch state) {
        if (stateVisitsDAO.contains(id)) {
            logger.warning("addState: Trying to add already existing state id");
        } else {
            stateVisitsDAO.add(id, state);
        }
    }

    public List<StateForSearch> getAllStatesAtDepth(int depth) {
        List<StateForSearch> statesAtDepth = new ArrayList<>();
        for (StateForSearch state : stateVisitsDAO.getAll()) {
            if (state.depth == depth) {
                statesAtDepth.add(state);
            }
        }

        return statesAtDepth;
    }

    public void addNewStateAndExperienceFromStep(String idFromState, int action, StepReturn stepReturn) {
        String newId = idFromState + "." + action;
        if (stateVisitsDAO.contains(newId)) {
            logger.warning("addNewStateAndExperienceFromStep: Trying to add already existing experience");
        } else {
            addState(newId, (StateForSearch) stepReturn.state);
            StateExperience stateExperience = new StateExperience(action, stepReturn.reward, stepReturn.termState, newId);
            addExperience(idFromState, stateExperience);
            setOfTerminalStatesDAO.addIdIfTerminal(newId, stepReturn);
            depthMax = Math.max(depthMax, ((StateForSearch) stepReturn.state).depth);
        }
    }

    public void addExperience(String id, StateExperience exp) {
        experiencesDAO.add(id, exp);
    }

    public List<StateExperience> getExperienceList(String id) {
        return new ArrayList<>(experiencesDAO.getExperienceList(id).values());
    }

    public boolean isExperienceOfStateTerminal(String id) {
        return setOfTerminalStatesDAO.isTerminal(id);
    }

    public int nofActionsTested(String id) {
        return experiencesDAO.nofActionsTested(id);
    }

    public List<Integer> testedActions(String id) {
        return experiencesDAO.testedActions(id);
    }

    public int getDepthMax() {
         return depthMax;
    }

    public int size() {
        return stateVisitsDAO.size();
    }

    public boolean isNoActionTriedInStateWithId(String id) {
        return (getExperienceList(id).size() == 0);
    }

    public boolean areAllActionsTriedInStateWithId(String id) {
        return (getExperienceList(id).size() == stateVisitsDAO.get(id).nofActions);
    }


    public double calcExplorationFactor(int excludedDepth) {
        return explorationFactorCalculator.calc(excludedDepth);
    }

    public Map<Integer, Integer> calcStatesAtDepth(int searchDepth) {
        Map<Integer, Integer> statePerDepthList = new HashMap<>();

        for (int depth = 0; depth <= searchDepth; depth++) {
            List<StateForSearch> statesAtDepth = this.getAllStatesAtDepth(depth);
            statePerDepthList.put(depth, statesAtDepth.size());
        }
        return statePerDepthList;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Visited states buffer");
        sb.append(System.getProperty("line.separator"));
        for (String stateId : stateVisitsDAO.keySet()) {
            StateForSearch state = getState(stateId);
            sb.append(state.searchSpecificPropertiesAsString());
            sb.append("; experience:" + getExperienceList(stateId));

            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();

    }


}
