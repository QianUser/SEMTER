package tracer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class TraceSuite implements Serializable, Iterable<Class<?>> {

    private static final long serialVersionUID = -1695157682674290617L;

    private final List<Class<?>> testcases;

    public TraceSuite(Class<?>... testcases) {
        this.testcases = Arrays.asList(testcases);
    }

    public int size() {
        return testcases.size();
    }

    public Class<?> get(int index) {
        return testcases.get(index);
    }

    @Override
    public Iterator<Class<?>> iterator() {
        return testcases.iterator();
    }

}
