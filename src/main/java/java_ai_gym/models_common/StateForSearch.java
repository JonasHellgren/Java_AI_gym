package java_ai_gym.models_common;

import org.bytedeco.opencv.presets.opencv_core;

public class StateForSearch extends State {

    public final String START_STATE_ID = "start";
    public final double INIT_VALUE=0;
    public String id;
    public int depth;
    public int nofActions;
    public double value;

    public StateForSearch() {
        super();
        id=START_STATE_ID;
        depth=0;
        nofActions=0;
        value=INIT_VALUE;
    }

    public StateForSearch(StateForSearch state) {
        this();
        this.copyState(state);

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
        this.value=state.value;
    }

    @Override
    public String toString() {

        StringBuilder sb=new StringBuilder();

        sb.append(searchSpecificPropertiesAsString());

        sb.append(super.toString());
        sb.append(System.getProperty("line.separator"));
        return sb.toString();
    }

    public String searchSpecificPropertiesAsString() {
        return  "id = "+this.id+", depth = "+this.depth+", value = "+this.value; //+", nofActions = "+this.nofActions;
    }

}
