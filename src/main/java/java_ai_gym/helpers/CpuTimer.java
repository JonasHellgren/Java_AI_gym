package java_ai_gym.helpers;

import lombok.Getter;

import java.util.logging.Logger;

@Getter
public class CpuTimer {

    static final Logger logger = Logger.getLogger(CpuTimer.class.getName());

    long startTimeMillis;  //starting time, long <=> minimum value of 0
    long startTimeNanos;
    protected long timeBudgetMillis;

    public CpuTimer(long timeBudgetMillis) {
        this.timeBudgetMillis=timeBudgetMillis;
        reset();
    }

    public void setTimeBudgetMillis(long time) {
        this.timeBudgetMillis=time;
    }

    public void reset() {
        startTimeNanos = System.nanoTime();
        startTimeMillis = System.currentTimeMillis();
    }

    public long getTimeInMillis() {
        return (System.currentTimeMillis() - startTimeMillis);
    }

    public long getTimeInNanoSeconds() {
        return ((System.nanoTime()- startTimeNanos));
    }

    public boolean isTimeExceeded() {
        return System.currentTimeMillis() > startTimeMillis + timeBudgetMillis;
    }

}
