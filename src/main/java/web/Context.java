package web;

import org.openqa.selenium.WebDriver;
import runner.Executor;
import utils.Pair;
import utils.StringUtils;
import web.Element.RelativePosition;
import web.collector.Collector;
import web.filter.UserWebElementFilter;
import web.filter.WebElementFilter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.StringUtils.occurTimes;

public class Context implements Serializable {

    private static final long serialVersionUID = 2562713050751259778L;

    private static final WebElementFilter webElementFilter = new UserWebElementFilter();

    private final List<Element> context;

    public Context(WebDriver driver, Element element) {
       if (Executor.repairMode == Executor.RepairMode.CONTEXT) {
           this.context = collect(driver, element);
       } else {
           this.context = new ArrayList<>();
       }
    }

    public Context(WebDriver driver, Element element, State state) {
        if (Executor.repairMode == Executor.RepairMode.CONTEXT) {
            this.context = collect(driver, element, state);
        } else {
            this.context = new ArrayList<>();
        }
    }

    public Element get(int index) {
        return context.get(index);
    }

    public int size() {
        return context.size();
    }

    public boolean isEmpty() {
        return context.isEmpty();
    }

    private static List<Element> collect(WebDriver driver, Element element) {
        List<Element> context = new ArrayList<>();
        String path = element.getXpath();
        String prev;
        int cnt = occurTimes(path, '/');
        addIfNotNull(context, element.getVirtualElement(driver));
        while (context.isEmpty() && cnt > 2) {
            prev = path;
            path = path.substring(0, path.lastIndexOf("/"));
            --cnt;
            List<Element> elements = Collector.collect(driver, path, webElementFilter, prev);
            context.addAll(elements);
            reserveBasicElements(context, element, path);
        }
        return filteredContext(context, element);
    }

    private static List<Element> collect(WebDriver driver, Element element, State state) {
        List<Element> context = new ArrayList<>();
        String path = element.getXpath();
        String prev;
        int cnt = occurTimes(path, '/');
        addIfNotNull(context, element.getVirtualElement(driver));
        while (context.isEmpty() && cnt > 2) {
            prev = path;
            path = path.substring(0, path.lastIndexOf("/"));
            --cnt;
            for (Element e : state) {
                if (e.getXpath().contains(path) && !e.getXpath().contains(prev)) {
                    context.add(e);
                }
            }
            reserveBasicElements(context, element, path);
        }
        return filteredContext(context, element);
    }

    private static List<Element> filteredContext(List<Element> context, Element element) {
        List<Element> result = new ArrayList<>();
        Element up = null, down = null, right = null, left = null;
        double dUp = Double.MAX_VALUE, dDown = Double.MAX_VALUE, dRight = Double.MAX_VALUE, dLeft = Double.MAX_VALUE;
        for (Element ce : context) {
            Pair<RelativePosition, Double> pair = ce.getRelativePosition(element);
            switch (pair.first) {
                case OVERLAP:
                    result.add(ce);
                    break;
                case UP:
                    if (pair.second < dUp) {
                        up = ce;
                        dUp = pair.second;
                    }
                    break;
                case DOWN:
                    if (pair.second < dDown) {
                        down = ce;
                        dDown = pair.second;
                    }
                    break;
                case RIGHT:
                    if (pair.second < dRight) {
                        right = ce;
                        dRight = pair.second;
                    }
                    break;
                case LEFT:
                    if (pair.second < dLeft) {
                        left = ce;
                        dLeft = pair.second;
                    }
                    break;
                default:
                    break;
            }
        }
        addIfNotNull(result, up, down, right, left);
        for (Element ce : context) {
            Pair<RelativePosition, Double> pair1 = ce.getRelativePosition(element);
            Pair<RelativePosition, Double> pair2;
            switch (pair1.first) {
                case UP:
                    pair2 = ce.getRelativePosition(up);
                    if ((pair2.first == RelativePosition.RIGHT || pair2.first == RelativePosition.LEFT
                            || pair2.first == RelativePosition.OVERLAP) && ce != up) {
                        result.add(ce);
                    }
                    break;
                case DOWN:
                    pair2 = ce.getRelativePosition(down);
                    if ((pair2.first == RelativePosition.RIGHT || pair2.first == RelativePosition.LEFT
                            || pair2.first == RelativePosition.OVERLAP) && ce != down) {
                        result.add(ce);
                    }
                    break;
                case RIGHT:
                    pair2 = ce.getRelativePosition(right);
                    if ((pair2.first == RelativePosition.UP || pair2.first == RelativePosition.DOWN
                            || pair2.first == RelativePosition.OVERLAP) && ce != right) {
                        result.add(ce);
                    }
                    break;
                case LEFT:
                    pair2 = ce.getRelativePosition(left);
                    if ((pair2.first == RelativePosition.UP || pair2.first == RelativePosition.DOWN
                            || pair2.first == RelativePosition.OVERLAP) && ce != left) {
                        result.add(ce);
                    }
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    private static void addIfNotNull(List<Element> list, Element... elements) {
        for (Element element : elements) {
            if (element != null) {
                list.add(element);
            }
        }
    }

    private static void reserveBasicElements(List<Element> elements, Element element, String rootPath) {
        Map<String, Element> map = new HashMap<>();
        map.put(element.getXpath(), element);
        elements.forEach(i -> map.put(i.getXpath(), i));
        for (Map.Entry<String, Element> entry : map.entrySet()) {
            if (!validImage(element) && !validText(entry.getValue())) {
                elements.remove(entry.getValue());
            }
            String path = entry.getKey();
            path = path.substring(0, path.lastIndexOf("/"));
            Element parent = null;
            while (parent == null && path.contains(rootPath)) {
                parent = map.get(path);
                path = path.substring(0, path.lastIndexOf("/"));
            }
            if (parent != null && (!validText(parent) || validText(entry.getValue()))) {
                elements.remove(parent);
            }
        }
    }

    private static boolean validText(Element element) {
        return element.getText() != null && !StringUtils.isStopWord(element.getText());
    }

    private static boolean validImage(Element element) {
        return element.getImage() != null && element.getImage().length != 0;
    }

}