# Change log

Changes that happened in releases

## 1.4.1

Not yet released

### New features

* Allow extraction of `CriteriaBuilder` for id-query from `FullQueryBuilder`
* Allow definition of CTE based on `CriteriaBuilder`
* Allow to control embedding of id query into pagination object query and enable by default if DBMS allows it
* Allow to control embedding of count query into pagination object/id query and enable by default if possible
* Implement option for inlining CTEs/Subquery in from clause and add lateral join support
* Use `JpqlFunction` for `NULL` literal expressions
* Use ` ` as query separator instead of `\n` for multi-query statements
* Use alias for `SET` clause in updatable entity view generated `UPDATE` statements to workaround the use of keywords as attribute names
* Introduce internal JPQL function for rendering of parameters for SELECT statements

### Bug fixes

* Validate that `VIEW_ROOT` and `EMBEDDING_VIEW` macros can't be used on `SELECT` or `SUBSELECT` fetched correlations when the view type has no `@IdMapping`
* Fix _null_/_empty_ flat view objects when using SELECT or SUBSELECT fetch strategy for a collection correlation
* Fix uniqueness analysis for single valued association id where the association is part of id class attributes
* Indexed element collection action optimization fails with `IndexOutOfBoundsException`
* Problems with `setFirstResult`/`setMaxResults` in subquery when in `EXISTS` predicate
* Fix problems with the Spring Data WebMvc integration regarding some auto configuration issues
* Fix rendering of `VALUES` clause with `ValuesStrategy.SELECT_UNION` necessary for MySQL 8.0.19+
* Support field access for entities as well for `VALUES` clause usage
* Fix `FULL` flushing of collections when elements are removed
* Fix `convertWith` support when using `convertAttribute` on collection view type attributes
* Support invoking setters of mutable attributes in updatable entity view constructor
* Implicit alias generation generates alias that is shadowed in child subquery which prevents copying
* Remove PathExpression resolving for expression cloning to avoid referring to later removed joins in copied queries
* Fix support for type converters for updatable entity views
* Fix for implicit parameter collision from query that is used for a subquery in another main query
* Applying entity-fetch computed collection actions fails because JPA collection elements assumed to be view elements

### Backwards-incompatible changes

None

## 1.4.0

04/01/2020 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.4.0) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue+milestone%3A1.4.0+is%3Aclosed+closed%3A%3C2020-01-05+sort%3Aupdated-desc)

### New features

None

### Bug fixes

* TREAT expression as join basis doesn't work
* Bytecode analysis fails in application servers
* Hibernate can't handle when SQL contains PostgreSQL double colon casts
* Fix entity joins with TREAT basis support

### Backwards-incompatible changes

None

## 1.4.0-Alpha4

29/12/2019 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.4.0-Alpha4) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue+milestone%3A1.4.0+is%3Aclosed+closed%3A%3C2019-12-30+sort%3Aupdated-desc)

### New features

* Jackson integration for Entity Views to allow deserializing updatable entity views
* Enable Jackson integration in the newly added JAX-RS integration
* Enable Jackson integration in Spring WebMvc integration
* Allow to define fetch graphs for Entity Views
* Create integration for Spring WebFlux similar to the Spring WebMvc one
* Add support for non-primary key associations in updatable entity views
* GraphQL integration for Entity Views with implementation for Relay spec
* Introduction of Entity View lifecycle listeners
* Bytecode analysis for creatable entity views regarding persistability validation
* Introduce possibility to force `PaginatedCriteriaBuilder` to always use a provided keyset
* Allow to configure keyset extraction and count query execution through `EntityViewSetting`
* Add `ISODAYOFWEEK` function to produce _Monday = 1 ... Sunday = 7_ and clarify documentation
* Official support for Java 13 and 14
* Add support for enum literals in builder API
* Allow to flush updatable entity views to entities directly
* Throw exception when `setFirstResult` or `setMaxResults` is used in a `CorrelationProvider` other than select with batch size 1
* Allow entity view conversion with fine grained control of attribute subview types
* Introduce type converters for entity views between `Calendar`/`GregorianCalendar` and `java.util.Date`

