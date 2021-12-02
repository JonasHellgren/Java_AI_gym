package java_ai_gym.helpers;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.List;
import java.util.Random;
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

    public static boolean isNeg(double value) {
        return value<-Double.MIN_VALUE;
    }

    public static boolean isPos(double value) {
        return value>Double.MIN_VALUE;
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


    public static int randInt(int min, int max) {

        // NOTE: This will (intentionally) not run as written so that folks
        // copy-pasting have to think about how to initialize their
        // Random instance.  Initialization of the Random instance is outside
        // the main scope of the question, but some decent options are to have
        // a field that is initialized once and then re-used as needed or to
        // use ThreadLocalRandom (if using at least Java 1.7).
        //
        // In particular, do NOT do 'Random rand = new Random()' here or you
        // will get not very good / not very random results.

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        Random rand = new Random();

        return rand.nextInt((max - min) + 1) + min;
    }

}
