package exception;

public class UnhandledLocatorException extends Exception {

    private static final long serialVersionUID = -399727003558837964L;

    public UnhandledLocatorException() {}

    public UnhandledLocatorException(String msg) {
        super(msg);
    }

}
