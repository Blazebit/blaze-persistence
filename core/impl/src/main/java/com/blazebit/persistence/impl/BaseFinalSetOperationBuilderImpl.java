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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.BaseFinalSetOperationBuilder;
import com.blazebit.persistence.BaseOngoingFinalSetOperationBuilder;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CustomSQLQuery;
import com.blazebit.persistence.impl.query.CustomSQLTypedQuery;
import com.blazebit.persistence.impl.query.EntityFunctionNode;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.impl.query.SetOperationQuerySpecification;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.OrderByElement;
import com.blazebit.persistence.spi.SetOperationType;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class BaseFinalSetOperationBuilderImpl<T, X extends BaseFinalSetOperationBuilder<T, X>, Y extends BaseFinalSetOperationBuilderImpl<T, X, Y>> extends AbstractCommonQueryBuilder<T, X, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, Y> implements BaseFinalSetOperationBuilder<T, X>, BaseOngoingFinalSetOperationBuilder<T, X> {

    protected T endSetResult;
    
    protected final SetOperationManager setOperationManager;
    protected final List<DefaultOrderByElement> orderByElements;

    public BaseFinalSetOperationBuilderImpl(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, SetOperationType operator, boolean nested, T endSetResult) {
        super(mainQuery, queryContext, isMainQuery, DbmsStatementType.SELECT, clazz, null, null, false);
        this.endSetResult = endSetResult;
        this.setOperationManager = new SetOperationManager(operator, nested);
        this.orderByElements = new ArrayList<DefaultOrderByElement>(0);
    }

    public BaseFinalSetOperationBuilderImpl(BaseFinalSetOperationBuilderImpl<T, X, Y> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
        this.setOperationManager = new SetOperationManager(builder.setOperationManager, queryContext);
        this.orderByElements = new ArrayList<>(builder.orderByElements);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && setOperationManager.isEmpty();
    }
    
    private static boolean isNestedAndComplex(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        if (queryBuilder instanceof BaseFinalSetOperationBuilderImpl<?, ?, ?>) {
            BaseFinalSetOperationBuilderImpl<?, ?, ?> builder = (BaseFinalSetOperationBuilderImpl<?, ?, ?>) queryBuilder;
            return builder.setOperationManager.isNested() && (builder.setOperationManager.hasSetOperations() || isNestedAndComplex(builder.setOperationManager.getStartQueryBuilder()));
        }
        
        return false;
    }
    
    @SuppressWarnings("unchecked")
    public X orderBy(String expression, boolean ascending, boolean nullFirst) {
        prepareAndCheck();
        AbstractCommonQueryBuilder<?, ?, ?, ?, ?> leftMostQuery = getLeftMost(setOperationManager.getStartQueryBuilder());

        int position;
        AliasInfo aliasInfo = leftMostQuery.aliasManager.getAliasInfo(expression);
        if (aliasInfo == null) {
            position = cbf.getExtendedQuerySupport().getSqlSelectAttributePosition(em, leftMostQuery.getTypedQueryForFinalOperationBuilder(), expression);
        } else {
            // find out the position by JPQL alias
            position = cbf.getExtendedQuerySupport().getSqlSelectAliasPosition(em, leftMostQuery.getTypedQueryForFinalOperationBuilder(), expression);
        }

        orderByElements.add(new DefaultOrderByElement(expression, position, ascending, isNullable(this, expression), nullFirst));
        return (X) this;
    }

    private boolean isNullable(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder, String expression) {
        if (queryBuilder instanceof BaseFinalSetOperationBuilderImpl<?, ?, ?>) {
            SetOperationManager setOpManager = ((BaseFinalSetOperationBuilderImpl<?, ?, ?>) queryBuilder).setOperationManager;
            if (isNullable(setOpManager.getStartQueryBuilder(), expression)) {
                return true;
            }
            for (AbstractCommonQueryBuilder<?, ?, ?, ?, ?> setOp : setOpManager.getSetOperations()) {
                if (isNullable(setOp, expression)) {
                    return true;
                }
            }

            return false;
        }

        AliasInfo aliasInfo = queryBuilder.aliasManager.getAliasInfo(expression);
        if (aliasInfo == null) {
            List<SelectInfo> selectInfos = queryBuilder.selectManager.getSelectInfos();
            if (selectInfos.size() > 1) {
                throw new IllegalArgumentException("Can't order by an attribute when having multiple select items! Use a select alias!");
            }
            JoinNode rootNode;
            if (selectInfos.isEmpty()) {
                rootNode = queryBuilder.joinManager.getRootNodeOrFail("Can't order by an attribute when having multiple query roots! Use a select alias!");
            } else {
                if (!(selectInfos.get(0).get() instanceof PathExpression)) {
                    throw new IllegalArgumentException("Can't order by an attribute when the select item is a complex expression! Use a select alias!");
                }
                rootNode = (JoinNode) ((PathExpression) selectInfos.get(0).get()).getBaseNode();
            }
            if (JpaMetamodelUtils.getAttribute(rootNode.getManagedType(), expression) == null) {
                throw new IllegalArgumentException("The attribute '" + expression + "' does not exist on the type '" + rootNode.getJavaType().getName() + "'! Did you maybe forget to use a select alias?");
            }
            return ExpressionUtils.isNullable(getMetamodel(), new PathExpression(
                    Arrays.<PathElementExpression>asList(new PropertyExpression(rootNode.getAlias()), new PropertyExpression(expression)),
                    new SimplePathReference(rootNode, expression, null),
                    false,
                    false
            ));
        } else {
            return ExpressionUtils.isNullable(getMetamodel(), ((SelectInfo) aliasInfo).getExpression());
        }
    }
    
    private AbstractCommonQueryBuilder<?, ?, ?, ?, ?> getLeftMost(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        if (queryBuilder instanceof BaseFinalSetOperationBuilderImpl<?, ?, ?>) {
            return getLeftMost(((BaseFinalSetOperationBuilderImpl<?, ?, ?>) queryBuilder).setOperationManager.getStartQueryBuilder());
        }
        
        return queryBuilder;
    }
    
    protected List<? extends OrderByElement> getOrderByElements() {
        return orderByElements;
    }
    
    public T getEndSetResult() {
        return endSetResult;
    }
    
    public void setEndSetResult(T endSetResult) {
        this.endSetResult = endSetResult;
    }

    public T endSet() {
        this.setOperationEnded = true;
        prepareAndCheck();
        return endSetResult;
    }

    @Override
    protected void prepareAndCheck() {
        // nothing to do here, except setting this to non-null to avoid exceptions
        this.nodesToFetch = Collections.emptySet();
    }

    public void verifyBuilderEnded(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> currentBuilder) {
        if (!setOperationEnded) {
            throw new IllegalStateException("Set operation builder not properly ended!");
        }

        super.verifyBuilderEnded();

        if (currentBuilder == setOperationManager.getStartQueryBuilder()) {
            return;
        }
        verifySetOperationEnded(setOperationManager.getStartQueryBuilder());

        for (AbstractCommonQueryBuilder<?, ?, ?, ?, ?> setOperand : setOperationManager.getSetOperations()) {
            if (currentBuilder == setOperand) {
                return;
            }
            verifySetOperationEnded(setOperand);
        }
    }

    private void verifySetOperationEnded(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder) {
        if (builder instanceof BaseFinalSetOperationBuilderImpl<?, ?, ?>) {
            builder.verifyBuilderEnded();
        } else if (!builder.setOperationEnded) {
            throw new IllegalStateException("Set operation builder not properly ended!");
        }
    }

    @Override
    protected void buildBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        boolean nested = isNestedAndComplex(setOperationManager.getStartQueryBuilder());
        if (nested) {
            sbSelectFrom.append('(');
        }
        
        setOperationManager.getStartQueryBuilder().buildBaseQueryString(sbSelectFrom, externalRepresentation);
        
        if (nested) {
            sbSelectFrom.append(')');
        }
        
        if (setOperationManager.hasSetOperations()) {
            String operator = getOperator(setOperationManager.getOperator());
            for (AbstractCommonQueryBuilder<?, ?, ?, ?, ?> setOperand : setOperationManager.getSetOperations()) {
                sbSelectFrom.append("\n");
                sbSelectFrom.append(operator);
                sbSelectFrom.append("\n");
                
                nested = isNestedAndComplex(setOperand);
                if (nested) {
                    sbSelectFrom.append('(');
                }
                
                setOperand.buildBaseQueryString(sbSelectFrom, externalRepresentation);
                
                if (nested) {
                    sbSelectFrom.append(')');
                }
            }
            
            applySetOrderBy(sbSelectFrom);
            applyJpaLimit(sbSelectFrom);
        }
    }
    
    protected void applySetOrderBy(StringBuilder sbSelectFrom) {
        if (orderByElements.isEmpty()) {
            return;
        }
        
        sbSelectFrom.append("\nORDER BY ");
        
        for (int i = 0; i < orderByElements.size(); i++) {
            if (i != 0) {
                sbSelectFrom.append(", ");
            }
            
            DefaultOrderByElement elem = orderByElements.get(i);
            sbSelectFrom.append(elem.getName());

            if (elem.isAscending()) {
                sbSelectFrom.append(" ASC");
            } else {
                sbSelectFrom.append(" DESC");
            }

            if (elem.isNullable()) {
                if (elem.isNullsFirst()) {
                    sbSelectFrom.append(" NULLS FIRST");
                } else {
                    sbSelectFrom.append(" NULLS LAST");
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected TypedQuery<T> getTypedQuery() {
        Set<String> parameterListNames = new HashSet<String>();
        Query leftMostQuery = setOperationManager.getStartQueryBuilder().getTypedQueryForFinalOperationBuilder();
        Query baseQuery;

        parameterManager.collectParameterListNames(leftMostQuery, parameterListNames);

        if (leftMostQuery instanceof CustomSQLQuery) {
            CustomSQLQuery customQuery = (CustomSQLQuery) leftMostQuery;
            List<Query> customQueryParticipants = customQuery.getParticipatingQueries();
            baseQuery = customQueryParticipants.get(0);
        } else if (leftMostQuery instanceof CustomSQLTypedQuery<?>) {
            CustomSQLTypedQuery<?> customQuery = (CustomSQLTypedQuery<?>) leftMostQuery;
            List<Query> customQueryParticipants = customQuery.getParticipatingQueries();
            baseQuery = customQueryParticipants.get(0);
        } else {
            baseQuery = leftMostQuery;
        }
        
        List<Query> setOperands = new ArrayList<Query>();

        for (AbstractCommonQueryBuilder<?, ?, ?, ?, ?> setOperand : setOperationManager.getSetOperations()) {
            Query q = setOperand.getQuery();
            setOperands.add(q);
            parameterManager.collectParameterListNames(q, parameterListNames);
        }

        String limit = null;
        String offset = null;

        // Main query will get the limit applied by the native mechanism
        if (!isMainQuery) {
            if (firstResult != 0) {
                offset = Integer.toString(firstResult);
            }
            if (maxResults != Integer.MAX_VALUE) {
                limit = Integer.toString(maxResults);
            }
        }

        // Since this builder has no query of it's own, there can be no joins
        List<String> keyRestrictedLeftJoinAliases = Collections.emptyList();
        List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery);
        boolean shouldRenderCteNodes = renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(false) : Collections.EMPTY_LIST;
        QuerySpecification querySpecification = new SetOperationQuerySpecification(
                this,
                leftMostQuery,
                baseQuery,
                setOperands,
                setOperationManager.getOperator(),
                getOrderByElements(),
                setOperationManager.isNested(),
                parameterManager.getParameters(),
                parameterListNames,
                limit,
                offset,
                keyRestrictedLeftJoinAliases,
                entityFunctionNodes,
                mainQuery.cteManager.isRecursive(),
                ctes,
                shouldRenderCteNodes
        );
        
        // Unfortunately we need this little adapter here
        @SuppressWarnings("rawtypes")
        TypedQuery<T> query = new CustomSQLTypedQuery<T>(
                querySpecification,
                baseQuery,
                parameterManager.getTransformers(),
                parameterManager.getValuesParameters(),
                parameterManager.getValuesBinders()
        );

        // The main query will use the native mechanism for limit/offset
        if (isMainQuery) {
            if (firstResult != 0) {
                query.setFirstResult(firstResult);
            }
            if (maxResults != Integer.MAX_VALUE) {
                query.setMaxResults(maxResults);
            }
        }

        parameterManager.parameterizeQuery(query);

        return applyObjectBuilder(query);
    }
    
    protected String getOperator(SetOperationType type) {
        switch (type) {
            case UNION: return "UNION";
            case UNION_ALL: return "UNION ALL";
            case INTERSECT: return "INTERSECT";
            case INTERSECT_ALL: return "INTERSECT ALL";
            case EXCEPT: return "EXCEPT";
            case EXCEPT_ALL: return "EXCEPT ALL";
            default: throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    @Override
    public TypedQuery<T> getQuery() {
        return getTypedQuery();
    }

    public List<T> getResultList() {
        return getTypedQuery().getResultList();
    }

    public T getSingleResult() {
        return getTypedQuery().getSingleResult();
    }

}
