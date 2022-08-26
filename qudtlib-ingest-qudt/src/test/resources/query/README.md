# Queries

This folder contains queries to be run against the qudt data as downloaded into during a maven build
````
qudtlib-ingest-qudt/target/generated-resources/qudt/vocab
````
The queries only serve the purpose of understanding the data. They are not meant to be used anywhere else in the project.

With the jena commandline tools installed, a query can be executed from this folder as follows:
```
sparql --data=../../../../target/generated-resources/qudt/vocab/unit/VOCAB_QUDT-UNITS-ALL-v2.1.ttl --query=allScalingOf.rq
```
or, if more data is needed,
```
sparql --data=../../../../target/generated-resources/qudt/vocab/unit/VOCAB_QUDT-UNITS-ALL-v2.1.ttl\
       --data=../../../../target/generated-resources/qudt/vocab/prefixes/VOCAB_QUDT-PREFIXES-v2.1.ttl\
       --query=allScalingOf.rq
```

