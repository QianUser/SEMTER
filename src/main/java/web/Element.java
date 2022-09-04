package web;

import exception.UnhandledLocatorException;
import org.openqa.selenium.*;
import utils.Pair;
import utils.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public class Element implements Serializable {

    private static final long serialVersionUID = -6277530688204086943L;

    private final String xpath;

    protected final Position position;

    protected final Dimension size;

    protected String text;

    protected byte[] image;

    private Type type;

    protected Locator locator;

    public Element(WebDriver driver, WebElement webElement) {
        this.xpath = getElementXPath(driver, webElement);
        this.position = new Position(webElement.getLocation());
        this.size = new Dimension(webElement.getSize());
        setType(webElement);
        setText(webElement);
        setImage(driver, webElement);
        this.locator = new Locator(Locator.How.XPATH, xpath);
    }

    public Element(WebDriver driver, WebElement webElement, Locator locator) {
        this(driver, webElement);
        this.locator = locator;
    }

    public String getXpath() {
        return xpath;
    }

    public Position getPosition() {
        return position;
    }

    public Dimension getSize() {
        return size;
    }

    public String getText() {
        return text;
    }

    public byte[] getImage() {
        return image;
    }

    public Type getType() {
        return type;
    }

    public Locator getLocator() {
        return locator;
    }

    public void setLocator(Locator locator) {
        this.locator = locator;
    }

    public boolean isTypeMatch(Element element) {
        return type == element.type
                || (type == Type.VIRTUAL_INPUT && element.type == Type.INPUT)
                || (type == Type.INPUT && element.type == Type.VIRTUAL_INPUT);
    }

    public Pair<RelativePosition, Double> getRelativePosition(Element element) {
        if (element == null) {
            return new Pair<>(RelativePosition.OTHER, Double.MAX_VALUE);
        }
        boolean horizontalOverlap = position.x + size.width > element.position.x && element.position.x + element.size.width > position.x;
        boolean verticalOverlap = position.y + size.height > element.position.y && element.position.y + element.size.height > position.y;
        if (!horizontalOverlap && !verticalOverlap) {
            return new Pair<>(RelativePosition.OTHER, Double.MAX_VALUE);
        }
        if (horizontalOverlap && verticalOverlap) {
            return new Pair<>(RelativePosition.OVERLAP, 0.);
        }
        if (horizontalOverlap) {
            if (position.y + size.height <= element.position.y) {
                return new Pair<>(RelativePosition.UP, (double) (element.position.y - position.y));
            } else {
                return new Pair<>(RelativePosition.DOWN, (double) (position.y - element.position.y));
            }
        }
        if (position.x + size.width <= element.position.x) {
            return new Pair<>(RelativePosition.LEFT, (double) (element.position.x - position.x));
        } else {
            return new Pair<>(RelativePosition.RIGHT, (double) (position.x - element.position.x));
        }
    }

    public boolean isPresent(WebDriver driver) {
        try {
            return new Element(driver, toWebElement(driver)).equals(this);
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    public WebElement toWebElement(WebDriver driver) {
        return driver.findElement(By.xpath(xpath));
    }

    public static boolean isParent(Element element1, Element element2) {
        if (element1 == null || element2 == null || element1.getXpath() == null || element2.getXpath() == null) {
            return false;
        }
        return element2.getXpath().contains(element1.getXpath());
    }

    public static boolean relevant(Element element1, Element element2) {
        return isParent(element1, element2) || isParent(element2, element1);
    }

    public static String getElementXPath(WebDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return (String) js
                .executeScript("var getElementXPath = function(element) {" + "return getElementTreeXPath(element);"
                        + "};" + "var getElementTreeXPath = function(element) {" + "var paths = [];"
                        + "for (; element && element.nodeType == 1; element = element.parentNode)  {" + "var index = 0;"
                        + "for (var sibling = element.previousSibling; sibling; sibling = sibling.previousSibling) {"
                        + "if (sibling.nodeType == Node.DOCUMENT_TYPE_NODE) {" + "continue;" + "}"
                        + "if (sibling.nodeName == element.nodeName) {" + "++index;" + "}" + "}"
                        + "var tagName = element.nodeName.toLowerCase();"
                        + "var pathIndex = (\"[\" + (index+1) + \"]\");" + "paths.splice(0, 0, tagName + pathIndex);"
                        + "}" + "return paths.length ? \"/\" + paths.join(\"/\") : null;" + "};"
                        + "return getElementXPath(arguments[0]);", element);
    }

    public static class Position implements Serializable {

        private static final long serialVersionUID = -179511513606984465L;

        private final int x;
        private final int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Position(Point point) {
            this(point.x, point.y);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    public static class Dimension implements Serializable {

        private static final long serialVersionUID = -5726638446029022339L;

        private final int width;
        private final int height;

        public Dimension(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public Dimension(org.openqa.selenium.Dimension dimension) {
            this(dimension.width, dimension.height);
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    public enum RelativePosition implements Serializable {
        UP, DOWN, RIGHT, LEFT, OVERLAP, OTHER
    }

    public static class Locator implements Serializable {

        private static final long serialVersionUID = 5478749820281050009L;

        public How how;

        public String value;

        public Locator(How how, String value) {
            this.how = how;
            this.value = value;
        }

        public Locator(By by) throws NoSuchFieldException, IllegalAccessException, UnhandledLocatorException {
            if (by instanceof By.ById) {
                setByCondition(by, How.ID, "id");
            } else if (by instanceof By.ByName) {
                setByCondition(by, How.NAME, "name");
            } else if (by instanceof By.ByTagName) {
                setByCondition(by, How.TAG_NAME, "tagName");
            } else if (by instanceof By.ByCssSelector) {
                setByCondition(by, How.CSS_SELECTOR, "cssSelector");
            } else if (by instanceof By.ByClassName) {
                setByCondition(by, How.CLASS_NAME, "className");
            } else if (by instanceof By.ByLinkText) {
                setByCondition(by, How.LINK_TEXT, "linkText");
            } else if (by instanceof By.ByPartialLinkText) {
                setByCondition(by, How.PARTIAL_LINK_TEXT, "partialLinkText");
            } else if (by instanceof By.ByXPath) {
                setByCondition(by, How.XPATH, "xpathExpression");
            } else {
                throw new UnhandledLocatorException();
            }
        }

        private void setByCondition(By by, How how, String fieldName) throws IllegalAccessException, NoSuchFieldException {
            this.how = how;
            Field field = by.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            this.value = (String) field.get(by);
        }

        public By getBy() {
            switch (how) {
                case ID: return By.id(value);
                case NAME: return By.name(value);
                case TAG_NAME: return By.tagName(value);
                case CSS_SELECTOR: return By.cssSelector(value);
                case CLASS_NAME: return By.className(value);
                case PARTIAL_LINK_TEXT: return By.partialLinkText(value);
                case LINK_TEXT: return By.linkText(value);
                case XPATH: return By.xpath(value);
            }
            throw new RuntimeException();
        }

        public WebElement toWebElement(WebDriver driver) {
            return driver.findElement(getBy());
        }

        public List<WebElement> toWebElements(WebDriver driver) {
            return driver.findElements(getBy());
        }

        public enum How implements Serializable {

            ID, NAME, TAG_NAME, CSS_SELECTOR, CLASS_NAME, LINK_TEXT, PARTIAL_LINK_TEXT, XPATH;

            public String toString() {
                switch (this) {
                    case ID: return "id";
                    case NAME: return "name";
                    case TAG_NAME: return "tagName";
                    case CSS_SELECTOR: return "cssSelector";
                    case CLASS_NAME: return "className";
                    case PARTIAL_LINK_TEXT: return "partialLinkText";
                    case LINK_TEXT: return "linkText";
                    case XPATH: return "xpath";
                }
                throw new RuntimeException();
            }

        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Locator &&
                    Objects.equals(how, ((Locator) obj).how) &&
                    Objects.equals(value, ((Locator) obj).value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(how, value);
        }

        public String toString() {
            return "By." + how.toString() + "(\"" + value + "\")";
        }

    }

    public enum Type {
        SELECT, INPUT, VIRTUAL_INPUT, ORDINARY
    }

    public Element getVirtualElement(WebDriver driver) {
        if (type == Type.VIRTUAL_INPUT) {
            WebElement webElement = toWebElement(driver);
            Element element = new Element(driver, webElement);
            element.image = null;
            element.text = webElement.getAttribute("placeholder");
            return element;
        }
        return null;
    }

    private void setText(WebElement webElement) {
        this.text = webElement.getText();
        if (StringUtils.isBlank(this.text)) {
            this.text = webElement.getAttribute("textContent");
        }
        String tagName = webElement.getTagName().toLowerCase();
        String type = webElement.getAttribute("type");
        if (StringUtils.isBlank(this.text) && (tagName.equals("textarea") ||
                (tagName.equals("input") && !type.equals("radio") && !type.equals("checkbox")))) {
            this.text = webElement.getAttribute("value");
        }
        if (StringUtils.isBlank(this.text)) {
            this.text = null;
        } else {
            this.text = this.text.trim();
        }
    }

    private void setImage(WebDriver driver, WebElement webElement) {
        if (this.text == null) {
            try {
                String placeHolder = webElement.getAttribute("placeholder");
                if (type == Type.INPUT && !StringUtils.isBlank(placeHolder)) {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript("arguments[0].setAttribute('placeholder', '')", webElement);
                    type = Type.VIRTUAL_INPUT;
                    this.image = webElement.getScreenshotAs(OutputType.BYTES);
                    js.executeScript("arguments[0].setAttribute('placeholder', '" + placeHolder + "')", webElement);
                } else {
                    this.image = webElement.getScreenshotAs(OutputType.BYTES);
                }
            } catch (WebDriverException exception) {
                this.image = null;
            }
        } else {
            this.image = null;
        }
    }

    private void setType(WebElement webElement) {
        switch (webElement.getTagName().toLowerCase()) {
            case "select":
                type = Type.SELECT;
                break;
            case "input":
            case "textarea":
                type = Type.INPUT;
                break;
            default:
                type = Type.ORDINARY;
                break;
        }
    }

}
