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

package com.blazebit.persistence.integration.eclipselink;

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.spi.JoinTable;
import com.blazebit.persistence.spi.JpaProvider;
import org.eclipse.persistence.internal.jpa.metamodel.AttributeImpl;
import org.eclipse.persistence.internal.jpa.metamodel.ManagedTypeImpl;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.mappings.CollectionMapping;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;
import org.eclipse.persistence.mappings.ManyToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EclipseLinkJpaProvider implements JpaProvider {

    private static final String[] EMPTY = {};

    public EclipseLinkJpaProvider() {
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
    public boolean needsBracketsForListParamter() {
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

    @Override
    public JoinTable getJoinTable(EntityType<?> ownerType, String attributeName) {
        DatabaseMapping mapping = getAttribute(ownerType, attributeName).getMapping();
        if (mapping instanceof OneToOneMapping) {
            OneToOneMapping oneToOneMapping = (OneToOneMapping) mapping;
            if (oneToOneMapping.hasRelationTable()) {
                Map<String, String> idColumnMapping = new HashMap<>();
                Map<String, String> keyMapping = null;
                Map<String, String> targetIdColumnMapping = new HashMap<>();
                return new JoinTable(
                        oneToOneMapping.getRelationTable().getName(),
                        idColumnMapping,
                        keyMapping,
                        targetIdColumnMapping

                );
            }
        } else if (mapping instanceof CollectionMapping) {
            CollectionMapping collectionMapping = (CollectionMapping) mapping;
            if (collectionMapping instanceof ManyToManyMapping) {
                ManyToManyMapping manyToManyMapping = (ManyToManyMapping) collectionMapping;
                Map<String, String> idColumnMapping = new HashMap<>();
                Map<String, String> keyMapping = null;
                Map<String, String> targetIdColumnMapping = new HashMap<>();
                return new JoinTable(
                        manyToManyMapping.getRelationTable().getName(),
                        idColumnMapping,
                        keyMapping,
                        targetIdColumnMapping
                );
            }
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
    public boolean isDeleteCascaded(ManagedType<?> ownerType, String attributeName) {
        AttributeImpl<?, ?> attribute = getAttribute(ownerType, attributeName);
        if (attribute != null && attribute.getMapping() instanceof ForeignReferenceMapping) {
            ForeignReferenceMapping mapping = (ForeignReferenceMapping) attribute.getMapping();
            return mapping.isCascadeRemove();
        }
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
    public void setCacheable(Query query) {
        query.setHint("eclipselink.query-results-cache", true);
    }

    @Override
    public List<String> getIdentifierOrUniqueKeyEmbeddedPropertyNames(EntityType<?> owner, String attributeName) {
        return Collections.emptyList();
    }

}
