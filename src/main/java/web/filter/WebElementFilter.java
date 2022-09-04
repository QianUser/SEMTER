package web.filter;

import org.openqa.selenium.WebElement;

public interface WebElementFilter {

    boolean accept(WebElement webElement);

}
