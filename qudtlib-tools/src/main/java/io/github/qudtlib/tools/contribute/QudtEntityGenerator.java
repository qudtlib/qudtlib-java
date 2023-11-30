package io.github.qudtlib.tools.contribute;

import java.util.function.Consumer;
import java.util.function.Predicate;
import org.eclipse.rdf4j.model.Statement;

public class QudtEntityGenerator {

    private ToolImpl tool;

    public QudtEntityGenerator() {
        this(true);
    }

    public QudtEntityGenerator(boolean performShaclValidation) {
        this.tool = new ToolImpl(performShaclValidation);
    }

    public void unitOfWork(Consumer<Tool> configurer) {
        configurer.accept(this.tool);
    }

    public void writeRdf(Predicate<Statement> statementPredicate) {
        this.tool.writeRdf(System.out, statementPredicate);
    }

    public void writeRdf() {
        this.writeRdf(s -> true);
    }
}
