package io.github.qudtlib.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

/**
 * Simple RDF operations needed by multiple modules.
 *
 * @author Florian Kleedorfer
 * @since 1.0
 */
public abstract class RdfOps {

    private static final boolean DEBUG = false;

    public static void writeTurtleFile(RepositoryConnection con, Path outfile) {
        System.out.println("writing RDF data to file " + outfile.toFile().getAbsolutePath());
        try (FileOutputStream out = new FileOutputStream(outfile.toFile())) {
            RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
            WriterConfig config =
                    writer.getWriterConfig()
                            .set(BasicWriterSettings.PRETTY_PRINT, true)
                            .set(BasicWriterSettings.INLINE_BLANK_NODES, true);
            writer.setWriterConfig(config);
            writer.startRDF();
            try (RepositoryResult<Namespace> namespaces = con.getNamespaces()) {
                namespaces.forEach(n -> writer.handleNamespace(n.getPrefix(), n.getName()));
            }
            try (RepositoryResult<Statement> statements =
                    con.getStatements(null, null, null, (Resource) null)) {
                for (Statement st : statements) {
                    writer.handleStatement(st);
                }
            }
            writer.endRDF();
        } catch (Exception e) {
            throw new IllegalStateException("Error writing RDF file " + outfile, e);
        }
    }

    public static void writeTurtleFile(Model model, Path outfile) {
        System.out.println("writing RDF data to file " + outfile.toFile().getAbsolutePath());
        try (FileOutputStream out = new FileOutputStream(outfile.toFile())) {
            RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
            WriterConfig config =
                    writer.getWriterConfig()
                            .set(BasicWriterSettings.PRETTY_PRINT, true)
                            .set(BasicWriterSettings.INLINE_BLANK_NODES, true);
            writer.setWriterConfig(config);
            writer.startRDF();
            model.getNamespaces().forEach(n -> writer.handleNamespace(n.getPrefix(), n.getName()));
            for (Statement st : model.getStatements(null, null, null, (Resource) null)) {
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
            Model model = QueryResults.asModel(result);
            if (DEBUG) {
                try {
                    File out =
                            File.createTempFile(
                                    "queryresult-" + new File(queryFile).getName(), ".txt");
                    System.err.println("writing query result to " + out);
                    writeTurtleFile(model, out.toPath());
                } catch (IOException e) {
                    System.err.println("Error writing query result file for debugging purposes");
                    e.printStackTrace();
                }
            }
            for (RepositoryConnection to : toCon) {
                to.add(model);
                to.commit();
            }
        }
    }

    public static void copyData(RepositoryConnection fromCon, RepositoryConnection toCon) {
        toCon.add(fromCon.getStatements(null, null, null, (Resource) null));
        toCon.commit();
    }

    public static void updateDataUsingQuery(RepositoryConnection con, String queryFile) {
        String query = loadQuery(queryFile);
        con.prepareUpdate(query).execute();
        con.commit();
    }

    public static void updateDataUsingNQueries(
            RepositoryConnection con, String queryFileRegex, int n) {
        List<String> queries = loadNQueries(queryFileRegex, n);
        for (String query : queries) {
            con.prepareUpdate(query).execute();
        }
        con.commit();
    }

    public static void addStatementsFromFile(RepositoryConnection con, String filename) {
        Model model = loadTurtleToModel(filename);
        con.add(model);
        model.getNamespaces().forEach(ns -> con.setNamespace(ns.getPrefix(), ns.getName()));
        con.commit();
    }

    public static void removeStatementsFromFile(RepositoryConnection con, String filename) {
        con.remove(loadTurtleToModel(filename));
        con.commit();
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

    public static List<String> loadNQueries(String queryFilepattern, int n) {
        List<String> ret = new ArrayList<>();
        for (int num = 0; num < n; num++) {
            String queryFile = queryFilepattern.replaceAll("\\[N\\]", num + "");
            try (InputStream in =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(queryFile)) {
                if (in != null) {
                    ret.add(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Error loading query from classpath resource " + queryFile, e);
            }
        }
        return ret;
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
