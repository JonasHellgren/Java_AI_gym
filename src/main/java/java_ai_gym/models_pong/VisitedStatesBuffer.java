package java_ai_gym.models_pong;

import java_ai_gym.helpers.CpuTimer;
import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.NullState;
import java_ai_gym.models_common.State;
import java_ai_gym.models_common.StateForSearch;
import java_ai_gym.models_common.StepReturn;
import lombok.Getter;

import java.util.*;
import java.util.logging.Logger;

@Getter
public class VisitedStatesBuffer {

    protected final static Logger logger = Logger.getLogger(VisitedStatesBuffer.class.getName());
    final double PROB_SELECTING_STATE_FOR_EXPLORATION_FACTOR_CALCULATION = 0.2;  //for speeding up
    final int MIN_LENGTH_EXP_FACTOR_LIST=100; //only regard PROB_SELECTING_STATE.. above this length
    final double EXPLORATION_FACTOR_IF_NO_STATE_FOUND=0.0;

    StateVisitsDAO stateVisitsDAO;
    ExperiencesDAO experiencesDAO;
    SetOfTerminalStatesDAO setOfTerminalStatesDAO;
    int depthMax;
    boolean timeExceedWhenTrimming;
    int removedNodes;
    protected final Random random;

    public VisitedStatesBuffer() {
        stateVisitsDAO = new StateVisitsDAO();
        experiencesDAO = new ExperiencesDAO();
        setOfTerminalStatesDAO = new SetOfTerminalStatesDAO();
        depthMax = 0;
        timeExceedWhenTrimming=false;
        removedNodes=0;
        random = new Random();
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
        //  List<StateExperience> list = new ArrayList<>(experiencesDAO1.getExperienceList(id));

        return new ArrayList<>(experiencesDAO.getExperienceList(id).values());
    }

    boolean isActionInStateTerminalAccordingToExperience(String id, int action) {
        List<StateExperience> expList = getExperienceList(id);

        for (StateExperience se : expList) {
            if (se.action == action) {
                return se.termState;
            }
        }
        return false;
    }

    public StateExperience searchExperienceOfSteppingToState(String id) {
        return experiencesDAO.searchExperienceOfSteppingToState(id);
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

    public VisitedStatesBuffer createNewVSBWithNoLooseNodesBelowDepth(int searchDepth, CpuTimer cpuTimer) {
        logger.info("createNewVSBWithNoLooseNodesBelowDepth called"+", searchDepth = "+searchDepth);

        if (getDepthMax() < searchDepth) {
            logger.warning("removeLooseNodesBelowDepth failed, can't remove below non existing depth: searchDepth= " + searchDepth + ", maxDepth = " + getDepthMax());
            return new VisitedStatesBuffer(this);
        }
        VisitedStatesBuffer vsbTrimmed = new VisitedStatesBuffer(this);
        vsbTrimmed.removedNodes = 0;
        vsbTrimmed.timeExceedWhenTrimming=false;  //assume will do it in time
        boolean nodeRemoved;
        do {
            nodeRemoved = false;
            for (int depth = searchDepth - 1; depth >= 0; depth--) {
                List<StateForSearch> statesAtDepth = vsbTrimmed.getAllStatesAtDepth(depth);
                if (cpuTimer.isTimeExceeded()) {
                    logger.warning("Time exceeded in createNewVSBWithNoLooseNodesBelowDepth !");
                    vsbTrimmed.timeExceedWhenTrimming=true;
                    break;
                }
                logger.fine("depth = "+depth+", nof states = "+statesAtDepth.size());
                for (StateForSearch state : statesAtDepth) {
                    logger.fine("state id =" + state.id + ", isNoActionTried =" + isNoActionTriedInStateWithId(state.id));
                    if (vsbTrimmed.isNoActionTriedInStateWithId(state.id)) {
                        logger.fine("removing state id = " + state.id + ", size =" + vsbTrimmed.size());
                        nodeRemoved = true;
                        vsbTrimmed.removedNodes++;
                        String idToRemove = state.id;
                        vsbTrimmed.getStateVisitsDAO().remove(idToRemove);
                        vsbTrimmed.getExperiencesDAO().removeExpItemWithNewStateId(idToRemove);
                    }
                }
                logger.fine("nof states after = "+statesAtDepth.size());
            }
        } while (nodeRemoved && !cpuTimer.isTimeExceeded());

        logger.info("Nof removed nodes are = " + vsbTrimmed.removedNodes);
        return vsbTrimmed;
    }

    public boolean anyLooseNodeBelowDepth(VisitedStatesBuffer vsb, int depthMax) {
        for (int depth = depthMax - 1; depth >= 0; depth--) {
            List<StateForSearch> statesAtDepth = vsb.getAllStatesAtDepth(depth);
            for (StateForSearch state : statesAtDepth) {
                logger.fine("state id =" + state.id + ", isNoActionTried =" + isNoActionTriedInStateWithId(state.id));
                if (isNoActionTriedInStateWithId(state.id)) {
                    logger.warning("Following node is loose and below depth " + state);
                    return true;
                }
            }
        }
        return false;
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

    /*
    public double calcExplorationFactor(int searchDepthPrev, int searchDepth) {  //TODO REMOVE

        int nofActionsTested = 0;
        int nofActionsAvailable = 0;
        for (int depth = searchDepth - 1; depth >= searchDepthPrev; depth--) {
            List<StateForSearch> statesAtDepth = this.getAllStatesAtDepth(depth);
            for (StateForSearch state : statesAtDepth) {
                nofActionsTested = nofActionsTested + this.nofActionsTested(state.id);
                nofActionsAvailable = nofActionsAvailable + state.nofActions;
            }
        }

        if (nofActionsAvailable == 0) {
            logger.warning("Sum of all actions available is zero, setting exploration factor as 1");
            return 1.0;
        }
        return (double) nofActionsTested / (double) nofActionsAvailable;
    }  */

    public double calcExplorationFactor(int excludedDepth) {

        List<Double> explorationFactorList = new ArrayList<>();
        for (StateForSearch state : this.stateVisitsDAO.getAll()) {
            double probability=explorationFactorList.size()>MIN_LENGTH_EXP_FACTOR_LIST
                    ?PROB_SELECTING_STATE_FOR_EXPLORATION_FACTOR_CALCULATION
                    :1.0;
            if (state.depth != excludedDepth &&
                MathUtils.calcRandomFromIntervall(0, 1) < probability &&
                !setOfTerminalStatesDAO.isTerminal(state.id)) {

                if (state.nofActions == 0) {
                    logger.warning("No actions in state = " + state.id);
                } else {
                    explorationFactorList.add((this.nofActionsTested(state.id) / (double) state.nofActions));
                }
            }
        }

        if (explorationFactorList.size() == 0) {
            logger.warning("No state fulfills criteria for exploration factor calculation");
            return EXPLORATION_FACTOR_IF_NO_STATE_FOUND;
        }

        DoubleSummaryStatistics stats = explorationFactorList.stream().mapToDouble(a -> a).summaryStatistics();
        return stats.getAverage();
    }

    public Map<Integer, Integer> calcStatesAtDepth(int searchDepth) {
        Map<Integer, Integer> statePerDepthList = new HashMap<>();

        for (int depth = 0; depth <= searchDepth; depth++) {
            List<StateForSearch> statesAtDepth = this.getAllStatesAtDepth(depth);
            statePerDepthList.put(depth, statesAtDepth.size());
        }
        return statePerDepthList;
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


}
