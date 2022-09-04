package runner;

import exception.FailToCreateFileException;
import statement.Statements;
import utils.FileUtils;

import java.io.IOException;
import java.io.Serializable;

public class RecordResult implements Serializable {

    private static final long serialVersionUID = 6675548679473740855L;

    private final RunResult runResult;

    private final Statements statements;

    public RecordResult(RunResult runResult, Statements statements) {
        this.runResult = runResult;
        this.statements = statements;
    }

    public RunResult getRunResult() {
        return runResult;
    }

    public Statements getStatements() {
        return statements;
    }

    public void write(String pathname) throws IOException, FailToCreateFileException {
        FileUtils.writeObject(this, pathname);
    }

    public static RecordResult read(String pathname) throws IOException, ClassNotFoundException {
        try {
            return (RecordResult) FileUtils.readObject(pathname);
        } catch (Exception e) {
            return new RecordResult(new RunResult(), new Statements());
        }
    }

}
