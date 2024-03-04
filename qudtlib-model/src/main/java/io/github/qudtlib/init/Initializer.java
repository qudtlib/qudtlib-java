package io.github.qudtlib.init;

import static java.util.stream.Collectors.toMap;

import io.github.qudtlib.exception.NotFoundException;
import io.github.qudtlib.model.*;
import io.github.qudtlib.nodedef.MapBackedNodeDefinition;
import io.github.qudtlib.nodedef.NodeDefinition;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Initializes the QUDTLib model.
 *
 * @author Florian Kleedorfer
 * @version 1.0
 */
public interface Initializer {

    static class Definitions {
        private Map<String, Unit.Definition> unitDefinitions;
        private Map<String, Prefix.Definition> prefixDefinitions;
        private Map<String, QuantityKind.Definition> quantityKindDefinitions;
        private Map<String, SystemOfUnits.Definition> systemOfUnitsDefinitions;

        private Map<String, ConstantValue.Definition> constantValueDefinitions;

        private Map<String, PhysicalConstant.Definition> physicalConstantDefinitions;

        public Definitions() {
            this.prefixDefinitions = new HashMap<>();
            this.unitDefinitions = new HashMap<>();
            this.quantityKindDefinitions = new HashMap<>();
            this.systemOfUnitsDefinitions = new HashMap<>();
            this.constantValueDefinitions = new HashMap<>();
            this.physicalConstantDefinitions = new HashMap<>();
        }

        public void addUnitDefinition(Unit.Definition definition) {
            this.unitDefinitions.put(definition.getId(), definition);
        }

        public void addQuantityKindDefinition(QuantityKind.Definition definition) {
            this.quantityKindDefinitions.put(definition.getId(), definition);
        }

        public void addPrefixDefinition(Prefix.Definition definition) {
            this.prefixDefinitions.put(definition.getId(), definition);
        }

        public void addSystemOfUnitsDefinition(SystemOfUnits.Definition definition) {
            this.systemOfUnitsDefinitions.put(definition.getId(), definition);
        }

        public void addConstantValueDefinition(ConstantValue.Definition definition) {
            this.constantValueDefinitions.put(definition.getId(), definition);
        }

        public void addPhysicalConstantDefinition(PhysicalConstant.Definition definition) {
            this.physicalConstantDefinitions.put(definition.getId(), definition);
        }

        public NodeDefinition<String, Unit> expectUnitDefinition(String iri) {
            if (iri == null) {
                return null;
            }
            return new MapBackedNodeDefinition<>(
                    this.unitDefinitions,
                    iri,
                    () -> new NotFoundException(String.format("No Unit found with iri %s", iri)));
        }

        public NodeDefinition<String, Prefix> expectPrefixDefinition(String iri) {
            if (iri == null) {
                return null;
            }
            return new MapBackedNodeDefinition<>(
                    this.prefixDefinitions,
                    iri,
                    () -> new NotFoundException(String.format("No Prefix found with iri %s", iri)));
        }

        public NodeDefinition<String, QuantityKind> expectQuantityKindDefinition(String iri) {
            if (iri == null) {
                return null;
            }
            return new MapBackedNodeDefinition<>(
                    this.quantityKindDefinitions,
                    iri,
                    () ->
                            new NotFoundException(
                                    String.format("No QuantityKind found with iri %s", iri)));
        }

        public NodeDefinition<String, SystemOfUnits> expectSystemOfUnitsDefinition(String iri) {
            if (iri == null) {
                return null;
            }
            return new MapBackedNodeDefinition<>(
                    this.systemOfUnitsDefinitions,
                    iri,
                    () ->
                            new NotFoundException(
                                    String.format("No System of Units found with iri %s", iri)));
        }

        public NodeDefinition<String, PhysicalConstant> expectPhysicalConstantDefinition(
                String iri) {
            if (iri == null) {
                return null;
            }
            return new MapBackedNodeDefinition<>(
                    this.physicalConstantDefinitions,
                    iri,
                    () ->
                            new NotFoundException(
                                    String.format("No PhysicalConstant found with iri %s", iri)));
        }

        public NodeDefinition<String, ConstantValue> expectConstantValueDefinition(String iri) {
            if (iri == null) {
                return null;
            }
            return new MapBackedNodeDefinition<>(
                    this.constantValueDefinitions,
                    iri,
                    () ->
                            new NotFoundException(
                                    String.format("No ConstantValue found with iri %s", iri)));
        }

        public boolean hasUnitDefinitions() {
            return !this.unitDefinitions.isEmpty();
        }

        public Optional<Unit.Definition> getUnitDefinition(String iri) {
            return Optional.ofNullable(this.unitDefinitions.get(iri));
        }

        public Optional<QuantityKind.Definition> getQuantityKindDefinition(String iri) {
            return Optional.of(this.quantityKindDefinitions.get(iri));
        }

        public Optional<Prefix.Definition> getPrefixDefinition(String iri) {
            return Optional.of(this.prefixDefinitions.get(iri));
        }

        public Optional<SystemOfUnits.Definition> getSystemOfUnitsDefinition(String iri) {
            return Optional.of(this.systemOfUnitsDefinitions.get(iri));
        }

        public Optional<ConstantValue.Definition> getConstantValueDefinition(String iri) {
            return Optional.of(this.constantValueDefinitions.get(iri));
        }

        public Optional<PhysicalConstant.Definition> getPhysicalConstantDefinition(String iri) {
            return Optional.of(this.physicalConstantDefinitions.get(iri));
        }
    }

    Definitions loadData();

    default Map<String, Unit> buildUnits(Definitions definitions) {
        return definitions.unitDefinitions.entrySet().stream()
                .collect(toMap(e -> e.getKey(), e -> e.getValue().build()));
    }

    default Map<String, QuantityKind> buildQuantityKinds(Definitions definitions) {
        return definitions.quantityKindDefinitions.entrySet().stream()
                .collect(toMap(e -> e.getKey(), e -> e.getValue().build()));
    }

    default Map<String, Prefix> buildPrefixes(Definitions definitions) {
        return Collections.unmodifiableMap(
                definitions.prefixDefinitions.entrySet().stream()
                        .collect(toMap(e -> e.getKey(), e -> e.getValue().build())));
    }

    default Map<String, SystemOfUnits> buildSystemsOfUnits(Definitions definitions) {
        return Collections.unmodifiableMap(
                definitions.systemOfUnitsDefinitions.entrySet().stream()
                        .collect(toMap(e -> e.getKey(), e -> e.getValue().build())));
    }

    default Map<String, PhysicalConstant> buildPhysicalConstants(Definitions definitions) {
        return definitions.physicalConstantDefinitions.entrySet().stream()
                .collect(toMap(e -> e.getKey(), e -> e.getValue().build()));
    }

    default Map<String, ConstantValue> buildConstantValues(Definitions definitions) {
        return definitions.constantValueDefinitions.entrySet().stream()
                .collect(toMap(e -> e.getKey(), e -> e.getValue().build()));
    }
}
