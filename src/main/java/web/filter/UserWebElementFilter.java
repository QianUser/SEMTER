package web.filter;

import org.openqa.selenium.WebElement;

public class UserWebElementFilter implements WebElementFilter {

    @Override
    public boolean accept(WebElement webElement) {
        String tagName = webElement.getTagName();
        return webElement.isDisplayed() && webElement.isEnabled() && !tagName.equalsIgnoreCase("br") && !tagName.equalsIgnoreCase("hr");
    }

}