### Bug fixes

* Flushing updatable entity view that has a readonly declared entity view type fails when setting creatable subtype
* `JoinOnBuilder.onExpression()` doesn't register parameters
* Flat views without method attributes fail to generate
* `SIZE` doesn't work with `TREAT` expression
* Updatable entity views inverse mapping fails to flush when parent has embedded id and child has non-association back-reference or non-primary key mapping is used
* Fix limit rendering in main query if preceded by a returning cte query
* `SELECT` fetch mappings should correlate embedding view entity when association has extra join condition e.g. use Hibernate's `@Where`
* `SELECT` and `SUBSELECT` fetching fail to correctly correlate by embedding view that uses entity view inheritance
* Synthetic `EXISTS` subquery for association usage in ON clause doesn't preserve cardinality
* Make recording collections properly `Serializable`
* Properly inherit `RANGE` clause from `WINDOW` clause into window functions
* Fix duplicate `sortResolver` issues when using Spring Data WebMvc integration with Spring Boot
* Fix calling post rollback callbacks necessary for proper updatable entity view state resets
* Fix `TREAT` on super type too restrictive, should allow subtypes too
* Fix NPE when accessing entity attributes in `@MappingSubquery` expression
* Invalid path expression generated when using `SUBSELECT` fetching in a `JOIN` correlated view

### Backwards-incompatible changes

* Rename of the maven artifact `blaze-persistence-integration-spring-data-rest` to `blaze-persistence-integration-spring-data-webmvc`

## 1.4.0-Alpha3

26/10/2019 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.4.0-Alpha3) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue+milestone%3A1.4.0+is%3Aclosed+closed%3A%3C2019-10-27+sort%3Aupdated-desc)

### New features

* Add support for Spring Boot 2.2 and Spring Data 2.2 through a new integration module
* Add `@Repeatable` annotation to `@ViewFilter` and `@AttributeFilter` annotations
* Add support for Spring managed transactions for entity view aware repositories and updatable entity views
* Implement `toString()` for entity views showing the id or whole state for flat views
* Introduce new method `EntityViewManager.save()` to replace the now deprecated `EntityViewManager.update()`
* Performance optimization for group joins that were introduced in Hibernate 5.2
* Add new `orderByAsc`/`orderByDesc` variants with null ordering defaults
* Add deactivatable strict cascading check for updatable/creatable entity views
* Add support for `FULL JOIN` as provided by Hibernate

### Bug fixes

* Entity view generated equality is now based on the entity inheritance top type
* Properly support getters/setters with `protected` or `default` visibility
* Add support for natural id-based join tables
* Fix a `StackOverflowException` exception appearing in a special unbounded type variable scenario
* Fix a window function parsing issue that appears when using multiple partitions
* A parser regression regarding top-level treat expressions was fixed
* Fix problems during flushing of a declared a read-only flat view attribute with updatable subtype
* Resolve problems with multi-query repository methods by using Spring managed transactions and remove custom `SharedEntityManager`
* Show proper error message for creatable/updatable views that don't have an `@IdMapping`
* Properly implement invocation of `@PostCreate` annotated default method for Java 9+
* Avoid generating a default from clause alias that could clash with an entity name
* Fix startup problems for Hibernate 5.4+ with associations that use `@NotFound(action = NotFoundAction.IGNORE)`
* Fix problems with `MultipleSubqueryInitiator` on `RestrictionBuilder` that always reported a build was not closed
* Fix issues with `EntityViewManager.remove()` due to Hibernate returning scalar results rather than an `Object[]`

### Backwards-incompatible changes

