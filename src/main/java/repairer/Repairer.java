package repairer;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runner.Executor;
import statement.*;
import utils.Pair;
import web.Context;
import web.Element;
import web.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static repairer.Algorithm.*;

@Aspect
public class Repairer {

    private final Logger logger = LoggerFactory.getLogger(Repairer.class);

    static WebDriver driver;

    static Statements oldStatements;

    static int oldIndex;

    static Statements correctStatements;

    static int correctIndex;

    static Statements repairedStatements;

    static Element lastElement;

    static Select lastSelect;

    static String newUrl;

    static long sleepTime;

    static long ignoredTime;

    static int beginLine;

    @Around(value = "call(* org.openqa.selenium.WebDriver.get(..))")
    public Object aroundGetUrl(ProceedingJoinPoint joinPoint) throws Throwable {
        if (Executor.runMode == Executor.RunMode.REPAIR) {
            Executor.runMode = Executor.RunMode.RUN;
            int line = getLine(joinPoint);
            logger.info("Start repair");
            driver = (WebDriver) joinPoint.getTarget();
            Statement newStatement = new DriverGetStatement(line, newUrl);
            newStatement.act(driver);
            repairedStatements.add(newStatement);
            ++oldIndex;
            ++correctIndex;
            invalidState = true;
            Executor.runMode = Executor.RunMode.REPAIR;
            return null;
        }
        return joinPoint.proceed();
    }

    @Around(value = "call(* Thread.sleep(..))")
    public Object aroundThreadSleep(ProceedingJoinPoint joinPoint) throws Throwable {
        if (Executor.runMode == Executor.RunMode.REPAIR) {
            Executor.runMode = Executor.RunMode.RUN;
            ThreadSleepStatement oldStatement = (ThreadSleepStatement) oldStatements.getStatement(oldIndex);
            repairedStatements.add(oldStatement);
            if (correctStatements != null) {
                align();
            } else {
                ++oldIndex;
                sleepTime += oldStatement.getMillis();
                oldStatement.act(driver);
            }
            lastElement = null;
            Executor.runMode = Executor.RunMode.REPAIR;
            return null;
        }
        return joinPoint.proceed(joinPoint.getArgs());
    }

    @Around(value = "call(* org.openqa.selenium.WebDriver.Navigation.refresh())")
    public Object aroundNavigateRefresh(ProceedingJoinPoint joinPoint) throws Throwable {
        if (Executor.runMode == Executor.RunMode.REPAIR) {
            Executor.runMode = Executor.RunMode.RUN;
            Statement oldStatement = oldStatements.getStatement(oldIndex);
            repairedStatements.add(oldStatement);
            if (correctStatements != null) {
                align();
            } else {
                ++oldIndex;
                oldStatement.act(driver);
            }
            lastElement = null;
            Executor.runMode = Executor.RunMode.REPAIR;
            return null;
        }
        return joinPoint.proceed();
    }

    @Around(value = "call(* org.openqa.selenium.WebDriver.findElement(..))")
    public Object aroundFindElement(ProceedingJoinPoint joinPoint) throws Throwable {
        if (Executor.runMode == Executor.RunMode.REPAIR) {
            Executor.runMode = Executor.RunMode.RUN;
            int line = getLine(joinPoint);
            Statement oldStatement = oldStatements.getStatement(oldIndex);
            Element oe = ((ElementStatement) oldStatement).getElement();
            if (oe.getText() == null && oe.getImage() == null) {
                logger.warn("Invalid element retrieved");
                return confirm(line, null, null);
            }
            Context oc= ((ElementStatement) oldStatement).getContext();
            Page op = ((ElementStatement) oldStatement).getPage();
            Page np = new Page(driver);
            if (isPageMatch(op, np)) {
                Element ne = getElementOnState(driver, oe, oc);
                if (ne != null && ne.getLocator().equals(oe.getLocator())) {
                    logger.info("No Breakage");
                    return confirm(line, ne, null);
                } else if (ne != null) {
                    logger.info("Breakage #1/2" + oe.getLocator() + " -> " + ne);
                    return confirm(line, ne, null);
                } else {
                    Pair<Element, Element> path = Algorithm.getElementOnPath(driver, oe, oc, op, np, false);
                    if (path != null) {
                        logger.info("Breakage #3: " + oe.getLocator() + " -> " + path);
                        return confirm(line, path.second, path.first);
                    } else {
                        logger.info("Breakage #4: " + oe.getLocator());
                        return confirm(line, null, null);
                    }
                }
            } else {
                Pair<Element, Element> path = getElementOnPath(driver, oe, oc, op, np, true);
                if (path != null) {
                    if (path.first == null && path.second.getLocator().equals(oe.getLocator())) {
                        logger.info("No Breakage");
                        return confirm(line, path.second, null);
                    } else if (path.first == null) {
                        logger.info("Breakage #1/2" + oe.getLocator() + " -> " + path.second);
                        return confirm(line, path.second, null);
                    } else {
                        logger.info("Breakage #3: " + oe.getLocator() + " -> " + path);
                        return confirm(line, path.second,  path.first);
                    }
                } else {
                    logger.info("Breakage #4: " + oe.getLocator());
                    return confirm(line, null, null);
                }
            }
        }
        return joinPoint.proceed(joinPoint.getArgs());
    }

