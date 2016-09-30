[![Build Status](https://travis-ci.org/Blazebit/blaze-persistence.svg?branch=master)](https://travis-ci.org/Blazebit/blaze-persistence)

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

How to use it?
==============
Blaze-Persistence is split up into different modules. We recommend that you define a version property in your parent pom that you can use for all artifacts. Modules are all released in one batch so you can safely increment just that property.

	<properties>
		<blaze-persistence.version>1.2.0.Alpha1</blaze-persistence.version>
	</properties>

For compiling you will only need API artifacts and for the runtime you need impl and integration artifacts.
Choose the integration artifacts based on your JPA provider.

Blaze-Persistence Core module dependencies

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
	
Blaze-Persistence Entity-View module dependencies

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

Blaze-Persistence Entity-View CDI integration dependencies

	<dependency>
		<groupId>com.blazebit</groupId>
		<artifactId>blaze-persistence-entity-view-impl</artifactId>
		<version>${blaze-persistence.version}</version>
		<scope>runtime</scope>
	</dependency>

Blaze-Persistence JPA provider integration module dependencies

Hibernate 5.2

	<dependency>
		<groupId>com.blazebit</groupId>
		<artifactId>blaze-persistence-integration-hibernate-5.2</artifactId>
		<version>${blaze-persistence.version}</version>
		<scope>runtime</scope>
	</dependency>
	
Hibernate 5+
	
	<dependency>
		<groupId>com.blazebit</groupId>
		<artifactId>blaze-persistence-integration-hibernate-5</artifactId>
		<version>${blaze-persistence.version}</version>
		<scope>runtime</scope>
	</dependency>

Hibernate 4.3

	<dependency>
		<groupId>com.blazebit</groupId>
		<artifactId>blaze-persistence-integration-hibernate-4.3</artifactId>
		<version>${blaze-persistence.version}</version>
		<scope>runtime</scope>
	</dependency>
	
Hibernate 4.2
	
	<dependency>
		<groupId>com.blazebit</groupId>
		<artifactId>blaze-persistence-integration-hibernate-4.2</artifactId>
		<version>${blaze-persistence.version}</version>
		<scope>runtime</scope>
	</dependency>
	
Datanucleus
	
	<dependency>
		<groupId>com.blazebit</groupId>
		<artifactId>blaze-persistence-integration-datanucleus</artifactId>
		<version>${blaze-persistence.version}</version>
		<scope>runtime</scope>
	</dependency>
	
EclipseLink
	
	<dependency>
		<groupId>com.blazebit</groupId>
		<artifactId>blaze-persistence-integration-eclipselink</artifactId>
		<version>${blaze-persistence.version}</version>
		<scope>runtime</scope>
	</dependency>
	
OpenJPA
	
	<dependency>
		<groupId>com.blazebit</groupId>
		<artifactId>blaze-persistence-integration-openjpa</artifactId>
		<version>${blaze-persistence.version}</version>
		<scope>runtime</scope>
	</dependency>

Blaze-Persistence JPA-Criteria module dependencies

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

Blaze-Persistence JPA-Criteria JPA 2.0 provider support dependencies

	<dependency>
		<groupId>com.blazebit</groupId>
		<artifactId>blaze-persistence-jpa-criteria-jpa-2-compatibility</artifactId>
		<version>${blaze-persistence.version}</version>
		<scope>runtime</scope>
	</dependency>
 
Licensing
=========

This distribution, as a whole, is licensed under the terms of the Apache
License, Version 2.0 (see LICENSE.txt).

References
==========

Project Site:              http://blazebit.com/persistence (Coming soon)
