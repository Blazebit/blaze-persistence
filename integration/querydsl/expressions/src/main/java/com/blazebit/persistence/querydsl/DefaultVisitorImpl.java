/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.querydsl;

import com.querydsl.core.types.Constant;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Operation;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.TemplateExpression;
import com.querydsl.core.types.Visitor;

/**
 * Default implementation for visitors that returns null for all methods.
 *
 * @param <R> Return type
 * @param <C> Context type
 * @since 1.5.0
 */
abstract class DefaultVisitorImpl<R, C> implements Visitor<R, C> {

    @Override
    public R visit(Constant<?> constant, C c) {
        return null;
    }

    @Override
    public R visit(FactoryExpression<?> factoryExpression, C c) {
        return null;
    }

    @Override
    public R visit(Operation<?> operation, C c) {
        return null;
    }

    @Override
    public R visit(ParamExpression<?> paramExpression, C c) {
        return null;
    }

    @Override
    public R visit(Path<?> path, C c) {
        return null;
    }

    @Override
    public R visit(SubQueryExpression<?> subQueryExpression, C c) {
        return null;
    }

    @Override
    public R visit(TemplateExpression<?> templateExpression, C c) {
        return null;
    }

}
