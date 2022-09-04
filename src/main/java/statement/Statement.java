package statement;

import org.openqa.selenium.WebDriver;

import java.io.Serializable;

public interface Statement extends Serializable {

    Object act(WebDriver driver);

    int getLine();

}
