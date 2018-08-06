[![Build Status](https://travis-ci.org/Blazebit/blaze-persistence.svg?branch=master)](https://travis-ci.org/Blazebit/blaze-persistence)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.blazebit/blaze-persistence-core-impl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.blazebit/blaze-persistence-core-impl)
[![Slack Status](https://blazebit.herokuapp.com/badge.svg)](https://blazebit.herokuapp.com)

[![Javadoc - Core](https://www.javadoc.io/badge/com.blazebit/blaze-persistence-core-api.svg?label=javadoc%20-%20core-api)](http://www.javadoc.io/doc/com.blazebit/blaze-persistence-core-api)
[![Javadoc - Entity-View](https://www.javadoc.io/badge/com.blazebit/blaze-persistence-entity-view-api.svg?label=javadoc%20-%20entity-view-api)](http://www.javadoc.io/doc/com.blazebit/blaze-persistence-entity-view-api)
[![Javadoc - JPA-Criteria](https://www.javadoc.io/badge/doc/com.blazebit/blaze-persistence-jpa-criteria-api.svg?label=javadoc%20-%20jpa-criteria-api)](http://www.javadoc.io/doc/com.blazebit/blaze-persistence-jpa-criteria-api)

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
* Make use of the RETURNING clause from DML statements
* Use the VALUES clause for reporting queries and soon make use of table generating functions
* Create queries that use SET operations like UNION, EXCEPT and INTERSECT
* Manage entity collections via DML statements to avoid reading them in memory
* Define functions similar to Hibernates SQLFunction in a JPA provider agnostic way
* Use many built-in functions like GROUP_CONCAT, date extraction, date arithmetic and many more
* Easy pagination and simple API to make use of keyset pagination

In addition to that, Blaze-Persistence also works around some JPA provider issues in a transparent way.

How to use it?
==============

Blaze-Persistence is split up into different modules. We recommend that you define a version property in your parent pom that you can use for all artifacts. Modules are all released in one batch so you can safely increment just that property. 

```xml
<properties>
    <blaze-persistence.version>1.2.1</blaze-persistence.version>
</properties>
```

Alternatively you can also use our BOM in the `dependencyManagement` section.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.blazebit</groupId>
            <artifactId>blaze-persistence-bom</artifactId>
            <version>1.2.1</version>
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
mvn archetype:generate "-DarchetypeGroupId=com.blazebit" "-DarchetypeArtifactId=blaze-persistence-archetype-core-sample" "-DarchetypeVersion=1.2.1"
```

Entity view archetype:

```bash
mvn archetype:generate "-DarchetypeGroupId=com.blazebit" "-DarchetypeArtifactId=blaze-persistence-archetype-entity-view-sample" "-DarchetypeVersion=1.2.1"
```

Spring-Data archetype:

```bash
mvn archetype:generate "-DarchetypeGroupId=com.blazebit" "-DarchetypeArtifactId=blaze-persistence-archetype-spring-data-sample" "-DarchetypeVersion=1.2.1"
```

Spring-Boot archetype:

```bash
mvn archetype:generate "-DarchetypeGroupId=com.blazebit" "-DarchetypeArtifactId=blaze-persistence-archetype-spring-boot-sample" "-DarchetypeVersion=1.2.1"
```

DeltaSpike Data archetype:

```bash
mvn archetype:generate "-DarchetypeGroupId=com.blazebit" "-DarchetypeArtifactId=blaze-persistence-archetype-deltaspike-data-sample" "-DarchetypeVersion=1.2.1"
```

Java EE archetype:

```bash
mvn archetype:generate "-DarchetypeGroupId=com.blazebit" "-DarchetypeArtifactId=blaze-persistence-archetype-java-ee-sample" "-DarchetypeVersion=1.2.1"
```

## Supported Java runtimes

All projects are built for Java 7 except for the ones where dependencies already use Java 8 like e.g. Hibernate 5.2, Spring Data 2.0 etc.
So you are going to need a JDK 8 for building the project.

We also support building the project with JDK 9 and try to keep up with newer versions.
If you want to run your application on a Java 9 JVM you need to handle the fact that JDK 9+ doesn't export the JAXB and JTA APIs anymore.
In fact, JDK 11 will even remove the modules so the command line flags to add modules to the classpath won't work.

Since libraries like Hibernate and others require these APIs you need to make them available. The easiest way to get these APIs back on the classpath is to package them along with your application.
This will also work when running on Java 8. We suggest you add the following dependencies.

```xml
<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
    <version>2.2.11</version>
</dependency>
<dependency>
    <groupId>com.sun.xml.bind</groupId>
    <artifactId>jaxb-core</artifactId>
    <version>2.2.11</version>
</dependency>
<dependency>
    <groupId>com.sun.xml.bind</groupId>
    <artifactId>jaxb-impl</artifactId>
    <version>2.2.11</version>
</dependency>
<dependency>
    <groupId>javax.transaction</groupId>
    <artifactId>javax.transaction-api</artifactId>
    <version>1.2</version>
    <!-- In a managed environment like Java EE, use 'provided'. Otherwise use 'compile' -->
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>javax.activation</groupId>
    <artifactId>activation</artifactId>
    <version>1.1.1</version>
    <!-- In a managed environment like Java EE, use 'provided'. Otherwise use 'compile' -->
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>javax.annotation</groupId>
    <artifactId>javax.annotation-api</artifactId>
    <version>1.3.2</version>
    <!-- In a managed environment like Java EE, use 'provided'. Otherwise use 'compile' -->
    <scope>provided</scope>
</dependency>
```

The `javax.transaction` and `javax.activation` dependencies are especially relevant for the JPA metamodel generation.

## Supported environments/libraries

The bare minimum is JPA 2.0. If you want to use the JPA Criteria API module, you will also have to add the JPA 2 compatibility module.
Generally, we support the usage in Java EE 6+ or Spring 4+ applications.

See the following table for an overview of supported versions.

Module                         | Minimum version                   | Supported versions
-------------------------------|-----------------------------------|--------------------
Hibernate integration          | Hibernate 4.2                     | 4.2, 4.3, 5.0, 5.1, 5.2, 5.3 (not all features are available in older versions)
EclipseLink integration        | EclipseLink 2.6                   | 2.6 (Probably 2.4 and 2.5 work as well, but only tested against 2.6)
DataNucleus integration        | DataNucleus 4.1                   | 4.1, 5.0
OpenJPA integration            | N/A                               | (Currently not usable. OpenJPA doesn't seem to be actively developed anymore and no users asked for support yet)
Entity View CDI integration    | CDI 1.0                           | 1.0, 1.1, 1.2
Entity View Spring integration | Spring 4.3                        | 4.3, 5.0
DeltaSpike Data integration    | DeltaSpike 1.7                    | 1.7, 1.8
Spring Data integration        | Spring Data 1.11                  | 1.11, 2.0
Spring Data Rest integration   | Spring Data 1.11, Spring MVC 4.3  | Spring Data 1.11 + Spring MVC 4.3, Spring Data 2.0 + Spring MVC 5.0

## Manual setup

For compiling you will only need API artifacts and for the runtime you need impl and integration artifacts.
Choose the integration artifacts based on your JPA provider.

Blaze-Persistence Core module dependencies

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-core-api</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-core-impl</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>runtime</scope>
</dependency>
```

Blaze-Persistence Entity-View module dependencies

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-entity-view-api</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-entity-view-impl</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>runtime</scope>
</dependency>
```

Blaze-Persistence Entity-View CDI integration dependencies

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-entity-view-cdi</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>runtime</scope>
</dependency>
```

Blaze-Persistence Entity-View Spring integration dependencies

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-entity-view-spring</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>compile</scope>
</dependency>
```

Blaze-Persistence Spring Data 2.1 integration dependencies

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-spring-data-2.1</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>compile</scope>
</dependency>
```

Blaze-Persistence Spring Data 2.0 integration dependencies

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-spring-data-2.0</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>compile</scope>
</dependency>
```

Blaze-Persistence Spring Data 1.x integration dependencies

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-spring-data-1.x</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>compile</scope>
</dependency>
```

Blaze-Persistence DeltaSpike Data integration

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-deltaspike-data-api</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>compile</scope>
</dependency>
```

Blaze-Persistence DeltaSpike Data 1.8 integration

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-deltaspike-data-impl-1.8</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>runtime</scope>
</dependency>
```

Blaze-Persistence DeltaSpike Data 1.7 integration

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-deltaspike-data-impl-1.7</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>runtime</scope>
</dependency>
```

Blaze-Persistence JPA provider integration module dependencies

Hibernate 5.4

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-hibernate-5.4</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>runtime</scope>
</dependency>
```

Hibernate 5.3

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-hibernate-5.3</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>runtime</scope>
</dependency>
```

Hibernate 5.2

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-hibernate-5.2</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>runtime</scope>
</dependency>
```

Hibernate 5.0 or 5.1

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-hibernate-5</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>runtime</scope>
</dependency>
```

Hibernate 4.3

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-hibernate-4.3</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>runtime</scope>
</dependency>
```

Hibernate 4.2

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-hibernate-4.2</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>runtime</scope>
</dependency>
```

Datanucleus 5.1

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-datanucleus-5.1</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>runtime</scope>
</dependency>
```

Datanucleus 4 and 5

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-datanucleus</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>runtime</scope>
</dependency>
```

EclipseLink

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-eclipselink</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>runtime</scope>
</dependency>
```

OpenJPA

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-integration-openjpa</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>runtime</scope>
</dependency>
```

Blaze-Persistence JPA-Criteria module dependencies

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-jpa-criteria-api</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-jpa-criteria-impl</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>runtime</scope>
</dependency>
```

Blaze-Persistence JPA-Criteria JPA 2.0 provider support dependencies

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-persistence-jpa-criteria-jpa-2-compatibility</artifactId>
    <version>${blaze-persistence.version}</version>
    <scope>compile</scope>
</dependency>
```

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

Drop by on [![Slack Status](https://blazebit.herokuapp.com/badge.svg)](https://blazebit.herokuapp.com) and ask questions any time or just create an issue on [GitHub](https://github.com/Blazebit/blaze-persistence/issues/new) or ask on [Stackoverflow](https://stackoverflow.com/questions/ask?tags=java+blaze-persistence).

Setup local development
=======================

Here some notes about setting up a local environment for testing.

## Setup general build environment

Although Blaze-Persistence still supports running on Java 7, the build must be run with at least JDK 8.
When doing a release at least a JDK 9 is required as we need to build some Multi-Release or MR JARs.
Since we try to support the latest JDK versions as well, we require developers that want to build the project with JDK 11+ to define a system property.

The system property `jdk8.home` should be set to the path to a Java 7 or 8 installation that contains either `jre/lib/rt.jar` or `jre/lib/classes.jar`.
This property is necessary when using JDK 11+ because `sun.misc.Unsafe.defineClass` was removed.

## Building the website and documentation

You have to install [GraphViz](http://www.graphviz.org/Download.php) and make it available in your PATH.

After that, it's easiest to just invoke `./serve-website.sh` which builds the documentation, website and starts an embedded server to serve at port 8820.

## Checkstyle in IntelliJ

1. Build the whole thing with `mvn clean install` once to have the checkstyle-rules jar in your M2 repository
2. Install the CheckStyle-IDEA Plugin
3. After a restart, go to Settings > Other Settings > Checkstyle
4. Add a _Third party check_ that points to the _checkstyle-rules.jar_ of your M2 repository
5. Add a configuration file named *Blaze-Persistence Checkstyle rules* pointing to `checkstyle-rules/src/main/resources/blaze-persistence/checkstyle-config.xml`
6. Use `target/checkstyle.cache` for the property `checkstyle.cache.file`

Now you should be able to select *Blaze-Persistence Checkstyle rules* in the dropdown of the CheckStyle window. +
Click on *Check project* and checkstyle will run once for the whole project, then it should do some work incrementally.

## Testing a JPA provider and DBMS combination

By default, a Maven build `mvn clean install` will test against H2 and Hibernate 5.2 but you can activate different profiles to test other combinations.
To test a specific combination, you need to activate at least 4 profiles

* One of the JPA provider profiles
** `hibernate-5.4`
** `hibernate-5.3`
** `hibernate-5.2`
** `hibernate-5.1`
** `hibernate-5.0`
** `hibernate-4.3`
** `hibernate`
** `eclipselink`
** `datanucleus-5.1`
** `datanucleus-5`
** `datanucleus-4`
** `openjpa`
* A DBMS profile
** `h2`
** `postgresql`
** `mysql`
** `oracle`
** `db2`
** `mssql`
** `firebird`
** `sqllite`
* A Spring data profile
** `spring-data-2.0.x`
** `spring-data-1.11.x`
* A Deltaspike profile
** `deltaspike-1.7`
** `deltaspike-1.8`

The default DBMS connection infos are defined via Maven properties, so you can override them in a build by passing the properties as system properties.

* `jdbc.url`
* `jdbc.user`
* `jdbc.password`
* `jdbc.driver`

The values are defined in e.g. `core/testsuite/pom.xml` in the respective DBMS profiles.

## Switching JPA provider profiles in IntelliJ

When switching between Hibernate and other JPA provider profiles, IntelliJ does not unmark the `basic` or `hibernate` source directories in *core/testsuite*.
If you encounter errors like _duplicate class file found_ or something alike, make sure that

* With a Hibernate profile you unmark the *core/testsuite/src/main/basic* directory as source root
* With a non-Hibernate profile you unmark the *core/testsuite/src/main/hibernate* and *core/testsuite/src/test/hibernate* directory as source root

Unmarking as source root can be done by right clicking on the source directory, going to the submenu _Mark directory as_ and finally clicking _Unmark as Sources Root_.

## Using DataNucleus profiles in IntelliJ

DataNucleus requires bytecode enhancement to work properly which requires an extra step to be able to do testing within IntelliJ.
Usually when switching the JPA provider profile, it is recommended to trigger a _Rebuild Project_ action in IntelliJ to avoid strange errors causes by previous bytecode enhancement runs.
After that, the entities in the project *core/testsuite* have to be enhanced. This is done through a Maven command.

* DataNucleus 4: `mvn -P "datanucleus-4" -pl core/testsuite datanucleus:enhance`
* DataNucleus 5: `mvn -P "datanucleus-5" -pl core/testsuite datanucleus:enhance`
* DataNucleus 5.1: `mvn -P "datanucleus-5.1" -pl core/testsuite datanucleus:enhance`

After doing that, you should be able to execute any test in IntelliJ.

Note that if you make changes to an entity class or add a new entity class you might need to redo the rebuild and enhancement. 

## Firebird

When installing the 3.x version, you also need a 3.x JDBC driver.
Additionally you should add the following to the firebird.conf

```
WireCrypt = Enabled
```

After creating the DB with `create database 'localhost:test' user 'sysdba' password 'sysdba';`, you can connect with JDBC with `jdbc:firebirdsql:localhost:test?charSet=utf-8`

## Oracle

When setting up Oracle locally, keep in mind that when you connect to it, you have to set the NLS_SORT to BINARY.
Since the JDBC driver derives values from the locale settings of the JVM, you should set the default locale settings to en_US.
In IntelliJ when defining the Oracle database, go to the Advanced tab an specify the JVM options `-Duser.country=us -Duser.language=en`. 

When using the Oracle docker container via `docker_db.sh oracle` you might want to specify the following properties when executing tests `-Djdbc.url=jdbc:oracle:thin:@192.168.99.100:1521/xe -Djdbc.user=SYSTEM -Djdbc.password=oracle`

### JDBC Driver

You have to install the JDBC driver manually. If you install Oracle XE locally, you can take it from $ORACLE_HOME/jdbc otherwise download it from http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html
Copy the jar to $M2_HOME/com/oracle/ojdbc14/10.2.0.4.0/ojdbc14-10.2.0.4.0.jar and you should be good to go.

If you use the docker container, extract the jdbc driver from the container via `docker cp oracle:/u01/app/oracle/product/11.2.0/xe/jdbc/lib/ojdbc6.jar ojdbc.jar`

`mvn -q install:install-file -Dfile=ojdbc.jar -DgroupId=com.oracle -DartifactId=ojdbc14 -Dversion=10.2.0.4.0 -Dpackaging=jar -DgeneratePom=true`

### Install Oracle locally

Download Oracle XE from http://www.oracle.com/technetwork/database/database-technologies/express-edition/downloads/index.html
During installation use the password "oracle" which is also the default password for the docker image.

## DB2

When using the DB2 docker container via `docker_db.sh db2` you might want to specify the following properties when executing tests `-Djdbc.url=jdbc:db2://192.168.99.100:50000/test -Djdbc.user=db2inst1 -Djdbc.password=db2inst1-pwd`

### JDBC Driver

You have to install the JDBC driver manually. If you install DB2 Express locally, you can take it from $DB2_HOME/sqllib/java otherwise download it from http://www-01.ibm.com/support/docview.wss?uid=swg21363866

When using the docker container, you can find in the copy script to extract the JDBC driver from the container in `docker_db.sh`.
Install via the following commands.

`mvn -q install:install-file -Dfile=db2jcc4.jar -DgroupId=com.ibm.db2 -DartifactId=db2jcc4 -Dversion=9.7 -Dpackaging=jar -DgeneratePom=true`

`mvn -q install:install-file -Dfile=db2jcc_license_cu.jar -DgroupId=com.ibm.db2 -DartifactId=db2jcc_license_cu -Dversion=9.7 -Dpackaging=jar -DgeneratePom=true`

## SQL Server

When using the DB2 docker container via `docker_db.sh mssql` you might want to specify the following properties when executing tests `-Djdbc.url=jdbc:sqlserver://192.168.99.100:1433`

### JDBC Driver

Since the JDBC driver is officially available in Maven central, you don't have to separately install it.

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
