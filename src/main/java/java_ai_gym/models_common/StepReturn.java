package java_ai_gym.models_common;

/** This class is used by the Environment class.
 *  It defines the return of the step method.
 *
*/

public  class StepReturn {
    public State state;
    public Double reward;
    public Boolean termState;

    public StepReturn(State state, Double reward, Boolean termState) {
        this.state = state;
        this.reward = reward;
        this.termState = termState;
    }

    public StepReturn() {
        this.state = new State();
        this.reward = 0.0;
        this.termState = false;
    }

    @Override
    public String toString() {
        return "StepReturnAbstract{" +
                "state=" + state +
                ", reward=" + reward +
                ", termState=" + termState +
                '}';
    }
}
