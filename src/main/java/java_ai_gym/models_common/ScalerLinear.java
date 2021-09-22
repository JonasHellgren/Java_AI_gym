package java_ai_gym.models_common;

/***
 * This class uses linear scaling, out=m*in+b, to relate output to input
 * Used to normalize network inputs
 */
public class ScalerLinear {
    private double b;
    private double m;

    public double d0, d1;    //domain, inputs
    public double r0, r1;    //range, outputs

    public ScalerLinear(double d0, double d1, double r0, double r1) {
        this.d0 = d0;
        this.d1 = d1;
        this.r0 = r0;
        this.r1 = r1;
        setScaleParameters(d0, d1, r0, r1);
    }

    public double calcOutDouble(double in) {
        return m * in + b;
    }

    private void setScaleParameters(double d0, double d1, double r0, double r1) {
        //solution to  r0=m*d0+b;  r1=m*d1+b assuming d0, d1, r0 and r1 as known
        m = (r1 - r0) / (d1 - d0);
        b = r0 - m * d0;
    }


}
