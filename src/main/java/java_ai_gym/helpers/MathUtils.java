package java_ai_gym.helpers;

import java.text.NumberFormat;

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


}
