/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.CommonQueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.criteria.impl.expression.AbstractSelection;

import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RenderContextImpl implements RenderContext {

    private final Map<ParameterExpression<?>, String> explicitParameterMapping = new HashMap<>();
    private final List<ImplicitParameterBinding> implicitParameterBindings = new ArrayList<>();

    private final StringBuilder buffer;
    private final List<SubqueryInitiator<?>> subqueryInitiatorStack;
    private ClauseType clauseType;

    private int aliasCount = 0;
    private int explicitParameterCount = 0;

    private int subqueryAliasCount = 0;
    private Map<String, InternalQuery<?>> aliasToSubqueries = new HashMap<>();
    private Map<Object, String> objectAliases = new IdentityHashMap<>();

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
    public String resolveAlias(Object aliasedObject, Class<?> entityClass) {
        return resolveAlias(aliasedObject, entityClass.getSimpleName());
    }

    @Override
    public String resolveAlias(Object aliasedObject, String name) {
        String alias = objectAliases.get(aliasedObject);
        if (alias != null) {
            return alias;
        }
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex != -1) {
            name = name.substring(dotIndex + 1);
        }
        objectAliases.put(aliasedObject, alias = "generated" + name + "_" + aliasCount++);
        return alias;
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
                }
            } else {
                jpaqlParameterName = criteriaQueryParameter.getName();
            }

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

    public Map<ParameterExpression<?>, String> getExplicitParameterMapping() {
        return explicitParameterMapping;
    }

    public List<ImplicitParameterBinding> getImplicitParameterBindings() {
        return implicitParameterBindings;
    }
}
