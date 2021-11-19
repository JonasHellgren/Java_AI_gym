package java_ai_gym.models_pong;

import java_ai_gym.models_common.StateForSearch;

import java.util.*;
import java.util.logging.Logger;

public class ExperiencesDAO implements DAO {

    protected final static Logger logger = Logger.getLogger(ExperiencesDAO.class.getName());



    Map<String, List<StateExperience>> expBuffer;   //<id,list of experiences >

    public ExperiencesDAO() {
        expBuffer = new HashMap<>();
    }

    @Override
    public StateForSearch get(String id) {
        return null;
    }

    @Override
    public void add(String id, StateForSearch state) {

    }

    public List<StateExperience> getExperienceList(String id) {
        if (!expBuffer.containsKey(id)) {
            logger.warning("No experience of state:" + id);
            return new ArrayList<>();
        }
        return expBuffer.get(id);
    }

    public void addExp(String id,  StateExperience exp) {
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



}
