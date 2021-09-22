package java_ai_gym.models_poleoncart;

import java_ai_gym.swing.Position2D;
import java_ai_gym.swing.ScaleLinear;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PanelChartPolePlot extends JPanel {
    private static final Logger logger = Logger.getLogger(PanelChartPolePlot.class.getName());

    ScaleLinear xScaler;
    ScaleLinear yScaler;

    public List<Position2D> circlePositionList=new ArrayList<>();
    public List<Integer> actionList=new ArrayList<>();
    int CIRCLE_RADIUS;

    public PanelChartPolePlot(ScaleLinear xScaler,
                              ScaleLinear yScaler,
                              List<Position2D> circlePositionList,
                              List<Integer> actionList,
                              int CIRCLE_RADIUS) {
        this.xScaler = xScaler;
        this.yScaler = yScaler;
        this.circlePositionList=circlePositionList;
        this.actionList = actionList;
        this.CIRCLE_RADIUS = CIRCLE_RADIUS;
    }


    public void setCircleData(List<Position2D> circlePositionList,List<Integer> actionList) {
        this.circlePositionList = circlePositionList;
        this.actionList = actionList;
    }

    private void drawCircles(Graphics2D g2d) {

        int i=0;
        Color col;
        if (circlePositionList.size()==actionList.size()) {
        for  (Position2D circle: circlePositionList) {
            int action = actionList.get(i);
            if (action == 0)
                col = Color.RED;
            else if (action == 1)
                col = Color.BLACK;
            else
                col = Color.GREEN;
            drawCircle(g2d, circle.x, circle.y, col);
            i++;
        }
        }
        else
            logger.warning("circlePositionList and actionList are not equal in length" );

    }

    private void drawCircle(Graphics2D g2d,double x,double y,Color col) {
        g2d.setColor(col);
        g2d.fillOval((int) (xScaler.calcOut(x)+-CIRCLE_RADIUS/2),
                (int) (yScaler.calcOut(y)+-CIRCLE_RADIUS/2),
                CIRCLE_RADIUS,CIRCLE_RADIUS);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);  //cleans the screen
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        drawCircles(g2d);


    }

}
