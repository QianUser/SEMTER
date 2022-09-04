package web;

import org.openqa.selenium.WebDriver;
import web.collector.Collector;
import web.filter.UserWebElementFilter;
import web.filter.WebElementFilter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class State implements Serializable, Iterable<Element> {

    private static final long serialVersionUID = 2709635277278215626L;

    private static final WebElementFilter userWebElementFilter = new UserWebElementFilter();

    private final List<Element> elements;

    public State(WebDriver driver) {
        this.elements = Collector.collect(driver, userWebElementFilter);
    }

    public State() {
        this.elements = new ArrayList<>();
    }

    public State(List<Element> elements) {
        this.elements = elements;
    }

    public List<Element> getElements() {
        return elements;
    }

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public boolean contains(Element element) {
        return elements.contains(element);
    }

    public void remove(Element element) {
        elements.remove(element);
    }

    @Override
    public Iterator<Element> iterator() {
        return elements.iterator();
    }

}
