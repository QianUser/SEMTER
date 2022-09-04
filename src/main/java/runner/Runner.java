package runner;

import java.io.IOException;

public class Runner {

    public static void main(String[] args) throws IOException {
        Executor executor = new Executor(null, Executor.RunMode.RUN);
        executor.run();
    }

}
