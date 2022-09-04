package statement;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utils.StringUtils;
import web.Context;
import web.Element;
import web.Page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ElementStatement implements Statement {

    private static final long serialVersionUID = -4900357817252676654L;

    private final int line;

    private final Element element;

    private final Context context;

    private final Page page;

    private final List<String> correctXpaths = new ArrayList<>();

    public ElementStatement(int line, WebDriver driver, Element element) {
        this(line, driver, element, true);
    }

    public ElementStatement(int line, WebDriver driver, Element element, boolean trace) {
        this.line = line;
        this.element = element;
        if (trace) {
            this.context = new Context(driver, element);
            this.page = new Page(driver);
        } else {
            this.context = null;
            this.page = null;
        }
    }

    public Element getElement() {
        return element;
    }

    public Context getContext() {
        return context;
    }

    public Page getPage() {
        return page;
    }

    public void addElementXPath() {
        correctXpaths.add(element.getXpath());
    }

    @Override
    public WebElement act(WebDriver driver) {
        // Implementation issues
        try {
            return element.toWebElement(driver);
        } catch (NoSuchElementException e) {
            return element.getLocator().toWebElement(driver);
        }
    }

    @Override
    public int getLine() {
        return line;
    }

    public List<String> getCorrectXpaths() {
        return correctXpaths;
    }

    public void addCorrectXPath(Iterable<String> xpaths) {
        xpaths.forEach(correctXpaths::add);
    }

    public void addCorrectXPath(String... xpaths) {
        correctXpaths.addAll(Arrays.asList(xpaths));
    }

    @Override
    public String toString() {
        return "driver.findElement(" + element.getLocator().toString() + ")";
    }

}