package runner;

import java.io.Serializable;

public class RunResult implements Serializable {

    private static final long serialVersionUID = -6423917190095894862L;

    private boolean success;

    private String failReason;

    public long time;

    public long sleepTime;

    public long ignoredTime;

    public RunResult() {
        this(false, 0);
    }

    public RunResult(boolean success, long time) {
        this.success = success;
        this.failReason = "";
        this.time = time;
        this.sleepTime = 0;
        this.ignoredTime = 0;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getTime() {
        return time;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public long getIgnoredTime() {
        return ignoredTime;
    }

    public RunResult setFailReason(String failReason) {
        this.failReason = failReason;
        return this;
    }

    public String getFailReason() {
        return failReason;
    }

    public RunResult setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }

    public RunResult setIgnoredTime(long ignoredTime) {
        this.ignoredTime = ignoredTime;
        return this;
    }


    public RunResult add(RunResult runResult) {
        success = success && runResult.success;
        time += runResult.time;
        sleepTime += runResult.sleepTime;
        ignoredTime += runResult.ignoredTime;
        return this;
    }

    @Override
    public String toString() {
        return "RunResult{" +
                "success=" + success +
                ", time=" + time +
                ", sleepTime=" + sleepTime +
                ", ignoredTime=" + ignoredTime +
                '}';
    }

}
