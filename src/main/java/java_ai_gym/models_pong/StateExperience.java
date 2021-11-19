package java_ai_gym.models_pong;

public class StateExperience {
    int action;
    double reward;
    boolean termState;
    String idNewState;

    public StateExperience(int action, double reward, boolean termState, String idNewState) {
        this.action = action;
        this.reward = reward;
        this.termState = termState;
        this.idNewState = idNewState;
    }

    @Override
    public String toString() {
        return "(a=" + action + ", r=" + reward + ", ts=" + termState + ", idNew=" + idNewState + ")";
    }
}


