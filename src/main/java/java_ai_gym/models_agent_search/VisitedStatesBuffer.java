package java_ai_gym.models_agent_search;
import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.StateForSearch;
import java_ai_gym.models_common.StepReturn;
import lombok.Getter;

import java.util.*;
import java.util.logging.Logger;

/***
 * This class can be seen as the memory of the class AgentDPSearch.
 * It resembles the experience replay buffer in the DQN algorithm.
 */

@Getter
public class VisitedStatesBuffer {

    protected final static Logger logger = Logger.getLogger(VisitedStatesBuffer.class.getName());

    public final String ID_STATE_EMPTY_BUFFER="";
    StateVisitsDAO stateVisitsDAO;
    ExperiencesDAO experiencesDAO;
    SetOfTerminalStatesDAO setOfTerminalStatesDAO;
    BufferHealthCalculator bufferHealthCalculator;
    int depthMax;
    double explorationFactor;
    double fractionLooseNodes;

    public VisitedStatesBuffer() {
        stateVisitsDAO = new StateVisitsDAO();
        experiencesDAO = new ExperiencesDAO();
        setOfTerminalStatesDAO = new SetOfTerminalStatesDAO();
        depthMax = 0;
        bufferHealthCalculator = new BufferHealthCalculator(this);
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
        bufferHealthCalculator =vsb.bufferHealthCalculator;
    }

    public void clear() {
        stateVisitsDAO.clear();
        experiencesDAO.clear();
        setOfTerminalStatesDAO.clear();
        explorationFactor = 0;
    }

    public StateForSearch getState(String id) {
        return stateVisitsDAO.get(id);
    }

    public StateForSearch selectRandomState() {
        return getState(selectRandomStateId());
    }

    public StateForSearch selectRandomStateFromDepth(int depth) {
        return getState(MathUtils.getRandomItemFromList(stateVisitsDAO.getAllIdsAtDepth(depth)));
    }

    public String selectRandomStateId() {
        if (stateVisitsDAO.idList.size()==0) {
            logger.warning("No state visits");
            return ID_STATE_EMPTY_BUFFER;
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

    public List<String> getAllIds() {
        return stateVisitsDAO.idList;
    }

    public List<StateForSearch> getAllStatesAtDepth(int depth) {
        List<StateForSearch> states = new ArrayList<>();
        if (stateVisitsDAO.getAllIdsAtDepth(depth)==null) {
            logger.fine("stateVisitsDAO.getAllIdsAtDepth(depth) is null");
        } else {
            for (String id : stateVisitsDAO.getAllIdsAtDepth(depth)) {
                states.add(getState(id));
            }
        }
        return states;
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

    public void calcExplorationFactor(int excludedDepth) {
        this.explorationFactor = bufferHealthCalculator.calcExplorationFactor(excludedDepth);
    }

    public void calcFractionLooseNodes(int excludedDepth) {
        fractionLooseNodes = bufferHealthCalculator.calcFractionLooseNodes(excludedDepth);
    }



    public Map<Integer, Integer> calcStatesAtDepth(int searchDepth) {
        Map<Integer, Integer> statePerDepthList = new HashMap<>();

        for (int depth = 0; depth <= searchDepth; depth++) {
            List<StateForSearch> statesAtDepth = this.getAllStatesAtDepth(depth);
            statePerDepthList.put(depth, statesAtDepth.size());
        }
        return statePerDepthList;
    }

    public void remove(String id) {
        stateVisitsDAO.remove(id);
        experiencesDAO.removeExpItemWithNewStateId(id);
    }

    public StateForSearch findStateWithNotAllActionsTestedAndNotTerminal(int searchDepth)  {

        for (StateForSearch state : this.stateVisitsDAO.getAll()) {
            if (!areAllActionsTriedInStateWithId(state.id) &&
                    !isExperienceOfStateTerminal(state.id) &&
                    state.depth!=searchDepth) {
                return stateVisitsDAO.get(state.id);
            }
        }
        return null;
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

    public String toStringLight() {
        StringBuilder sb = new StringBuilder();

        sb.append("Visited states buffer");
        sb.append(System.getProperty("line.separator"));


        for (int depth=0;depth<=getDepthMax();depth++) {
            List<StateForSearch> states= getAllStatesAtDepth(depth);
            sb.append("depth ="+depth+", nof states "+states.size());
         //   sb.append(state.searchSpecificPropertiesAsString());


            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();

    }


}
