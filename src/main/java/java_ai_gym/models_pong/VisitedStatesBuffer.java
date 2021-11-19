package java_ai_gym.models_pong;

import java_ai_gym.models_common.StateForSearch;
import java_ai_gym.models_common.StepReturn;

import java.util.*;
import java.util.logging.Logger;

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

    public void clear() {
        stateVisitsDAO.clear();
        experiencesDAO.clear();
    }

    public StateForSearch getState(String id) {
        return stateVisitsDAO.get(id);
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


    public void addNewStateAndExperienceFromStep(String idFromState, int action, StepReturn stepReturn) {
        String newId = idFromState + "." + action;
        System.out.println("newId ="+newId);
        addState(newId, (StateForSearch) stepReturn.state);
        StateExperience stateExperience = new StateExperience(action, stepReturn.reward, stepReturn.termState, newId);
        addExperience(idFromState, stateExperience);
    }

    public void addExperience(String id, StateExperience exp) {
        experiencesDAO.addExp(id,exp);
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
