package java_ai_gym.models_poleoncart;

import java_ai_gym.models_common.State;
import java_ai_gym.models_sixrooms.SixRoomsAgentNeuralNetwork;
import java_ai_gym.swing.LineData;
import java_ai_gym.swing.Position2D;
import java_ai_gym.swing.ScaleLinear;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.logging.Logger;

public class PanelCartPoleAnimation extends JPanel {

    private static final Logger logger = Logger.getLogger(SixRoomsAgentNeuralNetwork.class.getName());

    ScaleLinear xScaler;
    ScaleLinear yScaler;

    public Position2D cartPosition;
    public double theta;
    public double xDot;
    public double thetaDot;
    public int nofSteps;
    double maxQ;

    Color cartColor;
    Color poleColor;
    public JLabel labelX;
    public JLabel labelTheta;
    public JLabel labelXDot;
    public JLabel labelThetaDot;
    public JLabel labelNofSteps;
    public JLabel labelMaxQ;

    final double CART_WIDTH=0.3;  //in domain
    final double CART_HEIGHT=0.2;
    final int POLE_LENGTH=1;
    final double  CART_Y=0; //(double) CART_HEIGHT/2;   //center of gravity pos

    public PanelCartPoleAnimation(ScaleLinear xScaler,
                                  ScaleLinear yScaler) {
        this.xScaler = xScaler;
        this.yScaler = yScaler;
        cartPosition=new Position2D(0,CART_Y);

        this.theta = 0; this.maxQ=0;
        cartColor = Color.RED;
        poleColor = Color.BLACK;
    }

    public void drawPlot(Graphics2D g2d) {
        plotCart(g2d,cartPosition.x, cartPosition.y);
        plotPole(g2d,cartPosition.x,theta);
    }

    public void setCartPoleStates(State state, int action, double maxQ) {
        //int panelWidth=xScaler.
        cartPosition.x=state.getContinuousVariable("x");
        xDot=state.getContinuousVariable("xDot");
        cartPosition.y=CART_Y;
        this.theta = state.getContinuousVariable("theta");
        this.thetaDot = state.getContinuousVariable("thetaDot");
        this.nofSteps = state.getDiscreteVariable("nofSteps");
        this.maxQ=maxQ;

        switch (action) {
            case 0:
                cartColor = Color.RED;
                break;
            case 1:
                cartColor = Color.GREEN;
                break;
            default:
                cartColor = Color.BLACK;
                logger.warning("Non existing action, painting car as black");
                break;
        }
    }


    private void plotCart(Graphics2D g2d,double x,double y) {

        /***          CART_WIDTH
         *        ______________. (x1,y1)
         *       |      .(x,y) |     CART_HEIGHT
         *      .|_____________|
         *    (x0,y0)
         *
         *    g2d.drawRect(x0,y0,CART_WIDTH,CART_HEIGHT)
         */

        g2d.setColor(cartColor);
        g2d.drawRect(
                xScaler.calcOut(x- CART_WIDTH/2) ,
                yScaler.calcOut(y+ CART_HEIGHT/2) ,
                xScaler.scale(CART_WIDTH),
                yScaler.scale(CART_HEIGHT));
    }

    private void plotPole(Graphics2D g2d, double x, double theta) {

        /***             .  (x1,y1)
         *              /
         *     theta   /
         *          |_/
         *  _______.|/______
           |  (x0,y0)      |
           |_______________|
        */

        g2d.setColor(poleColor);
        double x0=x; double x1=x+POLE_LENGTH*Math.sin(theta);
        double y0=cartPosition.y+CART_HEIGHT/2; double y1=y0+POLE_LENGTH*Math.cos(theta);

        double[] lineX=new double[] {x0,x1};
        double[] lineY=new double[] {y0,y1};

        g2d.drawPolyline(xScaler.calcOut(lineX),yScaler.calcOut(lineY), lineX.length);
    }

    private void textCarStates(Graphics2D g2d,double x,double theta, double maxQ)  {
        labelX.setText("x:"+new DecimalFormat("#.##").format(x));
        labelTheta.setText("theta:"+new DecimalFormat("#.##").format(theta));
        labelXDot.setText("xDot:"+new DecimalFormat("#.##").format(xDot));
        labelThetaDot.setText("thetaDot:"+new DecimalFormat("#.##").format(thetaDot));
        labelNofSteps.setText("nofSteps:"+new DecimalFormat("#.##").format(nofSteps));
        labelMaxQ.setText("maxQ:"+new DecimalFormat("#.##").format(maxQ));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);  //cleans the screen
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        drawPlot(g2d);
        textCarStates(g2d,cartPosition.x, theta,maxQ);

    }

}
