package java_ai_gym.models_pong;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

public class ExperiencesDAO implements DAO<StateExperience> {

    protected final static Logger logger = Logger.getLogger(ExperiencesDAO.class.getName());

    Map<String, Map<Integer, StateExperience>> expBuffer;   //<id,experiences >

    public ExperiencesDAO() {
        expBuffer = new HashMap<>();
    }

    @Override
    public StateExperience get(String id) {
        //return (StateExperience) expBuffer.get(id);
        logger.warning("Not defined method");
        return null;
    }


    public Map<Integer, StateExperience> getExperienceList(String id) {
        if (!expBuffer.containsKey(id)) {
            logger.fine("No experience of state:" + id);
            return new HashMap<>();
        }
        return expBuffer.get(id);
    }

    @Override
    public void add(String id, StateExperience exp) {
        if (!expBuffer.containsKey(id)) {
            addStateWithNoExp(id);
        }
        Map<Integer, StateExperience> experiences = expBuffer.get(id);
        experiences.put(exp.action,exp);
    }

    public void addStateWithNoExp(String id)  {
        expBuffer.put(id, new HashMap<>());
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

    public void removeExpItemWithNewStateId(String idNewState) {
        /*
        StateExperience exp= searchExperienceOfSteppingToState(id);
        List<StateExperience> experiences= getExperienceList(id);
        System.out.println("id = "+id+", exp = "+exp);
        int sizeBefore=experiences.size();
        experiences.remove(exp);
        int sizeAfter=experiences.size();  */

        for (String id:expBuffer.keySet()) {
            Map<Integer, StateExperience> experiences=getExperienceList(id);
          //  int sizeBefore=experiences.size();
            StateExperience exp = getStateExperience(idNewState, experiences);
            if (exp != null) {
                experiences.remove(exp);
                //System.out.println("sizeBefore = "+sizeBefore+", sizeAfter = "+experiences.size());
            }
        }


    }

    public StateExperience searchExperienceOfSteppingToState(String idOfInterest) {

        for (String id:expBuffer.keySet()) {
            Map<Integer, StateExperience> experiences= getExperienceList(id);
            StateExperience exp = getStateExperience(idOfInterest, experiences);
            if (exp != null) return exp;
        }

        return new StateExperience(0, 0, false,"");
    }

    @Nullable
    private StateExperience getStateExperience(String idOfInterest, Map<Integer, StateExperience> experiences) {
        for (Integer a: experiences.keySet()) {
            StateExperience exp=experiences.get(a);
            if (exp.idNewState.equals(idOfInterest)) {
                return exp;
            }
        }
        return null;
    }

    public int nofActionsTested(String id) {
        List<Integer> actionSet=  testedActions(id);

        return actionSet.size();
    }

    public List<Integer> testedActions(String id) {
        Map<Integer, StateExperience> experiences = getExperienceList(id);
        Set<Integer> actionSet=new HashSet<>();

        for (Integer a: experiences.keySet()) {
            StateExperience exp=experiences.get(a);
            actionSet.add(exp.action);
        }

        if (actionSet.size() != experiences.size()) {
            logger.warning("Duplicate actions in state: "+id);
        }

        if (actionSet.size() == 0) {
            logger.fine("No actions in state: "+id);
            return new ArrayList<>();
        }

        return new ArrayList<>(actionSet);
    }

    public void copy(ExperiencesDAO experiencesDAO) {
        for (String id:experiencesDAO.keySet()) {
            //for (Map<Integer, StateExperience> exp:experiencesDAO.getExperienceList(id)) {
            Map<Integer, StateExperience> experiences = getExperienceList(id);
            for (Integer a: experiences.keySet()) {
                StateExperience exp=experiences.get(a);
                this.add(id,exp);
            }
        }
    }

}
