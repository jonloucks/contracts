# Contracts 
A lightweight, type-safe Java library for dependency inversion and inversion of control.

## Overview

**Contracts** provides a modern approach to decoupling API definitions from their implementations using a Contract-based system. This library enables clean architecture by separating what you need (the contract) from how it's provided (the implementation), without requiring reflection, annotations, or dependency injection frameworks.

### Key Features

- **Type-Safe Contracts**: Generic-based contracts eliminate casting and provide compile-time safety
- **Zero Reflection**: No reflection, annotations, or bytecode manipulation required
- **Java Module System**: Full JPMS support with strong encapsulation
- **Lifecycle Management**: Built-in reference counting and resource management
- **Framework Agnostic**: Works standalone or alongside Spring Boot, Guice, or other DI frameworks
- **SOLID Principles**: Designed around dependency inversion, interface segregation, and single responsibility
- **Security Focused**: OpenSSF Best Practices compliant with contract-level access control
- **Multi-Module Architecture**: Clean separation between API (`contracts-api`), implementation (`contracts-impl`), and testing (`contracts-test`)

## Quick Start

The Contract pattern has three simple steps:

### 1. Author a Contract
Define a contract as a unique key that represents an agreement between providers and consumers:

```java
// Define a contract for a service - each contract is a unique key
public static final Contract<WeatherService> WEATHER_SERVICE = 
    Contract.create("WeatherService");
```

### 2. Bind (Promise) an Implementation
A provider binds an implementation to the contract using a `Promisor`:

```java
// Bind an implementation to the contract
GlobalContracts.bindContract(WEATHER_SERVICE, () -> new WeatherServiceImpl());

// Or with a singleton pattern
GlobalContracts.bindContract(WEATHER_SERVICE, 
    GlobalContracts.singleton(() -> new WeatherServiceImpl()));
```

### 3. Claim (Consume) the Implementation
Consumers claim the implementation through the contract:

```java
// Claim the implementation - type-safe, no casting needed
WeatherService service = GlobalContracts.claimContract(WEATHER_SERVICE);
String forecast = service.getForecast();
```

## Core Concepts

### Contracts
A `Contract<T>` is a unique key that establishes an agreement between a provider (who binds) and a consumer (who claims). Each contract instance is unique by identity, ensuring proper encapsulation.

### Promisors
A `Promisor<T>` is a functional interface that provides the implementation. It supports:
- **Simple factories**: Return a new instance on each claim
- **Singletons**: Return the same instance (via `GlobalContracts.singleton()`)
- **Lifecycle management**: Reference-counted resources (via `GlobalContracts.lifeCycle()`)

### Global vs Local Contracts
- **GlobalContracts**: Singleton instance for application-wide contracts
- **Contracts.createContracts()**: Create isolated contract repositories for testing or modularity
## Advanced Features

### Reference Counting & Lifecycle Management
```java
// Lifecycle-managed promisor with automatic open/close
Promisor<Database> dbPromisor = GlobalContracts.lifeCycle(() -> new Database());
AutoClose unbind = GlobalContracts.bindContract(DB_CONTRACT, dbPromisor);

// When done, unbind to decrement reference count
unbind.close();
```

### Contract Configuration
```java
// Create a contract with custom configuration
Contract<Service> contract = Contract.create(Service.class, builder -> {
    builder.name("MyService")
           .replaceable(true);  // Allow rebinding
});
```

### Partners (Aggregated Contracts)
Create contract repositories that delegate to parent repositories for shared contracts.

## Project Structure

The library is organized into multiple Gradle submodules:

- **`contracts-api`**: Core API interfaces and contracts (no dependencies)
- **`contracts-impl`**: Default implementation of the Contracts system
- **`contracts-test`**: Reusable test utilities for testing contract-based code
- **`contracts-smoke`**: Smoke tests for validation
- **Root module**: Aggregates API and implementation for easy consumption

## Installation

### Gradle
```gradle
dependencies {
    implementation 'io.github.jonloucks.contracts:contracts:2.5.2'
}
```

### Maven
```xml
<dependency>
    <groupId>io.github.jonloucks.contracts</groupId>
    <artifactId>contracts</artifactId>
    <version>2.5.2</version>
</dependency>
```

## Use Cases

- **Plugin Architectures**: Define extension points without tight coupling
- **Testing**: Easily mock dependencies by binding test implementations
- **Modular Applications**: Decouple modules using contracts as boundaries
- **Library Development**: Provide APIs without exposing implementations
- **Configuration Management**: Bind different implementations based on environment
## Documentation & Resources

* [Javadoc API Documentation](https://jonloucks.github.io/contracts/javadoc/)
* [Test Coverage Report](https://jonloucks.github.io/contracts/jacoco/)
* [License](LICENSE.md)
* [Contributing Guidelines](CONTRIBUTING.md)
* [Code of Conduct](CODE_OF_CONDUCT.md)
* [Coding Standards](CODING_STANDARDS.md)
* [Security Policy](SECURITY.md)
* [Pull Request Template](PULL_REQUEST_TEMPLATE.md)

## Badges

[![OpenSSF Best Practices](https://www.bestpractices.dev/projects/11290/badge)](https://www.bestpractices.dev/projects/11290)
[![Coverage Badge](https://raw.githubusercontent.com/jonloucks/contracts/refs/heads/badges/main-coverage.svg)](https://jonloucks.github.io/contracts/jacoco/)
[![Javadoc Badge](https://raw.githubusercontent.com/jonloucks/contracts/refs/heads/badges/main-javadoc.svg)](https://jonloucks.github.io/contracts/javadoc/)

## Requirements

- **Deployment**: Java 11+ (LTS)
- **Compilation**: Java 9+ language level
- **Module System**: Full JPMS support

## Design Principles

The library adheres to these core principles:
1. **SOLID Principles**: Dependency inversion, interface segregation, single responsibility
2. **Security First**: OpenSSF Best Practices with contract-level access control
3. **Strong Encapsulation**: Proper data hiding and module boundaries
4. **Universal Compatibility**: Use anywhere, anytime across many codebases
5. **Type Safety**: No casting needed, no unchecked surprises
6. **Privacy by Default**: Contracts are only visible to the author unless explicitly shared
7. **LTS Support**: Deploys on oldest supported LTS Java version (currently 11) with JPMS
8. **Minimal Language Requirements**: Compiles with Java 9 language level
9. **Zero Magic**: No reflection, injection, or annotations required
10. **Testability**: Promotes black-box testing, leading to test reuse and maintainability
11. **Framework Neutral**: Works standalone or with Spring Boot, Guice, etc.

## Why Contracts?

Traditional dependency injection frameworks often require:
- Reflection and runtime classpath scanning
- Framework-specific annotations
- Complex configuration
- Tight coupling to the DI container

**Contracts** offers a simpler alternative:
- ✅ Compile-time type safety
- ✅ Explicit, readable code
- ✅ No framework lock-in
- ✅ Works with Java modules
- ✅ Zero runtime overhead from reflection
- ✅ Perfect for libraries that need to avoid DI framework dependencies

## Building from Source

```bash
# Build the project
./gradlew build

# Run all tests with coverage
./gradlew check jacocoTestReport

# Publish to local Maven repository
./gradlew publishToMavenLocal
```

## License

See [LICENSE.md](LICENSE.md) for details.

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before submitting pull requests.

