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

import javax.persistence.EntityManager;
import javax.persistence.OrderColumn;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

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
        return true;
    }

    @Override
    public boolean supportsTreatJoin() {
        return major >= 5;
    }

    @Override
    public boolean supportsRootTreatJoin() {
        return false;
    }

    @Override
    public boolean supportsSubtypePropertyResolving() {
        return false;
    }

    @Override
    public boolean supportsCountStar() {
        return false;
    }

    @Override
    public boolean isForeignJoinColumn(ManagedType<?> ownerClass, String attributeName) {
        // just return true since we don't need that for datanucleus anyway
        return true;
    }

    @Override
    public boolean isJoinTable(Attribute<?, ?> attribute) {
        // just return false since we don't need that for datanucleus anyway
        return false;
    }

    @Override
    public boolean isBag(Attribute<?, ?> attribute) {
        if (attribute instanceof PluralAttribute) {
            PluralAttribute<?, ?, ?> pluralAttr = (PluralAttribute<?, ?, ?>) attribute;
            if (pluralAttr.getCollectionType() == PluralAttribute.CollectionType.COLLECTION) {
                return true;
            } else if (pluralAttr.getCollectionType() == PluralAttribute.CollectionType.LIST) {
                // TODO: implement check
                Member member = pluralAttr.getJavaMember();
                if (member instanceof Field) {
                    return ((Field) member).getAnnotation(OrderColumn.class) == null;
                } else if (member instanceof Method) {
                    return ((Method) member).getAnnotation(OrderColumn.class) == null;
                }
            }
        }
        return false;
    }

    @Override
    public boolean supportsSingleValuedAssociationIdExpressions() {
        return false;
    }
}
