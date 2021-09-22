package java_ai_gym.models_common;

/***
 * This class uses exponential scaling, out=f(in), to relate output to input
 * Can for example be used to define probability for selecting random action
 *
 * out(in)=Out0*exp(-lambda*in)  (1)
 *
 * Boundary conditions:  out(in0)=Out0  (2), out(in1)=Out1  (3)
 *  (1) => ln(out/Out0)=-lambda*in  (4)
 *
 *  (4) and (3)  =>  lambda=-ln(Out1/Out0)/in1
 */
public class ScalerExponential {
    private double lambda;

    public double in0, in1;    //domain, inputs
    public double out0, out1;    //range, outputs

    public ScalerExponential(double in0, double in1, double out0, double out1) {
        this.in0 = in0;
        this.in1 = in1;
        this.out0 = out0;
        this.out1 = out1;
        setScaleParameters(in0, in1, out0, out1);
    }

    public double calcOutDouble(double in) {
        return out0*Math.exp(-lambda*in);
    }

    private void setScaleParameters(double in0, double in1, double out0, double out1) {
        lambda=-Math.log(out1/out0)/in1;
    }



}
