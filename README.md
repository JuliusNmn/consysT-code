# consistency-types-impl
IMPL project WS 2017-18


## Installation

1. The project is compiled using Maven.
2. Open a terminal in the main folder consistency-types-impl
3. Run `mvn org.apache.maven.plugins:maven-dependency-plugin:properties`
4. Run `mvn install`



## The consistency checker
The consistency checker module contains a checker for the [checker framework](https://checkerframework.org/). `ConsistencyChecker` can be configured as an annotation processor for the java compiler. With the java annotations `@High` and `@Low`, consistency levels can be assigned to variables. The checker ensures, that no low values can flow into a value assigned to a high variable. The default level for unannotated variables is `@Low`.  
Example use:  
`@High int a;`  
`int b = 42;`  
`// This assignment is not allowed and will throw assignment.type.incompatible at compile time.`  
`a = b;`  
For further examples, refer to the testcases.
## Running the testcases
1. [build the checker framework](https://checkerframework.org/manual/#build-source)
2. clone consistency-types-impl
3. edit consistency-types-impl/consistencyCheckerTest/build.properties:  
`checkerframework=/path/to/checkerframework`  
`consistency-types-impl=/path/to/consistency-types-impl`
4. in consistency-types-impl/consistencyCheckerTest/ run:  
`$ ant consistency_tests`
## The cassandra connector
The Cassandra module contains a connector to the [Cassandra distributed database system](https://cassandra.apache.org/). It provides wrappers for java objects whose values are stored in cassandra. The wrappers ensure consistency on different levels between the java objects and the data in the database.  
`HighValue` objects are read from and written to ALL cassandra nodes, and each read and write access to the object is synchronized with the database. `LowValue` objects on the other hand are only written to a single cassandra node (although cassandras eventual consistency guarantees that the value will propagade over time, if no other value is written), and read accesses may be cached in the wrapper, so the read value may not be the latest written value. The only API methods of the wrappers are `value()` to read the current value and `setValue(value)` to set a new value, as well as a `perform(function)` method that executes a function with the wrappers current value as an argument. Everything else will be handled behind the scenes. To make this work, one has to provide a `java.util.function.Supplier` and a `java.util.function.Consumer` lambda function to the constructor that tells the wrapper how to read from and write to Cassandra respectively.  
Classes that have fields wrapped in `HighValue` or `LowValue` should extend the `ConsistencyObject` class, which allows reading and writing of all members at once, while the `ConsistencyObject` objects are not stored in Cassandra themselves. Additionaly there is a `CollectionWrapper` class that allows batch processing of collections of `ConsistencyObject` objects.  
It is possible to have cyclic dependencies, i.e. a `ConsistencyObject` that has another `ConsistencyObject` inside a wrapper. It is ensured, that reading and writing is only performed once so that no infinite loop occures. For an example refer to the `CycleTest` class.  
The wrappers are compatible with the consistency annotations. For example, the value of a `HighValue` will always be annotated with `@High` and no `@Low` value can be assigned to a `HighValue` wrapper. A simple example application with a bank and customers is included.
