package java_ai_gym.swing;

//range:   0...r0............r1...rMax   where r0-0=rMax-r1=Margin
public class ScaleLinear {
//This function uses linear scaling out=m*in+b to relate output to input

    private double b;
    private double m;

    public double d0,d1;    //domain, inputs
    public double r0,r1;    //range, outputs

    int rMax;
    final  int H=300;  //frame size

    boolean flip; //true <=> flip range <=> for ex d0 corresponds to r1
    //false <=> not flip range <=> for ex d0 corresponds to r0
    
    public ScaleLinear(double d0, double d1, int r0, int r1,boolean flip, int MARGIN) {
        this.d0 = d0;
        this.d1 = d1;
        this.r0 = r0;
        this.r1 = r1;
        this.flip = flip;
        this.rMax= r1+MARGIN;

        setScaleParameters(d0, d1, r0, r1);
    }


    public int calcOut(double in) {
        double y=m*in+b;
        return (flip)?(int) (rMax-y):(int) (y);
    }

    public double calcOutDouble(double in) {
        double y=m*in+b;
        return (flip)? (rMax-y): (y);
    }

    public int scale(double sizeInDomain) {
        return (int) (sizeInDomain*(r1-r0)/(d1-d0));
    }


    public int[] calcOut(double[] inVec) {

        int[] outVec=new int[inVec.length];
        for (int i = 0; i < inVec.length; i++) {
            double y=m*inVec[i]+b;
            outVec[i]=(flip)? (int) (rMax-y):(int) y;

        }
        return outVec;
    }

    private void setScaleParameters(double d0, double d1, double r0, double r1) {
        //solution to  r0=m*d0+b;  r1=m*d1+b assuming d0, d1, r0 and r1 as known
        m=(r1-r0)/(d1-d0);
        b=r0-m*d0;
    }

}

