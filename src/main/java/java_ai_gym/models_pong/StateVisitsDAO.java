package java_ai_gym.models_pong;

import java_ai_gym.models_common.StateForSearch;

import java.util.*;
import java.util.logging.Logger;

public class StateVisitsDAO implements  DAO<StateForSearch> {

    protected final static Logger logger = Logger.getLogger(StateVisitsDAO.class.getName());

    Map<String, StateForSearch> stateBuffer;   //<id,stat>


    public StateVisitsDAO() {
        stateBuffer = new HashMap<>();
    }

    public void clear() {
        stateBuffer.clear();
    }

    public StateForSearch get(String id) {
        return stateBuffer.get(id);
    }

    public List<StateForSearch>  getAll () {

        List<StateForSearch> stateList= new ArrayList<>();
        for (String id:keySet()) {
            stateList.add(get(id));
        }
        return stateList;
    }

    public int size() {
        return stateBuffer.size();
    }

    public void add(String id, StateForSearch item) {
        item.id=id;
        stateBuffer.put(id, item);
    }

    public Set<String> keySet() {
        return stateBuffer.keySet();
    }

    public String selectRandomStateId() {

        if (stateBuffer.size() == 0) {
            logger.warning("Empty buffer");
            return "";
        } else {
            return getRandomSetElement(stateBuffer.keySet());
        }
    }

    static <E> E getRandomSetElement(Set<E> set) {
        return set.stream().skip(new Random().nextInt(set.size())).findFirst().orElse(null);
    }



}
