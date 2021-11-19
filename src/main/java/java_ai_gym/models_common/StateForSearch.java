package java_ai_gym.models_common;

import org.bytedeco.opencv.presets.opencv_core;

public class StateForSearch extends State {

    public String id;
    public int depth;
    public int nofActions;

    public StateForSearch() {
        super();
    }

    public StateForSearch(State state) {
        super(state);
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
        return  "id ="+this.id+", depth ="+this.depth+", nofActions ="+this.nofActions;
    }

}
