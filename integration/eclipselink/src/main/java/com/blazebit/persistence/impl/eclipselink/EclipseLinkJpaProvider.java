/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.impl.eclipselink;

import com.blazebit.persistence.spi.JpaProvider;
import org.eclipse.persistence.internal.jpa.metamodel.AttributeImpl;
import org.eclipse.persistence.internal.jpa.metamodel.ManagedTypeImpl;
import org.eclipse.persistence.mappings.CollectionMapping;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class EclipseLinkJpaProvider implements JpaProvider {

    public EclipseLinkJpaProvider(EntityManager em) {

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
    public boolean isForeignJoinColumn(ManagedType<?> ownerType, String attributeName) {
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
    public boolean isColumnShared(ManagedType<?> ownerType, String attributeName) {
        return false;
    }

    @Override
    public boolean isJoinTable(Attribute<?, ?> attribute) {
        DatabaseMapping mapping = ((AttributeImpl<?, ?>) attribute).getMapping();
        if (mapping instanceof OneToOneMapping) {
            return ((OneToOneMapping) mapping).hasRelationTable();
        } else if (mapping instanceof CollectionMapping) {
            return ((CollectionMapping) mapping).getMappedBy() == null;
        }
        return false;
    }

    @Override
    public boolean isBag(Attribute<?, ?> attribute) {
        if (attribute instanceof PluralAttribute) {
            PluralAttribute<?, ?, ?> pluralAttr = (PluralAttribute<?, ?, ?>) attribute;
            if (pluralAttr.getCollectionType() == PluralAttribute.CollectionType.COLLECTION) {
                return true;
            } else if (pluralAttr.getCollectionType() == PluralAttribute.CollectionType.LIST) {
                DatabaseMapping mapping = ((AttributeImpl<?, ?>) attribute).getMapping();
                if (mapping instanceof CollectionMapping) {
                    CollectionMapping collectionMapping = (CollectionMapping) mapping;
                    return collectionMapping.getListOrderField() == null;
                }
            }
        }
        return false;
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

}