    @AfterReturning(value = "call(* org.openqa.selenium.WebElement.*(..))")
    public void afterElementOp(JoinPoint joinPoint) {
        if (Executor.runMode == Executor.RunMode.REPAIR) {
            Executor.runMode = Executor.RunMode.RUN;
            String name = joinPoint.getSignature().getName();
            if (!name.startsWith("get") && !name.startsWith("is")) {
                invalidState = true;
            }
            if (name.equals("click") || name.equals("submit")) {
                handleAlert();
            }
            Executor.runMode = Executor.RunMode.REPAIR;
        }
    }

    @Around(value = "call(* org.openqa.selenium.WebElement.*(..))")
    public Object aroundElementOp(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed(joinPoint.getArgs());
        } catch (Throwable t) {
            return null;
        }
    }


    @Around(value = "call(org.openqa.selenium.support.ui.Select.new(..))")
    public Object aroundNewSelect(ProceedingJoinPoint joinPoint) throws Throwable {
        if (Executor.runMode == Executor.RunMode.REPAIR) {
            Executor.runMode = Executor.RunMode.RUN;
            if (lastElement == null) {
                return null;
            } else {
                try {
                    lastSelect = new Select(lastElement.toWebElement(driver));
                } catch (Exception e) {
                    lastSelect = null;
                }
            }
            Executor.runMode = Executor.RunMode.REPAIR;
            return lastSelect;
        }
        return joinPoint.proceed();
    }

    @Around(value = "call(* org.openqa.selenium.support.ui.Select.selectByVisibleText(..))")
    public Object aroundSelect(ProceedingJoinPoint joinPoint) throws Throwable {
        if (Executor.runMode == Executor.RunMode.REPAIR) {
            Executor.runMode = Executor.RunMode.RUN;
            int line = getLine(joinPoint);
            if (lastSelect == null) {
                if (correctStatements != null) {
                    align();
                } else {
                    logger.info("Select removed");
                    ++oldIndex;
                }
            } else {
                Select select = (Select) joinPoint.getTarget();
                SelectStatement oldStatement = (SelectStatement) oldStatements.getStatement(oldIndex);
                // Implementation issues: select.selectByVisibleText is not necessarily obtained according to getText()
                List<String> options = select.getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
                String bestOption = getBestOption(oldStatement.getSelected(), options);
                if (!bestOption.equals(oldStatement.getSelected())) {
                    logger.info("Select Breakage: " + oldStatement.getSelected() + " -> " + bestOption);
                }
                SelectStatement newStatement = new SelectStatement(line, driver, select, bestOption);
                repairedStatements.add(newStatement);
                if (correctStatements != null) {
                    align();
                } else {
                    ++oldIndex;
                    invalidState = true;
                    newStatement.act(driver);
                }
            }
            Executor.runMode = Executor.RunMode.REPAIR;
            return null;
        }
        return joinPoint.proceed(joinPoint.getArgs());
    }

    @Around(value = "call(* org.openqa.selenium.WebDriver.TargetLocator.alert()) ||" +
                    "call(* org.openqa.selenium.Alert.accept()) ||" +
                    "call(* org.openqa.selenium.Alert.getText())")
    public Object aroundAlert(ProceedingJoinPoint joinPoint) throws Throwable {
        if (Executor.runMode == Executor.RunMode.REPAIR) {
            return null;
        }
        return joinPoint.proceed(joinPoint.getArgs());
    }

    @Around(value = "call(* org.junit.Assert.assertTrue(..)) ||" +
                    "call(* org.junit.Assert.assertFalse(..)) ||" +
                    "call(* org.junit.Assert.assertEquals(..))")
    public Object aroundAssert(ProceedingJoinPoint joinPoint) throws Throwable {
        if (Executor.runMode == Executor.RunMode.REPAIR) {
            try {
                joinPoint.proceed(joinPoint.getArgs());
            } catch (Throwable throwable) {
                logger.warn("Assertion failed");
            }
            return null;
        }
        return joinPoint.proceed(joinPoint.getArgs());
    }

    private Object confirm(int line, Element ne, Element path) {
        if (correctStatements == null) {
            ++oldIndex;
            if (path != null) {
                ElementStatement statement = new ElementStatement(line, driver, path, false);
                statement.addElementXPath();
                repairedStatements.add(statement);
                statement.act(driver).click();
                handleAlert();
                lastElement = null;
                invalidState = true;
            }
            if (ne != null) {
                ElementStatement statement = new ElementStatement(line, driver, ne, false);
                statement.addElementXPath();
                repairedStatements.add(statement);
                lastElement = ne;
                Object result = statement.act(driver);
                Executor.runMode = Executor.RunMode.REPAIR;
                return result;
            } else {
                lastElement = null;
                Object result = driver.findElement(By.xpath("/html/body"));
                Executor.runMode = Executor.RunMode.REPAIR;
                return result;
            }
        } else {
            if (path != null) {
                ElementStatement statement = new ElementStatement(line, driver, path, false);
                statement.addElementXPath();
                repairedStatements.add(statement);
            }
            if (ne != null) {
                ElementStatement statement = new ElementStatement(line, driver, ne, false);
                statement.addElementXPath();
                repairedStatements.add(statement);
            }
            long start = System.currentTimeMillis();
            ElementStatement statement = (ElementStatement) oldStatements.getStatement(oldIndex);
            List<WebElement> webElements = statement.getElement().getLocator().toWebElements(driver);
            List<String> xpaths = new ArrayList<>();
            webElements.forEach(i -> xpaths.add(Element.getElementXPath(driver, i)));
            statement.addCorrectXPath(xpaths);
            ignoredTime += (System.currentTimeMillis() - start);
            Object object = align();
            Executor.runMode = Executor.RunMode.REPAIR;
            return object;
        }
    }

    private Object align() {
        Statement oldStatement = oldStatements.getStatement(oldIndex);
        List<Statement> list = correctStatements.align(correctIndex, oldStatements, oldIndex);
        ++oldIndex;
        correctIndex += list.size();
        for (Statement statement : list) {
            if (statement instanceof ThreadSleepStatement) {
                sleepTime += ((ThreadSleepStatement) statement).getMillis();
            }
            if (!(statement instanceof ElementStatement)) {
                invalidState = true;
            }
        }
        if (list.size() >= 2) {
            invalidState = true;
        }
        for (int i = 0; i < list.size() - 1; ++i) {
            if (list.get(i) instanceof ElementStatement) {
                // Implementation issues: statements that are not aligned perform click()
                ((ElementStatement) list.get(i)).act(driver).click();
                handleAlert();
            } else {
                list.get(i).act(driver);
            }
        }
        if (list.size() != 0) {
            Statement lastStatement = list.get(list.size() - 1);
            if (lastStatement.getLine() != oldStatement.getLine()) {
                lastElement = null;
                if (lastStatement instanceof ElementStatement) {
                    ((ElementStatement) lastStatement).act(driver).click();
                    handleAlert();
                } else {
                    lastStatement.act(driver);
                }
                if (oldStatement instanceof ElementStatement) {
                    return driver.findElement(By.xpath("/html/body"));
                } else {
                    return null;
                }
            } else {
                if (lastStatement instanceof ElementStatement) {
                    lastElement = ((ElementStatement) lastStatement).getElement();
                } else {
                    lastElement = null;
                }
                return lastStatement.act(driver);
            }
        } else {
            lastElement = null;
            if (oldStatement instanceof ElementStatement) {
                return driver.findElement(By.xpath("/html/body"));
            } else if (oldStatement instanceof SelectStatement) {
                ((ElementStatement) correctStatements.getStatement(correctIndex - 1)).act(driver).click();
                handleAlert();
                invalidState = true;
                return null;
            } else {
                return null;
            }
        }
    }

    private int getLine(JoinPoint joinPoint) {
        return joinPoint.getSourceLocation().getLine() - beginLine;
    }

    private void handleAlert() {
        while (true) {
            try {
                driver.switchTo().alert().accept();
                Thread.sleep(2000);
            } catch (Exception e) {
                break;
            }
        }
    }

}
