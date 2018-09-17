# Change log

Changes that happened in releases

## 1.3.0

Not yet released

### New features

* Support for binding Embeddables directly to embeddable path expressions in CTE's
* Support for binding associations that are part of an entities identifier
* Support for binding associations mapped by compound or foreign keys
* Using a comparator with `List` and `Collection` types in entity views will sort the collection after load
* Add option to force deduplication of elements in non-sets to `@CollectionMapping`

### Bug fixes

* Using non-bags in entity views will now properly deduplicate elements

### Backwards-incompatible changes

None yet

## 1.3.0-Alpha3

20/07/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.3.0-Alpha3) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue+milestone%3A1.3.0+is%3Aclosed+closed%3A%3C2018-09-06)

### New features

* Hibernate 5.4 support
* Add support for `GROUP BY` with `PaginatedCriteriaBuilder`
* `PaginatedCriteriaBuilder` API allows to define custom identifiers by which to paginate
* Optimizations for pagination and `ORDER BY` rendering based on improved nullability and uniqueness analysis
* Partial support for `@IdClass` or multiple id attribute use cases
* Support for using `Specification` in Spring Data Repository query methods
* Support updating/persisting updatable/creatable entity views in Spring Data repositories
* Add support for cascading of correlated updatable entity views
* Add support for inverse OneToOne mappings in updatable entity views
* Improve performance of aggregate queries when using pagination by doing a functional dependency analysis 
* Treat array expression joins like *ToOne joins and avoid unnecessary id query usages because of that
* Don't fallback to offset pagination when changing `maxResults`
* Make use of `getOffset` rather than `getPageNumber` in Spring Data and DeltaSpike Data repositories
* Introduce `offset` and `prevOffset` parameters for JAX-RS and Spring MVC integrations
* Avoid duplicate select items for when doing keyset extraction with `PaginatedCriteriaBuilder`
* Support keyset pagination with embedded ids
* Support keyset pagination with non-basic attribute types like e.g. enums
* Fetch just a singular id if possible when using an object type as correlation basis
* Omit joins that are only used in the select clause when removing the default select after query copying
* Add support for Spring Data 2.1 with a new integration module
* Introduced `EntityViewSettingProcessor` to allow customizing `EntityViewSetting` used in Spring Data repositories
* Add `with*` methods to `EntityViewSetting` for easy method chaining
* Allow to force the use of an id query with `withForceIdQuery` on `PaginatedCriteriaBuilder`
* Added workaround for [HHH-12942](https://hibernate.atlassian.net/browse/HHH-12942)

### Bug fixes

* Problems with the use of the `VALUES` clause and parameters in the select clause have been fixed
* Fix an NPE caused by passing a `null` sort param to count query creation
* Criteria query objects constructed from our JPA criteria implementation are now independent of an `EntityManager`
* Fix possible NPEs when passing a null predicate to criteria query objects
* Postfix query root alias if it equals a JPA keyword
* Fix _parent already set_ problem with updatable flat views that contain collections or subviews
* Always use `init` entity view constructor when no explicit constructor was requested rather than throwing an exception
* Fix a nasty bug related to the use of type converters within flat view types
* Fix adding updatable views with collections to inverse relationships with query flushing
* Properly restore a creatable collection element when rollback happened due to error in that element
* Properly apply `firstResult` and `maxResults` given in `CriteriaBuilder` to queries that involve custom SQL
* Reset updatable entity view parent id of inverse relations on rollback properly
* Fix parameter handling in custom functions for EclipseLink when rendering parameters as chunks
* Fix identity problems of updatable subviews for embeddables
* Fix wrong order of keysets in the keyset page returned from a _previous page request_
* Fix some problems with inverse collection flushing and setting the parent on existing view fields
* Fix a few problems with state resetting of updatable entity views on transaction rollback
* Fix query generation problems in JPA Criteria implementation when using correlated roots in subqueries

### Backwards-incompatible changes

* `BlazeCriteria` and `BlazeCriteriaBuilderFactory` now accept only a `CriteriaBuilderFactory`
* An `EntityManager` is now required during `CriteriaBuilder` construction in `BlazeCriteriaQuery.createCriteriaBuilder`
* `BlazeCriteriaQuery` does not implement `Queryable` anymore. Use `BlazeCriteriaQuery.createCriteriaBuilder(EntityManager)` which returns a `Queryable`
* `BlazeCriteriaDelete` does not implement `Executable` anymore. Use `BlazeCriteriaDelete.createCriteriaBuilder(EntityManager)` which returns a `Executable`
* `BlazeCriteriaUpdate` does not implement `Executable` anymore. Use `BlazeCriteriaUpdate.createCriteriaBuilder(EntityManager)` which returns a `Executable`
* DeltaSpike Data `KeysetPageRequest(KeysetPageable, Sort, int, int)` constructor was changed to match the order as defined in the `PageRequest`
* Spring Data `KeysetPageRequest(KeysetPageable, Sort, int, int)` constructor was changed to match the order as defined in the `PageRequest`
* Spring Data 2.0 integration maven artifact name is now `blaze-persistence-integration-spring-data-2.0` instead of `blaze-persistence-integration-spring-data-2.x`

## 1.3.0-Alpha2

20/07/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.3.0-Alpha2) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue+milestone%3A1.3.0+is%3Aclosed+closed%3A%3C2018-07-21)

