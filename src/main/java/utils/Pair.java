package utils;

import java.io.Serializable;
import java.util.Objects;

public class Pair<T, U> implements Serializable {

    private static final long serialVersionUID = -4648591609666703580L;

    public T first;

    public U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public void setSecond(U second) {
        this.second = second;
    }

    public Pair<T, U> copy() {
        return new Pair<>(first, second);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Pair
                && Objects.equals(first, ((Pair<?, ?>) obj).first)
                && Objects.equals(second, ((Pair<?, ?>) obj).second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    public String toString() {
        return "(" + first + ", " + second + ")";
    }

}
