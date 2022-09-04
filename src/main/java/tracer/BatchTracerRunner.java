package tracer;

import exception.FailToCreateFileException;
import japa.parser.ParseException;
import runner.Executor;

import java.io.IOException;

public class BatchTracerRunner {

    private final TraceSuite traceSuite;

    public BatchTracerRunner(TraceSuite traceSuite) {
        this.traceSuite = traceSuite;
    }

    public void run(Executor.RepairMode repairMode) throws IOException, FailToCreateFileException, ParseException {
        for (Class<?> testcase : traceSuite) {
            TraceRunner traceRunner = new TraceRunner(testcase);
            traceRunner.run(repairMode);
        }
    }

    public static void main(String[] args) throws IOException, FailToCreateFileException, ParseException {
        BatchTracerRunner batchTracerRunner = new BatchTracerRunner(null);
        batchTracerRunner.run(Executor.RepairMode.CONTEXT);
    }

}

