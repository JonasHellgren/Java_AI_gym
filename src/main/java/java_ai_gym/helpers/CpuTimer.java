package java_ai_gym.helpers;

import lombok.Getter;

import java.util.logging.Logger;

@Getter
public class CpuTimer {

    static final Logger logger = Logger.getLogger(CpuTimer.class.getName());
    long startTimeMillis;  //starting time, long <=> minimum value of 0
    protected long timeBudgetMillis;


    public CpuTimer(long timeBudgetMillis) {
        this.timeBudgetMillis=timeBudgetMillis;
        reset();
    }

    public void setTimeBudgetMillis(long time) {
        this.timeBudgetMillis=time;
    }

    public void reset() {
        startTimeMillis = System.currentTimeMillis();
   }


    public long getTimeSinceStartInMillis() {
        return (System.currentTimeMillis() - startTimeMillis);
    }


    public boolean isTimeExceeded() {
        return System.currentTimeMillis() > startTimeMillis + timeBudgetMillis;

    }

}
