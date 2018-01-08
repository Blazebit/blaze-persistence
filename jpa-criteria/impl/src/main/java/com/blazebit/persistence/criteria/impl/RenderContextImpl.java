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

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.CommonQueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.criteria.impl.expression.AbstractSelection;
import com.blazebit.persistence.criteria.impl.expression.ParameterExpressionImpl;

import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RenderContextImpl implements RenderContext {

    private final Map<ParameterExpression<?>, String> explicitParameterMapping = new HashMap<ParameterExpression<?>, String>();
    private final Map<String, ParameterExpression<?>> explicitParameterNameMapping = new HashMap<String, ParameterExpression<?>>();
    private final List<ImplicitParameterBinding> implicitParameterBindings = new ArrayList<ImplicitParameterBinding>();

    private final StringBuilder buffer;
    private final List<SubqueryInitiator<?>> subqueryInitiatorStack;
    private ClauseType clauseType;

    private int aliasCount = 0;
    private int explicitParameterCount = 0;

    private int subqueryAliasCount = 0;
    private Map<String, InternalQuery<?>> aliasToSubqueries = new HashMap<String, InternalQuery<?>>();

    public RenderContextImpl() {
        this.buffer = new StringBuilder();
        this.subqueryInitiatorStack = new ArrayList<SubqueryInitiator<?>>();
    }

    @Override
    public StringBuilder getBuffer() {
        return buffer;
    }

    public String takeBuffer() {
        String s = buffer.toString();
        buffer.setLength(0);
        return s;
    }

    @Override
    public SubqueryInitiator<?> getSubqueryInitiator() {
        return subqueryInitiatorStack.get(subqueryInitiatorStack.size() - 1);
    }

    public void pushSubqueryInitiator(SubqueryInitiator<?> subqueryInitiator) {
        subqueryInitiatorStack.add(subqueryInitiator);
    }

    public void popSubqueryInitiator() {
        subqueryInitiatorStack.remove(subqueryInitiatorStack.size() - 1);
    }

    @Override
    public ClauseType getClauseType() {
        return clauseType;
    }

    public void setClauseType(ClauseType clauseType) {
        this.clauseType = clauseType;
    }

    @Override
    public void apply(Selection<?> selection) {
        ((AbstractSelection<?>) selection).render(this);
    }

    @Override
    public String generateAlias(Class<?> entityClass) {
        return "generated" + entityClass.getSimpleName() + "_" + aliasCount++;
    }

    @Override
    public String generateSubqueryAlias(InternalQuery<?> subquery) {
        String subqueryAlias = "generatedSubquery_" + subqueryAliasCount++;
        aliasToSubqueries.put(subqueryAlias, subquery);
        return subqueryAlias;
    }

    public Map<String, InternalQuery<?>> takeAliasToSubqueryMap() {
        if (subqueryAliasCount == 0) {
            return Collections.emptyMap();
        }

        Map<String, InternalQuery<?>> map = aliasToSubqueries;
        this.aliasToSubqueries = new HashMap<String, InternalQuery<?>>();
        this.subqueryAliasCount = 0;
        return map;
    }

    private String generateParameterName() {
        return "generated_param_" + explicitParameterCount++;
    }

    @Override
    public String registerExplicitParameter(ParameterExpression<?> criteriaQueryParameter) {
        final String jpaqlParameterName;
        if (explicitParameterMapping.containsKey(criteriaQueryParameter)) {
            jpaqlParameterName = explicitParameterMapping.get(criteriaQueryParameter);
        } else {
            if (criteriaQueryParameter.getName() == null || criteriaQueryParameter.getName().isEmpty()) {
                if (criteriaQueryParameter.getPosition() != null) {
                    throw new IllegalArgumentException("Positional parameters are not supported in criteria queries!");
                } else {
                    jpaqlParameterName = generateParameterName();
                    ((ParameterExpressionImpl<?>) criteriaQueryParameter).setName(jpaqlParameterName);
                }
            } else {
                jpaqlParameterName = criteriaQueryParameter.getName();
            }

            explicitParameterNameMapping.put(jpaqlParameterName, criteriaQueryParameter);
            explicitParameterMapping.put(criteriaQueryParameter, jpaqlParameterName);
        }
        return jpaqlParameterName;
    }

    @Override
    public String registerLiteralParameterBinding(final Object literal, final Class javaType) {
        final String parameterName = generateParameterName();
        final ImplicitParameterBinding binding = new ImplicitParameterBinding() {

            public void bind(CommonQueryBuilder<?> builder) {
                builder.setParameter(parameterName, literal);
            }
        };

        implicitParameterBindings.add(binding);
        return parameterName;
    }

    public Map<String, ParameterExpression<?>> getExplicitParameterNameMapping() {
        return explicitParameterNameMapping;
    }

    public List<ImplicitParameterBinding> getImplicitParameterBindings() {
        return implicitParameterBindings;
    }
}
