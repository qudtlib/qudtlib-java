package com.github.qudlib.common;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

/**
 * Simple RDF operations needed by multiple modules.
 *
 * @author Florian Kleedorfer
 * @since 1.0
 */
public class RdfOps {
    public static void writeTurtleFile(RepositoryConnection con, Path outfile) {
        System.out.println("writing RDF data to file " + outfile.toFile().getAbsolutePath());
        try (FileOutputStream out = new FileOutputStream(outfile.toFile())) {
            RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
            writer.startRDF();
            for (Statement st : con.getStatements(null, null, null, (Resource) null)) {
                writer.handleStatement(st);
            }
            writer.endRDF();
        } catch (Exception e) {
            throw new IllegalStateException("Error writing RDF file " + outfile, e);
        }
    }

    public static void addDataUsingQuery(
            RepositoryConnection fromCon, String queryFile, RepositoryConnection... toCon) {
        String query = loadQuery(queryFile);
        try (GraphQueryResult result = fromCon.prepareGraphQuery(query).evaluate()) {
            for (RepositoryConnection to : toCon) {
                to.add(QueryResults.asModel(result));
            }
        }
    }

    public static void copyData(RepositoryConnection fromCon, RepositoryConnection toCon) {
        toCon.add(fromCon.getStatements(null, null, null, (Resource) null));
    }

    public static void updateDataUsingQuery(RepositoryConnection con, String queryFile) {
        String query = loadQuery(queryFile);
        con.prepareUpdate(query).execute();
    }

    public static void addStatementsFromFile(RepositoryConnection con, String filename) {
        con.add(loadTurtleToModel(filename));
    }

    public static Model loadTurtleToModel(String classpathTurtleFile) {
        Model model = new LinkedHashModel();
        RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
        parser.setRDFHandler(new StatementCollector(model));
        loadTtlFile(classpathTurtleFile, parser);
        return model;
    }

    public static void loadTtlFile(String ttlFile, RDFParser parser) {
        try (InputStream in =
                Thread.currentThread().getContextClassLoader().getResourceAsStream(ttlFile)) {
            parser.parse(in);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error loading data from classpath resource " + ttlFile, e);
        }
    }

    public static String loadQuery(String queryFile) {
        try (InputStream in =
                Thread.currentThread().getContextClassLoader().getResourceAsStream(queryFile)) {
            if (in == null) {
                throw new IllegalStateException("Could not read " + queryFile);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error loading query from classpath resource " + queryFile, e);
        }
    }

    public static void message(String message) {
        System.out.println(message);
    }
}
