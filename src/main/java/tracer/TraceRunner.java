package tracer;

import exception.FailToCreateFileException;
import japa.parser.ParseException;
import runner.Executor;
import runner.RecordResult;
import runner.RunResult;
import statement.Statements;
import utils.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class TraceRunner {

    private final Executor executor;

    public TraceRunner(Class<?> testcase) throws IOException, ParseException {
        this.executor = new Executor(testcase, Executor.RunMode.TRACE);
        Tracer.driver = null;
        Tracer.statements = new Statements();
        Tracer.sleepTime = 0;
        Tracer.ignoredTime = 0;
        Tracer.beginLine = ClassUtils.getTestBeginLine(testcase);
    }

    public void run(Executor.RepairMode repairMode) throws IOException, FailToCreateFileException {
        Executor.repairMode = repairMode;
        RunResult runResult = executor.run().setSleepTime(Tracer.sleepTime).setIgnoredTime(Tracer.ignoredTime);
        RecordResult traceResult = new RecordResult(runResult, Tracer.statements);
        traceResult.write(getTraceResultPath(executor.testcase));
    }

    public static String getTraceResultPath(Class<?> testcase) {
        return Paths.get("output", "trace", Executor.repairMode.name().toLowerCase(), testcase.getName().replace(".", File.separator)).toString();
    }

    public static void main(String[] args) throws IOException, FailToCreateFileException, ParseException {
        // Given the test case to trace
        TraceRunner traceRunner = new TraceRunner(null);
        // Whether to retrieve context semantic information for an element
        traceRunner.run(Executor.RepairMode.CONTEXT);
    }

}
