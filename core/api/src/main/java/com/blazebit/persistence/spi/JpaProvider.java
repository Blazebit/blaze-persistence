/*
 * Copyright 2014 - 2023 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.spi;

import com.blazebit.persistence.JoinType;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import java.util.List;
import java.util.Map;

/**
 * A JPA provider implementation provides information about which features are supported by a JPA implementation.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface JpaProvider {

    /**
     * Whether JPA 2.1 specification is supported.
     *
     * @return True if JPA 2.1 is supported, false otherwise
     */
    public boolean supportsJpa21();

    /**
     * Whether Entity Joins are supported.
     *
     * @return True if Entity Joins are supported, false otherwise
     */
    public boolean supportsEntityJoin();

    /**
     * Whether cross joins are supported.
     *
     * @return True if cross joins are supported, false otherwise
     * @since 1.6.7
     */
    public boolean supportsCrossJoin();

    /**
     * Whether Insert statements are supported.
     *
     * @return True if Insert statements are supported, false otherwise
     */
    public boolean supportsInsertStatement();

    /**
     * Whether brackets are needed around a list parameter.
     *
     * @return True if brackets are needed, false otherwise
     */
    public boolean needsBracketsForListParameter();

    /**
     * Returns whether key restricted left joins should be rewritten to subquery joins.
     * This is part of the workaround for https://hibernate.atlassian.net/browse/HHH-9329 which makes key restricted left joins wrong.
     *
     * @return true if joins should be rewritten to subquery joins, false otherwise
     */
    public boolean needsJoinSubqueryRewrite();

    /**
     * The JPQL expression to use for the given literal boolean value.
     * This expression is used in contexts where an expression is assumed.
     *
     * @param value The boolean value
     * @return The JPQL expression to use to reflect the boolean value
     */
    public String getBooleanExpression(boolean value);

    /**
     * The JPQL conditional expression to use for the given literal boolean value.
     * This expression is used in contexts where a predicate is assumed.
     *
     * @param value The boolean value
     * @return The JPQL conditional expression to use to reflect the boolean value
     */
    public String getBooleanConditionalExpression(boolean value);

    /**
     * The JPQL expression to use for the NULL literal.
     *
     * @return The JPQL expression to use to reflect the NULL value
     */
    public String getNullExpression();

    /**
     * The name of the clause that has the same semantics as the ON clause which was introduced in JPA 2.1.
     *
     * @return The name of the ON clause
     */
    public String getOnClause();

    /**
     * Normally returns <code>VALUE</code>, but since Hibernate does weird things when using that, it returns <code>null</code>.
     * Returning null results in omitting <code>VALUE</code> in the final query that is passed to the JPA provider.
     *
     * @return The value function
     */
    public String getCollectionValueFunction();

    /**
     * Whether dereferencing a VALUE function expression is supported by the JPA provider.
     *
     * @return True if dereferencing is supported, false otherwise
     */
    public boolean supportsCollectionValueDereference();

    /**
     * Whether the LIMIT and OFFSET clause in subqueries are supported by the JPA provider.
     *
     * @return True if LIMIT/OFFSET in subqueries are supported, false otherwise
     * @since 1.6.7
     */
    public boolean supportsSubqueryLimitOffset();

    /**
     * Whether set operations are supported by the JPA provider.
     *
     * @return True if set operations are supported, false otherwise
     * @since 1.6.7
     */
    public boolean supportsSetOperations();

    /**
     * Whether listagg syntax is supported.
     *
     * @return True if listagg syntax is supported, false otherwise
     * @since 1.6.7
     */
    public boolean supportsListagg();

    /**
     * The default result type of a scalar query.
     *
     * @return The default result type
     */
    public Class<?> getDefaultQueryResultType();

    /**
     * Returns the start of a JPQL representation of a function invocation for the given function name and argument count.
     * Normally this returns <code>FUNCTION('<i>functionName</i>',</code> but implementations may also allow to render the function name directly.
     *
     * @param functionName The function name
     * @param argumentCount The number of the arguments that the function is invoked with
     * @return The start of the JPQL represenation for the function invocation
     */
    public String getCustomFunctionInvocation(String functionName, int argumentCount);

    /**
     * The given escape character for a LIKE predicate as string.
     *
     * @param character The escape character
     * @return The string representation
     */
    public String escapeCharacter(char character);

    /**
     * Whether the query language supports the null precedence clause.
     *
     * @return True if the null precedence clause is supported, false otherwise
     */
    public boolean supportsNullPrecedenceExpression();

    /**
     * Renders the null precedence into the given string builder.
     *
     * @param sb The builder to which the null precedence should be appended to
     * @param expression The order by expression which might be a select alias
     * @param resolvedExpression The resolved expression for a possible select alias or the expression
     * @param order The order as string (<code>ASC</code> or <code>DESC</code>)
     * @param nulls The null precedence as string (<code>NULLS FIRST</code> or <code>NULLS LAST</code>)
     */
    public void renderNullPrecedence(StringBuilder sb, String expression, String resolvedExpression, String order, String nulls);

    /**
     * Whether treating a from/root alias is supported.
     * For example <code>SELECT TREAT(alias AS Subtype).property FROM ..</code>
     *
     * @return True if treating a from alias is supported, false otherwise
     */
    public boolean supportsRootTreat();

    /**
     * Whether a treat join is supported.
     * For example <code>SELECT ... FROM .. JOIN TREAT(alias.relation AS Subtype)</code>
     *
     * @return True if a treat join is supported, false otherwise
     */
    public boolean supportsTreatJoin();

    /**
     * Whether a correlation path with a treat expression is supported.
     * For example <code>SELECT (SELECT .. FROM TREAT(parent AS Subtype).relation) FROM ..</code>
     *
     * @return True if a treat in correlation expressions is supported, false otherwise
     */
    public boolean supportsTreatCorrelation();

    /**
     * Whether a root treat in a join is supported.
     * For example <code>SELECT ... FROM .. JOIN TREAT(alias AS Subtype).relation</code>
     *
     * @return True if a root treat in a join is supported, false otherwise
     */
    public boolean supportsRootTreatJoin();

    /**
     * Whether a root treat in a treat join is supported.
     * For example <code>SELECT ... FROM .. JOIN TREAT(TREAT(alias AS Subtype).relation AS Subtype)</code>
     *
     * @return True if a root treat in a treat join is supported, false otherwise
     */
    public boolean supportsRootTreatTreatJoin();

    /**
     * Whether properties accessed of a from node are implicitly resolved to properties of a subtype of the from node.
     *
     * @return True if subtype property resolving is supported, false otherwise
     */
    public boolean supportsSubtypePropertyResolving();

    /**
     * Whether relations of a from node in joins are implicitly resolved to the relations of a subtype of the from node.
     *
     * @return True if subtype relation resolving is supported, false otherwise
     */
    public boolean supportsSubtypeRelationResolving();

    /**
     * Whether the <code>COUNT(*)</code> syntax is supported.
     *
     * @return True if <code>COUNT(*)</code> syntax is supported, false otherwise
     */
    public boolean supportsCountStar();

    /**
     * Whether the custom functions are supported.
     *
     * @return True if custom functions are supported, false otherwise
     * @since 1.4.0
     */
    public boolean supportsCustomFunctions();

    /**
     * Whether the non scalar subqueries are supported.
     *
     * @return True if non scalar subqueries are supported, false otherwise
     * @since 1.4.1
     */
    public boolean supportsNonScalarSubquery();

    /**
     * Whether the subqueries in functions are supported.
     *
     * @return True if subqueries in functions are supported, false otherwise
     * @since 1.4.1
     */
    public boolean supportsSubqueryInFunction();

    /**
     * Whether the subqueries alias shadowing is supported.
     *
     * @return True if subqueries alias shadowing is supported, false otherwise
     * @since 1.4.1
     */
    public boolean supportsSubqueryAliasShadowing();

    /**
     * Returns an array with the column name of the discriminator of the given entity type and the discriminator value, or null.
     *
     * @param entityType The entity type
     * @return The column name of the discriminator and the discriminator value, or null
     * @since 1.3.0
     */
    public String[] getDiscriminatorColumnCheck(EntityType<?> entityType);

    /**
     * Whether the join columns for the given attribute are in a foreign table.
     *
     * @param ownerType The owner of the attribute
     * @param attributeName The attribute name to check
     * @return True if join columns are in a foreign table, false otherwise
     */
    public boolean isForeignJoinColumn(EntityType<?> ownerType, String attributeName);

    /**
     * Whether columns for the given attribute are shared between multiple subtypes
     * or shared by occupying the same slot in the resulting SQL.
     *
     * @param ownerType The owner of the attribute
     * @param attributeName The attribute name to check
     * @return True if columns of the attribute are shared, false otherwise
     */
    public boolean isColumnShared(EntityType<?> ownerType, String attributeName);

    /**
     * Returns the column names of the attribute of the given entity type.
     *
     * @param ownerType The owner of the attribute
     * @param attributeName The attribute name
     * @return The column names of the attribute
     */
    public String[] getColumnNames(EntityType<?> ownerType, String attributeName);

    /**
     * Returns the column names of the attribute of the given entity type within the element collection.
     *
     * @param ownerType The owner of the attribute
     * @param elementCollectionPath The path to the element collection within which the attribute is contained
     * @param attributeName The attribute name
     * @return The column names of the attribute
     * @since 1.3.0
     */
    public String[] getColumnNames(EntityType<?> ownerType, String elementCollectionPath, String attributeName);

    /**
     * Returns the SQL column type names of the given attribute of the given entity type.
     *
     * @param ownerType The owner of the attribute
     * @param attributeName The attribute name
     * @return The SQL column type names for the attribute
     */
    public String[] getColumnTypes(EntityType<?> ownerType, String attributeName);

    /**
     * Returns the SQL column type names of the given attribute of the given entity type within the element collection.
     *
     * @param ownerType The owner of the attribute
     * @param elementCollectionPath The path to the element collection within which the attribute is contained
     * @param attributeName The attribute name
     * @return The SQL column type names for the attribute
     * @since 1.3.0
     */
    public String[] getColumnTypes(EntityType<?> ownerType, String elementCollectionPath, String attributeName);

    /**
     * Returns where to put treat filters for a treat joined association of this attribute.
     * This is for JPA providers that don't correctly filter the types.
     *
     * Hibernate for example does not automatically add the type constraint to treat joins of a type that uses
     * the table per class inheritance strategy.
     *
     * @param ownerType The declaring type of the attribute to check
     * @param attributeName The attribute name for which to check the treat filter requirement or null
     * @param joinType The join type used for the treat join
     * @return The constraint type for the treat filter
     */
    public ConstraintType requiresTreatFilter(EntityType<?> ownerType, String attributeName, JoinType joinType);

    /**
     * If the given attribute is an inverse collection, the mapped by attribute name is returned.
     * Otherwise returns null.
     *
     * @param ownerType The declaring type of the attribute to check
     * @param attributeName The name of the inverse attribute for which to retrieve the mapped by value
     * @return The mapped by attribute name if the given attribute is an inverse collection, null otherwise
     */
    public String getMappedBy(EntityType<?> ownerType, String attributeName);

    /**
     * If the given attribute is <em>insertable = false</em> and <em>updatable = false</em> it returns the writable mappings for the inverse type.
     * Otherwise returns null.
     *
     * @param inverseType The type containing the inverse relation
     * @param ownerType The declaring type of the attribute to check
     * @param attributeName The name of the attribute for which to retrieve the writable mapped by mapping
     * @param inverseAttribute The name of the inverse attribute for which to retrieve the writable mapped by mapping
     * @return The writable mappings for the inverse type if the attribute is not insertable or updatable, null otherwise
     */
    public Map<String, String> getWritableMappedByMappings(EntityType<?> inverseType, EntityType<?> ownerType, String attributeName, String inverseAttribute);

    /**
     * If the given attribute is a collection that uses a join table, returns it's descriptor.
     * Otherwise returns null.
     *
     * @param ownerType The declaring type of the attribute to check
     * @param attributeName The name of the attribute for which to retrieve the join table name
     * @return The join table information if the attribute has one, null otherwise
     */
    public JoinTable getJoinTable(EntityType<?> ownerType, String attributeName);

    /**
     * Whether the given attribute is a non-indexed and non-ordered collection a.k.a. a bag.
     *
     * @param ownerType The declaring type of the attribute to check
     * @param attributeName The name of the attribute to check
     * @return True if it is a bag, false otherwise
     */
    public boolean isBag(EntityType<?> ownerType, String attributeName);

    /**
     * Whether orphan removal is activated for the given attribute.
     *
     * @param ownerType The declaring type of the attribute to check
     * @param attributeName The name of the attribute to check
     * @return True if orphan removal is activated, else false
     */
    public boolean isOrphanRemoval(ManagedType<?> ownerType, String attributeName);

    /**
     * Whether orphan removal is activated for the given attribute within the element collection.
     *
     * @param ownerType The declaring type of the attribute to check
     * @param elementCollectionPath The path to the element collection within which the attribute is contained
     * @param attributeName The name of the attribute to check
     * @return True if orphan removal is activated, else false
     * @since 1.3.0
     */
    public boolean isOrphanRemoval(ManagedType<?> ownerType, String elementCollectionPath, String attributeName);

    /**
     * Whether delete cascading is activated for the given attribute.
     *
     * @param ownerType The declaring type of the attribute to check
     * @param attributeName The name of the attribute to check
     * @return True if delete cascading is activated, else false
     */
    public boolean isDeleteCascaded(ManagedType<?> ownerType, String attributeName);

    /**
     * Whether delete cascading is activated for the given attribute within the element collection.
     *
     * @param ownerType The declaring type of the attribute to check
     * @param elementCollectionPath The path to the element collection within which the attribute is contained
     * @param attributeName The name of the attribute to check
     * @return True if delete cascading is activated, else false
     * @since 1.3.0
     */
    public boolean isDeleteCascaded(ManagedType<?> ownerType, String elementCollectionPath, String attributeName);

    /**
     * Whether the given attribute has a join condition.
     *
     * @param ownerType The declaring type of the attribute to check
     * @param elementCollectionPath The nullable path to the element collection within which the attribute is contained
     * @param attributeName The name of the attribute to check
     * @return True if it has a join condition, else false
     */
    public boolean hasJoinCondition(ManagedType<?> ownerType, String elementCollectionPath, String attributeName);

    /**
     * Returns whether the entity with the id is contained in the entity managers persistence context.
     *
     * @param em The entity manager
     * @param entityClass The entity class
     * @param id The entity id
     * @return True if it is contained, false otherwise
     */
    public boolean containsEntity(EntityManager em, Class<?> entityClass, Object id);

    /**
     * Indicates if the provider supports expressions like
     *
     *    doc.owner.id
     *
     * without generating an extra join.
     *
     * @return true if supported, else false
     */
    public boolean supportsSingleValuedAssociationIdExpressions();

    /**
     * Indicates if the provider supports the use of foreign associations in the ON clause.
     * This is the case when implicit joins in the ON clause are specially treated regarding SQL rendering.
     * If an expression like <code>alias.association.property</code> results in a subquery or table group join
     * when used in the ON clause, the JPA provider supports this feature. Normally this silently fails with invalid SQL.
     * By returning false, subqueries will be generated for such associations instead.
     *
     * The value is not yet used but will be in a future version. Also see: https://github.com/Blazebit/blaze-persistence/issues/402
     *
     * @return true if supported, else false
     */
    public boolean supportsForeignAssociationInOnClause();

    /**
     * Indicates whether an embeddable can be set via an update queries SET clause.
     * Although the JPA spec mandates this, it doesn't seem to be asserted so some providers don't support it.
     *
     * @return true if supported, else false
     */
    public boolean supportsUpdateSetEmbeddable();

    /**
     * Indicates whether an association id can be set via an update queries SET clause or if the association has to be set as a whole.
     *
     * @return true if supported, else false
     * @since 1.5.0
     */
    public boolean supportsUpdateSetAssociationId();


    /**
     * Indicates if the provider supports the use of transient entity objects as parameters.
     *
     * @return true if supported, else false
     */
    public boolean supportsTransientEntityAsParameter();

    /**
     * Indicates if the provider needs associations in the ON clause to use their id.
     * If needed, an expression like <code>alias.association</code> in the ON clause is rewritten to
     * <code>alias.association.id</code>.
     *
     * @return true if required, else false
     */
    public boolean needsAssociationToIdRewriteInOnClause();

    /**
     * Indicates if the provider needs associations in the ON clause to use their id.
     * If needed, an expression like <code>alias.association</code> in the ON clause is rewritten to
     * <code>alias.association.id</code> which relies on a <i>broken</i> type check in older Hibernate versions.
     *
     * @return true if required, else false
     */
    public boolean needsBrokenAssociationToIdRewriteInOnClause();

    /**
     * Indicates if the provider does <i>column sharing</i> for same named columns in inheritance mappings
     * and thus requires the use of a CASE WHEN expression for restricting casted accesses like e.g. <code>TREAT(alias AS Subtype).property</code>
     * to retain cast semantics.
     *
     * @return true if required, else false
     */
    public boolean needsTypeConstraintForColumnSharing();

    /**
     * Indicates whether the provider clears collection table entries on bulk delete operations.
     *
     * @return true if supported, else false
     */
    public boolean supportsCollectionTableCleanupOnDelete();

    /**
     * Indicates whether the provider clears join table entries on bulk delete operations.
     *
     * @return true if supported, else false
     */
    public boolean supportsJoinTableCleanupOnDelete();

    /**
     * Indicates whether the provider supports correlating inverse associations.
     *
     * @return true if needed, else false
     * @since 1.3.0
     */
    public boolean needsCorrelationPredicateWhenCorrelatingWithWhereClause();

    /**
     * Indicates whether the provider supports optimized natural id access.
     *
     * @return true if supported, else false
     * @since 1.3.0
     */
    public boolean supportsSingleValuedAssociationNaturalIdExpressions();

    /**
     * Indicates whether the provider supports group by entity alias properly.
     *
     * @return true if supported, else false
     * @since 1.3.0
     */
    public boolean supportsGroupByEntityAlias();

    /**
     * Indicates whether the provider needs to cutoff id properties when used as subpath of element collections.
     * This is to be able to workaround https://hibernate.atlassian.net/browse/HHH-13045 .
     *
     * @return true if needed, else false
     * @since 1.3.0
     */
    public boolean needsElementCollectionIdCutoff();

    /**
     * Indicates whether the provider requires an unproxied entity for field access to work.
     *
     * @return true if needed, else false
     * @since 1.4.1
     */
    public boolean needsUnproxyForFieldAccess();

    /**
     * Indicates whether the provider always requires an else branch in a case when expression.
     *
     * @return true if needed, else false
     * @since 1.6.0
     */
    public boolean needsCaseWhenElseBranch();

    /**
     * Indicates whether the provider supports automatic like pattern escaping according to the database requirements.
     *
     * @return true if supported, else false
     * @since 1.6.3
     */
    public boolean supportsLikePatternEscape();

    /**
     * Enables query result caching for the given query.
     *
     * @param query Enables query result caching for the query
     * @since 1.2.0
     */
    public void setCacheable(Query query);

    /**
     * Sets the given parameter as singular parameter on the given query.
     *
     * @param query The query to set the parameter on
     * @param name The parameter name
     * @param value The parameter value
     * @since 1.6.8
     */
    public void setSingularParameter(Query query, String name, Object value);

    /**
     * Get the identifier or unique key inverse properties of an association attribute.
     *
     * @param owner The owning entity type
     * @param attributeName The association attribute
     * @return the identifier or unique key inverse properties of the association attribute
     * @since 1.2.1
     * @deprecated Use {@link #getJoinMappingPropertyNames(EntityType, String, String)} instead
     */
    public List<String> getIdentifierOrUniqueKeyEmbeddedPropertyNames(EntityType<?> owner, String attributeName);

    /**
     * Get the identifier or unique key inverse properties of an association attribute within an element collection.
     *
     * @param owner The owning entity type
     * @param elementCollectionPath The path to the element collection within which the attribute is contained
     * @param attributeName The association attribute
     * @return the identifier or unique key inverse properties of the association attribute
     * @since 1.3.0
     * @deprecated Use {@link #getJoinMappingPropertyNames(EntityType, String, String)} instead
     */
    public List<String> getIdentifierOrUniqueKeyEmbeddedPropertyNames(EntityType<?> owner, String elementCollectionPath, String attributeName);

    /**
     * Returns the join mapping property names of an association attribute. This is a mapping from target to source property names.
     *
     * @param owner The owning entity type
     * @param elementCollectionPath The nullable path to the element collection within which the attribute is contained
     * @param attributeName The association attribute
     * @return the property names from target type to the source type
     * @since 1.4.0
     */
    public Map<String, String> getJoinMappingPropertyNames(EntityType<?> owner, String elementCollectionPath, String attributeName);

    /**
     * Whether the enum type of the given attribute supports being used as literal by it's FQN.
     *
     * @param ownerType The declaring type of the attribute to check
     * @param attributeName The name of the attribute to check
     * @param key Whether the key part of a map attribute should be checked or the attribute element type
     * @return <code>true</code> if supported, <code>false</code> otherwise
     * @since 1.5.0
     */
    public boolean supportsEnumLiteral(ManagedType<?> ownerType, String attributeName, boolean key);

    /**
     * Whether the JPA provider supports the temporal literal.
     *
     * @return <code>true</code> if supported, <code>false</code> otherwise
     * @since 1.5.0
     */
    public boolean supportsTemporalLiteral();

    /**
     * Whether the JPA provider supports the use of an alias other than the driving alias in the ON clause.
     *
     * @return <code>true</code> if supported, <code>false</code> otherwise
     * @since 1.5.0
     */
    public boolean supportsNonDrivingAliasInOnClause();

    /**
     * Whether the JPA provider supports the use of an entity alias in a subquery for comparison predicates when the entity has an embedded/composite id.
     *
     * @return <code>true</code> if supported, <code>false</code> otherwise
     * @since 1.5.0
     */
    public boolean supportsSelectCompositeIdEntityInSubquery();

    /**
     * Whether the JPA provider supports the use of an entity proxy as parameter for a non primary key association.
     *
     * See https://github.com/Blazebit/blaze-persistence/issues/1082 for details.
     *
     * @return <code>true</code> if supported, <code>false</code> otherwise
     * @since 1.6.0
     */
    public boolean supportsProxyParameterForNonPkAssociation();

    /**
     * Whether the JPA provider supports the removal of uninitialized proxies.
     *
     * @return <code>true</code> if supported, <code>false</code> otherwise
     * @since 1.6.9
     */
    public boolean supportsProxyRemove();

    /**
     * Ensures that the given entity is initialized.
     *
     * @param entity The entity
     * @since 1.6.9
     */
    public void initialize(Object entity);

    /**
     * Returns the identifier of the entity object.
     *
     * @param entity The entity
     * @return the primary identifier of the entity
     * @since 1.3.0
     */
    public Object getIdentifier(Object entity);

    /**
     * Returns the unproxied i.e. the real entity object.
     *
     * @param entity The entity, maybe a proxy
     * @param <T> The entity type
     * @return the real entity object
     * @since 1.3.0
     */
    public <T> T unproxy(T entity);

    /**
     * Returns the JpaMetamodelAccessor for this JPA vendor.
     *
     * @return the JpaMetamodelAccessor for this JPA vendor
     * @since 1.3.0
     */
    public JpaMetamodelAccessor getJpaMetamodelAccessor();

    /**
     * The possible locations of a constraint.
     *
     * @author Christian Beikov
     */
    public static enum ConstraintType {
        /**
         * No constraint.
         */
        NONE,
        /**
         * Constraint in the ON clause.
         */
        ON,
        /**
         * Constraint in the WHERE clause.
         */
        WHERE;
    }
}
