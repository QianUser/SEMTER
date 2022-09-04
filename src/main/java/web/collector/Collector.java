package web.collector;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import web.Element;
import web.filter.WebElementFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Collector {

    public static List<Element> collect(WebDriver driver, WebElementFilter webElementFilter, String... excludedPaths) {
        return collect(driver, "/html/body", true, webElementFilter, excludedPaths);
    }

    public static List<Element> collect(WebDriver driver, boolean addRoot, WebElementFilter webElementFilter, String... excludedPaths) {
        return collect(driver, "/html/body",  addRoot, webElementFilter, excludedPaths);
    }

    public static List<Element> collect(WebDriver driver, String startXpath, WebElementFilter webElementFilter, String... excludedPaths) {
        return collect(driver, startXpath, true, webElementFilter, excludedPaths);
    }

    public static List<Element> collect(WebDriver driver, String startXpath, boolean addRoot, WebElementFilter webElementFilter,  String... excludedPaths) {
        List<Element> elementSet = new ArrayList<>();
        WebElement webElement = driver.findElement(By.xpath(startXpath));
        String xpath = Element.getElementXPath(driver, webElement);
        if (!Arrays.asList(excludedPaths).contains(xpath)) {
            if (addRoot && webElementFilter.accept(webElement)) {
                elementSet.add(new Element(driver, webElement));
            }
            collect(driver, elementSet, webElement, webElementFilter, excludedPaths);
        }
        return elementSet;
    }

    private static void collect(WebDriver driver, List<Element> elementSet, WebElement webElement, WebElementFilter webElementFilter, String... excludedPaths) {
        List<WebElement> webElements = webElement.findElements(By.xpath("./*"));
        for (WebElement childWebElement : webElements) {
            String xpath = Element.getElementXPath(driver, childWebElement);
            if (!Arrays.asList(excludedPaths).contains(xpath)) {
                if (webElementFilter.accept(childWebElement)) {
                    elementSet.add(new Element(driver, childWebElement));
                }
                collect(driver, elementSet, childWebElement, webElementFilter);
            }
        }
    }
}
