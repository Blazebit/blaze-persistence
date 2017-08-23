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

package com.blazebit.persistence.impl.openjpa;

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.spi.JpaProvider;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class OpenJPAJpaProvider implements JpaProvider {

    @Override
    public boolean supportsJpa21() {
        return false;
    }

    @Override
    public boolean supportsEntityJoin() {
        return false;
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
        return value ? "CASE WHEN 1 = 1 THEN true ELSE false END" : "CASE WHEN 1 = 1 THEN false ELSE true END";
    }

    @Override
    public String getBooleanConditionalExpression(boolean value) {
        return value ? "1 = 1" : "1 = 0";
    }

    @Override
    public String getNullExpression() {
        return "NULLIF(1,1)";
    }

    @Override
    public String escapeCharacter(char character) {
        return Character.toString(character);
    }

    @Override
    public boolean supportsNullPrecedenceExpression() {
        return false;
    }

    @Override
    public void renderNullPrecedence(StringBuilder sb, String expression, String resolvedExpression, String order, String nulls) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public String getOnClause() {
        return "ON";
    }

    @Override
    public String getCollectionValueFunction() {
        return null;
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
        return functionName + "(";
    }

    @Override
    public boolean supportsRootTreat() {
        return false;
    }

    @Override
    public boolean supportsTreatJoin() {
        return false;
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
        return false;
    }

    @Override
    public boolean supportsCountStar() {
        return false;
    }

    @Override
    public boolean isForeignJoinColumn(ManagedType<?> ownerType, String attributeName) {
        // just return true since we don't need that for openjpa anyway
        return true;
    }

    @Override
    public boolean isColumnShared(ManagedType<?> ownerType, String attributeName) {
        return false;
    }

    @Override
    public ConstraintType requiresTreatFilter(ManagedType<?> type, String attributeName, JoinType joinType) {
        return ConstraintType.NONE;
    }

    @Override
    public boolean isJoinTable(Attribute<?, ?> attribute) {
        // just return false since we don't need that for openjpa anyway
        return false;
    }

    @Override
    public boolean isBag(Attribute<?, ?> attribute) {
        return false;
    }

    @Override
    public boolean supportsSingleValuedAssociationIdExpressions() {
        return true;
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