### New features

* Add support for providing entity view optional parameters in spring-data repositories via `@OptionalParam`
* Introduced `EMBEDDING_VIEW` function as proper replacement for many `OUTER` function uses in entity views
* Smoothen support for embeddables in updatable entity views
* Improve performance by omitting null precedence emulation on MySQL for the default null precedence
* Allow use of `OUTER` and other macros in subquery correlation join path

### Bug fixes

* Fix for `NullPointerException` that happened during query builder copying when having a parameter multiple times in a select clause
* Allow correlating entities with embedded ids with the batch select fetch strategy

### Backwards-incompatible changes

None

## 1.3.0-Alpha1

05/07/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.3.0-Alpha1) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=✓&q=is%3Aissue+milestone%3A1.3.0+is%3Aclosed+closed%3A<2018-07-06)

Note that 1.3.0-Alpha1 contains all fixes from 1.2.1 as well.

### New features

* Hibernate 5.3 support
* Allow querying by `javax.persistence.metamodel.EntityType` in addition to `Class` to support dynamic entities
* Entity join support in JPA Criteria API extension
* Support the use of Hibernate Envers Audited entities
* Move `BlazeCriteria` to jpa-criteria-api artifact

### Bug fixes

* Fix `AbstractMethodError` problems encountered when using JPA 2.1 Criteria methods
* Fix correlation mapping issue with `FetchStrategy.JOIN` when using joinable correlation result
* Fixed problems when using positional query parameters (i.e. through Hibernate's `@Filter`) on collections using the `SUBSELECT` fetch mode on entities that were fetched through a CTE query.
* Fix type determination of columns with custom Hibernate user types
* Fix problems with paths resolving to embeddable types in correlation basis mapping
* Fix problems with paths resolving to embeddable types when used as entity view root
* Fix problems with quoted identifiers
* Fix startup errors when using embedded id sub-property in `VIEW_ROOT` with `SUBSELECT` and `SELECT` strategy
* Support natural id associations
* Allow using array access syntax for maps contained in embeddables

### Backwards-incompatible changes

* Remove `BlazeCriteriaBuilder.functionFunction` in favor of using `CriteriaBuilder.function`

## 1.2.1

05/07/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.1) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue+milestone%3A1.2.1+is%3Aclosed)

### New features

* Hibernate 5.3 support
* Move `BlazeCriteria` to jpa-criteria-api artifact

### Bug fixes

