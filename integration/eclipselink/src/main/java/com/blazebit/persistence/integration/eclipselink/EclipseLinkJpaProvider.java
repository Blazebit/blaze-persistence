/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.integration.eclipselink;

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.integration.jpa.JpaMetamodelAccessorImpl;
import com.blazebit.persistence.spi.JoinTable;
import com.blazebit.persistence.spi.JpaMetamodelAccessor;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.reflection.ReflectionUtils;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.jpa.metamodel.AttributeImpl;
import org.eclipse.persistence.internal.jpa.metamodel.ManagedTypeImpl;
import org.eclipse.persistence.internal.jpa.metamodel.PluralAttributeImpl;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.mappings.AggregateCollectionMapping;
import org.eclipse.persistence.mappings.CollectionMapping;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectCollectionMapping;
import org.eclipse.persistence.mappings.DirectMapMapping;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;
import org.eclipse.persistence.mappings.ManyToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EclipseLinkJpaProvider implements JpaProvider {

    private static final String[] EMPTY = {};
    private final PersistenceUnitUtil persistenceUnitUtil;
    private final DB db;

    /**
     * @author Christian Beikov
     * @since 1.4.0
     */
    private static enum DB {
        OTHER,
        DB2;
    }

    public EclipseLinkJpaProvider(PersistenceUnitUtil persistenceUnitUtil, String dbms) {
        this.persistenceUnitUtil = persistenceUnitUtil;
        if ("db2".equals(dbms)) {
            this.db = DB.DB2;
        } else {
            this.db = DB.OTHER;
        }
    }

    @Override
    public boolean supportsJpa21() {
        return true;
    }

    @Override
    public boolean supportsEntityJoin() {
        return true;
    }

    @Override
    public boolean supportsInsertStatement() {
        return false;
    }

    @Override
    public boolean needsBracketsForListParameter() {
        return false;
    }

    @Override
    public boolean needsJoinSubqueryRewrite() {
        return false;
    }

    @Override
    public String getBooleanExpression(boolean value) {
        return value ? "TRUE" : "FALSE";
    }

    @Override
    public String getBooleanConditionalExpression(boolean value) {
        return value ? "TRUE" : "FALSE";
    }

    @Override
    public String getNullExpression() {
        return "NULL";
    }

    @Override
    public String escapeCharacter(char character) {
        return Character.toString(character);
    }

    @Override
    public boolean supportsNullPrecedenceExpression() {
        return db == DB.OTHER;
    }

    @Override
    public void renderNullPrecedence(StringBuilder sb, String expression, String resolvedExpression, String order, String nulls) {
        if (nulls != null) {
            if (db == DB.DB2) {
                // We need this workaround since EclipseLink doesn't handle null precedence properly on DB2..
                if ("FIRST".equals(nulls) && "DESC".equalsIgnoreCase(order) || "LAST".equals(nulls) && "ASC".equalsIgnoreCase(order)) {
                    // According to DB2 docs, the following are the defaults
                    // ASC + NULLS LAST
                    // DESC + NULLS FIRST
                    sb.append(expression);
                    sb.append(" ").append(order);
                    return;
                } else {
                    sb.append("CASE WHEN ").append(resolvedExpression != null ? resolvedExpression : expression).append(" IS NULL THEN ");
                    if ("FIRST".equals(nulls)) {
                        sb.append("0 ELSE 1");
                    } else {
                        sb.append("1 ELSE 0");
                    }
                    sb.append(" END, ");
                }
                sb.append(expression);
                if (order != null) {
                    sb.append(' ').append(order);
                }
            } else {
                sb.append(expression);
                if (order != null) {
                    sb.append(' ').append(order).append(" NULLS ").append(nulls);
                }
            }
        } else {
            sb.append(expression);
            if (order != null) {
                sb.append(' ').append(order);
            }
        }
    }

    @Override
    public String getOnClause() {
        return "ON";
    }

    @Override
    public String getCollectionValueFunction() {
        return "VALUE";
    }

    @Override
    public boolean supportsCollectionValueDereference() {
        return false;
    }

    @Override
    public Class<?> getDefaultQueryResultType() {
        return Object.class;
    }

    @Override
    public String getCustomFunctionInvocation(String functionName, int argumentCount) {
        // Careful, PaginatedCriteriaBuilder has some dependency on the "length" of the string for rendering in the count query
        if (argumentCount == 0) {
            return "OPERATOR('" + functionName + "',''";
        }

        return "OPERATOR('" + functionName + "',";
    }

    @Override
    public boolean supportsRootTreat() {
        return true;
    }

    @Override
    public boolean supportsTreatJoin() {
        return true;
    }

    @Override
    public boolean supportsTreatCorrelation() {
        return false;
    }

    @Override
    public boolean supportsRootTreatJoin() {
        return false;
    }

    @Override
    public boolean supportsRootTreatTreatJoin() {
        // Only a few cases work
        return true;
    }

    @Override
    public boolean supportsSubtypePropertyResolving() {
        return false;
    }

    @Override
    public boolean supportsSubtypeRelationResolving() {
        return false;
    }

    @Override
    public boolean supportsCountStar() {
        return false;
    }

    @Override
    public boolean supportsCustomFunctions() {
        return true;
    }

    @Override
    public boolean supportsNonScalarSubquery() {
        return false;
    }

    @Override
    public boolean supportsSubqueryInFunction() {
        return false;
    }

    @Override
    public boolean supportsSubqueryAliasShadowing() {
        return true;
    }

    @Override
    public String[] getDiscriminatorColumnCheck(EntityType<?> entityType) {
        return null;
    }

    @Override
    public boolean isForeignJoinColumn(EntityType<?> ownerType, String attributeName) {
        ManagedTypeImpl<?> managedType = (ManagedTypeImpl<?>) ownerType;
        String[] parts = attributeName.split("\\.");
        DatabaseMapping mapping = managedType.getDescriptor().getMappingForAttributeName(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            mapping = mapping.getReferenceDescriptor().getMappingForAttributeName(parts[i]);
        }
        if (mapping instanceof OneToOneMapping) {
            return ((OneToOneMapping) mapping).hasRelationTable();
        }
        return false;
    }

    @Override
    public boolean isColumnShared(EntityType<?> ownerType, String attributeName) {
        return false;
    }

    @Override
    public ConstraintType requiresTreatFilter(EntityType<?> ownerType, String attributeName, JoinType joinType) {
        return ConstraintType.NONE;
    }

    @Override
    public String getMappedBy(EntityType<?> ownerType, String attributeName) {
        DatabaseMapping mapping = getAttribute(ownerType, attributeName).getMapping();
        if (mapping instanceof CollectionMapping) {
            return ((CollectionMapping) mapping).getMappedBy();
        }

        return null;
    }

    @Override
    public Map<String, String> getWritableMappedByMappings(EntityType<?> inverseType, EntityType<?> ownerType, String attributeName, String inverseAttribute) {
        return null;
    }

    @Override
    public String[] getColumnNames(EntityType<?> ownerType, String attributeName) {
        return EMPTY;
    }

    @Override
    public String[] getColumnNames(EntityType<?> ownerType, String elementCollectionPath, String attributeName) {
        return EMPTY;
    }

    @Override
    public String[] getColumnTypes(EntityType<?> ownerType, String attributeName) {
        return EMPTY;
    }

    @Override
    public String[] getColumnTypes(EntityType<?> ownerType, String elementCollectionPath, String attributeName) {
        return EMPTY;
    }

    @Override
    public JoinTable getJoinTable(EntityType<?> ownerType, String attributeName) {
        DatabaseMapping mapping = getAttribute(ownerType, attributeName).getMapping();
        if (mapping instanceof OneToOneMapping) {
            OneToOneMapping oneToOneMapping = (OneToOneMapping) mapping;
            if (oneToOneMapping.hasRelationTable()) {
                Map<String, String> idColumnMapping = new LinkedHashMap<>();
                Map<String, String> keyMapping = null;
                Map<String, String> keyColumnTypes = null;
                Map<String, String> targetIdColumnMapping = new LinkedHashMap<>();
                return new JoinTable(
                        oneToOneMapping.getRelationTable().getName(),
                        null,
                        idColumnMapping,
                        keyMapping,
                        keyColumnTypes,
                        null,
                        targetIdColumnMapping

                );
            }
        } else if (mapping instanceof CollectionMapping) {
            CollectionMapping collectionMapping = (CollectionMapping) mapping;
            if (collectionMapping instanceof ManyToManyMapping) {
                ManyToManyMapping manyToManyMapping = (ManyToManyMapping) collectionMapping;
                Vector<DatabaseField> sourceKeyFields = manyToManyMapping.getSourceKeyFields();
                Vector<DatabaseField> sourceRelationKeyFields = manyToManyMapping.getSourceRelationKeyFields();
                Vector<DatabaseField> targetKeyFields = manyToManyMapping.getTargetKeyFields();
                Vector<DatabaseField> targetRelationKeyFields = manyToManyMapping.getTargetRelationKeyFields();

                Map<String, String> idColumnMapping = new LinkedHashMap<>(sourceKeyFields.size());
                Map<String, String> targetIdColumnMapping = new LinkedHashMap<>(targetKeyFields.size());

                for (int i = 0; i < sourceKeyFields.size(); i++) {
                    idColumnMapping.put(sourceKeyFields.get(i).getName(), sourceRelationKeyFields.get(i).getName());
                }
                for (int i = 0; i < targetKeyFields.size(); i++) {
                    targetIdColumnMapping.put(targetKeyFields.get(i).getName(), targetRelationKeyFields.get(i).getName());
                }

                return new JoinTable(
                        manyToManyMapping.getRelationTable().getName(),
                        null,
                        idColumnMapping,
                        keyMapping(manyToManyMapping.getContainerPolicy().getIdentityFieldsForMapKey()),
                        null,
                        null,
                        targetIdColumnMapping
                );
            } else if (collectionMapping instanceof DirectCollectionMapping) {
                DirectCollectionMapping directCollectionMapping = (DirectCollectionMapping) collectionMapping;
                Vector<DatabaseField> sourceKeyFields = directCollectionMapping.getSourceKeyFields();
                Vector<DatabaseField> referenceKeyFields = directCollectionMapping.getReferenceKeyFields();

                Map<String, String> idColumnMapping = new LinkedHashMap<>(sourceKeyFields.size());
                Map<String, String> targetIdColumnMapping = Collections.emptyMap();

                for (int i = 0; i < sourceKeyFields.size(); i++) {
                    idColumnMapping.put(sourceKeyFields.get(i).getName(), referenceKeyFields.get(i).getName());
                }
                return new JoinTable(
                        directCollectionMapping.getReferenceTableName(),
                        null,
                        idColumnMapping,
                        keyMapping(directCollectionMapping.getContainerPolicy().getIdentityFieldsForMapKey()),
                        null,
                        null,
                        targetIdColumnMapping
                );
            } else if (collectionMapping instanceof DirectMapMapping) {
                DirectMapMapping directMapMapping = (DirectMapMapping) collectionMapping;
                Vector<DatabaseField> sourceKeyFields = directMapMapping.getSourceKeyFields();
                Vector<DatabaseField> referenceKeyFields = directMapMapping.getReferenceKeyFields();

                Map<String, String> idColumnMapping = new LinkedHashMap<>(sourceKeyFields.size());
                Map<String, String> targetIdColumnMapping = Collections.emptyMap();

                for (int i = 0; i < sourceKeyFields.size(); i++) {
                    idColumnMapping.put(sourceKeyFields.get(i).getName(), referenceKeyFields.get(i).getName());
                }
                return new JoinTable(
                        directMapMapping.getReferenceTableName(),
                        null,
                        idColumnMapping,
                        keyMapping(directMapMapping.getContainerPolicy().getIdentityFieldsForMapKey()),
                        null,
                        null,
                        targetIdColumnMapping
                );
            } else if (collectionMapping instanceof AggregateCollectionMapping) {
                AggregateCollectionMapping aggregateCollectionMapping = (AggregateCollectionMapping) collectionMapping;
                Vector<DatabaseField> sourceKeyFields = aggregateCollectionMapping.getSourceKeyFields();
                Vector<DatabaseField> targetForeignKeyFields = aggregateCollectionMapping.getTargetForeignKeyFields();

                Map<String, String> idColumnMapping = new LinkedHashMap<>(sourceKeyFields.size());
                Map<String, String> targetIdColumnMapping = Collections.emptyMap();
                String tableName = null;

                for (int i = 0; i < sourceKeyFields.size(); i++) {
                    tableName = targetForeignKeyFields.get(i).getTableName();
                    idColumnMapping.put(sourceKeyFields.get(i).getName(), targetForeignKeyFields.get(i).getName());
                }
                return new JoinTable(
                        tableName,
                        null,
                        idColumnMapping,
                        keyMapping(aggregateCollectionMapping.getContainerPolicy().getIdentityFieldsForMapKey()),
                        null,
                        null,
                        targetIdColumnMapping
                );
            }
        }
        return null;
    }

    private static Map<String, String> keyMapping(List<DatabaseField> identityFieldsForMapKey) {
        if (identityFieldsForMapKey == null || identityFieldsForMapKey.isEmpty()) {
            return null;
        } else {
            Map<String, String> keyMapping = new LinkedHashMap<>(identityFieldsForMapKey.size());
            for (DatabaseField databaseField : identityFieldsForMapKey) {
                keyMapping.put(databaseField.getName(), databaseField.getName());
            }
            return keyMapping;
        }
    }

    @Override
    public boolean isBag(EntityType<?> ownerType, String attributeName) {
        AttributeImpl<?, ?> attribute = getAttribute(ownerType, attributeName);
        if (attribute instanceof PluralAttribute) {
            PluralAttribute<?, ?, ?> pluralAttr = (PluralAttribute<?, ?, ?>) attribute;
            if (pluralAttr.getCollectionType() == PluralAttribute.CollectionType.COLLECTION) {
                return true;
            } else if (pluralAttr.getCollectionType() == PluralAttribute.CollectionType.LIST) {
                DatabaseMapping mapping = attribute.getMapping();
                if (mapping instanceof CollectionMapping) {
                    CollectionMapping collectionMapping = (CollectionMapping) mapping;
                    return collectionMapping.getListOrderField() == null;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isOrphanRemoval(ManagedType<?> ownerType, String attributeName) {
        AttributeImpl<?, ?> attribute = getAttribute(ownerType, attributeName);
        if (attribute != null && attribute.getMapping() instanceof ForeignReferenceMapping) {
            ForeignReferenceMapping mapping = (ForeignReferenceMapping) attribute.getMapping();
            return mapping.isPrivateOwned() && mapping.isCascadeRemove();
        }
        return false;
    }

    @Override
    public boolean isOrphanRemoval(ManagedType<?> ownerType, String elementCollectionPath, String attributeName) {
        // TODO: Not yet supported
        return false;
    }

    @Override
    public boolean isDeleteCascaded(ManagedType<?> ownerType, String attributeName) {
        AttributeImpl<?, ?> attribute = getAttribute(ownerType, attributeName);
        if (attribute != null && attribute.getMapping() instanceof ForeignReferenceMapping) {
            ForeignReferenceMapping mapping = (ForeignReferenceMapping) attribute.getMapping();
            return mapping.isCascadeRemove();
        }
        return false;
    }

    @Override
    public boolean isDeleteCascaded(ManagedType<?> ownerType, String elementCollectionPath, String attributeName) {
        // TODO: Not yet supported
        return false;
    }

    @Override
    public boolean hasJoinCondition(ManagedType<?> ownerType, String elementCollectionPath, String attributeName) {
        return false;
    }

    @Override
    public boolean containsEntity(EntityManager em, Class<?> entityClass, Object id) {
        return em.unwrap(JpaEntityManager.class).getActiveSession().getIdentityMapAccessor().getFromIdentityMap(id, entityClass) != null;
    }

    private AttributeImpl<?, ?> getAttribute(ManagedType<?> ownerType, String attributeName) {
        if (attributeName.indexOf('.') == -1) {
            return (AttributeImpl<?, ?>) ownerType.getAttribute(attributeName);
        }
        ManagedType<?> t = ownerType;
        SingularAttribute<?, ?> attr = null;
        String[] parts = attributeName.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            Attribute<?, ?> attribute = t.getAttribute(parts[i]);
            if (attribute instanceof PluralAttribute) {
                // Skip id attribute accesses
                if (i + 1 != parts.length) {
                    return null;
                } else {
                    return (AttributeImpl<?, ?>) attribute;
                }
            }
            attr = (SingularAttribute<?, ?>) attribute;
            if (attr.getType().getPersistenceType() != Type.PersistenceType.BASIC) {
                t = (ManagedType<?>) attr.getType();
            } else if (i + 1 != parts.length) {
                throw new IllegalArgumentException("Illegal attribute name for type [" + ownerType.getJavaType().getName() + "]: " + attributeName);
            }
        }

        return (AttributeImpl<?, ?>) attr;
    }

    @Override
    public boolean supportsSingleValuedAssociationIdExpressions() {
        return false;
    }

    @Override
    public boolean supportsForeignAssociationInOnClause() {
        return true;
    }

    @Override
    public boolean supportsUpdateSetEmbeddable() {
        return true;
    }

    @Override
    public boolean supportsUpdateSetAssociationId() {
        // Not sure why, but it doesn't support it, although it supports setting individual embeddable elements
        return false;
    }

    @Override
    public boolean supportsTransientEntityAsParameter() {
        return true;
    }

    @Override
    public boolean needsAssociationToIdRewriteInOnClause() {
        return false;
    }

    @Override
    public boolean needsBrokenAssociationToIdRewriteInOnClause() {
        return false;
    }

    @Override
    public boolean needsTypeConstraintForColumnSharing() {
        return false;
    }

    @Override
    public boolean supportsCollectionTableCleanupOnDelete() {
        return true;
    }

    @Override
    public boolean supportsJoinTableCleanupOnDelete() {
        return true;
    }

    @Override
    public boolean needsCorrelationPredicateWhenCorrelatingWithWhereClause() {
        return false;
    }

    @Override
    public boolean supportsSingleValuedAssociationNaturalIdExpressions() {
        return false;
    }

    @Override
    public boolean supportsGroupByEntityAlias() {
        return true;
    }

    @Override
    public boolean needsElementCollectionIdCutoff() {
        return false;
    }

    @Override
    public boolean needsUnproxyForFieldAccess() {
        return false;
    }

    @Override
    public boolean needsCaseWhenElseBranch() {
        return true;
    }

    @Override
    public void setCacheable(Query query) {
        query.setHint("eclipselink.query-results-cache", true);
    }

    @Override
    public List<String> getIdentifierOrUniqueKeyEmbeddedPropertyNames(EntityType<?> ownerType, String attributeName) {
        AttributeImpl<?, ?> attribute = getAttribute(ownerType, attributeName);
        if (attribute instanceof SingularAttribute<?, ?> && ((SingularAttribute<?, ?>) attribute).getType() instanceof EntityType<?>) {
            EntityType<?> entityType = (EntityType<?>) ((SingularAttribute<?, ?>) attribute).getType();
            if (entityType.hasSingleIdAttribute()) {
                Class<?> idClass = entityType.getIdType().getJavaType();
                try {
                    return Collections.singletonList(entityType.getId(idClass).getName());
                } catch (IllegalArgumentException e) {
                    /**
                     * Eclipselink returns wrapper types from entityType.getIdType().getJavaType() even if the id type
                     * is a primitive.
                     * In this case, entityType.getId(...) throws an IllegalArgumentException. We catch it here and try again
                     * with the corresponding primitive type.
                     */
                    if (idClass != null) {
                        final Class<?> primitiveIdClass = ReflectionUtils.getPrimitiveClassOfWrapper(idClass);
                        if (primitiveIdClass != null) {
                            return Collections.singletonList(entityType.getId(primitiveIdClass).getName());
                        }
                    }
                    throw e;
                }
            } else {
                Set<SingularAttribute<?, ?>> attributes = (Set<SingularAttribute<?, ?>>) (Set) entityType.getIdClassAttributes();
                List<String> attributeNames = new ArrayList<>(attributes.size());

                for (Attribute<?, ?> attr : attributes) {
                    attributeNames.add(attr.getName());
                }

                return attributeNames;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getIdentifierOrUniqueKeyEmbeddedPropertyNames(EntityType<?> ownerType, String elementCollectionPath, String attributeName) {
        ClassDescriptor referenceDescriptor = ((PluralAttributeImpl) getAttribute(ownerType, elementCollectionPath)).getCollectionMapping().getReferenceDescriptor();
        Vector<DatabaseMapping> mappings = referenceDescriptor.getMappings();
        String path = attributeName.substring(elementCollectionPath.length() + 1);
        String[] parts = path.split("\\.");
        OUTER: for (int i = 0; i < parts.length; i++) {
            for (DatabaseMapping mapping : mappings) {
                if (parts[i].equals(mapping.getAttributeName())) {
                    referenceDescriptor = mapping.getReferenceDescriptor();
                    mappings = referenceDescriptor.getMappings();
                    continue OUTER;
                }
            }

            throw new IllegalArgumentException("Illegal attribute name for type [" + ownerType.getJavaType().getName() + "]: " + attributeName);
        }

        List<String> idProperties = new ArrayList<>();
        for (DatabaseMapping mapping : mappings) {
            if (mapping.isJPAId()) {
                idProperties.add(mapping.getAttributeName());
            }
        }
        return idProperties;
    }

    @Override
    public Map<String, String> getJoinMappingPropertyNames(EntityType<?> owner, String elementCollectionPath, String attributeName) {
        List<String> keys;
        if (elementCollectionPath == null) {
            keys = getIdentifierOrUniqueKeyEmbeddedPropertyNames(owner, attributeName);
        } else {
            keys = getIdentifierOrUniqueKeyEmbeddedPropertyNames(owner, elementCollectionPath, attributeName);
        }
        Map<String, String> map = new LinkedHashMap<>(keys.size());
        for (String key : keys) {
            map.put(key, null);
        }
        return map;
    }

    @Override
    public boolean supportsEnumLiteral(ManagedType<?> ownerType, String attributeName, boolean key) {
        return true;
    }

    @Override
    public boolean supportsTemporalLiteral() {
        return true;
    }

    @Override
    public boolean supportsNonDrivingAliasInOnClause() {
        return true;
    }

    @Override
    public boolean supportsSelectCompositeIdEntityInSubquery() {
        return true;
    }

    @Override
    public boolean supportsProxyParameterForNonPkAssociation() {
        return true;
    }

    @Override
    public Object getIdentifier(Object entity) {
        return persistenceUnitUtil.getIdentifier(entity);
    }

    @Override
    public <T> T unproxy(T entity) {
        // EclipseLink doesn't do proxying?
        return entity;
    }

    @Override
    public JpaMetamodelAccessor getJpaMetamodelAccessor() {
        return JpaMetamodelAccessorImpl.INSTANCE;
    }

}
