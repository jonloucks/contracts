# contracts 
Java Dependency Contracts for dependency inversion 

### Separates the API from its implementation by using a Contract.

A core java library for dependency inversion, Dependency injection, Inversion of Control,
 Solid Principles: Single responsibility, Open-closed, Liskov substitution, Interface segregation, Dependency inversion.
 OpenSSF Best Practices. Java Module Support. Strong data encapsulation.
```
// Authoring
public static final Contact<String> WEATHER = Contract.create("Current Weather");
```
```
// Promissing
GlobalContracts.bindContract(WEATHER, () -> "Raining");
```
```
// Consumption
String weather = GlobalContracts.claimContract(WEATHER);
```
## Documentation and Reports
[Java API](https://jonloucks.github.io/contracts/javadoc/)

[Java Test Coverage](https://jonloucks.github.io/contracts/jacoco/)

## Badges
[![OpenSSF Best Practices](https://www.bestpractices.dev/projects/11290/badge)](https://www.bestpractices.dev/projects/11290)
[![Coverage Badge](https://raw.githubusercontent.com/jonloucks/contracts/refs/heads/badges/main-coverage.svg)](https://jonloucks.github.io/contracts/jacoco/)
[![Javadoc Badge](https://raw.githubusercontent.com/jonloucks/contracts/refs/heads/badges/main-javadoc.svg)](https://jonloucks.github.io/contracts/javadoc/)

## Goals
1. Solid Principles
2. Security
3. Encapsulation
4. Use anywhere, anytime across many code bases
5. Type safe. no casting needed. No unchecked surprises
6. By default, a Contract is only visible to the author
7. Deploys on oldest supported LTS version of Java with JPMS. (currently 11)
8. Compiles with minimum Java language (currently 9)
9. Never require reflection, injection, annotations etc
10. Promote black box testing, leading to test reuse and maintainability
11. Can be used in conjunction with spring-boot, Guice or other dependency libraries.