* Fix `AbstractMethodError` problems encountered when using JPA 2.1 Criteria methods
* Fix correlation mapping issue with `FetchStrategy.JOIN` when using joinable correlation result
* Fixed problems when using positional query parameters (i.e. through Hibernate's `@Filter`) on collections using the `SUBSELECT` fetch mode on entities that were fetched through a CTE query.
* Fix type determination of columns with custom Hibernate user types
* Fix problems with paths resolving to embeddable types in correlation basis mapping
* Fix problems with paths resolving to embeddable types when used as entity view root
* Fix problems with quoted identifiers
* Fix startup errors when using embedded id sub-property in `VIEW_ROOT` with `SUBSELECT` and `SELECT` strategy
* Support natural id associations

### Backwards-incompatible changes

None

## 1.2.0

08/05/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.0) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?q=is%3Aissue+closed%3A2018-03-18..2018-05-08)

### New features

* Java 9 & 10 support
* Allow to skip count query in PaginatedCriteriaBuilder API
* Add support for orphan deletion in updatable entity views
* Improve expression caching performance

### Bug fixes

* Fix various issues in the Spring Data and DeltaSpike Data integration
* Fix query generation issue when using an aggregate around a `SIZE` expression

### Backwards-incompatible changes

* ExpressionCache SPI was changed to allow an easy implementation

## 1.2.0-Alpha6

17/03/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.0-Alpha6) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?q=is%3Aissue+closed%3A2018-03-16..2018-03-17)

### New features

None

### Bug fixes

* Fix problems related to the use of Spring Data's `JpaRepository` that caused startup errors
* Fix problems related to the use of DeltaSpike Data's `FullEntityRepository` that caused startup errors

### Backwards-incompatible changes

None

## 1.2.0-Alpha5

15/03/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.0-Alpha5) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?q=is%3Aissue+closed%3A2018-02-09..2018-03-16)

### New features

* Support for fetch strategies for non-correlated mappings
* Add method to enable query result caching
* `CorrelationBuilder` now returns `CorrelationQueryBuilder` that offers extended functionality
* Updatable attributes by default now allow all known subtypes that don't introduce cycles, rather than requiring the user to specify all allowed subtypes
* Spring Data Rest integration offering first class keyset pagination support
* DeltaSpike Data Rest integration offering pagination support similar to what Spring Data offers
* Support for Spring Data 2.0 via a new integration module
* Support for DeltaSpike Data 1.8 via a new integration module
* Experimental support for Hibernate 5.3 and DataNucleus 5.1
* Support for compiling and running on JDK 9 & 10 & 11-EA
* Add Automatic-Module-Name manifest entry for all modules
* Support for positional parameters
* Improve error messages for various error scenarios

### Bug fixes

* `EntityViewManager` was wrongly passed to `@PostCreate` Java 8 default methods in interfaces
* Fix illegal Entity-To-Association-Id rewrite
* Fix some bugs related to updatable inverse collection mappings
* Fix some small bugs related to the use of normal entity views and the change model API
* Fix bug related to Hibernate `@Subselect` usage
* Fix bug related to missing aliases in `Tuple`
* Fix in cyclic join detection that happened when using multiple join nodes in ON clause
* Fix exception related to cyclic select alias resolving
* Fix exception related to use of `@SeoncdaryTable` with entity inheritance

### Backwards-incompatible changes

* Renamed `whereExpression` to `setWhereExpression`
* Renamed `havingExpression` to `setHavingExpression`
* Renamed `onExpression` to `setOnExpression`
* Renamed `whereExpressionSubqueries` to `setWhereExpressionSubqueries`
* Renamed `havingExpressionSubqueries` to `setHavingExpressionSubqueries`
* Renamed `onExpressionSubqueries` to `setOnExpressionSubqueries`
* Package renamings for many modules to avoid split package problem on JDK 9
* Rename SpringData integration module since it was for 1.11 only and add module for 2.0
* Rename DeltaSpike integration module since it was for 1.7 only and add module for 1.8
* `CorrelationBuilder` now returns `CorrelationQueryBuilder` that offers extended functionality

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
