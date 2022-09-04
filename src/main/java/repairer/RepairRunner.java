package repairer;

import exception.FailToCreateFileException;
import japa.parser.ParseException;
import model.Model;
import runner.Executor;
import runner.RecordResult;
import runner.RunResult;
import statement.Statements;
import tracer.TraceRunner;
import utils.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class RepairRunner {

    private final Executor executor;

    public RepairRunner(Testcase testcase, Class<?> newTestcase, Executor.RepairMode repairMode) throws IOException, ClassNotFoundException, ParseException {
        Executor.repairMode = repairMode;
        this.executor = new Executor(testcase.testcase, Executor.RunMode.REPAIR);
        Repairer.driver = null;
        Repairer.oldStatements = RecordResult.read(TraceRunner.getTraceResultPath(testcase.testcase)).getStatements();
        Repairer.oldIndex = 0;
        if (newTestcase == null) {
            Repairer.correctStatements = null;
        } else {
            Repairer.correctStatements = RecordResult.read(TraceRunner.getTraceResultPath(newTestcase)).getStatements();
        }
        Repairer.correctIndex = 0;
        Repairer.repairedStatements = new Statements();
        Repairer.lastElement = null;
        Repairer.lastSelect = null;
        Repairer.newUrl = testcase.url;
        Repairer.sleepTime = 0;
        Repairer.ignoredTime = 0;
        Repairer.beginLine = ClassUtils.getTestBeginLine(testcase.testcase);
        Algorithm.onPathFilter = testcase.onPathFilter;
        Algorithm.onPathToClickFilter = testcase.onPathToClickFilter;
    }

    public void run() throws IOException, FailToCreateFileException {
        Model.getSimilarity("hello", "world");  // To load the semantic model in advance
        RunResult runResult = executor.run().setSleepTime(Repairer.sleepTime).setIgnoredTime(Repairer.ignoredTime);
        RecordResult repairResult = new RecordResult(runResult, Repairer.repairedStatements);
        repairResult.write(getRepairResultPath(executor.testcase));
        RecordResult alignResult = new RecordResult(runResult, Repairer.oldStatements);
        alignResult.write(getAlignResultPath(executor.testcase));
    }

    public static String getRepairResultPath(Class<?> testcase) {
        return Paths.get("output", "repair", Executor.repairMode.name().toLowerCase(), Algorithm.threshold + "", Algorithm.move + "", "result", testcase.getName().replace(".", File.separator)).toString();
    }

    public static String getAlignResultPath(Class<?> testcase) {
        return Paths.get("output", "repair", Executor.repairMode.name().toLowerCase(), Algorithm.threshold + "", Algorithm.move + "", "align", testcase.getName().replace(".", File.separator)).toString();
    }

    public static void main(String[] args) throws IOException, FailToCreateFileException, ClassNotFoundException, ParseException {
        RepairRunner repairRunner = new RepairRunner(
                // Given the test case to repair, including the URL of the updated version
                null,
                // The test case with the same functionality as the test case under repair, but runs correctly on the updated version
                // Set to null if not needed
                // Set to non null only during the test phase
                null,
                // Whether context semantic information is taken into account
                Executor.RepairMode.CONTEXT
        );
        repairRunner.run();
    }

}
