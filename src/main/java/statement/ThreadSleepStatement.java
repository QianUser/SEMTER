package statement;

import org.openqa.selenium.WebDriver;

public class ThreadSleepStatement implements Statement {

    private static final long serialVersionUID = 9181523517752204388L;

    private final int line;

    private final long millis;

    public ThreadSleepStatement(int line, long millis) {
        this.line = line;
        this.millis = millis;
    }

    public long getMillis() {
        return millis;
    }

    @Override
    public Object act(WebDriver driver) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "Thread.sleep(" + millis + ")";
    }

}
