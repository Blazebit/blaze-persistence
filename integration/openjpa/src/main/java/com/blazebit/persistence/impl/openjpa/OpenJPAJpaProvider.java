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

package com.blazebit.persistence.impl.openjpa;

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.spi.JoinTable;
import com.blazebit.persistence.spi.JpaProvider;
import org.apache.openjpa.persistence.OpenJPAQuery;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class OpenJPAJpaProvider implements JpaProvider {

    private static final String[] EMPTY = {};

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
    public boolean isForeignJoinColumn(EntityType<?> ownerType, String attributeName) {
        // just return true since we don't need that for openjpa anyway
        return true;
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
        // just return null since we don't need that for openjpa anyway
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
        // just return null since we don't need that for openjpa anyway
        return null;
    }

    @Override
    public boolean isBag(EntityType<?> ownerType, String attributeName) {
        return false;
    }

    @Override
    public boolean isOrphanRemoval(ManagedType<?> ownerType, String attributeName) {
        return false;
    }

    @Override
    public boolean isDeleteCascaded(ManagedType<?> ownerType, String attributeName) {
        return false;
    }

    @Override
    public boolean containsEntity(EntityManager em, Class<?> entityClass, Object id) {
        throw new UnsupportedOperationException("Not yet implemented!");
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
        return false;
    }

    @Override
    public boolean supportsJoinTableCleanupOnDelete() {
        return false;
    }

    @Override
    public void setCacheable(Query query) {
        if (query instanceof OpenJPAQuery) {
            ((OpenJPAQuery) query).getFetchPlan().setQueryResultCacheEnabled(true);
        }
    }
}
