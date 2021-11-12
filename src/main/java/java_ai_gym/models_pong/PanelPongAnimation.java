package java_ai_gym.models_pong;

import java_ai_gym.swing.Position2D;
import java_ai_gym.swing.ScaleLinear;

import javax.swing.*;
import java.awt.*;

public class PanelPongAnimation extends JPanel {

    final int BALL_RADIUS=10;
    final int RACKET_WIDTH=50;
    final int RACKET_HEIGHT=10;

    ScaleLinear xScaler;
    ScaleLinear yScaler;
    protected Position2D ballPosition;
    protected Position2D racketPos;
    protected Color ballColor=Color.BLUE;
    protected Color racketColor=Color.BLACK;
    double maxQ;
    public JLabel labelPosX;
    public JLabel labelPosY;

    public PanelPongAnimation(ScaleLinear xScaler,
                                     ScaleLinear yScaler,
                                     Position2D ballPosition, Position2D racketPos) {
        this.xScaler = xScaler;
        this.yScaler = yScaler;
        this.ballPosition = ballPosition;
        this.racketPos = racketPos;

    }


    public void setStates(Position2D ballPosition, Position2D racketXPos) {
        this.ballPosition = ballPosition;
        this.racketPos = racketXPos;
    }

    private void plotBall(Graphics2D g2d) {
        g2d.setColor(ballColor);

        g2d.drawOval(
                xScaler.calcOut(ballPosition.x)-BALL_RADIUS,
                yScaler.calcOut(ballPosition.y)-BALL_RADIUS,
                BALL_RADIUS,
                BALL_RADIUS);
    }

    private void plotRacket(Graphics2D g2d) {
        g2d.setColor(racketColor);

        g2d.drawRect(
                xScaler.calcOut(racketPos.x) - RACKET_WIDTH/2 ,
                yScaler.calcOut(racketPos.y) + RACKET_HEIGHT/2 ,
                RACKET_WIDTH,
                RACKET_HEIGHT);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);  //cleans the screen
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        plotBall(g2d);
        plotRacket(g2d);
        //textCarStates(g2d,carPosition.x, carPosition.y,velocity);
    }

}
