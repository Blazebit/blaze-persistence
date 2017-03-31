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

package com.blazebit.persistence.impl.datanucleus;

import com.blazebit.persistence.spi.JpaProvider;
import org.datanucleus.api.jpa.metamodel.AttributeImpl;
import org.datanucleus.api.jpa.metamodel.ManagedTypeImpl;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.EmbeddedMetaData;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class DataNucleusJpaProvider implements JpaProvider {

    private final int major;
    private final int minor;
    private final int fix;

    public DataNucleusJpaProvider(EntityManager em, int major, int minor, int fix) {
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
    public boolean isForeignJoinColumn(ManagedType<?> ownerType, String attributeName) {
        ManagedTypeImpl<?> managedType = (ManagedTypeImpl<?>) ownerType;
        String[] parts = attributeName.split("\\.");
        AbstractMemberMetaData metaData = managedType.getMetadata().getMetaDataForMember(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            EmbeddedMetaData embeddedMetaData = metaData.getEmbeddedMetaData();
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

        return metaData.getJoinMetaData() != null;
    }

    @Override
    public boolean isColumnShared(ManagedType<?> ownerType, String attributeName) {
        return false;
    }

    @Override
    public boolean isJoinTable(Attribute<?, ?> attribute) {
        AbstractMemberMetaData metaData = ((AttributeImpl<?, ?>) attribute).getMetadata();
        return metaData.getJoinMetaData() != null;
    }

    @Override
    public boolean isBag(Attribute<?, ?> attribute) {
        if (attribute instanceof PluralAttribute) {
            PluralAttribute<?, ?, ?> pluralAttr = (PluralAttribute<?, ?, ?>) attribute;
            if (pluralAttr.getCollectionType() == PluralAttribute.CollectionType.COLLECTION) {
                return true;
            } else if (pluralAttr.getCollectionType() == PluralAttribute.CollectionType.LIST) {
                AbstractMemberMetaData metaData = ((AttributeImpl<?, ?>) attribute).getMetadata();
                return metaData.getOrderMetaData() == null;
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
}
