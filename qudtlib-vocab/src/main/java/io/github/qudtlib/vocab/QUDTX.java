package io.github.qudtlib.vocab;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Vocabulary extending the QUDT ontology, as used in the QUDTLib's data generation module.
 *
 * @author Florian Kleedorfer
 * @since 1.0
 */
public class QUDTX {
    public static final IRI factorUnit = create("factorUnit");
    public static final IRI convertsTo = create("convertsTo");
    public static final IRI FactorUnit = create("FactorUnit");
    public static final IRI exponent = create("exponent");

    private static IRI create(String localname) {
        return SimpleValueFactory.getInstance().createIRI(QUDT.NAMESPACE + localname);
    }
}
