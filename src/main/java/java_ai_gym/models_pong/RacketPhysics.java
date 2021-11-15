package java_ai_gym.models_pong;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class RacketPhysics {

    private static final Logger logger = Logger.getLogger(RacketPhysics.class.getName());

    protected double xPos, xSpd;
    SinglePong env;
    List<Double> speedSet;

    public RacketPhysics(double xPos, SinglePong env) {
        this.xPos = xPos;
        this.xSpd = 0d;
        this.env = env;
        speedSet= Arrays.asList(-env.parameters.MAX_SPEED_RACKET,0d,env.parameters.MAX_SPEED_RACKET);
    }

    public void updateStates(int action) {

        if (action<0 || action>speedSet.size()-1) {
            logger.warning("Bad action value");
        } else
        {
            xSpd=speedSet.get(action);
            if (xPos + xSpd > env.parameters.MIN_X_POSITION && xPos + xSpd < env.parameters.MAX_X_POSITION)
                xPos = xPos + xSpd;
        }

    }

    public Rectangle getBounds() {
        SinglePong.EnvironmentParameters p = env.parameters;
        return new Rectangle(
                env.animationPanel.xScaler.calcOut(xPos)- env.animationPanel.RACKET_WIDTH_PIXELS / 2,
                env.animationPanel.yScaler.calcOut(env.parameters.Y_POSITION_RACKET)+env.animationPanel.RACKET_HEIGHT_PIXELS / 2,
                env.animationPanel.RACKET_WIDTH_PIXELS,
                env.animationPanel.RACKET_HEIGHT_PIXELS);
    }

}
