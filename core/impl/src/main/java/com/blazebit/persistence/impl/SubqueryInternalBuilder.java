/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.Subquery;
import com.blazebit.persistence.spi.JpqlFunctionProcessor;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface SubqueryInternalBuilder<T> extends Subquery {
    
    public T getResult();

    public List<Expression> getSelectExpressions();

    public Map<Integer, JpqlFunctionProcessor<?>> getJpqlFunctionProcessors();

    public Set<Expression> getCorrelatedExpressions(AliasManager aliasManager);
    
    public int getFirstResult();
    
    public int getMaxResults();
    
}