* The `name` attribute of the `EntityView` annotation was removed because it had no purpose and only lead to problems
* The `com.blazebit.persistence.parser.expression.ExpressionCache` interface changed. For details see [#857](https://github.com/Blazebit/blaze-persistence/issues/857)

## 1.4.0-Alpha2

17/09/2019 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.4.0-Alpha2) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue+milestone%3A1.4.0+is%3Aclosed+closed%3A%3C2019-09-18+sort%3Aupdated-desc)

### New features

* Introduce support for adding whole predicates to predicate builders via `whereExpression`/`whereExpressionSubqueries` etc.
* Add support for subquery expression in the `ON` clause
* Introduce new comparison methods in predicate builders to allow making use of literal rendering in a type safe way
* Introduce new `*-jar` artifacts for modules to allow using Blaze-Persistence with older JVMs/ASM versions
* Shade ANTLR into parser to avoid conflicts
* Update ANTLR to 4.7.2
* Rework the parser grammar for better performance and to get rid of semantic predicates
* Introduce possibility to check if CTE type has been defined
* Add DeltaSpike 1.9 and Java 12 build support
* Introduce method to allow controlling the amount of extracted keyset objects
* Pass optional parameters to entity view `CTEProvider`
* Add support for window functions and named windows in builder and expressions via e.g. `SUM(x) OVER(PARTITION BY y)`
* Improve OpenJPA support
* Add support for MySQL 8
* Move `ConfigurationProperties` classes to API artifacts
* Add support for view property sorting in Spring Data
* Complete the set of date extraction, date diff and date arithmetic functions
* Add `ANY` as alias for the `OR_AGG` function
* Add support for using entity views as parameters in Spring Data repositories
* Add support for boolean wrapper type when using `is` method prefix
* Add support for Date and Calendar to Instant conversion for entity views

### Bug fixes

* Fix Hibernate Envers issues with JPA Criteria implementation
* Fix a NPE happening due to doing a manual select in an exists subquery that joins associations
* Avoid a NPE happening when trying to render a parameter as literal that has no literal representation
* Fix wrong instantiation of inheritance base type when null is expected
* Fix an issue with using a sub-association as correlation basis
* Don't treat creatable or updatable subtypes for mutable attributes as readonly
* Prevent cache trashing object builder cache when macros don't support caching

### Backwards-incompatible changes

* Dropped module classified artifacts in favor of adding `module-info.class` file to standard artifacts which now requires e.g. Jandex 2.0.4.Final or later

## 1.4.0-Alpha1

29/05/2019 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.4.0-Alpha1) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue+milestone%3A1.4.0+is%3Aclosed+closed%3A%3C2019-05-30+sort%3Aupdated-desc)

### New features

* Added startup check for accidental polymorphic CTEs
* Add new API that allows to specify a collection valued parameter for an IN predicate
* Upgrade Javassist to 3.25.0-GA

### Bug fixes

* Fix support for de-referencing map key entity type expressions
* Fix support for map key subviews
* Fix for absolute paths referring to dependent joins in ON clauses
* Attributes having the same name as a correlation basis alias aren't prefixed in the correlation expression
* Allow use of `TREAT` operator in subview mapping
* Fix invalid reduction of collection cardinality due to non-consideration of the parent tuple identity
* Support correlating `TREAT` expression
* Support using CTE entity type for VALUES clause in INSERT-SELECT
* Fix integration issue in `HibernateJpqlFunctionAdapter` with Hibernate 5.3
* Fix issues in `ConstantifiedJoinNodeAttributeCollector` happening when paginating while using an embeddable in the WHERE clause
* Don't ignore offset query parameter in spring data rest integration
* Deterministically resolve `EntityManager` in spring data integration
* Make sure mapping parameters are copied during entity view conversion
* Fix query flushing of inverse collections in updatable entity views
* Initialize non-mutable attributes with proper defaults in entity views created via `EntityViewManager.create()`
* Fix empty correlations due to wrong view index calculation in subselect correlation provider when using `EMBEDDING_VIEW`
* Reuse correlated attributes during EXISTS subquery rewrite for join ON clause predicates
* Handle new exception thrown by Javassist 3.24.0-GA when class definition fails
* Fix support for correlation that don't make use of the correlation key
* Make sure nested empty collections work properly
* Clause dependencies are wrongly propagated leading to cardinality mandatory joins not being rendered when copying queries

