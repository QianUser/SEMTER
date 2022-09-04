package statement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class Statements implements Serializable, Iterable<Statement> {

    private static final long serialVersionUID = -2394627672561226243L;

    private final List<Statement> statements;

    public Statements() {
        this.statements = new ArrayList<>();
    }

    public void add(Statement statement) {
        statements.add(statement);
    }

    public void set(int index, Statement statement) {
        statements.set(index, statement);
    }

    public void removeIf(Function<Statement, Boolean> function) {
        List<Statement> newStatements = new ArrayList<>();
        for (Statement statement : statements) {
            if (!function.apply(statement)) {
                newStatements.add(statement);
            }
        }
        statements.clear();
        statements.addAll(newStatements);
    }

    public Statement getStatement(int index) {
        return statements.get(index);
    }

    public int size() {
        return statements.size();
    }

    @Override
    public Iterator<Statement> iterator() {
        return statements.iterator();
    }

    public List<Statement> align(int index, Statements oldStatements, int oldIndex) {
        int line = oldStatements.getStatement(oldIndex).getLine();
        List<Statement> list = new ArrayList<>();
        if (index >= statements.size()) {
            return list;
        } else {
            while (index < statements.size() && statements.get(index).getLine() <= line) {
                list.add(statements.get(index));
                ++index;
            }
        }
        Statement oldStatement = oldStatements.getStatement(oldIndex);
        if (oldStatement instanceof ElementStatement && oldStatements.size() > oldIndex + 1 &&
                oldStatements.getStatement(oldIndex + 1) instanceof SelectStatement &&
                list.size() >= 2 && list.get(list.size() - 1) instanceof SelectStatement) {
            list.remove(list.size() - 1);
        } else if (oldStatement instanceof SelectStatement && list.size() >= 2 && list.get(list.size() - 1) instanceof SelectStatement) {
            list.remove(list.size() - 2);
        } else if (oldStatement instanceof SelectStatement && !list.isEmpty() && list.get(list.size() -1) instanceof ElementStatement) {
            list.remove(list.size() - 1);
        }
        return list;
    }

}
