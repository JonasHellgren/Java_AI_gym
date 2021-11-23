package java_ai_gym.models_pong;

import org.jetbrains.annotations.Nullable;

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
        //return (StateExperience) expBuffer.get(id);
        logger.warning("Not defined method");
        return null;
    }


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

    public void removeExpItemWithNewStateId(String idNewState) {
        /*
        StateExperience exp= searchExperienceOfSteppingToState(id);
        List<StateExperience> expList= getExperienceList(id);
        System.out.println("id = "+id+", exp = "+exp);
        int sizeBefore=expList.size();
        expList.remove(exp);
        int sizeAfter=expList.size();  */

        for (String id:expBuffer.keySet()) {
            List<StateExperience> expList=getExperienceList(id);
            int sizeBefore=expList.size();
            StateExperience exp = getStateExperience(idNewState, expList);
            if (exp != null) {
                expList.remove(exp);
                //System.out.println("sizeBefore = "+sizeBefore+", sizeAfter = "+expList.size());
            }
        }


    }

    public StateExperience searchExperienceOfSteppingToState(String idOfInterest) {

        for (String id:expBuffer.keySet()) {
            List<StateExperience> expList= getExperienceList(id);
            StateExperience exp = getStateExperience(idOfInterest, expList);
            if (exp != null) return exp;
        }

        return new StateExperience(0, 0, false,"");
    }

    @Nullable
    private StateExperience getStateExperience(String idOfInterest, List<StateExperience> expList) {
        for (StateExperience exp: expList) {
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
        List<StateExperience> expList = getExperienceList(id);
        Set<Integer> actionSet=new HashSet<>();

        for (StateExperience exp:expList) {
            actionSet.add(exp.action);
        }

        if (actionSet.size() != expList.size()) {
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
            for (StateExperience exp:experiencesDAO.getExperienceList(id)) {
                this.add(id,exp);
            }
        }
    }

}
