package statement;

import org.openqa.selenium.WebDriver;

public class DriverGetStatement implements Statement {

    private static final long serialVersionUID = -2794997832554477244L;

    private final int line;

    public DriverGetStatement(int line, String url) {
        this.line = line;
        this.url = url;
    }

    private final String url;

    public String getUrl() {
        return url;
    }

    @Override
    public Object act(WebDriver driver) {
        driver.get(url);
        return null;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "driver.get(\"" + url + "\")";
    }

}
