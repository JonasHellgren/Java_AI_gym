package java_ai_gym.models_common;
import java.util.List;

/** Interface used by Agent classes */

public interface Learnable {
    State getState();
    int chooseBestAction(State state);
    double findMaxQ(State state);
    int chooseRandomAction(List<Integer> actions);
    int chooseAction(double fractionEpisodesFinished,List<Integer> actions);
    void writeMemory(State oldState, Integer Action, Double value);
    double readMemory(State state, int Action);
}
