package exception;

public class FailToCreateFileException extends Exception {

    private static final long serialVersionUID = -2137892878967590734L;

    public FailToCreateFileException() {};

    public FailToCreateFileException(String msg) {
        super(msg);
    }

}
