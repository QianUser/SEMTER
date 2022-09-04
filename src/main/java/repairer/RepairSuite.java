package repairer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class RepairSuite implements Serializable, Iterable<Testcase> {

    private static final long serialVersionUID = -9135251412579083645L;

    private final List<Testcase> testcases;

    public RepairSuite(Testcase... testcases) {
        this.testcases = Arrays.asList(testcases);
    }

    public int size() {
        return testcases.size();
    }

    public Testcase get(int index) {
        return testcases.get(index);
    }

    @Override
    public Iterator<Testcase> iterator() {
        return testcases.iterator();
    }

}
