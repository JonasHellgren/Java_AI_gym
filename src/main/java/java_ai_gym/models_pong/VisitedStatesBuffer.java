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

    protected final Random random;

    public VisitedStatesBuffer() {
        stateVisitsDAO = new StateVisitsDAO();
        experiencesDAO = new ExperiencesDAO();
        random = new Random();
    }

    public VisitedStatesBuffer(StateForSearch startState) {
        this();
        StateForSearch startStateClone=new StateForSearch(startState);
        addState(startStateClone.START_STATE_ID,  startStateClone);
        experiencesDAO.addStateWithNoExp(startStateClone.START_STATE_ID);
    }

    public VisitedStatesBuffer(VisitedStatesBuffer vsb) {
        this();
        stateVisitsDAO.copy(vsb.getStateVisitsDAO());
        experiencesDAO.copy(vsb.getExperiencesDAO());
    }

    public void clear() {
        stateVisitsDAO.clear();
        experiencesDAO.clear();
    }

    public StateForSearch getState(String id) {
        return stateVisitsDAO.get(id);
    }

    public StateForSearch selectRandomState() {
        return getState(stateVisitsDAO.selectRandomStateId());
    }

    public String selectRandomStateId() {
      return stateVisitsDAO.selectRandomStateId();
    }

    public int nofStates() {
        return stateVisitsDAO.size();
    }

    public void addState(String id, StateForSearch state) {
        stateVisitsDAO.add(id, state);
    }

    public List<StateForSearch> getAllStatesAtDepth(int depth) {
        List<StateForSearch> statesAtDepth= new ArrayList<>() ;
        for (StateForSearch state:stateVisitsDAO.getAll()) {
            if (state.depth==depth) {
                statesAtDepth.add(state);
            }
        }

    return statesAtDepth;
    }

    public void addNewStateAndExperienceFromStep(String idFromState, int action, StepReturn stepReturn) {
        String newId = idFromState + "." + action;
        addState(newId, (StateForSearch) stepReturn.state);
        StateExperience stateExperience = new StateExperience(action, stepReturn.reward, stepReturn.termState, newId);
        addExperience(idFromState, stateExperience);
    }

    public void addExperience(String id, StateExperience exp) {
        experiencesDAO.add(id,exp);
    }

    public List<StateExperience> getExperienceList(String id) {
    return experiencesDAO.getExperienceList(id);
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

    public StateExperience searchExperienceOfSteppingToState(String id) {
       return  experiencesDAO.searchExperienceOfSteppingToState(id);
    }

    public int nofActionsTested(String id) {
        return experiencesDAO.nofActionsTested(id);
    }

    public List<Integer> testedActions(String id) {
        return experiencesDAO.testedActions(id);
    }

    public int getMaxDepth() {

        if (stateVisitsDAO.size()==0) {
            logger.warning("Depth not defined for zero size buffer");
            return 0;
        }

        int depthMax=0;
        for (StateForSearch state:stateVisitsDAO.getAll()) {
            depthMax=Math.max(depthMax,state.depth);
        }
        return depthMax;
    }

    public VisitedStatesBuffer removeLooseNodesBelowDepth(int depthMax) {

        VisitedStatesBuffer vsbTrimmed=new VisitedStatesBuffer(this);
        int removedNodes=0;

        boolean nodeRemoved;
        do {
            nodeRemoved = false;
            for (int depth = depthMax - 1; depth >= 0; depth--) {
                List<StateForSearch> statesAtDepth = vsbTrimmed.getAllStatesAtDepth(depth);
                //  System.out.println("depth = "+depth);
                for (StateForSearch state : statesAtDepth) {
                    // System.out.println("id = "+state.id+", exp length = "+getExperienceList(state.id).size());
                    if (vsbTrimmed.getExperienceList(state.id).size() == 0) {
                    //    logger.info("Found node to trim = " + state.id);
                        nodeRemoved = true;
                        removedNodes++;
                        String idToRemove = state.id;
                        vsbTrimmed.getStateVisitsDAO().remove(idToRemove);
                        vsbTrimmed.getExperiencesDAO().removeExpItemWithNewStateId(idToRemove);
                    }
                }
            }
         }  while (nodeRemoved);
        //} while (anyLooseNodeBelowDepth(vsbTrimmed,vsbTrimmed.getMaxDepth()));

        if (anyLooseNodeBelowDepth(vsbTrimmed,depthMax)) {
            logger.warning("removeLooseNodesBelowDepth failed, still loose node(s).");
        }

        logger.info("Nof removed nodes are = "+ removedNodes);

        return  vsbTrimmed;
    }

    public boolean anyLooseNodeBelowDepth(VisitedStatesBuffer vsb, int depthMax) {

        /*
        List<StateForSearch> allStates = vsb.getStateVisitsDAO().getAll();
        List<StateForSearch> statesAtDepth = vsb.getAllStatesAtDepth(depth);
        List<StateForSearch> statesBelowDepth=MathUtils.getDifferenceBetweenLists2(allStates,statesAtDepth);

        for (StateForSearch state : statesBelowDepth) {
            if (vsb.getExperienceList(state.id).size() == 0) {
                logger.warning("Following node is loose and below depth "+state);
                return true;
            }
        }  */

        for (int depth = depthMax - 1; depth >= 0; depth--) {
            List<StateForSearch> statesAtDepth = vsb.getAllStatesAtDepth(depth);
            for (StateForSearch state : statesAtDepth) {
                if (vsb.getExperienceList(state.id).size() == 0) {
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


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Visited states buffer");
        sb.append(System.getProperty("line.separator"));
        for (String stateId : stateVisitsDAO.keySet()) {
            StateForSearch state=  getState(stateId);
            sb.append(state.searchSpecificPropertiesAsString());
            sb.append("; experience:"+getExperienceList(stateId));

            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();

    }


}
