package tracer;

import exception.UnhandledLocatorException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import runner.Executor;
import statement.*;
import web.Element;
import web.Element.Locator;

@Aspect
public class Tracer {

    static WebDriver driver;

    static Statements statements;

    static long sleepTime;

    static long ignoredTime;

    static int beginLine;

    @Before(value = "call(* org.openqa.selenium.WebDriver.get(..))")
    public void beforeGetUrl(JoinPoint joinPoint) {
        if (Executor.runMode == Executor.RunMode.TRACE) {
            Executor.runMode = Executor.RunMode.RUN;
            int line = getLine(joinPoint);
            driver = (WebDriver) joinPoint.getTarget();
            statements.add(new DriverGetStatement(line, (String) joinPoint.getArgs()[0]));
            Executor.runMode = Executor.RunMode.TRACE;
        }
    }

    @Before(value = "call(* Thread.sleep(..))")
    public void beforeThreadSleep(JoinPoint joinPoint) {
        if (Executor.runMode == Executor.RunMode.TRACE) {
            Executor.runMode = Executor.RunMode.RUN;
            int line = getLine(joinPoint);
            sleepTime += (long) joinPoint.getArgs()[0];
            statements.add(new ThreadSleepStatement(line, (Long) joinPoint.getArgs()[0]));
            Executor.runMode = Executor.RunMode.TRACE;
        }
    }

    @Before(value = "call(* org.openqa.selenium.WebDriver.Navigation.refresh())")
    public void afterNavigateRefresh(JoinPoint joinPoint) {
        if (Executor.runMode == Executor.RunMode.TRACE) {
            Executor.runMode = Executor.RunMode.RUN;
            int line = getLine(joinPoint);
            statements.add(new NavigateRefreshStatement(line));
            Executor.runMode = Executor.RunMode.TRACE;
        }
    }

    @AfterReturning(value = "call(* org.openqa.selenium.WebDriver.findElement(..))", returning = "webElement")
    public void afterFindElement(JoinPoint joinPoint, WebElement webElement) throws IllegalAccessException, NoSuchFieldException, UnhandledLocatorException {
        if (Executor.runMode == Executor.RunMode.TRACE) {
            Executor.runMode = Executor.RunMode.RUN;
            int line = getLine(joinPoint);
            Element element = new Element(driver, webElement, new Locator((By) joinPoint.getArgs()[0]));
            statements.add(new ElementStatement(line, driver, element));
            Executor.runMode = Executor.RunMode.TRACE;
        }
    }

    @Before(value = "call(* org.openqa.selenium.support.ui.Select.selectByVisibleText(..))")
    public void afterSelect(JoinPoint joinPoint) {
        if (Executor.runMode == Executor.RunMode.TRACE) {
            Executor.runMode = Executor.RunMode.RUN;
            int line = getLine(joinPoint);
            statements.add(new SelectStatement(line, driver, (Select) joinPoint.getTarget(), (String) joinPoint.getArgs()[0]));
            Executor.runMode = Executor.RunMode.TRACE;
        }
    }

    private int getLine(JoinPoint joinPoint) {
        return joinPoint.getSourceLocation().getLine() - beginLine;
    }

}
