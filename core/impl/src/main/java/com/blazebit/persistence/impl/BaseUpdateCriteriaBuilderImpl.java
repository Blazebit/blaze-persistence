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

import com.blazebit.persistence.BaseUpdateCriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.DbmsStatementType;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class BaseUpdateCriteriaBuilderImpl<T, X extends BaseUpdateCriteriaBuilder<T, X>, Y> extends AbstractModificationCriteriaBuilder<T, X, Y> implements BaseUpdateCriteriaBuilder<T, X>, SubqueryBuilderListener<X>, ExpressionBuilderEndedListener {

    protected final Map<String, Expression> setAttributes = new LinkedHashMap<>();
    private SubqueryInternalBuilder<X> currentSubqueryBuilder;
    private String currentAttribute;

    public BaseUpdateCriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, String alias, String cteName, Class<?> cteClass, Y result, CTEBuilderListener listener) {
        super(mainQuery, queryContext, isMainQuery, DbmsStatementType.UPDATE, clazz, alias, cteName, cteClass, result, listener);
    }

    public BaseUpdateCriteriaBuilderImpl(BaseUpdateCriteriaBuilderImpl<T, X, Y> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
        builder.verifyBuilderEnded();
        for (Entry<String, Expression> entry : builder.setAttributes.entrySet()) {
            this.setAttributes.put(entityType.getName(), entry.getValue().clone(false));
        }
    }

    @Override
    protected void collectParameters() {
        ParameterRegistrationVisitor parameterRegistrationVisitor = parameterManager.getParameterRegistrationVisitor();
        ClauseType oldClauseType = parameterRegistrationVisitor.getClauseType();
        AbstractCommonQueryBuilder<?, ?, ?, ?, ?> oldQueryBuilder = parameterRegistrationVisitor.getQueryBuilder();
        try {
            parameterRegistrationVisitor.setQueryBuilder(this);
            parameterRegistrationVisitor.setClauseType(ClauseType.SET);
            for (Expression value : setAttributes.values()) {
                value.accept(parameterRegistrationVisitor);
            }
            parameterRegistrationVisitor.setClauseType(ClauseType.WHERE);
            whereManager.acceptVisitor(parameterRegistrationVisitor);
        } finally {
            parameterRegistrationVisitor.setClauseType(oldClauseType);
            parameterRegistrationVisitor.setQueryBuilder(oldQueryBuilder);
        }
    }

    @Override
    protected void applyVisitor(VisitorAdapter expressionVisitor) {
        for (Expression value : setAttributes.values()) {
            value.accept(expressionVisitor);
        }
        super.applyVisitor(expressionVisitor);
    }

    @Override
    @SuppressWarnings("unchecked")
    public X set(String attributeName, Object value) {
        verifyBuilderEnded();
        attributeName = checkAttribute(attributeName);
        Expression attributeExpression = parameterManager.addParameterExpression(value, ClauseType.SET, this);
        setAttributes.put(attributeName, attributeExpression);
        return (X) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public X setExpression(String attributeName, String expression) {
        verifyBuilderEnded();
        attributeName = checkAttribute(attributeName);
        Expression attributeExpression = expressionFactory.createScalarExpression(expression);
        parameterManager.collectParameterRegistrations(attributeExpression, ClauseType.SET, this);
        setAttributes.put(attributeName, attributeExpression);
        return (X) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SubqueryInitiator<X> set(String attribute) {
        verifySubqueryBuilderEnded();
        attribute = checkAttribute(attribute);
        this.currentAttribute = attribute;
        return subqueryInitFactory.createSubqueryInitiator((X) this, this, false, ClauseType.SET);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MultipleSubqueryInitiator<X> setSubqueries(String attribute, String expression) {
        verifySubqueryBuilderEnded();
        attribute = checkAttribute(attribute);
        this.currentAttribute = attribute;
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        MultipleSubqueryInitiator<X> initiator = new MultipleSubqueryInitiatorImpl<X>((X) this, expr, this, subqueryInitFactory, ClauseType.SET);
        return initiator;
    }

    @Override
    public SubqueryBuilder<X> set(String attribute, FullQueryBuilder<?, ?> criteriaBuilder) {
        verifySubqueryBuilderEnded();
        attribute = checkAttribute(attribute);
        this.currentAttribute = attribute;
        return subqueryInitFactory.createSubqueryBuilder((X) this, this, false, criteriaBuilder, ClauseType.SET);
    }

    private void verifySubqueryBuilderEnded() {
        if (currentAttribute != null) {
            throw new BuilderChainingException("An initiator was not ended properly.");
        }
        if (currentSubqueryBuilder != null) {
            throw new BuilderChainingException("An subquery builder was not ended properly.");
        }
    }

    @Override
    public void onBuilderEnded(ExpressionBuilder builder) {
        if (currentAttribute == null) {
            throw new BuilderChainingException("There was an attempt to end a builder that was not started or already closed.");
        }

        Expression attributeExpression = builder.getExpression();
        parameterManager.collectParameterRegistrations(attributeExpression, ClauseType.SET, this);
        setAttributes.put(currentAttribute, attributeExpression);
    }

    @Override
    public void onBuilderEnded(SubqueryInternalBuilder<X> builder) {
        if (currentAttribute == null) {
            throw new BuilderChainingException("There was an attempt to end a builder that was not started or already closed.");
        }
        if (currentSubqueryBuilder == null) {
            throw new BuilderChainingException("There was an attempt to end a builder that was not started or already closed.");
        }
        Expression attributeExpression = new SubqueryExpression(builder);
        parameterManager.collectParameterRegistrations(attributeExpression, ClauseType.SET, this);
        setAttributes.put(currentAttribute, attributeExpression);
        currentAttribute = null;
        currentSubqueryBuilder = null;
    }

    @Override
    public void onBuilderStarted(SubqueryInternalBuilder<X> builder) {
        if (currentAttribute == null) {
            throw new BuilderChainingException("There was an attempt to start a builder without an originating initiator.");
        }
        if (currentSubqueryBuilder != null) {
            throw new BuilderChainingException("There was an attempt to start a builder but a previous builder was not ended.");
        }
        currentSubqueryBuilder = builder;
    }

    @Override
    public void onReplaceBuilder(SubqueryInternalBuilder<X> oldBuilder, SubqueryInternalBuilder<X> newBuilder) {
        throw new IllegalArgumentException("Replace not valid!");
    }

    @Override
    public void onInitiatorStarted(SubqueryInitiator<?> initiator) {
        throw new IllegalArgumentException("Initiator started not valid!");
    }

    protected String checkAttribute(String attributeName) {
        // Just do that to assert the attribute exists
        JpaMetamodelUtils.getBasicAttributePath(getMetamodel(), entityType, attributeName);
        Expression attributeExpression = setAttributes.get(attributeName);
        
        if (attributeExpression != null) {
            throw new IllegalArgumentException("The attribute [" + attributeName + "] has already been bound!");
        }
        return attributeName;
    }

    @Override
    protected void buildBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        sbSelectFrom.append("UPDATE ");
        sbSelectFrom.append(entityType.getName()).append(' ');
        sbSelectFrom.append(entityAlias);
        appendSetClause(sbSelectFrom);
        appendWhereClause(sbSelectFrom, externalRepresentation);
    }

    protected void appendSetClause(StringBuilder sbSelectFrom) {
        sbSelectFrom.append(" SET ");

        queryGenerator.setClauseType(ClauseType.SET);
        queryGenerator.setQueryBuffer(sbSelectFrom);
        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);

        Iterator<Entry<String, Expression>> setAttributeIter = setAttributes.entrySet().iterator();
        if (setAttributeIter.hasNext()) {
            Map.Entry<String, Expression> attributeEntry = setAttributeIter.next();
            appendSetElement(sbSelectFrom, attributeEntry.getKey(), attributeEntry.getValue());
            while (setAttributeIter.hasNext()) {
                attributeEntry = setAttributeIter.next();
                sbSelectFrom.append(',');
                appendSetElement(sbSelectFrom, attributeEntry.getKey(), attributeEntry.getValue());
            }
        }

        queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        queryGenerator.setClauseType(null);
    }

    protected final void appendSetElement(StringBuilder sbSelectFrom, String attribute, Expression valueExpression) {
        String trimmedPath = attribute.trim();
        if (appendSetElementEntityPrefix(trimmedPath)) {
            sbSelectFrom.append(entityAlias).append('.');
        }
        sbSelectFrom.append(attribute);
        sbSelectFrom.append(" = ");
        queryGenerator.generate(valueExpression);
    }

    protected boolean appendSetElementEntityPrefix(String trimmedPath) {
        String indexStart = "index(";
        String keyStart = "key(";
        return !trimmedPath.regionMatches(true, 0, indexStart, 0, indexStart.length())
                && !trimmedPath.regionMatches(true, 0, keyStart, 0, keyStart.length());
    }

}
