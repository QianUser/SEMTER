package repairer;

import repairer.filter.OnPathFilter;
import repairer.filter.OnPathToClickFilter;

import java.io.Serializable;

public class Testcase implements Serializable {

    private static final long serialVersionUID = 858668813095640254L;

    public Class<?> testcase;
    public String url;
    public OnPathFilter onPathFilter;
    public OnPathToClickFilter onPathToClickFilter;

    public Testcase(Class<?> testcase, String url, OnPathFilter onPathFilter, OnPathToClickFilter onPathToClickFilter) {
        this.testcase = testcase;
        this.url = url;
        this.onPathFilter = onPathFilter;
        this.onPathToClickFilter = onPathToClickFilter;
    }

    public Testcase(Class<?> testcase, String url) {
        this(testcase, url, null, null);
    }

    public Testcase(Class<?> testcase, String url, OnPathFilter onPathFilter) {
        this(testcase, url, onPathFilter, null);
    }

    public Testcase(Class<?> testcase, String url, OnPathToClickFilter onPathToClickFilter) {
        this(testcase, url, null, onPathToClickFilter);
    }

}