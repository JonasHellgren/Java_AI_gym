package java_ai_gym.helpers;

public class CpuTimer {

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
        return ((System.nanoTime()- startTimeNanos)/1000000);
    }

    public long getTimeInNanoSeconds() {
        return ((System.nanoTime()- startTimeNanos));
    }

    public boolean isTimeExceeded() {
        return System.currentTimeMillis() > startTimeMillis + timeBudgetMillis;
    }

}
