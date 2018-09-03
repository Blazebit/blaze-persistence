/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.integration.datanucleus;

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.spi.JoinTable;
import com.blazebit.persistence.spi.JpaProvider;
import org.datanucleus.ExecutionContext;
import org.datanucleus.api.jpa.metamodel.AttributeImpl;
import org.datanucleus.api.jpa.metamodel.EntityTypeImpl;
import org.datanucleus.api.jpa.metamodel.ManagedTypeImpl;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.EmbeddedMetaData;
import org.datanucleus.metadata.KeyMetaData;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DataNucleusJpaProvider implements JpaProvider {

    private static final String[] EMPTY = {};
    private final PersistenceUnitUtil persistenceUnitUtil;
    private final int major;
    private final int minor;
    private final int fix;

    public DataNucleusJpaProvider(PersistenceUnitUtil persistenceUnitUtil, int major, int minor, int fix) {
        this.persistenceUnitUtil = persistenceUnitUtil;
        this.major = major;
        this.minor = minor;
        this.fix = fix;
    }

    @Override
    public boolean supportsJpa21() {
        return true;
    }

    @Override
    public boolean supportsEntityJoin() {
        return major >= 5;
    }

    @Override
    public boolean supportsInsertStatement() {
        return false;
    }

    @Override
    public boolean needsBracketsForListParamter() {
        return true;
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
        return true;
    }

    @Override
    public void renderNullPrecedence(StringBuilder sb, String expression, String resolvedExpression, String order, String nulls) {
        sb.append(expression);
        if (order != null) {
            sb.append(' ').append(order);

            if (nulls != null) {
                sb.append(" NULLS ").append(nulls);
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
        return true;
    }

    @Override
    public Class<?> getDefaultQueryResultType() {
        return null;
    }

    @Override
    public String getCustomFunctionInvocation(String functionName, int argumentCount) {
        // Careful, PaginatedCriteriaBuilder has some dependency on the "length" of the string for rendering in the count query
        return functionName + "(";
    }

    @Override
    public boolean supportsRootTreat() {
        // Although it might parse, it isn't really supported for JOINED inheritance as wrong SQL is generated
        // TODO: create an issue for this
        return true;
    }

    @Override
    public boolean supportsTreatJoin() {
        return major >= 5;
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
        return false;
    }

    @Override
    public boolean supportsSubtypePropertyResolving() {
        return false;
    }

    @Override
    public boolean supportsSubtypeRelationResolving() {
        // Interestingly, joining a relation that is only available on a subtype works
        return true;
    }

    @Override
    public boolean supportsCountStar() {
        return false;
    }

    @Override
    public boolean isForeignJoinColumn(EntityType<?> ownerType, String attributeName) {
        ManagedTypeImpl<?> managedType = (ManagedTypeImpl<?>) ownerType;
        String[] parts = attributeName.split("\\.");
        AbstractMemberMetaData metaData = managedType.getMetadata().getMetaDataForMember(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            EmbeddedMetaData embeddedMetaData = metaData.getEmbeddedMetaData();
            if (embeddedMetaData == null) {
                // Probably trying to access the id attribute of a ToMany relation
                return metaData.getJoinMetaData() != null;
            } else {
                AbstractMemberMetaData[] metaDatas = embeddedMetaData.getMemberMetaData();
                metaData = null;
                for (int j = 0; j < metaDatas.length; j++) {
                    if (parts[i].equals(metaDatas[j].getName())) {
                        metaData = metaDatas[j];
                        break;
                    }
                }

                if (metaData == null) {
                    throw new IllegalArgumentException("Could not find property '" + parts[i] + "' in embeddable type: " + ((AbstractMemberMetaData) embeddedMetaData.getParent()).getType().getName());
                }
            }
        }

        return metaData.getJoinMetaData() != null;
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
        AbstractMemberMetaData metaData = getAttribute(ownerType, attributeName).getMetadata();
        return metaData.getMappedBy();
    }

    @Override
    public Map<String, String> getWritableMappedByMappings(EntityType<?> inverseType, EntityType<?> ownerType, String attributeName) {
        return null;
    }

    @Override
    public String[] getColumnNames(EntityType<?> ownerType, String attributeName) {
        return EMPTY;
    }

    @Override
    public String[] getColumnTypes(EntityType<?> ownerType, String attributeName) {
        return EMPTY;
    }

    private AttributeImpl<?, ?> getAttribute(ManagedType<?> ownerType, String attributeName) {
        if (attributeName.indexOf('.') == -1) {
            return (AttributeImpl<?, ?>) ownerType.getAttribute(attributeName);
        }
        ManagedType<?> t = ownerType;
        SingularAttribute<?, ?> attr = null;
        String[] parts = attributeName.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            attr = t.getSingularAttribute(parts[i]);
            if (attr.getType().getPersistenceType() != Type.PersistenceType.BASIC) {
                t = (ManagedType<?>) attr.getType();
            } else if (i + 1 != parts.length) {
                throw new IllegalArgumentException("Illegal attribute name for type [" + ownerType.getJavaType().getName() + "]: " + attributeName);
            }
        }

        return (AttributeImpl<?, ?>) attr;
    }

    @Override
    public JoinTable getJoinTable(EntityType<?> ownerType, String attributeName) {
        AttributeImpl<?, ?> attribute = getAttribute(ownerType, attributeName);
        AbstractMemberMetaData metaData = attribute.getMetadata();
        if (metaData.getJoinMetaData() != null) {
            Map<String, String> keyMapping = null;
            KeyMetaData keyMetaData = metaData.getKeyMetaData();
            if (keyMetaData != null && keyMetaData.getColumnMetaData() != null) {
                keyMapping = new HashMap<>();
                ColumnMetaData[] keyColumnMetaData = keyMetaData.getColumnMetaData();
                ColumnMetaData[] keyTargetPrimaryKeyColumnMetaData = keyMetaData.getForeignKeyMetaData() == null ? null : keyMetaData.getForeignKeyMetaData().getColumnMetaData();
                if (keyTargetPrimaryKeyColumnMetaData == null) {
                    keyMapping.put(keyMetaData.getColumnName(), keyMetaData.getColumnName());
                } else {
                    for (int i = 0; i < keyTargetPrimaryKeyColumnMetaData.length; i++) {
                        keyMapping.put(keyColumnMetaData[i].getName(), keyTargetPrimaryKeyColumnMetaData[i].getName());
                    }
                }
            } else if (metaData.getOrderMetaData() != null) {
                String columnName = metaData.getOrderMetaData().getColumnName();
                keyMapping = Collections.singletonMap(columnName, columnName);
            }

            String tableName;
            Map<String, String> idColumnMapping;
            Map<String, String> targetIdColumnMapping;
            if (metaData.getJoinMetaData().getTable() == null) {
                tableName = metaData.getTable();
                idColumnMapping = Collections.emptyMap();
                targetIdColumnMapping = Collections.emptyMap();
            } else {
                tableName = metaData.getJoinMetaData().getTable();
                ColumnMetaData[] primaryKeyColumnMetaData = metaData.getJoinMetaData().getPrimaryKeyMetaData().getColumnMetaData();
                ColumnMetaData[] foreignKeyColumnMetaData = metaData.getJoinMetaData().getForeignKeyMetaData().getColumnMetaData();
                idColumnMapping = new HashMap<>(primaryKeyColumnMetaData.length);
                for (int i = 0; i < foreignKeyColumnMetaData.length; i++) {
                    idColumnMapping.put(foreignKeyColumnMetaData[i].getName(), primaryKeyColumnMetaData[i].getName());
                }

                ColumnMetaData[] targetColumnMetaData = metaData.getJoinMetaData().getColumnMetaData();
                ColumnMetaData[] targetPrimaryKeyColumnMetaData = metaData.getElementMetaData().getForeignKeyMetaData().getColumnMetaData();
                targetIdColumnMapping = new HashMap<>(targetPrimaryKeyColumnMetaData.length);

                for (int i = 0; i < targetColumnMetaData.length; i++) {
                    targetIdColumnMapping.put(targetColumnMetaData[i].getName(), targetPrimaryKeyColumnMetaData[i].getName());
                }
            }

            return new JoinTable(
                    tableName,
                    idColumnMapping,
                    keyMapping,
                    targetIdColumnMapping
            );
        }

        return null;
    }

    @Override
    public boolean isBag(EntityType<?> ownerType, String attributeName) {
        AttributeImpl<?, ?> attribute = getAttribute(ownerType, attributeName);
        if (attribute instanceof PluralAttribute) {
            PluralAttribute<?, ?, ?> pluralAttr = (PluralAttribute<?, ?, ?>) attribute;
            if (pluralAttr.getCollectionType() == PluralAttribute.CollectionType.COLLECTION) {
                return true;
            } else if (pluralAttr.getCollectionType() == PluralAttribute.CollectionType.LIST) {
                AbstractMemberMetaData metaData = attribute.getMetadata();
                return metaData.getOrderMetaData() == null;
            }
        }
        return false;
    }

    @Override
    public boolean isOrphanRemoval(ManagedType<?> ownerType, String attributeName) {
        AttributeImpl<?, ?> attribute = getAttribute(ownerType, attributeName);
        return attribute != null && attribute.getMetadata().isCascadeRemoveOrphans();
    }

    @Override
    public boolean isDeleteCascaded(ManagedType<?> ownerType, String attributeName) {
        AttributeImpl<?, ?> attribute = getAttribute(ownerType, attributeName);
        return attribute != null && attribute.getMetadata().isCascadeDelete();
    }

    @Override
    public boolean containsEntity(EntityManager em, Class<?> entityClass, Object id) {
        ExecutionContext ec = em.unwrap(ExecutionContext.class);
        return ec.getAttachedObjectForId(ec.newObjectId(entityClass, id)) != null;
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
    public boolean supportsTransientEntityAsParameter() {
        return false;
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
        return false;
    }

    @Override
    public boolean supportsJoinTableCleanupOnDelete() {
        return false;
    }

    @Override
    public boolean supportsJoinElementCollectionsOnCorrelatedInverseAssociations() {
        return true;
    }

    @Override
    public void setCacheable(Query query) {
        query.setHint("datanucleus.query.results.cached", true);
    }

    @Override
    public List<String> getIdentifierOrUniqueKeyEmbeddedPropertyNames(EntityType<?> ownerType, String attributeName) {
        AttributeImpl<?, ?> attribute = getAttribute(ownerType, attributeName);
        if (attribute.getType() instanceof EntityType<?>) {
            EntityTypeImpl<?> entityType = (EntityTypeImpl<?>) attribute.getType();
            if (entityType.hasSingleIdAttribute()) {
                return Collections.singletonList(entityType.getId(entityType.getIdType().getJavaType()).getName());
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
    public Object getIdentifier(Object entity) {
        return persistenceUnitUtil.getIdentifier(entity);
    }
}
