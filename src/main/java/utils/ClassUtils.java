package utils;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.junit.Test;
import runner.Executor;

import java.io.File;
import java.io.IOException;

public class ClassUtils {

    private static class GetTestMethodLineVisitor extends VoidVisitorAdapter<Pair<Class<?>, Integer>> {
        @Override
        public void visit(MethodDeclaration methodDeclaration, Pair<Class<?>, Integer> pair) {
            if (methodDeclaration.getAnnotations() != null
                    && methodDeclaration.getAnnotations().get(0).getName().getName().equals(pair.first.getSimpleName())) {
                pair.second = methodDeclaration.getBody().getBeginLine();
            }
        }
    }

    private static class GetStatementNumVisitor extends VoidVisitorAdapter<Pair<Class<?>, Integer>> {
        @Override
        public void visit(MethodDeclaration methodDeclaration, Pair<Class<?>, Integer> pair) {
            if (methodDeclaration.getAnnotations() != null
                    && methodDeclaration.getAnnotations().get(0).getName().getName().equals(pair.first.getSimpleName())) {
                pair.second = methodDeclaration.getBody().getStmts().size();
            }
        }
    }

    public static int getTestBeginLine(Class<?> clazz) throws IOException, ParseException {
        String path = Executor.getTestcasePath(clazz);
        Pair<Class<?>, Integer> pair = new Pair<>(Test.class, 0);
        CompilationUnit compilationUnit = JavaParser.parse(new File(path));
        new GetTestMethodLineVisitor().visit(compilationUnit, pair);
        return pair.second;
    }

}
