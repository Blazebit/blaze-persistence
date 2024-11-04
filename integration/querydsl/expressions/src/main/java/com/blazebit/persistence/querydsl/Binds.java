/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Operation;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Visitor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code FactoryExpression} for representing CTE bindings.
 *
 * @param <X> CTE expression result type
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public class Binds<X> implements FactoryExpression<X> {

    private static final long serialVersionUID = -7253467353994901968L;

    private final List<Operation<?>> args = new ArrayList<>();

    public <T> Binds<X> bind(Path<? super T> path, Expression<? extends T> expression) {
        args.add(JPQLNextExpressions.bind(path, expression));
        return this;
    }

    public void addBinds(List<Operation<?>> binds) {
        this.args.addAll(binds);
    }

    @Override
    public List<Expression<?>> getArgs() {
        return Collections.<Expression<?>> unmodifiableList(args);
    }

    @Nullable
    @Override
    public X newInstance(Object... objects) {
        throw new IllegalStateException("Instances may not be created for a CTE binds projection");
    }

    @Nullable
    @Override
    public <R, C> R accept(Visitor<R, C> visitor, @Nullable C c) {
        return visitor.visit(this, c);
    }

    @Override
    public Class<? extends X> getType() {
        return (Class) ((Path) args.get(0).getArg(1)).getRoot().getType();
    }
}
