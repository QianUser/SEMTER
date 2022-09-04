package runner;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class Executor {

    private static final Logger logger = LoggerFactory.getLogger(Executor.class);

    public static final String driverPath = Paths.get("resources", "browser", "geckodriver.exe").toString();

    public static final String driverProperty = Paths.get("webdriver.gecko.driver").toString();

    public enum RunMode {
        RUN, TRACE, REPAIR
    }

    public enum RepairMode {
        CONTEXT, NO_CONTEXT;
    }

    public static RunMode runMode;

    public static RepairMode repairMode;

    static {
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
        System.setProperty(driverProperty, driverPath);
    }

    public Class<?> testcase;

    public Executor(Class<?> testcase, RunMode runMode) {
        Executor.runMode = runMode;
        this.testcase = testcase;
    }

    public static String getTestcasePath(Class<?> clazz) {
        return Paths.get("src", "main", "java", clazz.getName().replace(".", File.separator)).toString() + ".java";
    }

    public RunResult run() throws IOException {
        long startTime = System.currentTimeMillis();
        Result result = JUnitCore.runClasses(testcase);
        if (!result.wasSuccessful()) {
            logger.warn("Test failed");
            for (Failure failure : result.getFailures()) {
                logger.warn(failure.toString());
            }
        } else {
            logger.info("Test passed");
        }
        long elapsed = System.currentTimeMillis() - startTime;
        logger.info("Time spent: " + elapsed);
        if (result.wasSuccessful()) {
            return new RunResult(true, elapsed);
        } else {
            RunResult runResult = new RunResult(false, elapsed);
            waitForFail();
            return runResult.setFailReason(result.getFailures().toString());
        }
    }

    public void waitForFail() throws IOException {
        System.out.println("Run failed: " + testcase.getName());
        new BufferedReader(new InputStreamReader(System.in)).readLine();
    }

}
