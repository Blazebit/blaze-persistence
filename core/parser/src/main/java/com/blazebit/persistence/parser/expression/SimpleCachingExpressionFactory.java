/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;


/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SimpleCachingExpressionFactory extends AbstractCachingExpressionFactory {

    public SimpleCachingExpressionFactory(ExpressionFactory delegate) {
        super(delegate, new ConcurrentHashMapExpressionCache());
    }

    public SimpleCachingExpressionFactory(ExpressionFactory delegate, ExpressionCache expressionCache) {
        super(delegate, expressionCache);
    }
}