### Backwards-incompatible changes

None

## 1.3.2

26/02/2019 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.3.2) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?q=is%3Aissue+milestone%3A1.4.0+is%3Aclosed+closed%3A%3C2019-02-26+label%3Abug+sort%3Aupdated-desc)

### New features

None

### Bug fixes

* Fix issues with DeltaSpike Data integration and EclipseLink + DB2
* NPE during view attribute accessor resolving when lacking an explicit version mapping
* Workaround EclipseLink's wrong null precedence handling on platforms that don't support it
* Select correlation wrongly tries to load data based on null correlation basis
* Select alias expressions should be resolved when copying a query or when switching the select clause
* Non-entity-view deltaspike repository method wrongly tries to resolve entity class via view class
* Repository method findAll(int start, int max) for entity type doesn't properly paginate
* Replacing normal collection with recording during flush fails because of invalid setParent call due to ClearAction
* Readonly parents collection isn't copied when replacing the attribute values
* Updatable plural attribute doesn't allow updatable/creatable subtypes when missing the setter

### Backwards-incompatible changes

None

## 1.3.1

15/01/2019 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.3.1) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?q=is%3Aissue+milestone%3A1.4.0+is%3Aclosed+closed%3A%3C2019-01-15+label%3Abug+sort%3Aupdated-desc)

### New features

None

### Bug fixes

* Don't consider re-adds to a set to be adds
* Include constantified expression predicates in pagination object query
* Updatable entity view containing deeply nested collection that is empty wrongly removes tuples from result
* Nested join correlations results in exception due to invalid join base alias being used bug entity-view
* Empty flat view is added to non-indexed collection invalidly
* Dirty updatable flat views contained in maps aren't flushed if re-added
* Dirty state is not properly copied when converting between view types
* Invalid handling of discriminator predicate in collection update query generation
* Basic element types of collection bindings are being expanded
* State field wrongly sized for updatable entity views with inheritance leads to IOOBE
* Functional dependency analysis for pagination doesn't treat partial constantified many to one joins like single value id paths
* Implicit group by adds nested correlated subquery expression to grandfather query
* Joining embeddable on correlated root with JPA Criteria API produces invalid JPQL
* Multi-Column attribute where one column is a formula produces a startup exception

### Backwards-incompatible changes

None

## 1.3.0

23/11/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.3.0) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue+milestone%3A1.3.0+is%3Aclosed+closed%3A%3C2018-11-23+sort%3Aupdated-desc)

### New features

* Support for binding embeddables directly to embeddable path expressions in CTEs
* Support for binding associations that are part of an entities identifier
* Support for binding associations mapped by compound or foreign keys
* Using a comparator with `List` and `Collection` types in entity views will sort the collection after load
* Add option to force deduplication of elements in non-sets to `@CollectionMapping`
* Support plain `VALUES` clause with embeddable and most basic types
* Support for binding embeddable parameters in CTEs, insert and update queries
* Properly implement dirty state transfer when converting one entity view to another
* Added validation for `equals`/`hashCode` implementations of JPA managed types that are used within entity views which can be disabled with the property `com.blazebit.persistence.view.managed_type_validation_disabled`
* Add support for DeltaSpike Data 1.9
* Make use of Collection DML API when using the `QUERY` flush strategy in updatable entity views
* Automatic embeddable splitting within `GROUP BY` clause to avoid Hibernate bugs
* Support for entity view attribute filters and sorters on attributes of inheritance subtypes
* Introduced new method `EntityViewManager.getEntityReference()` to get an entity reference by an entity view object
* Allow to specify example attribute for `VALUES` clause for exact SQL types
* Implemented creatability validation for creatable entity views
* Implemented `SET_NULL` inverse remove strategy validation for updatable entity views
* Add updatable entity view support for inverse collections without a mapped by i.e. explicit join columns
* Rewrtite implicit joins in `ON` clause automatically to subqueries
* Add support for multiple non-cascading parent objects for updatable entity views
* Add support for correlated inheritance mappings and correlations in inheritance subtypes

