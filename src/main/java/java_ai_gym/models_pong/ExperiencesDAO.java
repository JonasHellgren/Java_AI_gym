package java_ai_gym.models_pong;

import java_ai_gym.models_common.StateForSearch;

import java.util.*;
import java.util.logging.Logger;

public class ExperiencesDAO implements DAO<StateExperience> {

    protected final static Logger logger = Logger.getLogger(ExperiencesDAO.class.getName());

    Map<String, List<StateExperience>> expBuffer;   //<id,list of experiences >

    public ExperiencesDAO() {
        expBuffer = new HashMap<>();
    }

    @Override
    public StateExperience get(String id) {
        //return expBuffer.get(id);
        return null;
    }

    /*
    @Override
    public void add(String id, StateForSearch item) {
    }  */

    public List<StateExperience> getExperienceList(String id) {
        if (!expBuffer.containsKey(id)) {
            logger.fine("No experience of state:" + id);
            return new ArrayList<>();
        }
        return expBuffer.get(id);
    }

    @Override
    public void add(String id, StateExperience exp) {
        if (!expBuffer.containsKey(id)) {
            expBuffer.put(id, new ArrayList<>());
        }
        List<StateExperience> expList = expBuffer.get(id);
        expList.add(exp);
        expBuffer.put(id, expList);
    }

    public void addStateWithNoExp(String id)  {
        expBuffer.put(id, new ArrayList<>());
    }

    @Override
    public void clear() {
        expBuffer.clear();
    }

    @Override
    public int size() {
        return expBuffer.size();
    }

    @Override
    public Set<String> keySet() {
        return expBuffer.keySet();
    }

    public StateExperience searchExperienceOfSteppingToState(String idOfInterest) {

        for (String id:expBuffer.keySet()) {
            List<StateExperience> expList= getExperienceList(id);
            for (StateExperience exp:expList) {
                if (exp.idNewState.equals(idOfInterest)) {
                    return exp;
                }
            }
        }

        return new StateExperience(0, 0, false,"");
    }

    public int nofActionsTested(String id) {
        List<StateExperience> expList = getExperienceList(id);
        Set<Integer> actionSet=new HashSet<>();

        for (StateExperience exp:expList) {
            actionSet.add(exp.action);
        }


        if (actionSet.size() != expList.size()) {
            logger.warning("Duplicate actions in state: "+id);
        }

        return actionSet.size();
    }

}
