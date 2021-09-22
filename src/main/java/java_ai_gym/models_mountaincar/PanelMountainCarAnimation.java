package java_ai_gym.models_mountaincar;

import java_ai_gym.models_sixrooms.SixRoomsAgentNeuralNetwork;
import java_ai_gym.swing.LineData;
import java_ai_gym.swing.Position2D;
import java_ai_gym.swing.ScaleLinear;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.logging.Logger;

public class PanelMountainCarAnimation extends JPanel {

    private static final Logger logger = Logger.getLogger(SixRoomsAgentNeuralNetwork.class.getName());

    ScaleLinear xScaler;
    ScaleLinear yScaler;
    LineData roadData;
    public Position2D carPosition;
    Color carColor;
    double velocity;
    double maxQ;
    public JLabel labelPosX;
    public JLabel labelPosY;
    public JLabel labelVelocity;
    public JLabel  labelMaxQ;

    double CAR_RADIUS;

    public PanelMountainCarAnimation(ScaleLinear xScaler,
                                     ScaleLinear yScaler,
                                     LineData roadData,
                                     Position2D carPosition,
                                     double CAR_RADIUS) {
        this.xScaler = xScaler;
        this.yScaler = yScaler;
        this.roadData = roadData;
        this.carPosition=carPosition;
        this.CAR_RADIUS = CAR_RADIUS;
    }

    public void drawPlot(Graphics2D g2d) {
        plotLine(g2d,roadData.xData,roadData.yData);
        plotCar(g2d,carPosition.x, carPosition.y);
    }

    public void setCarStates(double x, double y,double velocity, int action, double maxQ) {
        carPosition.x=x;   carPosition.y=y; this.velocity = velocity; this.maxQ=maxQ;


        switch (action) {
            case 0:
                carColor = Color.RED;
                break;
            case 1:
                carColor = Color.YELLOW;
                break;
            case 2:
                carColor = Color.GREEN;
                break;
            default:
                carColor = Color.BLACK;
                logger.warning("Non existing action, painting car as black");
                break;
        }


    }


    private void plotLine(Graphics2D g2d, double[] line1x, double[] line1y) {
            ////flip because swing defines y=0 as upper in panel
            g2d.drawPolyline(xScaler.calcOut(line1x),yScaler.calcOut(line1y), line1x.length);
    }

    private void plotCar(Graphics2D g2d,double x,double y) {
        g2d.setColor(carColor);

        g2d.drawOval((int) (xScaler.calcOut(x)+-1*xScaler.scale(CAR_RADIUS)/2),
                (int) (yScaler.calcOut(y)+-1*xScaler.scale(CAR_RADIUS)/2),
                xScaler.scale(CAR_RADIUS),yScaler.scale(CAR_RADIUS));
    }

    private void textCarStates(Graphics2D g2d,double x,double y, double velocity)  {
        labelPosX.setText("pos x:"+new DecimalFormat("#.##").format(x));
        labelPosY.setText("pos y:"+new DecimalFormat("#.##").format(y));
        labelVelocity.setText("velocity:"+new DecimalFormat("#.##").format(velocity));
        labelMaxQ.setText("maxQ:"+new DecimalFormat("#.##").format(maxQ));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);  //cleans the screen
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        drawPlot(g2d);
        textCarStates(g2d,carPosition.x, carPosition.y,velocity);

    }

}
