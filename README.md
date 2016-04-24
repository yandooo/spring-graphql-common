<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

- [Spring Framework GraphQL Library](#spring-framework-graphql-library)
- [Intro](#intro)
- [Requires](#requires)
- [Usage](#usage)
  - [Supported types](#supported-types)
  - [Creating a new Object Type](#creating-a-new-object-type)
  - [Creating a new Interface Type](#creating-a-new-interface-type)
  - [Creating a new Enum Type](#creating-a-new-enum-type)
  - [Creating a new Union Type](#creating-a-new-union-type)
  - [Creating a Object-Input Type](#creating-a-object-input-type)
  - [Mutations](#mutations)
  - [Spring configuration](#spring-configuration)
  - [Protection Against Malicious Queries](#protection-against-malicious-queries)
    - [Query Complexity Analysis](#query-complexity-analysis)
    - [Limiting Query Depth](#limiting-query-depth)
  - [Relay Support](#relay-support)
  - [Contributions](#contributions)
- [Acknowledgment](#acknowledgment)
- [License](#license)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Spring Framework GraphQL Library

[![Build Status](https://travis-ci.org/oembedler/spring-graphql-common.svg?branch=master)](https://travis-ci.org/oembedler/spring-graphql-common)   [ ![Download](https://api.bintray.com/packages/oembedler/maven/spring-graphql-common/images/download.svg) ](https://bintray.com/oembedler/maven/spring-graphql-common/_latestVersion)

# Intro

Initially project was inspired by multiple projects 

* [graffiti](https://github.com/RisingStack/graffiti)
* [graffiti-mongoose](https://github.com/RisingStack/graffiti-mongoose)
* [sangria-relay](https://github.com/sangria-graphql/sangria-relay)
+ multiple graphql-codegen projects (python-graphene, graphql-swift etc.)

and main idea was transformed into generic lightweight mechanism for schema definition in java.
It will be helpful for those who starts coding schema from scratch or upgrading technology stack moving to GraphQL. 

The library facilitates GraphQL schema development in SpringFramework environment heavily using annotations.
The entire schema lives in Spring context which means developer can leverage all Spring features like AOP, IO etc.
There is one important dependency on [graphql-java](https://github.com/andimarek/graphql-java) for schema imperative building and GraphQL query execution.
The query execution strategy for the READ and MUTATE queries is based on Reactor Reactive Streams with high level of parallelism.

**Note**: _process has been started on moving to complete Reactor RS Stack leveraging Netty GraphQL NIO server (spring boot starters should be updated as well). 
Reactor execution strategy will be strategic one available in the future releases. 
RxJava-based strategies are deprecated and won't be maintained anymore._

`graphql-spring-boot-starter` and `graphiql-spring-boot-starter` are available in [graphql-spring-boot repository](https://github.com/oembedler/graphql-spring-boot).

# Requires

  * Java 1.8
  * Spring Framework v4.x (core & context)
  * java-graphql v2.0+
  * Reflections v0.9.10
  * Apache Commons Lang v3.4
  * Jackson v2.6.x
  * RxJava v1.1.1  **deprecated**
  * RxJava Math v1.0.0  **deprecated**
  * Slf4j 

some of the dependencies can be removed in the future.

```gradle
repositories {
    // stable build
    jcenter()
    // development build
    maven { url  "http://dl.bintray.com/oembedler/maven" }
}
```

Dependency:

```gradle
dependencies {
  compile 'com.embedler.moon.graphql:spring-graphql-common:INSERT_LATEST_VERSION_HERE'
}
```

How to use the latest build with Maven:

```xml
<repository>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
    <id>bintray-oembedler-maven</id>
    <name>bintray</name>
    <url>http://dl.bintray.com/oembedler/maven</url>
</repository>
```

Dependency:

```xml
<dependency>
    <groupId>com.embedler.moon.graphql</groupId>
    <artifactId>spring-graphql-common</artifactId>
    <version>LATEST_VERSION_HERE</version>
</dependency>
```


# Usage

The entire schema definition is annotations driven. 
There is a good support for generic types - so don't miss chance to use them if it's appropriate (see `Relay Support` as an example).
Class level annotations (like `@GraphQLSchema`, `@GraphQLObject` etc) are Spring `@Component` annotations. 
 
```java
@GraphQLSchema
public class TodoSchema {

    @GraphQLSchemaQuery
    private RootObjectType root;

    public static class AddTodoIn {
        private String text;
    }
    
    // default value provider for an input mutation parameter 
    public AddTodoIn getAddTodoInputDefaultValue() {
        AddTodoIn addTodoInput = new AddTodoIn();
        addTodoInput.setText("--- default text ---");
        return addTodoInput;
    }

    @GraphQLMutation
    public
    @GraphQLOut("todoEdge")
    TodoObjectType.TodoEdgeObjectType addTodoMutation(@GraphQLIn("addTodoInput", defaultProvider = "getAddTodoInputDefaultValue") AddTodoIn addTodoInput) {
    
        // mutation implementaion goes here
        
        return todoEdgeObjectType;
    }
```

Use `@GraphQLSchemaQuery` to set root query for the schema.

## Supported types

All Java wrapper types directly map to GraphQL types:

* String -> GraphQLString
* Character -> GraphQLString
* Boolean -> GraphQLBoolean
* Integer -> GraphQLInt
* Long -> GraphQLLong
* Float -> GraphQLFloat

same for corresponding Java primitives.
`Long`, `Float` types are passed in the GraphQL queries as a `Strings`. 
It's due to underlying query parser implementation.

Extensions:

* Double -> GraphQLDouble
* Date -> GraphQLDateType (format defined in configuration)
* LocalDateTime (JDK 8) -> GraphQLLocalDateTimeType (format defined in configuration)
* Timestamp -> GraphQLTimestamp (milliseconds since 1970 1st Jan)

`Double`, `Date` and `Timestamp` values are passed in the GraphQL queries as a `String` values.

* Java `Enum` type is directly mapped to `GraphQLEnumType`.
* Java `Interface` is mapped to `GraphQLInterfaceType` if marked with `GraphQLInterface` annotation.
* Java `Interface` is mapped to `GraphQLUnionType` if marked with `GraphQLUnion` annotation.
* Java `List` and `Array` collections are automatically wrapped into `GraphQLList`.
* GraphQL `Non-null` element can be marked as such using `@GraphQLNonNull` annotation.
* `GraphQL ID` element can be marked as such using `@GraphQLID` annotation.

## Creating a new Object Type

`@GraphQLObject` annotation is used to mark class as `GraphQLObjectType`.
Recursive object type references are handled automatically.

```java
@GraphQLObject("Root")
public class RootObjectType {

    @GraphQLNonNull
    @GraphQLDescription("Root query version number")
    private String version;

    @GraphQLField
    @GraphQLDescription("Root viwer node as per Relay spec")
    public UserObjectType viewer(/** no input expected **/) {
    
        // custom data fetcher for the field with name 'viewer'
        
        return userObjectType;
    }
}
```

If class field is accessible through getters \ setters - define field as a class member. 
All class fields are included into object definition unless `@GraphQLIgnore` annotation is used.
If field needs custom data fetcher - define field as a class method and mark class with `@GraphQLField` annotation.
`@GraphQLDescription` annotation can be used in most likely any context to set description for the GraphQL schema element.

## Creating a new Interface Type

If interface must be considered as a part of the object hierarchy use `GraphQLInterface` annotation.
Interfaces not marked with the annotation are ignored. 

```java
@GraphQLInterface("Node")
public interface RelayNode {

    @GraphQLID("id")
    @GraphQLNonNull
    @GraphQLDescription("GraphQL Relay global object unique identifier")
    String getId(RelayNode relayNode);
}
```

## Creating a new Enum Type

Java enums are automatically discovered and mapped to `GraphQLEnumType`.

```java
    // annotation is not required if enum names are acceptable as values
    @GraphQLEnum(valueProvider = "getValue")
    public enum Episode {
        @GraphQLDescription("Released in 1977.")
        NEWHOPE,
        @GraphQLDescription("Released in 1980.")
        EMPIRE,
        @GraphQLDescription("Released in 1983.")
        JEDI;

        // enum field value provider must be static method with 1 input argument - its own enumeration type
        public static Object getValue(Episode self) {
            if (self == NEWHOPE)
                return 4;
            else if (self == EMPIRE)
                return 5;
            else
                return 6;
        }
    }
```

`@GraphQLEnum` is not required for enum defition as Java enums are discovered automatically by library.
On the other side using that annotation enum name and value can be changed. 
Note optional `valueProvider` annotation element should point to static method in enum which accepts one single argument - 
enum itself and returns value for the enum.
The return value can be of any desired type.
Also default value for enum can be provided through SpEL expression as follows:

```java
    // avaliable context objects are `obj` (enum instance itself) and `cls` - enum class
    @GraphQLEnum(defaultSpel = "#obj.getActualValue()")
    public enum EpisodeV2 {
        @GraphQLDescription("Released in 1977.")
        NEWHOPE(4),
        @GraphQLDescription("Released in 1980.")
        EMPIRE(5),
        @GraphQLDescription("Released in 1983.")
        JEDI(6);

        int actualValue;

        EpisodeV2(int val) {
            actualValue = val;
        }

        public int getActualValue() {
            return actualValue;
        }
    }
```

## Creating a new Union Type

GraphQL Union type is a java interface marked with `@GraphQLUnion` annotation.
While annotation value (type name) can be empty `possibleType` annotation field must contain list of possible types for union type.

```java
    @GraphQLUnion(value = "Pet", possibleTypes = {Dog.class, Cat.class})
    public interface PetsUnionType {
       // empty marker
       // possible types must implement this interface
    }
    
    //...
    
    @GraphQLField("pets")
    public List<PetsUnionType> getPets() {
       List<PetsUnionType> pets = new ArrayList<>();
       pets.addAll(cats);
       pets.addAll(dogs);
       return pets;
    }    
```

## Creating a Object-Input Type

Input method arguments are automatically converted into `GraphQLInputObjectType`.
All expected input arguments to either mutation or data fetcher have to be marked with `@GraphQLIn` annotation.
Names for the method parameters can be automatically discovered using `org.springframework.core.DefaultParameterNameDiscoverer`.
However it's recommended to define parameter name in `@GraphQLIn` annotation value field.
To provide default value for an input parameter the `defaultProvider` annotation field should point to the local class method (either static or instance).
The return value of the method must be of the same type (derived type) as defined in a mutation method signature.

```java
    @GraphQLField
    public TodoObjectType.TodoConnectionObjectType todos(@GraphQLIn RelayConnectionArguments args) {
       
       // `args` is extracted from arguments context
       
    }
```

```java
    @GraphQLField("manager")
    public UserObjectType getManager(@GraphQLIn UserObjectType employee) {
    
       // `employee` is extracted from upstream 'source' element as input parameters are empty
    
    }
```

## Mutations

Mutation must be declared in the class marked as `@GraphQLSchema` annotation.
Use `@GraphQLMutation` annotation to mark method as a GraphQL Mutation.
Input method parameters are input `variables`.
Method returned result is a mutation nested nodes.

```java

    // default value provider for an input mutation parameter 
    public AddTodoIn getAddTodoInputDefaultValue() {
        AddTodoIn addTodoInput = new AddTodoIn();
        addTodoInput.setText("--- default text ---");
        return addTodoInput;
    }

    @GraphQLMutation("addTodoMutation")
    public
    @GraphQLOut("todoEdge")
    TodoObjectType.TodoEdgeObjectType addTodoMutation(@GraphQLIn("addTodoInput", defaultProvider = "getAddTodoInputDefaultValue") AddTodoIn addTodoInput, AddTodoIn2 addTodoInput2) {
    
        // `addTodoInput` created based on input `variables`
        // `addTodoInput2` is skipped as it's not marked explicitly as `@GraphQLIn` parameter
    
    }
```

Value for `@GraphQLMutation` is optional. If omitted - method name is used as a name of a mutation.
`@GraphQLOut` is used to give a name for a mutation output type.
All expected input variables have to be marked with `@GraphQLIn` annotation.
To provide default value for an input parameter the `defaultProvider` annotation field should point to the local class method (either static or regular).
The return value of the method must be of the same type (derived type) as defined in mutation method signature.
Also default value can be provided using SpEL expression:

```java
    @GraphQLField("manager")
    public UserObjectType getManager(UserObjectType employee,
                                     @GraphQLIn(value = "ids", defaultSpel = "T(java.util.Collections).EMPTY_SET") Set<String> ids) {
        // omitted for simplicity
    }

```

## Spring configuration

```java
    // there must be either `@ComponentScan` annotation defined for a schema base package 
    // or all beans must be instantiated explicitly in configuration class
    @Configuration
    @ComponentScan(basePackages = "com.oembedler.moon.graphql.test.todoschema")
    public static class TodoSchemaConfiguration {

        // use as is
        @Bean
        public GraphQLSchemaBeanFactory graphQLSchemaBeanFactory() {
            return new SpringGraphQLSchemaBeanFactory();
        }

        // configuration can be customized depending on the case
        @Bean
        public GraphQLSchemaConfig graphQLSchemaConfig() {
            GraphQLSchemaConfig graphQLSchemaConfig = new GraphQLSchemaConfig();
            return graphQLSchemaConfig;
        }

        // use as is
        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder() {
            return new GraphQLSchemaBuilder(graphQLSchemaConfig(), graphQLSchemaBeanFactory());
        }

        // use as is
        @Bean
        public GraphQLSchemaHolder graphQLSchemaHolder() {
            return graphQLSchemaBuilder().buildSchema(TodoSchema.class);
        }
    }
```

Executing queries async:

```java
        RxExecutionResult result = GraphQLQueryExecutor
                                             .create(graphQLSchemaHolder)
                                             .query("{viewer{ id }}")
                                             .execute();
        // work with execution result
```

Executing queries async with concurrent fields resolution 
(see `GraphQLQueryExecutor.forkJoinExecutorService()` or 
`GraphQLQueryExecutor.forkJoinExecutorService(int parallelism)`):

```java

        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor.builder()
                        .create(graphQLSchemaHolder)
                        .forkJoinExecutorService()
                        .query("{viewer{ id }}")
                        .execute();
```

check `GraphQLQueryExecutor` class to find more ways how to run queries.

Schema build process can be customized using `com.oembedler.moon.graphql.engine.GraphQLSchemaConfig`:

```java

    private String clientMutationIdName = "clientMutationId";
    private boolean injectClientMutationId = true;
    private boolean allowEmptyClientMutationId = false;
    private String mutationInputArgumentName = "input";
    private String outputObjectNamePrefix = "Payload";
    private String inputObjectNamePrefix = "Input";
    private String schemaMutationObjectName = "Mutation";
    private boolean dateAsTimestamp = true;
    private String dateFormat = "yyyy-MM-dd'T'HH:mm'Z'";

    // there will be more config options added in the future
```

## Protection Against Malicious Queries

Since typical GraphQL schemas contain recursive types and circular dependencies, 
clients are able to send infinitely deep queries which may have high impact on server performance. 
The library provides two mechanisms to protect your GraphQL server from malicious or too expensive queries.

### Query Complexity Analysis

Query complexity analysis makes an estimation of the query complexity **during execution**. 
The complexity is `Double` number that is calculated according to the simple rule described below.
Every field in the query gets a default score `1.0` (including `GraphQLObjectType` nodes). 
The `complexity` of the query is the sum of all field scores.

```java
    public Integer getFirstDefaultValue() {
        return 1;
    }

    public Integer getLastDefaultValue() {
        return 1;
    }

    // `before`, `after`, `first`, `last` and `childScore` are avaliable in SpEL expression
    @GraphQLField
    @GraphQLComplexity("1 + first * #childScore")
    public TodoConnectionObjectType todos(@GraphQLIn(value = "before") String before,
                                          @GraphQLIn(value = "after") String after,
                                          @GraphQLIn(value = "first", defaultProvider = "getFirstDefaultValue") Integer first,
                                          @GraphQLIn(value = "last", defaultProvider = "getLastDefaultValue") Integer last) {

        // implementation ommitted for the sake of simplicity        

        return todoConnectionObjectType;
    }

```

Note that above example has `GraphQLComplexity` annotation value is a Spring SpEL expression.
This annotation can be used to customize complexity calculation for a node.
SpEL expression context has field input parameters and `childScore` parameter which is sum of all child fields scores.

To set `maxQueryComplexity` use `GraphQLQueryExecutor`:

```java
   GraphQLQueryExecutor.create(graphQLSchemaHolder).maxQueryComplexity(1500);
```

The query complexity algorithm is _dynamic_ so typical `introspection` query doesn't have static permanent complexity - 
having more fields definitions, arguments and objects in a schema causes complexity to grow.

During execution when maximum query complexity reached - library throws an `QueryComplexityLimitExceededRuntimeException` exception. 

### Limiting Query Depth

Limiting query depth can be done by providing the `maxQueryDepth` argument to the `GraphQLQueryExecutor`:

```java
GraphQLQueryExecutor.create(graphQLSchemaHolder).maxQueryDepth(4);
```

When maximum query depth is reached library __does not throw any exception__ but returns `null` for unresolved field(s).

## Relay Support

Library adds abstractions for the Relay support.
Please look at the tests for `TodoSchema` for an example. 
There are ways how to extend Relay classes to get custom behaviour.

Some tips:

```java
@GraphQLObject("Todo")
public class TodoObjectType extends BaseObjectType {

    // fields definitions are omitted for clarity 

    @GraphQLObject
    public static class TodoEdgeObjectType extends EdgeObjectType<TodoObjectType> {
    
      // `EdgeObjectType` is generic class that can be extended to add custom behaviour
      
    }

    @GraphQLObject
    public static class TodoConnectionObjectType extends ConnectionObjectType<TodoEdgeObjectType, PageInfoObjectType> {
    
    // `ConnectionObjectType` is generic class that can be extended to add custom behaviour
    
    }
}
```

Node interface (given schema uses proper hierarchy of objects):

```java

// defined in library `relay` package
@GraphQLInterface("Node")
public interface RelayNode {

    @GraphQLID("id")
    @GraphQLNonNull
    @GraphQLDescription("GraphQL Relay global object unique identifier")
    String getId(RelayNode relayNode);
}

```

All custom objects implement that interface through intermediate base class (no need to implement default bahaviour in each class):

```java
public class BaseObjectType implements RelayNode {

    @GraphQLIgnore
    private String id;

    @GraphQLID("id")
    @GraphQLNonNull
    @GraphQLDescription("Global object unique identifier")
    public String getId(RelayNode relayNode) {
        BaseObjectType baseObjectType = (BaseObjectType) relayNode;
        
        // `id` can be encoded into base64 if opaque value is required
        
        return baseObjectType.id;
    }
}

```

Data resolver is defined in a root object query as follows:

```java
@GraphQLObject("Root")
public class RootObjectType {

    @GraphQLField
    public RelayNode node(@GraphQLID @GraphQLNonNull @GraphQLIn("id") final String id) {
    
        // data resolver by global ID goes here
        
        return null;
    }
    
}
```

Relay `ConnectionArguments` can be passed in multiple ways to a data resolver.

```java
    @GraphQLField
    public TodoConnectionObjectType todos(@GraphQLIn(value = "before") String before,
                                          @GraphQLIn(value = "after") String after,
                                          @GraphQLIn(value = "first") Integer first,
                                          @GraphQLIn(value = "last") Integer last) {}
```

Complex (nested) input objects are also supported.

```java
    public class RelayConnectionArguments {
        public String before;
        public String after;
        public Integer first;
        public Integer last;
        // ...
    }

    @GraphQLField
    public TodoConnectionObjectType todos(@GraphQLIn("connArgs") RelayConnectionArguments) {
         // when query for a data arguments must be passed as embedded input argument `connArgs` 
         // { ... todos(connArgs{first: 10}){...} ... }
    }
```

Note using wrapper object for arguments make them surface from that wrapper node but not from input root. 

In general the library does not force to build Relay compliant schemas - 
it's up to developer to decide if this compatibility should be maintained. 

## Contributions

Contributions are welcome.

Tips:

- Respect the [Code of Conduct](http://contributor-covenant.org/version/1/3/0/).
- Before opening an Issue to report a bug, please try the latest development version. 
It might happen that the problem is already solved.
- Please use  Markdown to format your comments properly. 
If you are not familiar with that: [Getting started with writing and formatting on GitHub](https://help.github.com/articles/getting-started-with-writing-and-formatting-on-github/)
- For Pull Requests:
  - Here are some [general tips](https://github.com/blog/1943-how-to-write-the-perfect-pull-request)
  - Please be a as focused and clear as possible and don't mix concerns. 
    This includes refactorings mixed with bug-fixes/features, see [Open Source Contribution Etiquette](http://tirania.org/blog/archive/2010/Dec-31.html) 
  - It would be good to add an automatic test(s). 
  
# Acknowledgment

This implementation is based on the java reference implementation. 
For example the `TodoSchema` and the tests (among a lot of other things) are simply adapted to use with library code.

# License

`spring-graphql-common` is licensed under the MIT License. See [LICENSE](LICENSE.md) for details.

[graphql-java License](https://github.com/andimarek/graphql-java/blob/master/LICENSE.md)

[graphql-js License](https://github.com/graphql/graphql-js/blob/master/LICENSE)
