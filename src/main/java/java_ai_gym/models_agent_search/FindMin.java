package java_ai_gym.models_agent_search;

import java.util.List;
import java.util.OptionalDouble;

public class FindMin implements Strategy {
    @Override
    public double findBestInList(List<Double> numbers) {
        logMessageIfEmptyList(numbers);
        OptionalDouble min = numbers.stream().mapToDouble(v -> v).min();
        return min.orElse(BEST_IF_EMPTY_LIST);
    }

    @Override
    public boolean isFirstBetterThanSecond(double num1, double num2) {
        return (num1<num2);
    }

    @Override
    public double badNumber() {
        return Double.MAX_VALUE;
    }
}
