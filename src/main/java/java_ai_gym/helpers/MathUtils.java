package java_ai_gym.helpers;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.List;
import java.util.stream.Collectors;

public class MathUtils {

    public static double calcRandomFromIntervall(double minValue, double maxValue) {
        return minValue+Math.random()*(maxValue-minValue);
    }

    public static double clip(double variable, double minValue, double maxValue) {
        double lowerThanMax= Math.min(variable, maxValue);
        return Math.max(lowerThanMax, minValue);
    }


    public static boolean isZero(double value) {
        return (Math.abs(value-0)<2*Double.MIN_VALUE);
    }

    public static double setAsSmallIfZero(double value) {
        return (isZero(value)?Double.MIN_VALUE:value);
    }

    public static String getRoundedNumberAsString(Double value, int nofDigits)  {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(nofDigits);
        return nf.format(value);
    }

    @NotNull
    public static List<Integer> getDifferenceBetweenLists(List<Integer> listA, List<Integer> listB) {
        List<Integer> availableActions= listA.stream()
                .filter(element -> !listB.contains(element))
                .collect(Collectors.toList());
        return availableActions;  //listA-listB
    }

    @NotNull
    public static <T> List<T> getDifferenceBetweenLists2(List<T> listA, List<T> listB) {
        List<T> availableActions= listA.stream()
                .filter(element -> !listB.contains(element))
                .collect(Collectors.toList());
        return availableActions;  //listA-listB
    }


}
