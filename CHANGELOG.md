# Change log

Changes that happened in releases

## 1.2.0

Not yet released

### New features

* Support for fetch strategies for non-correlated mappings
* Add method to enable query result caching
* `CorrelationBuilder` now returns `CorrelationQueryBuilder` that offers extended functionality
* Updatable attributes by default now allow all known subtypes that don't introduce cycles, rather than requiring the user to specify all allowed subtypes

### Bug fixes

* `EntityViewManager` was wrongly passed to `@PostCreate` Java 8 default methods in interfaces
* Fix illegal Entity-To-Association-Id rewrite
* Fix some bugs related to updatable inverse collection mappings

### Backwards-incompatible changes

* Renamed `whereExpression` to `setWhereExpression`
* Renamed `havingExpression` to `setHavingExpression`
* Renamed `onExpression` to `setOnExpression`
* Renamed `whereExpressionSubqueries` to `setWhereExpressionSubqueries`
* Renamed `havingExpressionSubqueries` to `setHavingExpressionSubqueries`
* Renamed `onExpressionSubqueries` to `setOnExpressionSubqueries`

## 1.2.0-Alpha4

10/02/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.0-Alpha4) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?q=is%3Aissue+project%3ABlazebit%2Fblaze-persistence%2F3+is%3Aclosed)

### New features

* Keyset pagination generates more efficient predicates that can make use of indexes more easily
* A very advanced prototype for Updatable Entity Views was introduced. You might want to check out the [documentation](https://persistence.blazebit.com/documentation/entity-view/manual/en_US/index.html#updatable-entity-views)
* Introduced DeltaSpike Data integration and aligned the Spring Data integration with it
* Introduced Maven archetypes for Java EE, DeltaSpike Data and Spring Boot
* Support for DML operations on entity collections
* Conversion between Entity View types
* Support for attribute converters in Entity Views

### Bug fixes

* The cyclic join dependency algorithm wrongly reported an error when entity joins were used
* Wrong interpretation of alias when same named association existed
* Some Entity View inheritance mappings led to the generation of clashing constructors
* Entity View inheritance for deep (3+ levels) polymorphic entity hierarchies produced wrong results
* Expression copying did not work properly which led to wrong queries after some optimizations
* Implicit joins on a query that happened in expressions of subqueries were wrongly removed in paginated queries
* Flat views were not instantiated if all properties were null

### Backwards-incompatible changes

* Many methods in the JpaProvider SPI changed their signatures
* Moved methods from ExtendedQuerySupport SPI to JpaProvider SPI
* Small signature changes in Entity View Metamodel API
* Spring integration package names were changed from `com.blazebit.persistence.view.impl.spring.*` to `com.blazebit.persistence.view.spring.impl.*`

## 1.2.0-Alpha3

27/04/2017 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.0-Alpha3) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue%20is%3Aclosed%20milestone%3A1.2.0%20closed%3A2017-02-01..2017-04-27)

### New features

