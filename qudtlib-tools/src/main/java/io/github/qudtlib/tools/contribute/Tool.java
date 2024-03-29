package io.github.qudtlib.tools.contribute;

import io.github.qudtlib.model.*;
import io.github.qudtlib.tools.contribute.model.QuantityKindMetadata;
import io.github.qudtlib.tools.contribute.model.UnitMetadata;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;

public interface Tool {
    Unit addDerivedUnit(
            FactorUnits factorUnits,
            Consumer<Unit.Definition> unitConfigurer,
            Consumer<UnitMetadata.Builder> metadataConfigurer,
            String nonstandardLocalname);

    Unit addDerivedUnit(
            FactorUnits factorUnits,
            Consumer<Unit.Definition> unitConfigurer,
            Consumer<UnitMetadata.Builder> metadataConfigurer);

    Unit addDerivedUnit(FactorUnits factorUnits, Consumer<Unit.Definition> unitConfigurer);

    QuantityKind addQuantityKind(
            FactorUnits factorUnits,
            String localname,
            Consumer<QuantityKind.Definition> quantityKindConfigurer,
            Consumer<QuantityKindMetadata.Builder> metadataConfigurer);

    QuantityKind addQuantityKind(
            FactorUnits factorUnits,
            String localname,
            Consumer<QuantityKind.Definition> quantityKindConfigurer);

    Set<Unit> findUnitBySymbolOrUcumCode(String symbol);

    boolean checkUnitExists(FactorUnits factorUnits);

    boolean checkUnitExists(FactorUnits factorUnits, DerivedUnitSearchMode mode);

    void printFactorUnitTree(Unit unit);

    void printFactorUnitTree(Unit unit, Function<FactorUnit, String> unitFormatter);

    void printFactorUnitTree(
            Unit unit, Function<FactorUnit, String> unitFormatter, OutputStream out);

    List<Unit> listUnitsWithSameDimensions(Unit unit);

    boolean checkQuantityKindExists(FactorUnits factorUnits);

    List<QuantityKind> searchQuantityKinds(String nameRegex);

    String generateJavaCodeStringForFactorUnits(FactorUnits factorUnits);

    void addUnitsForUcumCodeBestEffort(String ucumCode, boolean force);

    void addUnitsForFactorUnitsBestEffort(FactorUnits factorUnits, boolean force);

    Set<Unit> findExistingQudtUnitsForFactorUnits(FactorUnits factorUnits);

    List<FactorUnits> parseUcumCodeToFactorUnits(String ucumCode);

    List<QuantityKind> searchQuantityKinds(Predicate<QuantityKind> filter);

    void writeOut(Model model, OutputStream out, Predicate<Statement> statementPredicate);

    void writeOut(Model model, OutputStream out);
}
