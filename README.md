# contracts
Java Dependency Contracts for dependency inversion

Separates the consumption of an API from its implementation by using a 'Contract'

```
// Authoring
public static final Contact<String> WEATHER = Contract.create("Current Weather");
```
```
// Promissing
Contracts.bindContract(WEATHER, () -> "Raining");
```
```
// Consumption
String weather = Contracts.claimContract(WEATHER);
```


Goals
1. Security
2. Encapsulation
3. Use anywhere
4. Type safe. no casting needed. No uncheck surprises
5. By default a Contract is only visible to the author
6. Deploys on oldest supported LTS version of Java with JPMS. (currently 11)
7. Compiles with minimum Java language (currently 9)
8. Never impose reflection, injection, annotations etc
9. Promote black box testing, leading to test reuse and maintainability
10. Can be used in conjunction with spring-boot, Guice or other dependency libraries.