* Finally there is a full [reference documentation](https://persistence.blazebit.com/documentation.html) available
* Keyset pagination now falls back to offset pagination when requesting the _first page_
* Created test case [template projects](https://github.com/Blazebit/blaze-persistence-test-case-template)
* Entity View Spring integration now allows the use of `includeFilters` and `excludeFilters` on `@EnableEntityViews`
* Extended `SubqueryInitiator` by most of the `from()` variants
* Support enum and entity type literal like the JPA spec says
* Introduction of `@MappingCorrelatedSimple` for simple correlations
* Allow empty correlation result with `JOIN` fetch strategy
* Support for join fetching with scalar selects
* Support fetches for entity mappings in entity views
* Automatic rewrites of id expressions in equality predicates to avoid joins
* Various performance improvements
* Support referring to `this` in all mapping types for putting values in embedded objects
* Relaxed strict requirements for `@IdMapping` and removed `@EmbeddableEntityView`
* Full support for `TREAT` operator. Note that EclipseLink and DataNucleus only support very few basic usages
* Spring Data JPA Specifications support
* Support for entity view inheritance as well as using the `TREAT` operator in entity view mappings

### Bug fixes

* Fixed issue with usage of Hibernate @Formula
* Fixed entity manager leak
* Fixed GROUP BY map key regression
* Fixed possible problems when from element alias clashes with attribute name
* Fixed Hibernate integration dependency version
* Allow ORDER BY a SELECT alias that is a CASE WHEN expression
* Fixed missing LIMIT/OFFSET clause for set operations
* Workaround for Hibernate parser bug when encountering arithmetic expressions in the THEN of a CASE WHEN
* Fixed keyset pagination issue that caused the user to receive a reversed result
* Better handling of raw types and improved JPA attribute type resolving for field access types and `targetEntity` uses
* VALUES clause issues with *ToOne types have been fixed
* Handle more inheritance strategies when extracting DBMS details
* Fixed problems with entities that have a fixed schema or catalog
* Consider default batch size for correlated attributes
* Fixed problems with inconsistent default naming of VALUES clause from elements
* Fixed critical query caching bug related to parameter lists in queries

### Backwards-incompatible changes

* `ReturningObjectBuilder` was changed to accept a `SimpleReturningBuilder`. This is actually a bug fix because the feature was broken before
* Moved the `CTEBuilder` interface implements entry from `BaseCriteriaBuilder` to `CriteriaBuilder` so that set operations can't simply add CTEs
* Introduced `MiddleOngoingSetOperationXXXBuilder` types that don't allow query building for better consistency
* Renamed Spring-Data integration from `blaze-persistence-spring-data` to `blaze-persistence-integration-spring-data` for consistency
* Renamed fetch strategy `SUBQUERY` to `SELECT`
* Changed the default for `partial` for updatable entity views to be `false`
* Clarified the type hierarchy for entity view metamodel elements
* Renamed showcase project artifacts to be consistent
* Removed special qualified literals for enums and entity types
* Removed the `QueryTransformer` SPI as it is not required anymore
* Changed the default correlation fetch strategy from `JOIN` to `SELECT`
* Changed `CorrelationProvider` and `CorrelationBuilder` to disallow specifying a custom alias
* Entity view metamodel changed to fix consistency problems and adapt for `@EmbeddableEntityView` removal
* The `EntityManagerFactoryIntegrator` SPI changed to allow retrieving existing registered functions as `JpqlFunction` objects 

## 1.2.0-Alpha2

01/02/2017 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.0-Alpha2) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue%20is%3Aclosed%20milestone%3A1.2.0%20closed%3A2016-09-29..2017-02-01)

### New features

* Spring integration for Entity Views
* Spring Data integration for Entity Views
* Showcase for CDI and Spring usage
* Allow to reuse keyset page in more situations
* Oracle and SQL Server support
* Basic EclipseLink support similar to DataNucleus
* Subviews for map keys
* Embeddable support for CTEs and VALUES clause
* Support for correlating unrelated entities in entity views
* Support for primitive types in entity views

### Bug fixes

* Support for primitive type in Entity Views
* Keyset pagination backwards scrolling didn't invert the result list
* JOIN FETCH problems with PaginatedCriteriaBuilder
* Duplicate entity mappings in case of classpath scanning
* MySQL 5.7 group by problems
* Edge cases for duplicate results in entity view collections
* Better builder end tracking to easier find errors in the code
* Various fixes for implicit group by generation

### Backwards-incompatible changes

* DbmsDialect has been extended. If you have a custom implementation you have to adapt
* Implicit downcasting has been removed and will be replaced with proper TREAT support in the next version. The branch *implicit-downcast* still has support for this but won't be further maintained.

## 1.2.0-Alpha1

29/09/2016 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.0-Alpha1) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue%20is%3Aclosed%20milestone%3A1.2.0%20closed%3A%3C2016-09-29)

### New features

* Entity join support
* Entity views can reference the `VIEW_ROOT` and have a custom root
* Type literal support in parser
* Correlated paths for subqueries support
* JPQL-Treat support
* Entity view can correlate subviews through subqueries
* VALUES clause support
* JPA Criteria API implementation backed by blaze-persistence
* SIZE to COUNT transformation was reworked for better reliability
* Entity view provider class validation

### Bug fixes

* SIZE to COUNT for indexed collections
* Hibernate Bug workaround HHH-9329
* Embeddable entity views allowed to be abstract now
* Some generics issues have been fixed

### Backwards-incompatible changes

* IN-predicate API of core module was changed to accept Collection instead of List

## 1.1.1

29/09/2016 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.1.1) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue%20is%3Aclosed%20milestone%3A1.1.1)

### New features

None

### Bug fixes

* Workaround for composite id entities in PaginatedCriteriaBuilder
* Hibernate integration issue fixes

### Backwards-incompatible changes

None
