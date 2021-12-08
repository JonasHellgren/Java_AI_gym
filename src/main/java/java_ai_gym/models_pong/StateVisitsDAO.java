package java_ai_gym.models_pong;

import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.StateForSearch;
import lombok.ToString;

import java.util.*;
import java.util.logging.Logger;

public class StateVisitsDAO implements  DAO<StateForSearch> {

    protected final static Logger logger = Logger.getLogger(StateVisitsDAO.class.getName());

    Map<String, StateForSearch> stateBuffer;   //<id,stat>
    List<String> idList;

    public StateVisitsDAO() {
        stateBuffer = new HashMap<>();
        idList = new ArrayList<>();
    }

    @Override
    public void clear() {
        stateBuffer.clear();
        idList.clear();
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
        idList.add(id);
    }

    @Override
    public Set<String> keySet() {
        return stateBuffer.keySet();
    }

    public void remove(String id) {
        if (!contains(id)) {
            logger.warning("Id not present = " +id);
        } else {
            stateBuffer.remove(id);
            idList.remove(id);
        }
    }

    public boolean contains(String id) {
        return stateBuffer.containsKey(id);
    }


    public void copy(StateVisitsDAO stateVisitsDAO) {
        for (String id:stateVisitsDAO.keySet()) {
            this.add(id,stateVisitsDAO.get(id));
        }
    }

    @Override
    public String toString() {
        StringBuilder sbBuffer = new StringBuilder();
        sbBuffer.append(System.getProperty("line.separator"));
        sbBuffer.append("sbBuffer =");
        for (String id:stateBuffer.keySet()) {
            sbBuffer.append(id+", ");
        }
        sbBuffer.append(System.getProperty("line.separator"));

        StringBuilder sbIdList = new StringBuilder();
        sbBuffer.append("sbIdList =");
        for (String id:idList) {
            sbIdList.append(id+", ");
        }
        return  sbBuffer.toString()+ sbIdList.toString();

    }


}
