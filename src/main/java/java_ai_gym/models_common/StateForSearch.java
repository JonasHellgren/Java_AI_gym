package java_ai_gym.models_common;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    @Override
    public String toString() {

        StringBuilder sb=new StringBuilder();

        sb.append("id ="+this.id+", depth ="+this.depth+", nofActions ="+this.nofActions);
        sb.append(System.getProperty("line.separator"));
        sb.append(super.toString());
        return sb.toString();
    }

}
