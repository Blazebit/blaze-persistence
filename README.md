<img src="https://persistence.blazebit.com/images/blaze_persistence_logo_colors_render.png" width="200" />

[![Build Status](https://github.com/Blazebit/blaze-persistence/workflows/Blaze-Persistence%20CI/badge.svg)](https://github.com/Blazebit/blaze-persistence/actions)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.blazebit/blaze-persistence-core-impl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.blazebit/blaze-persistence-core-impl)
[![Zulip Chat](https://img.shields.io/badge/zulip-join_chat-brightgreen.svg)](https://blazebit.zulipchat.com)

[![Javadoc - Core](https://www.javadoc.io/badge2/com.blazebit/blaze-persistence-core-api/Core%20API%20Javadoc.svg?color=green)](http://www.javadoc.io/doc/com.blazebit/blaze-persistence-core-api)
[![Javadoc - Entity-View](https://www.javadoc.io/badge2/com.blazebit/blaze-persistence-entity-view-api/EntityView%20API%20Javadoc.svg?color=green)](http://www.javadoc.io/doc/com.blazebit/blaze-persistence-entity-view-api)
[![Javadoc - JPA-Criteria](https://www.javadoc.io/badge2/com.blazebit/blaze-persistence-jpa-criteria-api/JPA%20Criteria%20API%20Javadoc.svg?color=green)](http://www.javadoc.io/doc/com.blazebit/blaze-persistence-jpa-criteria-api)

Blaze-Persistence
==========
Blaze-Persistence is a rich Criteria API for JPA providers.

What is it?
===========

Blaze-Persistence is a rich Criteria API for JPA providers that aims to be better
than all the other Criteria APIs available.
It provides a fluent API for building queries and removes common restrictions
encountered when working with JPA directly.
It offers rich pagination support and also supports keyset pagination.

The Entity-View module can be used to create views for JPA entites.
You can roughly imagine an entity view is to an entity, what a RDBMS view is to a table.

The JPA-Criteria module implements the Criteria API of JPA but is backed by the Blaze-Persistence Core API
so you can get a query builder out of your CriteriaQuery objects.

With Spring Data or DeltaSpike Data integrations you can make use of Blaze-Persistence easily in your existing repositories.

Features
==============

Blaze-Persistence is not only a Criteria API that allows to build queries easier,
but it also comes with a lot of features that are normally not supported by JPA providers.

Here is a rough overview of new features that are introduced by Blaze-Persistence on top of the JPA model

* Use CTEs and recursive CTEs
* Use modification CTEs aka DML in CTEs
* Make use of the `RETURNING` clause from DML statements
* Use the `VALUES` clause for reporting queries and soon make use of table generating functions
* Create queries that use SET operations like `UNION`, `EXCEPT` and `INTERSECT`
* Manage entity collections via DML statements to avoid reading them in memory
* Define functions similar to Hibernates `SQLFunction` in a JPA provider agnostic way
* Use many built-in functions like `GROUP_CONCAT`, date extraction, date arithmetic and many more
* Easy pagination and simple API to make use of keyset pagination

In addition to that, Blaze-Persistence also works around some JPA provider issues in a transparent way.

How to use it?
==============

Blaze-Persistence is split up into different modules. We recommend that you define a version property in your parent pom that you can use for all artifacts. Modules are all released in one batch so you can safely increment just that property. 

```xml
<properties>
    <blaze-persistence.version>1.6.17</blaze-persistence.version>
</properties>
```

Alternatively you can also use our BOM in the `dependencyManagement` section.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.blazebit</groupId>
            <artifactId>blaze-persistence-bom</artifactId>
            <version>${blaze-persistence.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>    
    </dependencies>
</dependencyManagement>
```

## Quickstart

If you want a sample application with everything setup where you can poke around and try out things, just go with our archetypes!

Core-only archetype:

```bash
mvn archetype:generate "-DarchetypeGroupId=com.blazebit" "-DarchetypeArtifactId=blaze-persistence-archetype-core-sample" "-DarchetypeVersion=1.6.17"
```

Entity view archetype:

```bash
mvn archetype:generate "-DarchetypeGroupId=com.blazebit" "-DarchetypeArtifactId=blaze-persistence-archetype-entity-view-sample" "-DarchetypeVersion=1.6.17"
```

Spring-Data archetype:

```bash
mvn archetype:generate "-DarchetypeGroupId=com.blazebit" "-DarchetypeArtifactId=blaze-persistence-archetype-spring-data-sample" "-DarchetypeVersion=1.6.17"
```

Spring-Boot archetype:

```bash
mvn archetype:generate "-DarchetypeGroupId=com.blazebit" "-DarchetypeArtifactId=blaze-persistence-archetype-spring-boot-sample" "-DarchetypeVersion=1.6.17"
```

DeltaSpike Data archetype:

```bash
mvn archetype:generate "-DarchetypeGroupId=com.blazebit" "-DarchetypeArtifactId=blaze-persistence-archetype-deltaspike-data-sample" "-DarchetypeVersion=1.6.17"
```

Java EE archetype:

```bash
mvn archetype:generate "-DarchetypeGroupId=com.blazebit" "-DarchetypeArtifactId=blaze-persistence-archetype-jakarta-ee-sample" "-DarchetypeVersion=1.6.17"
```

## Supported Java runtimes

All projects are built for Java 11 except for the ones where dependencies already use Java 17 like e.g. Spring Data integration etc.
So you are going to need a JDK 17 for building the project. The latest Java version we test and support is Java 21.

```xml
<dependency>
    <groupId>jakarta.xml.bind</groupId>
    <artifactId>jakarta.xml.bind-api</artifactId>
    <version>3.0.1</version>
    <!-- In a managed environment like Java/Jakarta EE, use 'provided'. Otherwise use 'compile' -->
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>com.sun.xml.bind</groupId>
    <artifactId>jaxb-impl</artifactId>
    <version>3.0.2</version>
    <!-- In a managed environment like Java/Jakarta EE, use 'provided'. Otherwise use 'compile' -->
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>jakarta.transaction</groupId>
    <artifactId>jakarta.transaction-api</artifactId>
    <version>2.0.1</version>
    <!-- In a managed environment like Java/Jakarta EE, use 'provided'. Otherwise use 'compile' -->
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>jakarta.activation</groupId>
    <artifactId>jakarta.activation-api</artifactId>
    <version>2.0.1</version>
    <!-- In a managed environment like Java/Jakarta EE, use 'provided'. Otherwise use 'compile' -->
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>jakarta.annotation</groupId>
    <artifactId>jakarta.annotation-api</artifactId>
    <version>2.1.1</version>
    <!-- In a managed environment like Java/Jakarta EE, use 'provided'. Otherwise use 'compile' -->
    <scope>provided</scope>
</dependency>
```

The `jakarta.transaction` and `jakarta.activation` dependencies are especially relevant for the JPA metamodel generation.

## Supported environments/libraries

The bare minimum is JPA 3.1. Generally, we support the usage in Java EE 10+, Quarkus 3+ or Spring 6+ applications.

See the following table for an overview of supported versions.

Module                           | Minimum version    | Supported versions
---------------------------------|--------------------|--------------------
Hibernate integration            | Hibernate 6.2      | 6.2, 6.4, 6.6, 7.1+
EclipseLink integration          | EclipseLink 4.0    | 4.0
Entity View CDI integration      | CDI 4.0            | 4.0+
Entity View Spring integration   | Spring 6.0         | 6.0, 6.1, 6.2, 7.0
DeltaSpike Data integration      | DeltaSpike 2.0     | 2.0
Spring Data integration          | Spring Data 3.1    | 3.1 - 3.5, 4.0
Spring Data WebMvc integration   | Spring WebMvc 6.0  | 6.0, 6.1, 6.2, 7.0
Spring Data WebFlux integration  | Spring WebFlux 6.0 | 6.0, 6.1, 6.2, 7.0
Spring HATEOAS WebMvc integration| Spring HATEOAS 2.1 | Spring WebMvc 6.0+, Spring HATEOAS 2.1+
Jackson integration              | 2.8.11             | 2.8.11+
GraphQL integration              | 17.3               | 17.3+
JAX-RS integration               | JAX-RS 3.1         | 3.1+
Quarkus integration              | 3.20               | 3.20+

## Manual setup

For compiling you will only need API artifacts and for the runtime you need impl and integration artifacts.

See the [core documentation](https://persistence.blazebit.com/documentation/core/manual/en_US/index.html#maven-setup) for the necessary dependencies needed to setup
Blaze-Persistence. If you want to use entity views, the [entity view documentation](https://persistence.blazebit.com/documentation/entity-view/manual/en_US/index.html#maven-setup) 
contains a similar setup section describing the necessary dependencies.

Documentation
=========

The current documentation is a reference manual and is split into a reference for the [core module](https://persistence.blazebit.com/documentation/core/manual/en_US/index.html) and for the [entity-view module](https://persistence.blazebit.com/documentation/entity-view/manual/en_US/index.html).
At some point we might introduce topical documentation, but for now you can find articles on the [Blazebit Blog](https://blazebit.com/blog.html)

Core quick-start
=================

First you need to create a `CriteriaBuilderFactory` which is the entry point to the core api. 

```java
CriteriaBuilderConfiguration config = Criteria.getDefault();
// optionally, perform dynamic configuration
CriteriaBuilderFactory cbf = config.createCriteriaBuilderFactory(entityManagerFactory);
```

NOTE: The `CriteriaBuilderFactory` should have the same scope as your `EntityManagerFactory` as it is bound to it.

For demonstration purposes, we will use the following simple entity model.

```java
@Entity
public class Cat {
    @Id
    private Integer id;
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    private Cat father;
    @ManyToOne(fetch = FetchType.LAZY)
    private Cat mother;
    @OneToMany
    private Set<Cat> kittens;
    // Getter and setters omitted for brevity
}
```

If you want to select all cats and fetch their kittens as well as their father you do the following.

```java
cbf.create(em, Cat.class).fetch("kittens.father").getResultList();
```

This will create quite a query behind the scenes:

```sql
SELECT cat FROM Cat cat LEFT JOIN FETCH cat.kittens kittens_1 LEFT JOIN FETCH kittens_1.father father_1
```

An additional bonus is that the paths and generally every expression you write will get checked against the metamodel so you can spot typos very early.

JPA Criteria API quick-start
=================

Blaze-Persistence provides an implementation of the JPA Criteria API what allows you to mostly code against the standard JPA Criteria API,
but still be able to use the advanced features Blaze-Persistence provides.

All you need is a `CriteriaBuilderFactory` and when constructing the actual query, an `EntityManager`.

```java
// This is a subclass of the JPA CriteriaBuilder interface
BlazeCriteriaBuilder cb = BlazeCriteria.get(criteriaBuilderFactory);
// A subclass of the JPA CriteriaQuery interface
BlazeCriteriaQuery<Cat> query = cb.createQuery(Cat.class);

// Do your JPA Criteria query logic with cb and query
Root<Cat> root = query.from(Cat.class);
query.where(cb.equal(root.get(Cat_.name), "Felix"));

// Finally, transform the BlazeCriteriaQuery to the Blaze-Persistence Core CriteriaBuilder
CriteriaBuilder<Cat> builder = query.createCriteriaBuilder(entityManager);
// From here on, you can use all the power of the Blaze-Persistence Core API

// And finally fetch the result
List<Cat> resultList = builder.getResultList();
```

This will create a query that looks just about what you would expect:

```sql
SELECT cat FROM Cat cat WHERE cat.name = :param_0
```

This alone is not very spectacular. The interesting part is that you can use the Blaze-Persistence `CriteriaBuilder` then to do your advanced SQL things
or consume your result as entity views as explained in the next part.  

Entity-view usage
=================

Every project has some kind of DTOs and implementing these properly isn't easy. Based on the super quick-start model we will show how entity views come to the rescue!

To make use of entity views, you will need a `EntityViewManager` with entity view classes registered. In a CDI environment you can inject a `EntityViewConfiguration` that has all discoverable entity view classes registered, but in a normal Java application you will have to register the classes yourself like this:

```java
EntityViewConfiguration config = EntityViews.createDefaultConfiguration();
config.addEntityView(CatView.class);
EntityViewManager evm = config.createEntityViewManager(criteriaBuilderFactory);
```

NOTE: The `EntityViewManager` should have the same scope as your `EntityManagerFactory` and `CriteriaBuilderFactory` as it is bound to it.

An entity view itself is a simple interface or abstract class describing the structure of the projection that you want. It is very similar to defining an entity class with the difference that it is based on the entity model instead of the DBMS model.

```java
@EntityView(Cat.class)
public interface CatView {
    @IdMapping
    public Integer getId();

    @Mapping("CONCAT(mother.name, 's kitty ', name)")
    public String getCuteName();

    public SimpleCatView getFather();

}
```

```java
@EntityView(Cat.class)
public interface SimpleCatView {
    @IdMapping
    public Integer getId();

    public String getName();

}
```

The `CatView` has a property `cuteName` which will be computed by the JPQL expression `CONCAT(mother.name, 's kitty ', name)` and a subview for `father`. Note that although not required in this particular case,
every entity view for an entity type should have an id mapping if possible. Entity views without an id mapping will by default have equals and hashCode implementations that consider all attributes, whereas with an id mapping, only the id is considered.
The `SimpleCatView` is the projection which is used for the `father` relation and only consists of the `id` and the `name` of the `Cat`.

You just created two DTO interfaces that contain projection information. Now the interesting part is that entity views can be applied on any query, so you can define a base query and then create the projection like this:

```java
CriteriaBuilder<Cat> cb = criteriaBuilderFactory.create(entityManager, Cat.class);
cb.whereOr()
    .where("father").isNull()
    .where("father.name").like().value("Darth%").noEscape()
.endOr();
CriteriaBuilder<CatView> catViewBuilder = evm.applySetting(EntityViewSetting.create(CatView.class), cb);
List<CatView> catViews = catViewBuilder.getResultList();
```

This will behind the scenes execute the following optimized query and transparently build your entity view objects based on the results.

```sql
SELECT
    cat.id,
    CONCAT(mother_1.name, 's kitty ', cat.name),
    father_1.id,
    father_1.name
FROM Cat cat
LEFT JOIN cat.father father_1
LEFT JOIN cat.mother mother_1
WHERE father_1 IS NULL
   OR father_1.name LIKE :param_0
```

See the left joins created for relations used in the projection? These are implicit joins which are by default what we call "model-aware". If you specified that a relation is `optional = false`, we would generate an inner join instead.
This is different from how JPQL path expressions are normally interpreted, but in case of projections like in entity views, this is just what you would expect!
You can always override the join type of implicit joins with `joinDefault` if you like.

Questions or issues
===================

Drop by on [![Zulip Chat](https://img.shields.io/badge/zulip-join_chat-brightgreen.svg)](https://blazebit.zulipchat.com) and ask questions any time or just create an issue on [GitHub](https://github.com/Blazebit/blaze-persistence/issues/new) or ask on [Stackoverflow](https://stackoverflow.com/questions/ask?tags=java+blaze-persistence).

Commercial support
==================

You can find commercial support offerings by Blazebit in the [support section](https://persistence.blazebit.com/support.html#_blaze_persistence_support).

If you are a commercial customer and want to use commercial releases, you need to define the following repository in a profile of your project or the `settings.xml` located in `~/.m2`.

```xml
<repository>
  <id>blazebit</id>
  <name>Blazebit</name>
  <url>https://nexus.blazebit.com/repository/maven-releases/</url>
</repository>
```

You also need to add the following server in the `settings.xml` with the appropriate credentials:

```xml
<server>
  <id>blazebit</id>
  <username>USERNAME</username>
  <password>PASSWORD</password>
</server>
```

Commercial customers also get access to the [commercial repository](https://github.com/Blazebit-Commercial/blaze-persistence) where they access the source code of commercial releases,
create issues that are treated with higher priority and browse commercial releases.

Using snapshots
==================

To use the current snapshots which are published to the Sonatype OSS snapshot repository,
you need to define the following repository in a profile of your project or the `settings.xml` located in `~/.m2`.

```xml
<repository>
  <id>sonatype-snapshots</id>
  <name>Sonatype Snapshots</name>
  <url>https://central.sonatype.com/repository/maven-snapshots/</url>
</repository>
```

Also see the [Maven documentation](https://maven.apache.org/guides/introduction/introduction-to-repositories.html) for further details.

Setup local development
=======================

Here some notes about setting up a local environment for testing.

## Setup general build environment

Although Blaze-Persistence still supports running on Java 11, the build must be run with at least JDK 17.

## Building the website and documentation

You have to install [GraphViz](http://www.graphviz.org/Download.php) and make it available in your PATH.

After that, it's easiest to just invoke `./serve-website.sh` which builds the documentation, website and starts an embedded server to serve at port 8820.

## Checkstyle in IntelliJ

1. Build the whole thing with `mvn clean install` once to have the checkstyle-rules jar in your M2 repository
2. Install the CheckStyle-IDEA Plugin
3. After a restart, go to Settings > Other Settings > Checkstyle and configure version `9.3.0`
4. Add a _Third party check_ that points to the _checkstyle-rules.jar_ of your M2 repository
5. Add a configuration file named *Blaze-Persistence Checkstyle rules* pointing to `checkstyle-rules/src/main/resources/blaze-persistence/checkstyle-config.xml`

Now you should be able to select *Blaze-Persistence Checkstyle rules* in the dropdown of the CheckStyle window. +
Click on *Check project* and checkstyle will run once for the whole project, then it should do some work incrementally.

## Testing a JPA provider and DBMS combination

By default, a Maven build `mvn clean install` will test against H2, Hibernate 6.6 and Spring Boot/Data 3.3, but you can activate different profiles to test other combinations.
To test a specific combination, you need to activate at least 4 profiles

* One of the JPA provider profiles
  * `hibernate-7.1` + the `jakarta` profile
  * `hibernate-6.6`
  * `hibernate-6.4`
  * `hibernate-6.2`
  * `eclipselink`
* A DBMS profile
  * `h2`
  * `postgresql`
  * `mysql`
  * `oracle`
  * `db2`
  * `mssql`
* A Spring data profile
  * `spring-data-4.0.x`
  * `spring-data-3.5.x`
  * `spring-data-3.4.x`
  * `spring-data-3.3.x`
  * `spring-data-3.2.x`
  * `spring-data-3.1.x`
  * `spring-data-3.3.x`
  * `spring-data-3.2.x`
  * `spring-data-3.1.x`
* A DeltaSpike profile
  * `deltaspike-2.0`

The default DBMS connection infos are defined via Maven properties, so you can override them in a build by passing the properties as system properties.

* `jdbc.url`
* `jdbc.user`
* `jdbc.password`
* `jdbc.driver`

The values are defined in e.g. `core/testsuite/pom.xml` in the respective DBMS profiles.

For executing tests against a database on a dedicated host you might want to specify the following system property `-DdbHost=192.168.99.100`.

## Switching JPA provider profiles in IntelliJ

When switching between Hibernate and other JPA provider profiles, IntelliJ does not unmark the `basic` or `hibernate` source directories in *core/testsuite*.
If you encounter errors like _duplicate class file found_ or something alike, make sure that

* With a Hibernate profile you unmark the *core/testsuite/src/main/basic* directory as source root
* With a non-Hibernate profile you unmark the *core/testsuite/src/main/hibernate* and *core/testsuite/src/test/hibernate* directory as source root

Unmarking as source root can be done by right clicking on the source directory, going to the submenu _Mark directory as_ and finally clicking _Unmark as Sources Root_.

## Oracle

When setting up Oracle locally, keep in mind that when you connect to it, you have to set the NLS_SORT to BINARY.
Since the JDBC driver derives values from the locale settings of the JVM, you should set the default locale settings to en_US.
In IntelliJ when defining the Oracle database, go to the Advanced tab an specify the JVM options `-Duser.country=us -Duser.language=en`.

## GraalVM for native images with Quarkus

The general setup required for building native images with GraalVM is described in https://quarkus.io/guides/building-native-image.

* Install GraalVM 22.3.0 (Java 17) and make sure you install the native-image tool and set `GRAALVM_HOME` environment variable
* Install required packages for a C development environment
  *  Under Windows, install [Visual Studio 2017 Visual C++ Build Tools](https://aka.ms/vs/15/release/vs_buildtools.exe)

For example, run the following maven build to execute native image tests for H2:

```
mvn -pl examples/quarkus/testsuite/native/h2 -am integration-test -Pnative,h2,spring-data-3.3.x,deltaspike-2.0
```

Under Windows, make sure you run maven builds that use native image from the VS2017 native tools command line.

## Website deployment

You can use `build-deploy-website.sh` to deploy to the target environment but need to configure in ~/.m2/settings.xml the following servers.

Id: staging-persistence.blazebit.com
User/Password: user/****

Id: persistence.blazebit.com
User/Password: user/****

Licensing
=========

This distribution, as a whole, is licensed under the terms of the Apache
License, Version 2.0 (see LICENSE.txt).

References
==========

Project Site:              https://persistence.blazebit.com
