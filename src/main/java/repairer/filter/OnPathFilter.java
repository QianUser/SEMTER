package repairer.filter;

import org.openqa.selenium.WebDriver;
import web.Element;

public interface OnPathFilter {

    boolean accept(WebDriver driver, Element element);

}
