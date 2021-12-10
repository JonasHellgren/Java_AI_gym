package java_ai_gym.models_agent_search;

import java_ai_gym.models_common.StateForSearch;
import lombok.Getter;

import java.util.*;
import java.util.logging.Logger;

@Getter
public class StateVisitsDAO implements  DAO<StateForSearch> {

    protected final static Logger logger = Logger.getLogger(StateVisitsDAO.class.getName());

    Map<String, StateForSearch> stateBuffer;   //<id,stat>
    List<String> idList;
    Map<Integer, List<String>> idListAtDepth;   //<depth,ids>

    public StateVisitsDAO() {
        stateBuffer = new HashMap<>();
        idList = new ArrayList<>();
        idListAtDepth = new HashMap<>();
    }

    @Override
    public void clear() {
        stateBuffer.clear();
        idList.clear();
        idListAtDepth.clear();
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
        if (!idListAtDepth.containsKey(item.depth)) {
            idListAtDepth.put(item.depth,new ArrayList<>());
        }
        idListAtDepth.get(item.depth).add(id);
    }

    @Override
    public Set<String> keySet() {
        return stateBuffer.keySet();
    }



    public List<String> getAllIdsAtDepth(int depth) {
        if (idListAtDepth==null) {
            logger.warning("idListAtDepth is null");
            return  new ArrayList<>();
        } else {
            if (idListAtDepth.containsKey(depth)) {
                return idListAtDepth.get(depth);
            } else {
                return new ArrayList<>();
            }
        }
    }

    public void remove(String id) {
        if (!contains(id)) {
            logger.warning("Id not present = " +id);
        } else {
            stateBuffer.remove(id);
            idList.remove(id);
            removeIdFromIdListAtDepth(id);  //todo test
        }
    }

    private void removeIdFromIdListAtDepth(String id) {
        for(int depth: idListAtDepth.keySet()) {
            idListAtDepth.get(depth).remove(id);
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

        StringBuilder sbIdListAtDepth = new StringBuilder();
        sbBuffer.append("idListAtDepth =");
        for(int depth: idListAtDepth.keySet()) {
            sbIdListAtDepth.append(idListAtDepth.get(depth));
        }

        return  sbBuffer.toString()+ sbIdList.toString()+sbIdListAtDepth.toString();

    }


}
