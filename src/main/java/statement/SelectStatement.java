package statement;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import web.Element;

public class SelectStatement implements Statement {

    private static final long serialVersionUID = -4859279047917807381L;

    private final int line;

    private final Element element;

    private final String selected;

    public SelectStatement(int line, WebDriver driver, Select select, String selected) {
        this.line = line;
        this.element = new Element(driver, select.getWrappedElement());
        this.selected = selected;
    }

    @Override
    public Object act(WebDriver driver) {
        new Select(element.toWebElement(driver)).selectByVisibleText(selected);
        return null;
    }

    @Override
    public int getLine() {
        return line;
    }

    public String getSelected() {
        return selected;
    }

    @Override
    public String toString() {
        return ".selectByVisibleText(" + selected + ")";
    }

}
