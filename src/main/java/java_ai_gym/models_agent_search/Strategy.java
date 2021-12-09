package java_ai_gym.models_agent_search;

import java.util.List;
import java.util.logging.Logger;

public interface  Strategy {

    double BEST_IF_EMPTY_LIST=0d;
    Logger logger = Logger.getLogger(Strategy.class.getName());


    default void logMessageIfEmptyList(List<Double> numbers) {
        if (numbers.isEmpty()) {
            logger.warning("Finding min or max of empty list");
        }
    }

    double findBestInList(List<Double> numbers);
    boolean isFirstBetterThanSecond(double num1, double num2);
    double badNumber();
}
