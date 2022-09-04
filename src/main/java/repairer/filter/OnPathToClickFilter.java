package repairer.filter;

import org.openqa.selenium.WebDriver;
import web.Element;

public interface OnPathToClickFilter {

    boolean accept(WebDriver driver, Element element);

}
