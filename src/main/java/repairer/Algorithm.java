package repairer;

import model.Model;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import repairer.filter.OnPathFilter;
import repairer.filter.OnPathToClickFilter;
import utils.Pair;
import web.Context;
import web.Element;
import web.Page;
import web.State;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class Algorithm {

    public static double threshold = 0.6;

    public static double move = 0.75;

    private final static int pageNum = 3;

    private final static double pageThreshold = 0.3;

    private static State lastState = null;

    static boolean invalidState = true;

    static OnPathFilter onPathFilter;

    static OnPathToClickFilter onPathToClickFilter;

    public static Pair<Element, Element> getElementOnPath(WebDriver driver, Element oe, Context oc, Page op, Page np, boolean addCurrentState) {
        if (onPathFilter != null && !onPathFilter.accept(driver, oe)) {
            return null;
        }
        String url = driver.getCurrentUrl();
        if (invalidState && addCurrentState) {  // Logically, if invalidState is true, addCurrentState must be true
            lastState = new State(driver);
            invalidState = false;
        }
        List<Element> elementsForIter;
        if (onPathToClickFilter == null) {
            elementsForIter = lastState.getElements();
        } else {
            elementsForIter = lastState.getElements().stream().filter(i -> onPathToClickFilter.accept(driver, i)).collect(Collectors.toList());
        }
        Map<Page, Element> pageLocatorMap = new HashMap<>();
        Set<String> oldWindows = driver.getWindowHandles();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.open()");
        String oldWindow = driver.getWindowHandle();
        String newWindow = getNewWindow(oldWindows, driver.getWindowHandles());
        driver.switchTo().window(newWindow);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofMillis(1000));
        boolean isGet = true;
        Page p1 = null;
        for (Element ne : elementsForIter) {
            try {
                if (isGet) {
                    driver.get(url);
                    p1 = new Page(driver);
                }
                ne.toWebElement(driver).click();
                Page p2 = new Page(driver);
                if (!p1.equals(p2)) {
                    pageLocatorMap.put(p2, ne);
                    isGet = true;
                } else {
                    isGet = false;
                }
            } catch (Exception e) {
                isGet = true;
            }
        }
        if (addCurrentState) {
            pageLocatorMap.put(np, null);
        }
        List<Pair<Page, Double>> pageSimList = new ArrayList<>();
        pageLocatorMap.forEach((key, value) -> pageSimList.add(new Pair<>(key, getPageSimilarity(key, op))));
        List<Page> selectedPages = pageSimList.stream().sorted(Comparator.comparing(o -> -o.second)).
                limit(pageNum).map(i -> i.first).collect(Collectors.toList());
        if (addCurrentState && !selectedPages.contains(np)) {
            selectedPages.add(np);
        }
        for (Page page : selectedPages) {
            Element pe = pageLocatorMap.get(page);
            if (pe == null) {
                driver.switchTo().window(oldWindow);
            } else {
                driver.switchTo().window(newWindow);
                driver.get(url);
                try {
                    pe.toWebElement(driver).click();
                } catch (Exception ignored) {}
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Element ne = getElementOnState(driver, oe, oc, pe != null);
            if (ne != null) {
                driver.switchTo().window(newWindow);
                js.executeScript("window.close()");
                driver.switchTo().window(oldWindow);
                return new Pair<>(pe, ne);
            }
        }
        driver.manage().timeouts().pageLoadTimeout(Duration.ofMillis(65535));
        driver.switchTo().window(newWindow);
        js.executeScript("window.close()");
        driver.switchTo().window(oldWindow);
        return null;
    }

    public static Element getElementOnState(WebDriver driver, Element oe, Context oc) {
        return getElementOnState(driver, oe, oc, false);
    }

    public static Element getElementOnState(WebDriver driver, Element oe, Context oc, boolean fromPath) {
        List<WebElement> webElements = oe.getLocator().toWebElements(driver);
        Element ne = getElementOnLocator(driver, oe,
                webElements.stream().map(i -> new Element(driver, i)).collect(Collectors.toList()), oc);
        return ne == null ? searchElementOnState(driver, oe, oc, fromPath) : ne;
    }

    public static Element getElementOnLocator(WebDriver driver, Element oe, List<Element> nes, Context oc) {
        if (nes.size() == 1 && getElementSimilarity(oe, nes.get(0)) >= threshold) {
            nes.get(0).setLocator(oe.getLocator());
            return nes.get(0);
        }
        double maxS = 0;
        Element maxE = null;
        for (Element ne : nes) {
            Context nc = new Context(driver, ne);
            double sim = getContextSimilarity(oe, ne, oc, nc);
            if (sim > maxS && sim >= threshold) {
                maxS = sim;
                maxE = ne;
            }
        }
        return maxE;
    }

    public static Element searchElementOnState(WebDriver driver, Element oe, Context oc, boolean fromPath) {
        State ns;
        if (invalidState || fromPath) {
            ns = new State(driver);
            setCaches(ns);
        } else {
            ns = lastState;
        }
        if (invalidState && !fromPath) {
            lastState = ns;
        }
        double maxS = 0;
        Element maxE = null;
        for (Element ne : ns) {
            Context nc = new Context(driver, ne, ns);
            double sim = getContextSimilarity(oe, ne, oc, nc);
            if (!(Element.relevant(maxE, ne) && oe.isTypeMatch(maxE) && !oe.isTypeMatch(ne))) {
                if (sim >= threshold && Element.relevant(maxE, ne) && !oe.isTypeMatch(maxE) && oe.isTypeMatch(ne)) {
                    maxE = ne;
                } else if (sim >= threshold && sim >= maxS) {
                    maxE = ne;
                }
            }
            if (sim >= maxS) {
                maxS = sim;
            }
        }
        if (!fromPath) {
            invalidState = false;
        }
        return maxE;
    }

    public static boolean isPageMatch(Page op, Page np) {
        return getPageSimilarity(op, np) >= pageThreshold;
    }

    public static double getPageSimilarity(Page op, Page np) {
        if (op.getSize() == 0 && np.getSize() == 0) {
            return 1;
        }
        if (op.getSize() == 0 || np.getSize() == 0) {
            return 0;
        }
        Model.encodeTexts(op.getPage());
        Model.encodeTexts(np.getPage());
        double[][] m = new double[op.getSize()][np.getSize()];
        for (int i = 0; i < op.getSize(); ++i) {
            for (int j = 0; j < np.getSize(); ++j) {
                m[i][j] = Model.getSimilarity(op.get(i), np.get(j));
            }
        }
        int cnt = 0;
        while (getAndSetMax(m) >= threshold) {
            ++cnt;
        }
        return cnt / Math.sqrt(op.getSize() * np.getSize());
    }

    public static double getContextSimilarity(Element oe, Element ne, Context oc, Context nc) {
        double sim = getElementSimilarity(oe, ne);
        if (oc.isEmpty() || nc.isEmpty()) {
            return sim;
        }
        double[][] m = new double[oc.size()][nc.size()];
        for (int i = 0; i < oc.size(); ++i) {
            for (int j = 0; j < nc.size(); ++j) {
                m[i][j] = getElementSimilarity(oc.get(i), nc.get(j));
            }
        }
        double sum = 0;
        double maxMatch;
        while ((maxMatch = getAndSetMax(m)) >= threshold) {
            sum += (maxMatch - threshold);
        }
//        double c = Math.sqrt((double) Math.min(oc.size(), nc.size()) / Math.max(oc.size(), nc.size()));
        double c = 1;
        return sim + move * sum * c;
    }

    public static String getBestOption(String original, List<String> targets) {
        if (targets.contains(original)) {
            return original;
        }
        Model.encodeTexts(targets);
        double maxS = 0;
        String max = null;
        for (String target : targets) {
            double s = Model.getSimilarity(original, target);
            if (s > maxS) {
                max = target;
                maxS = s;
            }
        }
        return max;
    }

    public static double getElementSimilarity(Element oe, Element ne) {
        String text1 = oe.getText();
        String text2 = ne.getText();
        if (text1 != null && text2 != null) {
            return Model.getSimilarity(text1, text2);
        } else if (text1 != null && ne.getImage() != null) {
            return Model.getSimilarity(text1, ne.getImage());
        } else if (text2 != null && oe.getImage() != null) {
            return Model.getSimilarity(text2, oe.getImage());
        } else if (oe.getImage() != null && ne.getImage() != null) {
            return Model.getSimilarity(oe.getImage(), ne.getImage());
        } else {
            return 0;
        }
    }

    private static double getAndSetMax(double[][] m) {
        double max = 0;
        int first = 0;
        int second = 0;
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[0].length; ++j) {
                if (m[i][j] > max) {
                    max = m[i][j];
                    first = i;
                    second = j;
                }
            }
        }
        for (double[] mm : m) {
            mm[second] = 0;
        }
        for (int i = 0; i < m[0].length; ++i) {
            m[first][i] = 0;
        }
        return max;
    }

    private static String getNewWindow(Set<String> oldWindows, Set<String> newWindows) {
        for (String window : newWindows) {
            if (!oldWindows.contains(window)) {
                return window;
            }
        }
        throw new RuntimeException();
    }

    private static void setCaches(State state) {
        List<String> strings = new ArrayList<>();
        List<byte[]> bytes = new ArrayList<>();
        for (Element element : state) {
            if (element.getText() != null) {
                strings.add(element.getText());
            } else if (element.getImage() != null && element.getImage().length != 0) {
                bytes.add(element.getImage());
            }
        }
        Model.encodeTexts(strings);
        Model.encodeImages(bytes);
    }

}
