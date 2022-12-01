# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

[Unreleased]: https://github.com/qudtlib/qudtlib-java/compare/v3.0.1...HEAD
[3.0.1]: https://github.com/qudtlib/qudtlib-java/compare/v3.0.0...v3.0.1
[3.0.0]: https://github.com/qudtlib/qudtlib-java/compare/v2.2.0...v3.0.0
[2.2.0]: https://github.com/qudtlib/qudtlib-java/compare/v2.1.1...v2.2.0
[2.1.1]: https://github.com/qudtlib/qudtlib-java/compare/v2.1.0...v2.1.1
[2.1.0]: https://github.com/qudtlib/qudtlib-java/compare/v2.0.1...v2.1.0
[2.0.1]: https://github.com/qudtlib/qudtlib-java/compare/v2.0.0...v2.0.1
[2.0.0]: https://github.com/qudtlib/qudtlib-java/compare/v1.0.0...v2.0.0