### Bug fixes

* Using non-bags in entity views will now properly deduplicate elements
* Fix support for exists repository methods in Spring Data repositories
* Fix problems with count queries that require parameters in Spring Data repositories
* Properly set parent id on converted creatable subviews contained in inverse collections
* Fix type variable related issues with Spring Data Repositories
* Properly set parent id on converted creatable subviews contained in inverse collections
* Fix entity view build time validation error with certain inheritance mappings
* Fix problems with objects of wrong types being returned from standard repository methods with Spring Data 2.0+
* Fix various little issues related to embeddable handling in updatable entity views
* Fix support for mapping the inverse side of a `ManyToMany` as updatable collection
* Fix cycle detection for certain polymorphic entity view graphs
* Fix problems with correlated subqueries that used a `ManyToMany` collection

### Backwards-incompatible changes

* Require `@AllowUpdatableEntityViews` to be able to use updatable entity view types by for *ToOne relationships in updatable entity views to avoid [possible problems](https://persistence.blazebit.com/documentation/entity-view/manual/en_US/index.html#updatable-mappings-subview)

## 1.3.0-Alpha3

20/07/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.3.0-Alpha3) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue+milestone%3A1.3.0+is%3Aclosed+closed%3A%3C2018-09-06+sort%3Aupdated-desc)

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

20/07/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.3.0-Alpha2) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue+milestone%3A1.3.0+is%3Aclosed+closed%3A%3C2018-07-21+sort%3Aupdated-desc)

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

05/07/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.3.0-Alpha1) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=✓&q=is%3Aissue+milestone%3A1.3.0+is%3Aclosed+closed%3A<2018-07-06+sort%3Aupdated-desc)

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

05/07/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.1) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue+milestone%3A1.2.1+is%3Aclosed+sort%3Aupdated-desc)

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

08/05/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.0) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?q=is%3Aissue+closed%3A2018-03-18..2018-05-08+sort%3Aupdated-desc)

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

17/03/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.0-Alpha6) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?q=is%3Aissue+closed%3A2018-03-16..2018-03-17+sort%3Aupdated-desc)

### New features

None

### Bug fixes

* Fix problems related to the use of Spring Data's `JpaRepository` that caused startup errors
* Fix problems related to the use of DeltaSpike Data's `FullEntityRepository` that caused startup errors

### Backwards-incompatible changes

None

## 1.2.0-Alpha5

15/03/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.0-Alpha5) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?q=is%3Aissue+closed%3A2018-02-09..2018-03-16+sort%3Aupdated-desc)

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

10/02/2018 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.0-Alpha4) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?q=is%3Aissue+project%3ABlazebit%2Fblaze-persistence%2F3+is%3Aclosed+sort%3Aupdated-desc)

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

27/04/2017 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.0-Alpha3) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue%20is%3Aclosed%20milestone%3A1.2.0%20closed%3A2017-02-01..2017-04-27+sort%3Aupdated-desc)

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

01/02/2017 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.0-Alpha2) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue%20is%3Aclosed%20milestone%3A1.2.0%20closed%3A2016-09-29..2017-02-01+sort%3Aupdated-desc)

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

29/09/2016 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.0-Alpha1) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue%20is%3Aclosed%20milestone%3A1.2.0%20closed%3A%3C2016-09-29+sort%3Aupdated-desc)

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

29/09/2016 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.1.1) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue%20is%3Aclosed%20milestone%3A1.1.1+sort%3Aupdated-desc)

### New features

None

### Bug fixes

* Workaround for composite id entities in PaginatedCriteriaBuilder
* Hibernate integration issue fixes

### Backwards-incompatible changes

None
