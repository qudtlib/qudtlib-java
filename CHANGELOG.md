# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

## [1.0.0] - 2022-07-21

### Added

- Initial release.

[Unreleased]: https://github.com/qudtlib/qudtlib-java/compare/v2.0.0...HEAD
[2.0.0]: https://github.com/qudtlib/qudtlib-java/compare/v1.0.0...v2.0.0
[1.0.0]: https://github.com/qudtlib/qudtlib-java/releases/tag/v1.0.0

