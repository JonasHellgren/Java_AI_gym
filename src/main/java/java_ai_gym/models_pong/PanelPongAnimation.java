package java_ai_gym.models_pong;

import java_ai_gym.swing.Position2D;
import java_ai_gym.swing.ScaleLinear;

import javax.swing.*;
import java.awt.*;

public class PanelPongAnimation extends JPanel {

    final int BALL_RADIUS_PIXELS = 3;
    final int RACKET_WIDTH_PIXELS = 50;
    final int RACKET_HEIGHT_PIXELS = 10;

    ScaleLinear xScaler;
    ScaleLinear yScaler;
    protected Position2D ballPosition;
    protected Position2D racketPos;
    protected Color ballColor = Color.BLUE;
    protected Color racketColor = Color.BLACK;
    protected Color borderColor = Color.BLACK;
    double maxQ;
    public JLabel labelPosX;
    public JLabel labelPosY;

    public PanelPongAnimation(ScaleLinear xScaler,
                              ScaleLinear yScaler) {
        this.xScaler = xScaler;
        this.yScaler = yScaler;
        this.ballPosition = new Position2D(0,0);
        this.racketPos =  new Position2D(0,0);

    }


    public void setStates(Position2D ballPosition, Position2D racketXPos) {
        this.ballPosition = ballPosition;
        this.racketPos = racketXPos;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);  //cleans the screen
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        plotBall(g2d);
        plotRacket(g2d);
        plotBorder(g2d);
        //textBallStates(g2d,carPosition.x, carPosition.y,velocity);
        //textRacketStates
    }

    private void plotBall(Graphics2D g2d) {
        g2d.setColor(ballColor);

        g2d.fillOval(
                xScaler.calcOut(ballPosition.x) - BALL_RADIUS_PIXELS,
                yScaler.calcOut(ballPosition.y) - BALL_RADIUS_PIXELS,
                2*BALL_RADIUS_PIXELS,
                2*BALL_RADIUS_PIXELS);
    }

    private void plotRacket(Graphics2D g2d) {
        g2d.setColor(racketColor);

        g2d.fillRect(
                xScaler.calcOut(racketPos.x) - RACKET_WIDTH_PIXELS / 2,
                yScaler.calcOut(racketPos.y) + RACKET_HEIGHT_PIXELS / 2,
                RACKET_WIDTH_PIXELS,
                RACKET_HEIGHT_PIXELS);
    }

    private void plotBorder(Graphics2D g2d) {
        g2d.setColor(borderColor);

        Position2D lowerLeft=new Position2D(xScaler.d0,yScaler.d0);
        Position2D upperLeft=new Position2D(xScaler.d0,yScaler.d1);
        Position2D upperRight=new Position2D(xScaler.d1,yScaler.d1);
        Position2D lowerRight=new Position2D(xScaler.d1,yScaler.d0);

        System.out.println(upperLeft);
        System.out.println(lowerRight);

        drawBorderLine(g2d, lowerLeft, upperLeft);
        drawBorderLine(g2d, upperLeft, upperRight);
        drawBorderLine(g2d, upperRight, lowerRight);

    }

    private void drawBorderLine(Graphics2D g2d, Position2D posA, Position2D posB) {
        g2d.drawLine(
                xScaler.calcOut(posA.x),
                yScaler.calcOut(posA.y),
                xScaler.calcOut(posB.x),
                yScaler.calcOut(posB.y));
    }


}
