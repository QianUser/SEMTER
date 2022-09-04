package repairer;

import exception.FailToCreateFileException;
import japa.parser.ParseException;
import runner.Executor;
import tracer.TraceSuite;

import java.io.IOException;

public class BatchRepairerRunner {

    private final RepairSuite repairSuite;

    private final TraceSuite correctSuite;

    public BatchRepairerRunner(RepairSuite repairSuite, TraceSuite correctSuite) {
        this.repairSuite = repairSuite;
        this.correctSuite = correctSuite;
    }

    public void run(Executor.RepairMode repairMode) throws IOException, FailToCreateFileException, ClassNotFoundException, ParseException {
        if (correctSuite == null) {
            for (Testcase testcase : repairSuite) {
                new RepairRunner(testcase, null, repairMode).run();
            }
        } else {
            int index = 0;
            for (Testcase testcase : repairSuite) {
                while (!correctSuite.get(index).getSimpleName().equals(testcase.testcase.getSimpleName())) {
                    ++index;
                }
                new RepairRunner(testcase, correctSuite.get(index), repairMode).run();
            }
        }
    }

    public static void main(String[] args) throws FailToCreateFileException, IOException, ClassNotFoundException, ParseException {
        BatchRepairerRunner batchRepairerRunner = new BatchRepairerRunner(null, null);
        batchRepairerRunner.run(Executor.RepairMode.CONTEXT);
    }

}
