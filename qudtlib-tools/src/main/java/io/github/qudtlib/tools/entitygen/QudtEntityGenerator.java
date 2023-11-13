package io.github.qudtlib.tools.entitygen;

import java.util.function.Consumer;

public class QudtEntityGenerator {

    private ToolImpl tool = new ToolImpl();

    public void unitOfWork(Consumer<Tool> configurer) {
        configurer.accept(this.tool);
    }

    public void writeRdf() {
        this.tool.writeRdf(System.out);
    }
}
