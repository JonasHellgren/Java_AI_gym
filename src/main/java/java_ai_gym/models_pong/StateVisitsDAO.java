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

    @Override
    public void clear() {
        stateBuffer.clear();
    }

@Override
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

    @Override
    public int size() {
        return stateBuffer.size();
    }

    @Override
    public void add(String id, StateForSearch item) {
        item.id=id;
        stateBuffer.put(id, item);
    }

    @Override
    public Set<String> keySet() {
        return stateBuffer.keySet();
    }

    public void remove(String id) {
        stateBuffer.remove(id);
    }

    public boolean contains(String id) {
        return stateBuffer.containsKey(id);
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


    public void copy(StateVisitsDAO stateVisitsDAO) {
        for (String id:stateVisitsDAO.keySet()) {
            this.add(id,stateVisitsDAO.get(id));
        }
    }
}
