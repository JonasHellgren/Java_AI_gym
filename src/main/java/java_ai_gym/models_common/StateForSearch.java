package java_ai_gym.models_common;

import org.bytedeco.opencv.presets.opencv_core;

public class StateForSearch extends State {

    public final String START_STATE_ID = "start";
    public String id;
    public int depth;
    public int nofActions;

    public StateForSearch() {
        super();
        id=START_STATE_ID;
        depth=0;
        nofActions=0;
    }

    public StateForSearch(State state) {
        this();
        copyState(state);
    }


    public void setIdDepthNofActions(String id,int depth,int nofActions) {
        this.id = id;
        this.depth = depth;
        this.nofActions = nofActions;
    }

    public void setDepthNofActions(int depth,int nofActions) {
        this.depth = depth;
        this.nofActions = nofActions;
    }

    public void copyState(StateForSearch state) {
        super.copyState(state);
        this.id=state.id;
        this.depth=state.depth;
        this.nofActions=state.nofActions;
    }

    @Override
    public String toString() {

        StringBuilder sb=new StringBuilder();

        sb.append(searchSpecificPropertiesAsString());
        sb.append(System.getProperty("line.separator"));
        sb.append(super.toString());
        return sb.toString();
    }

    public String searchSpecificPropertiesAsString() {
        return  "id = "+this.id+", depth = "+this.depth+", nofActions = "+this.nofActions;
    }

}
