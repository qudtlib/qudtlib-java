# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

- Upgrade to QUDT v2.1.19
- Upgrade to RDF4J 4.2.0
- Move any code from `Qudt` to model classes (`Unit`, `QuantityValue`, etc.) if the
code does not directly depend on pre-instantiated units.
- Refactor factor unit matching (fixing some bugs in the process)
- Fix errors in RDF generation
- Fix errors in constants generation
- Remove unused code
- Add module `qudtlib-js-gen`, which generates Typescript code to instantiate units, quantitykinds and prefixes for `qudtlib/qudtlib-js`
- Improve unit tests
- Change `toString()` methods in a number of model classes for better readability

## [1.0.0] - 2022-07-21

Initial release.
