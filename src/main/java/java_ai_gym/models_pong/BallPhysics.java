package java_ai_gym.models_pong;

import java.awt.*;
import java.util.logging.Logger;

public class BallPhysics {
    private static final Logger logger = Logger.getLogger(BallPhysics.class.getName());

    protected double xPos, yPos, xSpd, ySpd;
    SinglePong env;

    public BallPhysics(double xPos, double yPos, double xSpd, double ySpd, SinglePong env) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.xSpd = xSpd;
        this.ySpd = ySpd;
        this.env = env;
    }

    public void updateStates(RacketPhysics racketPhysics) {

        SinglePong.EnvironmentParameters p = env.parameters;
        if (xPos < p.MIN_X_POSITION)
            xSpd = p.SPEED_BALL;
        if (xPos   > p.MAX_X_POSITION)
            xSpd = -p.SPEED_BALL;
        if (yPos > p.MAX_Y_POSITION_BALL)
            ySpd = -p.SPEED_BALL;
        if (collision(racketPhysics)) {
            logger.finest("Ball-racket collision");
            ySpd = p.SPEED_BALL;  }

        xPos  = xPos  + xSpd ;
        yPos  = yPos  + ySpd ;

    }

    public Rectangle getBounds() {

        int r=env.animationPanel.BALL_RADIUS_PIXELS;
        return new Rectangle(
                env.animationPanel.xScaler.calcOut(xPos)-r,
                env.animationPanel.yScaler.calcOut(yPos)-r,
                2*r,
                2*r);
    }

    private boolean collision(RacketPhysics racket) {
        //Returns true if racket and ball collides
        return racket.getBounds().intersects(getBounds());
    }

}
