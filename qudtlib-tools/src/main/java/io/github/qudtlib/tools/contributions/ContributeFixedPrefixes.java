package io.github.qudtlib.tools.contributions;

import io.github.qudtlib.Qudt;
import io.github.qudtlib.tools.contribute.QudtEntityGenerator;

public class ContributeFixedPrefixes {
    public static void main(String[] args) {
        QudtEntityGenerator entityGenerator = new QudtEntityGenerator();
        entityGenerator.unitOfWork(
                tool -> {
                    Qudt.allUnits().stream()
                            .filter(u -> u.getPrefix().isPresent())
                            .filter(
                                    u ->
                                            u.getIriLocalname()
                                                            .toLowerCase()
                                                            .indexOf(
                                                                    u
                                                                            .getPrefix()
                                                                            .get()
                                                                            .getLabels()
                                                                            .stream()
                                                                            .findFirst()
                                                                            .get()
                                                                            .getString()
                                                                            .toLowerCase())
                                                    == -1)
                            .forEach(
                                    u -> {
                                        System.err.println(
                                                String.format(
                                                        "check unit %s: prefix label %s not found in localname",
                                                        u.getIriAbbreviated(),
                                                        u.getPrefix().get().getLabels().stream()
                                                                .findFirst()
                                                                .get()));
                                    });
                });
    }
}
