package statement;

import org.openqa.selenium.WebDriver;

public class NavigateRefreshStatement implements Statement {

    private static final long serialVersionUID = -7434633484620286422L;

    private final int line;

    public NavigateRefreshStatement(int line) {
        this.line = line;
    }

    @Override
    public Object act(WebDriver driver) {
        driver.navigate().refresh();
        return null;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "driver.navigate.refresh()";
    }

}
