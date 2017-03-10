# Change log

Changes that happened in releases

## 1.2.0-Alpha3

Not yet released

### New features

* Finally there is a full [reference documentation](https://persistence.blazebit.com/documentation.html) available
* Keyset pagination now falls back to offset pagination when requesting the _first page_
* Created test case [template projects](https://github.com/Blazebit/blaze-persistence-test-case-template)
* Entity View Spring integration now allows the use of `includeFilters` and `excludeFilters` on `@EnableEntityViews`
* Extended `SubqueryInitiator` by most of the `from()` variants
* Support enum and entity type literal like the JPA spec says

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

## 1.2.0-Alpha2

1. February 2017 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.0-Alpha2) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue%20is%3Aclosed%20milestone%3A1.2.0%20closed%3A%3E2016-09-29%20closed%3A%3C2017-02-01%20)

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

29. September 2016 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.2.0-Alpha1) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue%20is%3Aclosed%20milestone%3A1.2.0%20closed%3A%3C2016-09-29)

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

29. September 2016 - [Release tag](https://github.com/Blazebit/blaze-persistence/releases/tag/1.1.1) [Resolved issues](https://github.com/Blazebit/blaze-persistence/issues?utf8=%E2%9C%93&q=is%3Aissue%20is%3Aclosed%20milestone%3A1.1.1)

### New features

None

### Bug fixes

* Workaround for composite id entities in PaginatedCriteriaBuilder
* Hibernate integration issue fixes

### Backwards-incompatible changes

None
