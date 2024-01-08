# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed

- `unit:MHO`, `unit:MicroMHO`: fix dimension vector and quantity kind
- `unit:F`: fix `unit:isScalingOf`

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

[Unreleased]: https://github.com/qudtlib/qudtlib-java/compare/v6.1.0...HEAD
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

