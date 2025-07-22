# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [7.0.0-alpha.3] - 2025-07-22

### Added

- `Unit.dependents` - the number of units that refer to that unit as one of its factor units or are scaled from that base unit.
  this value is required to achieve better sorting in `Qudt.derivedUnitListFromFactorUnits`, allowing to prefer
  more important units (those with more dependents)

### Changed

- Upgrade to QUDT 3.1.4
  - Calculation of scalings and factor units now happens in QUDT (see PR [1129](https://github.com/qudt/qudt-public-repo/pull/1129))
    However, a few things have changed:
    - rename `qudt:isScalingOf` to `qudt:scalingOf`
    - rename `qudt:factorUnit/qudt:unit` to `qudt:hasFactorUnit/qudt:hasUnit`
  - Currency units have changed:
    - they were moved back into the graph 'http://qudt.org/vocab/unit/'
    - their localnames have received the prefix `CCY_`
    - both changes are 'soft', ie, the old way is still available, but deprecated
    - for example: `http://qudt.org/vocab/currency/EUR` is now `http://qudt.org/vocab/unit/CCY_EUR`.
    - QUDTLib makes the change now instead of adopting the deprecation, as there are other breaking changes as well, so upgrading will require work anyway.
    - for QUDTLib constants, this means that the pattern `${CurrencyName}_Currency` is dropped in favor of the QUDT style `CCY_${CurrencyName}`, e.g. `Qudt.Units.EUR_Currency` becomes `Qudt.Units.CCY_EUR`
  - Many conversion multipliers were changed in QUDT. They now consistently have at most 34 significant digits (ie, BigDecimal's precision of 34). This fixes problems such as the one reported in [Conversion error from MilliSV__PER__HR to MicroSV__PER__HR](https://github.com/qudtlib/qudtlib-java/issues/109).
  - scalings of `unit:GM` deserve special mention:
    - in the upcoming QUDT release we have `unit:GM qudt:scalingOf unit:KiloGM`, making `qudt:scalingOf` and `qudt:conversionMultiplier` consistent in all cases
    - QUDTLib makes the change as well, with the effect that unit factorization does not use `Units.GM` (which probably surprised a few people) for the mass dimension but `Units.KiloGM` (which is what one would expect)
  - Sorting in `Qudt.derivedUnitListFromFactorUnits` now takes more aspects into account and ordering has changed in some cases.
  - When creating a Unit instance, its factor units are checked for factors `x^N^M`, where x has no more factors.
    Such factors are simplified as x^(N*M). The same is applied to newly instantiated `FactorUnits` objects.

### Fixed

- Fixed a sorting bug in `Qudt.derivedUnitListFromFactorUnits` that caused wrong sorting for some very similar units.

## [6.8.1] - 2025-01-29

### Fixed

- Add quantitykind constants for those quantitykinds that are defined in the currency file in QUDT.

## [6.8.0] - 2024-12-11

### Added

- Add `Unit.altSymbols`
- Add `Qudt.parseUnit()`
- Add unit index structures in `Qudt` to improve performance of unit searches and factor unit matching

### Changed

- Bump QUDT version to [2.1.46](https://github.com/qudt/qudt-public-repo/releases/tag/v2.1.46)

## [6.7.0] - 2024-04-04

### Changed

- Bump QUDT version to [2.1.37](https://github.com/qudt/qudt-public-repo/releases/tag/v2.1.37)

## [6.5.0] - 2024-03-17

### Added

- Add model classes PhysicalConstant and ConstantValue, populate from data.
- Add tool for generating the decimal/scientific values for QUDT
- Add description to `Unit, QuantityKind and PhysicalConstant` (i.e. the string value of `dcterms:description`)

### Changed

- Update to qudt-public-repo@16f1b30
- Handle TemperatureDifference edge case (ignore offset when converting)

## [6.4.3] - 2024-02-20

### Added

- Add units `unit:KiloGM-PER-PA-SEC-M`, `unit:M2-K-PER-W`, `KiloM2`, `CentiM4`

### Changed

- Improve qudtlib-tools for contributions to QUDT

## [6.4.2] - 2024-02-20

## [6.4.1] - 2024-02-08

## [6.4.0] - 2024-02-08

### Added

- `boolean Unit.generated()` method indicating whether a unit was generated during the QUDTLib build process (and is therefore not in QUDT)
- Added two units and a quantity kind pending upstream PR merging
- New class `io.github.qudtlib.QudtMainForDebugging` in `qudtlib-main-rdf` that can be used for thread-debugging the non-hardcoded static initializer when other methods fail.
- `FactorUnits.getConversionMultiplierOpt` obtains the multiplier only if all factor units have one, instead of falling back to using 1.0.

### Changed

- Bump qudtlib version to [2.1.35](https://github.com/qudt/qudt-public-repo/releases/tag/v2.1.35)
- Refactoring and extensions of qudtlib-tools to keep up with incoming changes and slowly working toward a state in which the tools can be used in github actions for correctness checks.
- Factor units can now be sorted for a given unit IRI, resulting in symbols being generated with expected ordering of factors.
- Data generation input files have been renamed for consistency

## [6.3.0] - 2024-02-03

### Added

- `Qudt.unitsFrom*` methods for obtaining units that match a given specification expressed via factor units.
- `FactorUnits FactorUnits.withoutScaleFactor()` to get the FactorUnits object with scaleFactor 1.

### Changed

- `Qudt.unitsFromFactorUnits` has been changed to give highest priority to units with factorUnits equal to the requested ones.

### Deprecated

- `Qudt.derivedUnitsFrom*` have been deprecated. The implementations have become slower. The alternatives are listed in the code comments. The replacing methods have been renamed to fit better with the other unit-related methods and return `List<Unit>` instead of `Set<Unit>`. The reason is that the list is sorted according to match quality, which helps clients to choose the best after filtering.

## [6.2.0] - 2024-01-11

### Changed

- Simplified BEST_MATCH algorithm for obtaining a unit from a set of factor units. Recent additions to the data model (scalingOf and factorUnit relationships) led to a larger set of candidates and the complexity of the previous algorithm led to very high computation time.
- Changed the behaviour of Unit.hasFactorUnits() such that for a FactorUnits object with only one top-level factor unit (such as [N^1]), the method returns false.

### Fixed

- `unit:MHO`, `unit:MicroMHO`: fix dimension vector and quantity kind
- `unit:F`: fix `unit:scalingOf`

## [6.1.0] - 2024-01-05

### Added

- Josh Feingold(@occamsystems) contributed the ability to add units and quantity kinds at runtime, along with a
  number of improvements

## [6.0.3] - 2023-12-20

### Added

- Created CONTRIBUGING.md

### Fixed

- Bugfix by Josh Feingold(@occamsystems): Offset difference and scale difference no longer throw exceptions when the provided Units do not have explicit conversion values

## [6.0.2] - 2023-12-18

## [6.0.1] - 2023-12-18

## [6.0.0] - 2023-12-15

### Added

- Add (crude) tools for inspecting QUDT units/quantitykinds and for generating triples for unit/quantitykind contributions
- Add simple SHACL validation for contribution
- Add some units/quantitykinds for the construction domain
- Generate all possible localnames from factor units for checking if one of them is used for a unit
- Add Unit.getIriLocalname()
- Add member ucumCode with getters/setters and builder
- Add setFactorUnits() to Unit.Definition
- Add Qudt.currencyFromLocalname()
- Add Qudt.isBroaderQuantityKind()
- Add FactorUnits.getDimensionVectorIri()
- Add inspection of Factor unit tree in qudtlib-tools

### Changed

- Make getSymbol() return Optional

### Fixed

- handling of currency units such that they can now also be factor units
- Exclude unit:Kilo-FT3 from QUDTLib because it breaks unit localname parsing
- Implement a not so recent change in QUDTLib whereby quantitykinds are no longer skos:broader but
  qudt:exactMatch in certain conditions. See https://github.com/qudtlib/qudtlib-java/issues/61. The change
  adds an `exactMatch` property to the `QuantityKind` and `Unit` classes and uses the exactMatch property
  where appropriate in algorithms.
- Improve quantitykind extraction query
- Fix use of Unit constructor

## [5.0.1] - 2023-09-21

### Fixed

- `quantitykind:Currency` was missing in 5.0.0. Its definition was moved to the currencies file in the QUDT repo
  which used to be ingested only for units code generation. Now it is also used for quantity kinds code generation.

## [5.0.0] - 2023-09-15

### Changed

- Updated QUDT to [v2.1.29](https://github.com/qudt/qudt-public-repo/releases/tag/v2.1.29)

## [4.3.0] - 2023-03-10

### Added

- Added currency symbols for major currencies

## [4.2.0] - 2023-03-03

### Added

- Add qudt namespace to Qudt.NAMESPACES

### Changed

- Updated QUDT to [v2.1.25](https://github.com/qudt/qudt-public-repo/releases/tag/v2.1.25)

## [4.1.0] - 2023-02-13

### Added

- Corresponding unit in system functionality - finds the best matching units to a given unit in a desired unit system, such as the corresponding unit for inch in the SI system (centimeter).

## [4.0.0] - 2023-02-07

### Added

- SystemOfUnits as a model class
- Several manual links from units to their base units or factor units

### Changed

- BREAKING: Made QUDTlib data model immutable, changed instantiation to force use of builders.

## [3.1.0] - 2023-01-20

### Added

- Temporarily added quantitykinds `PositivePlaneAngle, NonNegativeLength, PositiveLength and Count` until upstream [upstream PR 630](https://github.com/qudt/qudt-public-repo/pull/630) and [upstream PR 631](https://github.com/qudt/qudt-public-repo/pull/631) are released.

### Changed

- Updated QUDT to [v2.1.24](https://github.com/qudt/qudt-public-repo/releases/tag/v2.1.24)

## [3.0.2] - 2022-12-06

### Added

- Add quantity kinds for positive dimensionless ratio and normalized dimensionless ratio temporarily until the canges from the [upstream PR](https://github.com/qudt/qudt-public-repo/pull/619) are released.

## [3.0.1] - 2022-12-01

### Changed

- Fix bug in unit comparison

## [3.0.0] - 2022-11-30

### Added

- Add QuantityKinds and Units required to map IFC to QUDT. These are added temporarily until an upstream release of QUDT.

### Changed

- BREAKING: Modify the selection of units matching a set of factor units. The matching algorithm as well as the scoring algorithm of matching units was adapted. With this change, there are only two options for the client: get all matching units or get the one that matches best.
- Upgrade to QUDT [v2.1.22](https://github.com/qudt/qudt-public-repo/releases/tag/v2.1.22). All data temporariy added via `/qudtlib-data-gen/src/main/resources/triples-to-add-to-(units|quantitykinds).ttl` while waiting for PRs to be released upstream have been removed.

## [2.2.0] - 2022-10-28

### Changed

- Update QUDT to [2.1.21](https://github.com/qudt/qudt-public-repo/releases/tag/v2.1.21)
- Improve the release process on GitHub

## [2.1.1] - 2022-10-25

### Added

- Add triples missing in Qudt v2.1.20 temporarily until the [upstream fix](https://github.com/qudt/qudt-public-repo/pull/594) is released.

### Changed

- All `(add|set).+` methods of `Unit`, `QuantityKind`, and `Prefix` now require non-null parameters. Not performing this check led to a [bug in QUDT v2.1.20](https://github.com/qudt/qudt-public-repo/issues/593) to slip through.

## [2.1.0] - 2022-10-24

### Changed

- Update QUDT to [2.1.20](https://github.com/qudt/qudt-public-repo/releases/tag/v2.1.20)

### Fixed

- Fix MW/mW derivation bug (#27) by adding missing QUDT triples manually (until the [upstream fix](https://github.com/qudt/qudt-public-repo/pull/592) is released)

## [2.0.1] - 2022-10-21

### Changed

- Improve Changelog handling during release

## [2.0.0] - 2022-10-20

### Added

- Add module `qudtlib-js-gen`, which generates Typescript code to instantiate units, quantitykinds and prefixes for `qudtlib/qudtlib-js`
- Add project `qudtlib-js` as a git submodule to allow the build process to generate javascript into that project
- Add `Qudt.*required` static methods that return e.g. `Unit`, not `Optional<Unit>` and throw an Exception if there is nothing to return.

### Changed

- A number of breaking changes without prior deprecations, sorry about that.
- Upgrade to QUDT v2.1.19
- Move any code from `Qudt` to model classes (`Unit`, `QuantityValue`, etc.) if the code does not directly depend on pre-instantiated units.
- Refactor `Qudt.*` static methods to return `Optional`s where appropriate
- Made `Qudt.convert(BigDecimal, Unit, Unit)` return `BigDecimal` (was:`QuantityValue`) while `Qudt.convert(QuantityValue, Unit)` still returns a `QuantityValue`. It's cleaner: you get what you provide.
- Refactor factor unit matching (fixing some bugs in the process)
- Fix errors in RDF generation
- Fix errors in generation of constants (such as `Qudt.Units.M`)
- Improve unit tests
- Change `toString()` methods in a number of model classes for better readability
- Upgrade dependencies to latest versions

### Removed

- Remove unused code

## 1.0.0 - 2022-07-21

### Added

- Initial release.

[Unreleased]: https://github.com/qudtlib/qudtlib-java/compare/v7.0.0-alpha.3...HEAD
[7.0.0-alpha.3]: https://github.com/qudtlib/qudtlib-java/compare/v6.8.1...v7.0.0-alpha.3
[6.8.1]: https://github.com/qudtlib/qudtlib-java/compare/v6.8.0...v6.8.1
[6.8.0]: https://github.com/qudtlib/qudtlib-java/compare/v6.7.0...v6.8.0
[6.7.0]: https://github.com/qudtlib/qudtlib-java/compare/v6.5.0...v6.7.0
[6.5.0]: https://github.com/qudtlib/qudtlib-java/compare/v6.4.3...v6.5.0
[6.4.3]: https://github.com/qudtlib/qudtlib-java/compare/v6.4.2...v6.4.3
[6.4.2]: https://github.com/qudtlib/qudtlib-java/compare/v6.4.1...v6.4.2
[6.4.1]: https://github.com/qudtlib/qudtlib-java/compare/v6.4.0...v6.4.1
[6.4.0]: https://github.com/qudtlib/qudtlib-java/compare/v6.3.0...v6.4.0
[6.3.0]: https://github.com/qudtlib/qudtlib-java/compare/v6.2.0...v6.3.0
[6.2.0]: https://github.com/qudtlib/qudtlib-java/compare/v6.1.0...v6.2.0
[6.1.0]: https://github.com/qudtlib/qudtlib-java/compare/v6.0.3...v6.1.0
[6.0.3]: https://github.com/qudtlib/qudtlib-java/compare/v6.0.2...v6.0.3
[6.0.2]: https://github.com/qudtlib/qudtlib-java/compare/v6.0.1...v6.0.2
[6.0.1]: https://github.com/qudtlib/qudtlib-java/compare/v6.0.0...v6.0.1
[6.0.0]: https://github.com/qudtlib/qudtlib-java/compare/v5.0.1...v6.0.0
[5.0.1]: https://github.com/qudtlib/qudtlib-java/compare/v5.0.0...v5.0.1
[5.0.0]: https://github.com/qudtlib/qudtlib-java/compare/v4.3.0...v5.0.0
[4.3.0]: https://github.com/qudtlib/qudtlib-java/compare/v4.2.0...v4.3.0
[4.2.0]: https://github.com/qudtlib/qudtlib-java/compare/v4.1.0...v4.2.0
[4.1.0]: https://github.com/qudtlib/qudtlib-java/compare/v4.0.0...v4.1.0
[4.0.0]: https://github.com/qudtlib/qudtlib-java/compare/v3.1.0...v4.0.0
[3.1.0]: https://github.com/qudtlib/qudtlib-java/compare/v3.0.2...v3.1.0
[3.0.2]: https://github.com/qudtlib/qudtlib-java/compare/v3.0.1...v3.0.2
[3.0.1]: https://github.com/qudtlib/qudtlib-java/compare/v3.0.0...v3.0.1
[3.0.0]: https://github.com/qudtlib/qudtlib-java/compare/v2.2.0...v3.0.0
[2.2.0]: https://github.com/qudtlib/qudtlib-java/compare/v2.1.1...v2.2.0
[2.1.1]: https://github.com/qudtlib/qudtlib-java/compare/v2.1.0...v2.1.1
[2.1.0]: https://github.com/qudtlib/qudtlib-java/compare/v2.0.1...v2.1.0
[2.0.1]: https://github.com/qudtlib/qudtlib-java/compare/v2.0.0...v2.0.1
[2.0.0]: https://github.com/qudtlib/qudtlib-java/compare/v1.0.0...v2.0.0

