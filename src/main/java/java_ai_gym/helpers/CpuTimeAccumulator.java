package java_ai_gym.helpers;

import java.util.logging.Logger;

public class CpuTimeAccumulator {

    static final Logger logger = Logger.getLogger(CpuTimeAccumulator.class.getName());

    protected long accumulatedTimeMillis;
    protected long playStartTimeMillis;

    public CpuTimeAccumulator() {
        accumulatedTimeMillis=0;
        playStartTimeMillis=0;

    }

    public void reset() {
        accumulatedTimeMillis=0;
    }

    public void play() {
        playStartTimeMillis = System.currentTimeMillis();
    }

    public void pause() {
        accumulatedTimeMillis=accumulatedTimeMillis+(System.currentTimeMillis() - playStartTimeMillis);
    }

    public long getAccumulatedTimeMillis() {
        return accumulatedTimeMillis;
    }

}
