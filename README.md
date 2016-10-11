[![Build Status](https://travis-ci.org/Blazebit/blaze-persistence.svg?branch=master)](https://travis-ci.org/Blazebit/blaze-persistence)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.blazebit/blaze-persistence-core-impl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.blazebit/blaze-persistence-core-impl)
[![Slack Status](https://blazebit.herokuapp.com/badge.svg)](https://blazebit.herokuapp.com)

[![Javadoc - Core](https://javadoc-emblem.rhcloud.com/doc/com.blazebit/blaze-persistence-core-api/badge.svg?subject=javadoc%20-%20core-api)](http://www.javadoc.io/doc/com.blazebit/blaze-persistence-core-api)
[![Javadoc - Entity-View](https://javadoc-emblem.rhcloud.com/doc/com.blazebit/blaze-persistence-entity-view-api/badge.svg?subject=javadoc%20-%20entity-view-api)](http://www.javadoc.io/doc/com.blazebit/blaze-persistence-entity-view-api)
[![Javadoc - JPA-Criteria](https://javadoc-emblem.rhcloud.com/doc/com.blazebit/blaze-persistence-jpa-criteria-api/badge.svg?subject=javadoc%20-%20jpa-criteria-api)](http://www.javadoc.io/doc/com.blazebit/blaze-persistence-jpa-criteria-api)

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

Features
==============

Blaze-Persistence is not only a Criteria API that allows to build queries easier,
but it also comes with a lot of features that are normally not supported by JPA providers.

Here is a rough overview of new features that are introduced by Blaze-Persistence

* Use CTEs and recursive CTEs
* Use modification CTEs aka DML in CTEs
* Make use of the RETURNING clause from DML statements
* Use the VALUES clause for reporting queries and soon make use of table generating functions
* Create queries that use SET operations like UNION, EXCEPT and INTERSECT
* Define functions similar to Hibernates SQLFunction in a JPA provider agnostic way
* Use many built-in functions like GROUP_CONCAT, date extraction, date arithmetic and many more
* Easy pagination and simple API to make use of keyset pagination

In addition to that, Blaze-Persistence also works around some JPA provider issues in a transparent way.

How to use it?
==============
Blaze-Persistence is split up into different modules. We recommend that you define a version property in your parent pom that you can use for all artifacts. Modules are all released in one batch so you can safely increment just that property.

```xml
<properties>
	<blaze-persistence.version>1.2.0.Alpha1</blaze-persistence.version>
</properties>
```

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
	<artifactId>blaze-persistence-entity-view-impl</artifactId>
	<version>${blaze-persistence.version}</version>
	<scope>runtime</scope>
</dependency>
```

Blaze-Persistence JPA provider integration module dependencies

Hibernate 5.2

```xml
<dependency>
	<groupId>com.blazebit</groupId>
	<artifactId>blaze-persistence-integration-hibernate-5.2</artifactId>
	<version>${blaze-persistence.version}</version>
	<scope>runtime</scope>
</dependency>
```
	
Hibernate 5+

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
	
Datanucleus

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
	<scope>runtime</scope>
</dependency>
```

Documentation
=========

The current documentation is still pretty raw and we are happy about every contribution!
The documentation is split into a reference for the [core module](https://github.com/Blazebit/blaze-persistence/blob/master/documentation/src/main/asciidoc/core/manual/en_US/index.adoc) and for the [entity-view module](https://github.com/Blazebit/blaze-persistence/blob/master/documentation/src/main/asciidoc/entity-view/manual/en_US/entity-view-index.adoc).
 
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

If you want select all cats and fetch their kittens as well as their father you do the following.

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
EntityViewManager evm = config.createEntityViewManager(criteriaBuilderFactory, entityManagerFactory);
```

NOTE: The `EntityViewManager` should have the same scope as your `EntityManagerFactory` and `CriteriaBuilderFactory` as it is bound to it.

The entity view itself is a simple interface describing the structure of the projection that you want. It is very similar to defining an entity class with the difference that it is based on the entity model instead of the DBMS model.

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

The `CatView` has a property `cuteName` which will be computed by the JPQL expression `CONCAT(mother.name, 's kitty ', name)` and a subview for `father`. Note that every entity view needs an id mapping except for `EmbeddableEntityView` classes.
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

See the left joins created for relations used in the projection? These are implicit joins which are by default what we call "model-aware". If you specified that a relation is `optional = false`, we would generate an inner join instead. This is different from how JPQL path expressions are normally interpreted, but in case of projections like in entity views, this is just what you would expect! You can always override the join type of implicit joins with `joinDefault` if you like.

Questions or issues
===================

Drop by on [![Slack Status](https://blazebit.herokuapp.com/badge.svg)](https://blazebit.herokuapp.com) and ask questions any time or just create an issue on [GitHub](https://github.com/Blazebit/blaze-persistence/issues/new).

Licensing
=========

This distribution, as a whole, is licensed under the terms of the Apache
License, Version 2.0 (see LICENSE.txt).

References
==========

Project Site:              http://blazebit.com/persistence (Coming soon)
