package web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Page implements Serializable {

    private static final long serialVersionUID = -6486725229675902853L;

    private final List<String> page;

    public Page(WebDriver driver) {
        String pageText = driver.findElement(By.xpath("/html/body")).getText();
        this.page = Arrays.stream(pageText.split("\\n+"))
                .map(String::trim)
                .filter(i -> !StringUtils.isBlank(i))
                .collect(Collectors.toList());
    }

    public List<String> getPage() {
        return page;
    }

    public int getSize() {
        return page.size();
    }

    public String get(int index) {
        return page.get(index);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Page && Objects.equals(page, ((Page) obj).page);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(page);
    }

}
