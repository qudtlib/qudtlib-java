package io.github.qudtlib.tools.contributions;

import static io.github.qudtlib.model.Units.PA__M__PER__SEC;
import static io.github.qudtlib.model.Units.W__PER__M2;

import io.github.qudtlib.tools.contribute.QudtEntityGenerator;

public class TmpTests {

    public static void main(String[] args) {

        QudtEntityGenerator entityGenerator = new QudtEntityGenerator();
        entityGenerator.unitOfWork(
                tool -> {
                    tool.printFactorUnitTree(W__PER__M2);
                    tool.printFactorUnitTree(PA__M__PER__SEC);
                });
    }
}
